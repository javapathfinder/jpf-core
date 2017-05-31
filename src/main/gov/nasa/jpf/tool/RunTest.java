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
import gov.nasa.jpf.JPFClassLoader;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.JPFSiteUtils;
import java.io.File;
import java.lang.reflect.Field;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * tool to run JPF test with configured classpath
 *
 * arguments are supposed to be of type
 *
 *   {<config-option>} <JPF-test-class> {<test-method>}
 *
 * all leading config options are used to create the initial Config, but be
 * aware of that each test (TestJPF.verifyX() invocation) uses its own
 * Config and JPF object, i.e. can have different path settings
 *
 * This automatically adds <project>.test_classpath to the startup classpath
 */
public class RunTest extends Run {

  public static final int HELP  = 0x1;
  public static final int SHOW  = 0x2;
  public static final int LOG   = 0x4;
  public static final int QUIET = 0x8;

  static final String TESTJPF_CLS = "gov.nasa.jpf.util.test.TestJPF";
  
  static Config config;

  public static Config getConfig(){
    return config;
  }

  public static class Failed extends RuntimeException {
    public Failed (){
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

        } else if ("-quiet".equals(a)){
          args[i] = null;
          mask |= QUIET;
        }
      }
    }

    return mask;
  }

  public static boolean isOptionEnabled (int option, int mask){
    return ((mask & option) != 0);
  }

  public static void showUsage() {
    System.out.println("Usage: \"java [<vm-option>..] -jar ...RunTest.jar [<jpf-option>..] [<class> [<app-arg>..]]");
    System.out.println("  <jpf-option> : -help : print usage information and exit");
    System.out.println("               | -log : print configuration initialization steps");
    System.out.println("               | -show : print configuration dictionary contents"); 
    System.out.println("               | -quiet : don't show System.out test output");
    System.out.println("               | +<key>=<value>  : add or override <key>/<value> pair to global config");
    System.out.println("               | +test.<key>=<value>  : add or override <key>/<value> pair in test config");
    System.out.println("  <class>      : application class name");
    System.out.println("  <methods>    : test methods of application class");
  }
  
  public static void main(String[] args) {
    int options = getOptions( args);
    
    if (isOptionEnabled(HELP, options)) {
      showUsage();
      return;
    }

    if (isOptionEnabled(LOG, options)) {
      Config.enableLogging(true);
    }

    config = new Config(args);

    if (isOptionEnabled(SHOW, options)) {
      config.printEntries();
    }
    
    args = removeConfigArgs( args);
    String testClsName = getTestClassName(args);
    String[] testArgs = getTestArgs(args);
    
    String[] testPathElements = getTestPathElements(config);
    JPFClassLoader cl = config.initClassLoader(RunTest.class.getClassLoader());
    addTestClassPath( cl, testPathElements);

    Class<?> testJpfCls = null;
    try {
      testJpfCls = cl.loadClass( TESTJPF_CLS);
      
      if (isOptionEnabled(QUIET, options)){
        Field f = testJpfCls.getDeclaredField("quiet");
        f.setAccessible(true);
        f.setBoolean( null, true);
      }
      
    } catch (NoClassDefFoundError ncfx) {
      error("class did not resolve: " + ncfx.getMessage());
      return;
    } catch (ClassNotFoundException cnfx) {
      error("class not found " + cnfx.getMessage() + ", check native_classpath in jpf.properties");
      return;
      
    // we let these pass for now since it only means the quiet option is not going to work
    } catch (NoSuchFieldException ex) {
      warning("incompatible " + TESTJPF_CLS + " version, quiet mode will not work");
    } catch (IllegalAccessException ex) {
      warning("incompatible " + TESTJPF_CLS + " version, quiet mode will not work");
    }
    
    // <2do> refactor - each test class should be (optionally) loaded through a new ClassLoader instance
    // to make sure tests don't have static field carry-over
    
    List<Class<?>> testClasses = getTestClasses(cl, testJpfCls, testPathElements, testClsName);
    if (testClasses.isEmpty()){
      System.out.println("no test classes found");
      return;
    }
    
    int nTested = 0;
    int nPass = 0;
    
    for (Class<?> testCls : testClasses){
      nTested++;
      
      try {
        try {
          try { // check if there is a main(String[]) method
            Method mainEntry = testCls.getDeclaredMethod("main", String[].class);
            mainEntry.invoke(null, (Object) testArgs);

          } catch (NoSuchMethodException x) { // no main(String[]), call TestJPF.runTests(testCls,args) directly
            Method mainEntry = testJpfCls.getDeclaredMethod("runTests", Class.class, String[].class);
            mainEntry.invoke(null, new Object[]{testCls, testArgs});
          }
          
          nPass++;
          
        } catch (NoSuchMethodException x) {
          error("no suitable main() or runTests() in " + testCls.getName());
        } catch (IllegalAccessException iax) {
          error(iax.getMessage());
        }
        
      } catch (NoClassDefFoundError ncfx) {
        error("class did not resolve: " + ncfx.getMessage());
      } catch (InvocationTargetException ix) {
        Throwable cause = ix.getCause();
        if (cause instanceof Failed){
          // no need to report - the test did run and reported why it failed
          System.exit(1);
        } else {
          error(ix.getCause().getMessage());
        }
      }
    }    
        
    System.out.println();
    System.out.printf("tested classes: %d, passed: %d\n", nTested, nPass);
  }

  static Class<?> loadTestClass (JPFClassLoader cl, Class<?> testJpfCls, String testClsName){
    try {
      Class<?> testCls = cl.loadClass(testClsName);
      if (testJpfCls.isAssignableFrom(testCls)){
        if (!Modifier.isAbstract(testCls.getModifiers())){
          return testCls;
        }
      }
      
      return null;
      
    } catch (NoClassDefFoundError ncfx) {
      error("class did not resolve: " + ncfx.getMessage());
      return null;
      
    } catch (ClassNotFoundException cnfx) {
      error("class not found " + cnfx.getMessage() + ", check test_classpath in jpf.properties");
      return null;
    }
  }
  
  static boolean hasWildcard (String pattern){
    return (pattern.indexOf('*') >= 0);
  }
  
  static List<Class<?>> getTestClasses (JPFClassLoader cl, Class<?> testJpfCls, String[] testPathElements, String testClsPattern ){
    List<Class<?>> testClasses = new ArrayList<Class<?>>();
    
    if (testClsPattern.startsWith(".")){
      testClsPattern = "gov.nasa.jpf" + testClsPattern;
    }
    
    if (!hasWildcard(testClsPattern)){ // that's simple, no need to look into dirs
      Class<?> testCls = loadTestClass( cl, testJpfCls, testClsPattern);
      if (testCls == null){ // error if this was an explicit classname
        error ("specified class name not found or no TestJPF derived class: " + testClsPattern);  
      }
      testClasses.add(testCls);
      
    } else { // we have to recursively look into the testPathElements for potential test classes
      List<String> classFileList = getClassFileList( testPathElements, testClsPattern);
      
      for (String candidate : classFileList){        
        Class<?> testCls = loadTestClass( cl, testJpfCls, candidate);
        if (testCls != null){
          testClasses.add(testCls);
        }
      }
    }
    
    return testClasses;
  }
  
  static void collectMatchingFiles (int nPrefix, File dir, List<String> list, String pattern){
    for (File e : dir.listFiles()){
      if (e.isDirectory()){
        collectMatchingFiles(nPrefix, e, list, pattern);
        
      } else if (e.isFile()){
        String pn = e.getPath().substring(nPrefix);
        if (pn.matches(pattern)){
          String clsName = pn.substring(0, pn.length() - 6); // strip cp entry and ".class"
          clsName = clsName.replace( File.separatorChar, '.');
          list.add(clsName);
        }
      }
    }
  }
  
  static List<String> getClassFileList (String[] testPathElements, String testClsPattern){
    List<String> list = new ArrayList<String>();
    String tcp = testClsPattern.replace('.', File.separatorChar);
    tcp = tcp.replace("*", ".*") + "\\.class";
    
    for (String tpe : testPathElements){
      File tp = new File(tpe);
      int nPrefix = tp.getPath().length()+1;
      collectMatchingFiles( nPrefix, tp, list, tcp);
    }
    
    return list;
  }
  
  static boolean isPublicStatic (Method m){
    int mod = m.getModifiers();
    return ((mod & (Modifier.PUBLIC | Modifier.STATIC)) == (Modifier.PUBLIC | Modifier.STATIC));
  }
  
  static String[] getTestPathElements (Config conf){
    String projectId = JPFSiteUtils.getCurrentProjectId();
    
    if (projectId != null) {
      String testCpKey = projectId + ".test_classpath";
      return  config.getCompactTrimmedStringArray(testCpKey);
      
    } else {
      return new String[0];
    }    
  }
  
  static void addTestClassPath (JPFClassLoader cl, String[] testPathElements){
    if (testPathElements != null) {
      for (String pe : testPathElements) {
        try {
          cl.addURL(FileUtils.getURL(pe));
        } catch (Throwable x) {
          error("malformed test_classpath URL: " + pe);
        }
      }
    }
  }

  static boolean isOptionArg(String a){
    if (a != null && !a.isEmpty()){
      char c = a.charAt(0);
      if ((c == '+') || (c == '-')){
        return true;
      }
    }
    return false;
  }
  
  static String getTestClassName(String[] args){
    for (int i=0; i<args.length; i++){
      String a = args[i];
      if (a != null && !isOptionArg(a)){
        return a;
      }
    }

    return null;
  }

  // return everything after the first free arg
  static String[] getTestArgs(String[] args){
    int i;

    for (i=0; i<args.length; i++){
      String a = args[i];
      if (a != null && !isOptionArg(a)){
        break;
      }
    }

    if (i >= args.length-1){
      return new String[0];
    } else {
      String[] testArgs = new String[args.length-i-1];
      System.arraycopy(args,i+1, testArgs, 0, testArgs.length);
      return testArgs;
    }
  }


}
