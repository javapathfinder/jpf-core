/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package gov.nasa.jpf.tool;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.util.JPFSiteUtils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Properties;

/**
 * This class is a wrapper for loading JPF or a JPFShell through a classloader
 * that got initialized from a Config object (i.e. 'native_classpath').
 *
 * This is the main-class entry in the executable RunJPF.jar, which does not
 * require any JPF specific classpath settings, provided the site.properties
 * is configured correctly
 *
 * NOTE this class is not allowed to use any types that would require
 * loading JPF classes during class resolution - this would result in
 * NoClassDefFoundErrors if the respective class is not in RunJPF.jar
 */
public class RunJPF extends Run {

  public static final int HELP         = 0x1;
  public static final int SHOW         = 0x2;
  public static final int LOG          = 0x4;
  public static final int BUILD_INFO   = 0x8;
  public static final int ADD_PROJECT  = 0x10;
  public static final int VERSION      = 0x20;
  public static final int DELAY_START  = 0x40;
  public static final int DELAY_EXIT   = 0x80;

  static final String JPF_CLASSNAME = "gov.nasa.jpf.JPF";

  static void delay (String msg) {
    System.out.println(msg);
    try {
      System.in.read();
    } catch (IOException iox) {
      // we don't care
    }    
  }
  
  public static void main (String[] args) {
    try {
      int options = getOptions(args);

      if (args.length == 0 || isOptionEnabled(HELP, options)) {
        showUsage();
        return;
      }

      if (isOptionEnabled(ADD_PROJECT, options)){
        addProject(args);
        return;
      }
      
      if (isOptionEnabled(DELAY_START, options)) {
        delay("press any key to start");
      }
      
      if (isOptionEnabled(LOG, options)) {
        Config.enableLogging(true);
      }

      Config conf = new Config(args);

      if (isOptionEnabled(SHOW, options)) {
        conf.printEntries();
      }

      ClassLoader cl = conf.initClassLoader(RunJPF.class.getClassLoader());

      if (isOptionEnabled(VERSION, options)) {
        showVersion(cl);
      }

      if (isOptionEnabled(BUILD_INFO, options)) {
        showBuild(cl);
      }

      // using JPFShell is Ok since it is just a simple non-derived interface
      // note this uses a <init>(Config) ctor in the shell class if there is one, i.e. there is no need for a separate
      // start(Config,..) or re-loading the config itself
      JPFShell shell = conf.getInstance("shell", JPFShell.class);
      if (shell != null) {
        shell.start( removeConfigArgs(args)); // responsible for exception handling itself

      } else {
        // we have to load JPF explicitly through the URLClassLoader, and
        // call its start() via reflection - interfaces would only work if
        // we instantiate a JPF object here, which would force us to duplicate all
        // the logging and event handling that preceedes JPF instantiation
        Class<?> jpfCls = cl.loadClass(JPF_CLASSNAME);
        if (!call( jpfCls, "start", new Object[] {conf,args})){
          error("cannot find 'public static start(Config,String[])' in " + JPF_CLASSNAME);
        }
      }
      
      if (isOptionEnabled(DELAY_EXIT, options)) {
        delay("press any key to exit");
      }

      
    } catch (NoClassDefFoundError ncfx){
      ncfx.printStackTrace();
    } catch (ClassNotFoundException cnfx){
      error("cannot find " + JPF_CLASSNAME);
    } catch (InvocationTargetException ix){
      // should already be handled by JPF
      ix.getCause().printStackTrace();
    }
    
  }

  public static int getOptions (String[] args){
    int mask = 0;

    if (args != null){

      for (int i = 0; i < args.length; i++) {
        String a = args[i];
        if ("-help".equals(a)){
          args[i] = null;
          mask |= HELP;

        } else if ("-show".equals(a)) {
          args[i] = null;
          mask |= SHOW;

        } else if ("-log".equals(a)){
          args[i] = null;
          mask |= LOG;

        } else if ("-buildinfo".equals(a)){
          args[i] = null;
          mask |= BUILD_INFO;
          
        } else if ("-addproject".equals(a)){
          args[i] = null;
          mask |= ADD_PROJECT;

        } else if ("-delay-start".equals(a)) {
          args[i] = null;
          mask |= DELAY_START;
          
        } else if ("-delay-exit".equals(a)) {
          args[i] = null;
          mask |= DELAY_EXIT;
          
        } else if ("-version".equals(a)){
          args[i] = null;
          mask |= VERSION;
        }
      }
    }

    return mask;
  }

  public static boolean isOptionEnabled (int option, int mask){
    return ((mask & option) != 0);
  }

  public static void showUsage() {
    System.out.println("Usage: \"java [<vm-option>..] -jar ...RunJPF.jar [<jpf-option>..] [<app> [<app-arg>..]]");
    System.out.println("  <jpf-option> : -help : print usage information and exit");
    System.out.println("               | -version : print JPF version information");    
    System.out.println("               | -buildinfo : print build and runtime information");
    System.out.println("               | -addproject [init] [<pathname>] : add project to site properties and exit");    
    System.out.println("               | -log : print configuration initialization steps");
    System.out.println("               | -show : print configuration dictionary contents");
    System.out.println("               | +<key>=<value>  : add or override key/value pair to config dictionary");
    System.out.println("  <app>        : *.jpf application properties file pathname | fully qualified application class name");
    System.out.println("  <app-arg>    : arguments passed into main() method of application class");
  }
  
  public static void addProject(String[] args){
    boolean init = false;
    int i=0;
    String sitePathName = null;
    
    // check if the first non-null arg is 'init', which means this project
    // should be added to the 'extensions' list
    for(; i<args.length; i++){
      if (args[i] != null){
        if ("init".equals(args[i])){
          init = true;
          continue;
        } else {
          sitePathName = args[i];
        }
        break;
      }
    }
    
    File siteProps = (sitePathName == null) ? JPFSiteUtils.getStandardSiteProperties() : new File(sitePathName);
    if (siteProps == null) {
      siteProps = new File(JPFSiteUtils.getGlobalSitePropertiesPath());
    }
    
    File curDir = new File( System.getProperty("user.dir"));
    String pid = JPFSiteUtils.getCurrentProjectId();
    if (pid == null){
      error("current dir not a valid JPF project: " + curDir);
    }
    
    if ("jpf-core".equals(pid)){ // jpf-core always needs to be in the extensions list
      init = true;
    }
    
    if (JPFSiteUtils.addProject( siteProps, pid, curDir, init)){
      System.out.println("added project '" + pid + "' to site properties at: " + siteProps);
    } else {
      error("failed to add project: '" + pid + "' to site properties at: " + siteProps);
    }
  }

  public static void showVersion (ClassLoader cl){
    try {
      InputStream is = cl.getResourceAsStream("gov/nasa/jpf/.version");
      if (is != null){
        System.out.print("JPF version: ");
        
        int len = is.available();
        byte[] data = new byte[len];
        is.read(data);
        is.close();
        String version = new String(data);
        System.out.println(version);
        
      } else {
        System.out.println("no JPF version information available");
      }
      

    } catch (Throwable t){
      System.err.println("error reading version information: " + t.getMessage());
    }    
  }
  
  // print out the build.properties settings
  public static void showBuild(ClassLoader cl) {
    try {
      InputStream is = cl.getResourceAsStream("gov/nasa/jpf/build.properties");
      if (is != null){
        System.out.println("JPF build information:");

        Properties buildProperties = new Properties();
        buildProperties.load(is);

        for (Map.Entry<Object, Object> e : buildProperties.entrySet()) {
          System.out.print('\t');
          System.out.print(e.getKey());
          System.out.print(" = ");
          System.out.println(e.getValue());
        }

        is.close();

      } else {
        System.out.println("no JPF build information available");
      }

    } catch (Throwable t){
      System.err.println("error reading build information: " + t.getMessage());
    }
  }

}
