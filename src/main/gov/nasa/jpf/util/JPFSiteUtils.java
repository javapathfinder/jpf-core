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

package gov.nasa.jpf.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * utility class for JPF site configuration related functions. This is partially redundant to Config since
 * it is used during bootstrapping, and gov.nasa.jpf.Config might not be in the classpath yet. It mostly
 * differs in terms of key/value expansion, which is only partially supported here.
 * 
 * NOTE - this class is not allowed to use any JPF specific types
 */
public class JPFSiteUtils {

  //--- preparse support - we need this if we use app properties to locat lower level property files

  static Pattern keyValPattern = Pattern.compile("^[ \t]*([^# \t][^ \t]*)[ \t]*=[ \t]*(.+?)[ \t]*$");

  /**
   * minimal parsing - only local key, system property and and config_path expansion
   * NOTE this stops after finding the key, and it doesn't add the file to the 'sources'
   */
  public static String getMatchFromFile (String pathName, String lookupKey){
    String value = null;
    Pattern lookupPattern = Pattern.compile(lookupKey);

    File propFile = new File(pathName);
    if (!propFile.isFile()){
      return null;
    }

    HashMap<String, String> map = new HashMap<String, String>();
    String dir = propFile.getParent();
    if (dir == null) {
      dir = ".";
    }
    map.put("config_path", dir);

    try {
      FileReader fr = new FileReader(propFile);
      BufferedReader br = new BufferedReader(fr);

      for (String line = br.readLine(); line != null; line = br.readLine()) {
        Matcher m = keyValPattern.matcher(line);
        if (m.matches()) {
          String key = m.group(1);
          String val = m.group(2);

          val = expandLocal(val, map);

          if ((key.length() > 0) && (val.length() > 0)) {
            // check for continuation lines
            if (val.charAt(val.length() - 1) == '\\') {
              val = val.substring(0, val.length() - 1).trim();
              for (line = br.readLine(); line != null; line = br.readLine()) {
                line = line.trim();
                int len = line.length();
                if ((len > 0) && (line.charAt(len - 1) == '\\')) {
                  line = line.substring(0, line.length() - 1).trim();
                  val += expandLocal(line, map);
                } else {
                  val += expandLocal(line, map);
                  break;
                }
              }
            }

            Matcher lookupMatcher = lookupPattern.matcher(key);
            if (lookupMatcher.matches()) {
              value = val;
              break;
            } else {
              if (key.charAt(key.length() - 1) == '+') {
                key = key.substring(0, key.length() - 1);
                String v = map.get(key);
                if (v != null) {
                  val = v + val;
                }
              } else if (key.charAt(0) == '+') {
                key = key.substring(1);
                String v = map.get(key);
                if (v != null) {
                  val = val + v;
                }
              }
              map.put(key, val);
            }
          }
        }
      }
      br.close();

    } catch (FileNotFoundException fnfx) {
      return null;
    } catch (IOException iox) {
      return null;
    }

    return value;
  }

  /**
   * this returns the contents of a config source in-order, without expanding values or keys
   */
  public static List<Pair<String,String>> getRawEntries (Reader reader) throws IOException {
    ArrayList<Pair<String,String>> list = new ArrayList<Pair<String,String>>();
    BufferedReader br = new BufferedReader(reader);
    
    for (String line = br.readLine(); line != null; line = br.readLine()) {
      Matcher m = keyValPattern.matcher(line);
      if (m.matches()) {
        String key = m.group(1);
        String val = m.group(2);
        
        if ((key.length() > 0) && (val.length() > 0)) {
          // check for continuation lines
          if (val.charAt(val.length() - 1) == '\\') {
            val = val.substring(0, val.length() - 1).trim();
            for (line = br.readLine(); line != null; line = br.readLine()) {
              line = line.trim();
              int len = line.length();
              if ((len > 0) && (line.charAt(len - 1) == '\\')) {
                line = line.substring(0, line.length() - 1).trim();
                val += line;
              } else {
                val += line;
                break;
              }
            }
          }
          
          list.add( new Pair<String,String>(key,val));
        }
      }
    }
    
    return list;
  }
  
  // simple non-recursive, local key and system property expander
  private static String expandLocal (String s, HashMap<String,String> map) {
    int i, j = 0;
    if (s == null || s.length() == 0) {
      return s;
    }

    while ((i = s.indexOf("${", j)) >= 0) {
      if ((j = s.indexOf('}', i)) > 0) {
        String k = s.substring(i + 2, j);
        String v = null;

        if (map != null){
          v = map.get(k);
        }
        if (v == null){
          v = System.getProperty(k);
        }

        if (v != null) {
          s = s.substring(0, i) + v + s.substring(j + 1, s.length());
          j = i + v.length();
        } else {
          s = s.substring(0, i) + s.substring(j + 1, s.length());
          j = i;
        }
      }
    }

    return s;
  }

  public static File getCoreDir (File siteProps){
    if (siteProps != null){
      String path = getMatchFromFile(siteProps.getAbsolutePath(), "jpf-core");
      if (path != null) {
        File coreDir = new File(path);
        if (coreDir.isDirectory()) {
          return coreDir;
        }
      }
    }
    return null;
  }
  
  public static File getSiteCoreDir (String[] args){
    File siteProps = getSiteProperties( args);    
    return getCoreDir( siteProps);
  }

  /**
   * get location of jpf-core from site.properties
   * @return null if it doesn't exist
   */
  public static File getSiteCoreDir() {
    File siteProps = getStandardSiteProperties();
    return getCoreDir( siteProps);
  }

  /**
   * find project properties (jpf.properties) from current dir
   */
  public static File getCurrentProjectProperties() {
    File d = new File(System.getProperty("user.dir"));
    do {
      File f = new File(d, "jpf.properties");
      if (f.isFile()){
        return f;
      }
      d = d.getParentFile();
    } while (d != null);

    return null;
  }


  static Pattern idPattern = Pattern.compile("^[ \t]*([^# \t][^ \t]*)[ \t]*=[ \t]*\\$\\{config_path\\}");

  static String projectId;

  /**
   * look for a "<id> = ${config_path}" entry in current dir/jpf.properties
   * this looks recursively upwards
   * @return null if no jpf.properties found
   */
  public static String getCurrentProjectId (){
    if (projectId == null) {
      File propFile = getCurrentProjectProperties();

      if (propFile != null) {
        try {
          FileReader fr = new FileReader(propFile);
          BufferedReader br = new BufferedReader(fr);

          for (String line = br.readLine(); line != null; line = br.readLine()) {
            Matcher m = idPattern.matcher(line);
            if (m.matches()) {
              projectId = m.group(1);
            }
          }
          br.close();

        } catch (FileNotFoundException fnfx) {
          return null;
        } catch (IOException iox) {
          return null;
        }
      }
    }

    return projectId;
  }
  
  public static boolean isFreeArg (String a){
    return ((a != null) && (a.length() > 0) && a.charAt(0) != '+' && a.charAt(0) != '-');
  }
  
  public static File getSiteProperties (String[] args){
    //--- 1. check for a +site=<path> argument up to first free arg
    for (int i=0; i<args.length; i++){
      String a = args[i];
      if (!isFreeArg(a)){
        if (a.startsWith("+site=")) {
          String path = a.substring(6).trim();
          return new File(path);
        }
      } else {
        break;
      }
    }
    
    //--- 2. check if the first free arg is an application property file (*.jpf), and it contains a 'site=..' setting
    for (int i=0; i<args.length; i++){
      String a = args[i];
      if (isFreeArg(a)){
        if (a.matches("[^+-].*\\.jpf")) {
          String path = getMatchFromFile(a, "site");
          if (path != null) {
            return new File(path);
          }
        }
        break;
      }
    }
    
    //--- 3. finally, check upwards from the current dir up to the home dir 
    return JPFSiteUtils.getStandardSiteProperties();
  } 
  
  /**
   * locate the site.properties. Start with the current dir, go upwards until the
   * user.home is reached. If site.properties isn't found there, look for '.jpf' and
   * 'jpf' dirs within the home dir. If no site.properties is found there either, give up
   */
  public static File getStandardSiteProperties(){    
    String userDir = System.getProperty("user.dir");
    File dir = new File(userDir);
    for (; dir != null; dir = dir.getParentFile()) {
      File f = new File(dir, "site.properties");
      if (f.isFile()) {
        return f;
      }
    }

    String[] jpfDirCandidates = { ".jpf", "jpf" };
    String userHome = System.getProperty("user.home");
    
    for (String jpfDir : jpfDirCandidates){
      dir = new File(userHome, jpfDir);
      if (dir.isDirectory()) {
        File f = new File(dir, "site.properties");
        if (f.isFile()) {
          return f;
        }
      }
    }
    
    return null;
  }

  public static String getGlobalSitePropertiesPath() {
    String userHome = System.getProperty("user.home");
    String globalPath = userHome + File.separator + ".jpf"
         + File.separator + "site.properties";
    return globalPath;
  }
  
  public static List<Pair<String,String>> getRawEntries (File siteProps){
    FileReader fr = null;
    if (siteProps.isFile()) {
      try {
        fr = new FileReader(siteProps);
        List<Pair<String,String>> entries = getRawEntries(fr);
        fr.close();
        
        return entries;

      } catch (IOException iox) {
      } finally {
        try { fr.close(); } catch (IOException _ignore){}
      }
    }    
    
    return new ArrayList<Pair<String,String>>();
  }
    
  /**
   * this returns a list of all the project ids in the 'extensions' entries (also
   * handles accumulated 'extensions+=.." entries
   */
  public static List<String> getExtensions (List<Pair<String,String>> entries){
    ArrayList<String> list = new ArrayList<String>();
    
    for (Pair<String,String> p : entries){
      if (p._1.startsWith("extensions")){
        for (String pid : p._2.split("[,;]")){
          pid = pid.trim();
          if (pid.charAt(0) == '$'){
            pid = pid.substring(2, pid.length()-1);
            list.add( pid);
          }
        }
      }
    }
    
    return list;
  }
  
  
  public static boolean addProject (File siteProps, String projectId, File projectDir, boolean isExt){
    List<Pair<String,String>> entries = getRawEntries(siteProps);
    List<String> extensions = getExtensions(entries);

    if ("jpf-core".equals(projectId)){ // jpf-core always has to be in the extensions list
      isExt = true;
    }
    
    try {
      FileUtils.ensureDirs(siteProps);
      String projectPath = FileUtils.asCanonicalUserPathName(projectDir.getAbsolutePath());

      PrintWriter pw = new PrintWriter(siteProps);

      pw.println("# auto-generated JPF site properties");
      pw.println();

      boolean alreadyThere = false;
      for (Pair<String, String> e : entries) {
        if (!"extensions".equals(e._1)) {

          pw.print(e._1);
          pw.print(" = ");

          if (projectId.equals(e._1)) {
            alreadyThere = true;
            // Hmm, not sure its best to use absolute pathnames here (e.g. when doing local site installs)
            pw.println(projectPath);
            
            // check if we have to update extensions
            if (extensions.contains(projectId)){
              if (!isExt){
                extensions.remove(projectId);
              }
            } else {
              extensions.add(projectId);
            }
            
          } else {
            pw.println(e._2);
          }
        }
      }

      if (!alreadyThere) {
        if (isExt) {
          extensions.add(projectId);
        }

        pw.print(projectId);
        pw.print(" = ");
        pw.println(projectPath);
      }

      pw.println();
      pw.print("extensions = ");

      boolean isFirst = true;
      for (String e : extensions) {
        if (isFirst) {
          isFirst = false;
        } else {
          pw.print(',');          
        }
        pw.print("${");
        pw.print(e);
        pw.print('}');
      }
      
      pw.println();
      pw.close();

    } catch (IOException iox) {
      iox.printStackTrace();
      return false;
    }
    
    return true;
  }
}
