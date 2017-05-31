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


import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.logging.Logger;


/**
 * utility class to access arbitrary source files by line number
 * sources can be files inside of root directories, or
 * can be entries in jars
 */
public class Source {

  static Logger logger = JPF.getLogger("gov.nasa.jpf.util.Source");

  static List<SourceRoot> sourceRoots;
  static Hashtable<String,Source> sources = new Hashtable<String,Source>();
  static Source noSource = new Source(null, null);

  static abstract class SourceRoot { // common base
    abstract InputStream getInputStream (String fname);
  }

  static class DirRoot extends SourceRoot {
    String path;

    DirRoot (String path){
      this.path = path;
    }

    @Override
	InputStream getInputStream (String fname) {
      if (File.separatorChar != '/'){
        fname = fname.replace('/', File.separatorChar);
      }

      File f = new File(path, fname);
      if (f.exists()) {
        try {
          return new FileInputStream(f);
        } catch (FileNotFoundException fnfx) {
          return null;
        }
      } else {
        return null;
      }
    }

    @Override
	public boolean equals (Object other){
      return (other != null) && (other instanceof DirRoot) &&
              path.equals(((DirRoot)other).path);
    }

    @Override
	public String toString() {
      return path;
    }
  }

  static class JarRoot extends SourceRoot {
    JarFile jar;
    String  entryPrefix;

    JarRoot (String path, String ep) throws IOException {
      jar = new JarFile(path);

      if (ep == null) {
        entryPrefix = null;
      } else {
        entryPrefix = ep;
        if (ep.charAt(ep.length()-1) != '/') {
          entryPrefix += '/';
        }
      }
    }

    @Override
	InputStream getInputStream (String fname) {
      String en = (entryPrefix != null) ? entryPrefix + fname : fname;
      JarEntry entry = jar.getJarEntry(en);
      if (entry != null) {
        try {
          return jar.getInputStream(entry);
        } catch (IOException e) {
          return null;
        }
      } else {
        return null;
      }
    }

    @Override
	public boolean equals (Object other){
      if ( (other != null) && (other instanceof JarRoot)){

        // just how hard can it be to check if two JarFiles instances refer to
        // the same file?
        JarRoot o = (JarRoot)other;
        File f = new File(jar.getName());
        File fOther = new File(o.jar.getName());
        if (f.getAbsolutePath().equals(fOther.getAbsolutePath())){
          if (entryPrefix == null){
            return o.entryPrefix == null;
          } else {
            entryPrefix.equals(o.entryPrefix);
          }
        }
      }

      return false;
    }

    @Override
	public String toString() {
      return jar.getName();
    }
  }

  static void addSourceRoot (Config config, List<SourceRoot> roots, String spec){
    SourceRoot sr = null;

    try {
      int i = spec.indexOf(".jar");
      if (i >= 0) {  // jar
        String pn = FileUtils.asPlatformPath(spec.substring(0, i + 4));
        File jar = new File(pn);
        if (jar.exists()) {
          int i0 = i + 5; // scrub the leading path separator
          // JarFile assumes Unix for archive-internal paths (also on Windows)
          String ep = (spec.length() > i0) ? FileUtils.asCanonicalUnixPath(spec.substring(i0)) : null;
          // we should probably check here if there is such a dir in the Jar
          sr = new JarRoot(pn, ep);
        }

      } else {       // directory
        String pn = FileUtils.asPlatformPath(spec);
        File dir = new File(pn);
        if (dir.exists()) {
          sr = new DirRoot(pn);
        }
      }
    } catch (IOException iox) {
      // we report this below
      }

    if (sr != null) {
      if (!roots.contains(sr)){
        roots.add(sr);
      }
    } else {
      logger.info("not a valid source root: " + spec);
    }
  }

  static String findSrcRoot (String cpEntry){
    if (cpEntry.endsWith(".jar")){
      // check if there is a 'src' dir in the jar
      try {
        JarFile jf = new JarFile(cpEntry);
        JarEntry srcEntry = jf.getJarEntry("src");
        if (srcEntry != null && srcEntry.isDirectory()) {
          return jf.getName() + "/src"; // jar internal paths use '/' separators
        }
      } catch (IOException iox){
        return null;
      }

    } else { // is it a dir?
      File cpe = new File(cpEntry);
      if (cpe.isDirectory()){
        // go up until you hit a dir that has a 'src' subdir
        // remember the traversed path elements
        LinkedList<String> dirStack = new LinkedList<String>();
        dirStack.addFirst(cpe.getName());
        for (File pd = cpe.getParentFile(); pd != null; pd = pd.getParentFile()){
          File sd = new File(pd,"src");
          if (sd.isDirectory()){
            String srcRoot = sd.getPath();
            for (String e : dirStack) {
              srcRoot = srcRoot + File.separatorChar + e;
            }
            sd = new File(srcRoot);
            if (sd.isDirectory()){
              return srcRoot;
            }
          } else {
            dirStack.addFirst(pd.getName());
          }
        }
      }
    }

    return null;
  }

  public static void init (Config config) {
    ArrayList<SourceRoot> roots = new ArrayList<SourceRoot>();

    String[] srcPaths = config.getCompactStringArray("sourcepath");
    if (srcPaths != null){
      for (String e : srcPaths){
        addSourceRoot(config, roots, e);
      }
    }

    sourceRoots = roots;
    sources.clear();
    
    //printRoots();
  }

  // for debugging purposes
  static void printRoots() {
    System.out.println("source roots:");
    for (SourceRoot sr : sourceRoots){
      System.out.println("  " + sr);
    }
  }

  public static Source getSource (String relPathName) {
    if (relPathName == null){
      return null;
    }
    
    Source s = sources.get(relPathName);
    if (s == noSource) {
       return null;
    }

    if (s == null) {
      for (SourceRoot root : sourceRoots) {
        InputStream is = root.getInputStream(relPathName);
        if (is != null) {
          try {
          s = new Source(root,relPathName);
          s.loadLines(is);
          is.close();

          sources.put(relPathName, s);
          return s;
          } catch (IOException iox) {
            logger.warning("error reading " + relPathName + " from" + root);
            return null;
          }
        }
      }
    } else {
      return s;
    }

    sources.put(relPathName, noSource);
    return null;
  }

  //--- the Source instance data itself
  protected SourceRoot root;
  protected String     fname;
  protected String[]   lines;


  protected Source (SourceRoot root, String fname) {
    this.root = root;
    this.fname = fname;
  }

  protected void loadLines (InputStream is) throws IOException {
    BufferedReader in = new BufferedReader(new InputStreamReader(is));

    ArrayList<String> l = new ArrayList<String>();
    for (String line = in.readLine(); line != null; line = in.readLine()) {
      l.add(line);
    }
    in.close();

    if (l.size() > 0) {
      lines = l.toArray(new String[l.size()]);
    }
  }


  /**
   * this is our sole purpose in life - answer line strings
   * line index is 1-based
   */
  public String getLine (int i) {
    if ((lines == null) || (i <= 0) || (i > lines.length)) {
      return null;
    } else {
      return lines[i-1];
    }
  }

  public int getLineCount()
  {
     return(lines.length);
  }

  public String getPath() {
    return root.toString() + File.separatorChar + fname;
  }
}
