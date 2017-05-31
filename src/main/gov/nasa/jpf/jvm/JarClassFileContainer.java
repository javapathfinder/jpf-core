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

package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.vm.ClassFileMatch;
import gov.nasa.jpf.vm.ClassParseException;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * a ClassFileContainer that loads classes from jar files
 */
public class JarClassFileContainer extends JVMClassFileContainer {
  protected JarFile jar;
  protected String pathPrefix; // optional

  static String getContainerUrl (File file){
    try {
      return "jar:" + file.toURI().toURL().toString() + "!/";
    } catch (MalformedURLException x) {
      return "jar:" + file.getAbsolutePath() + "!/";
    }
  }

  public JarClassFileContainer (File file) throws IOException {
    super(file.getPath(), getContainerUrl(file));
    jar = new JarFile(file);
  }

  public JarClassFileContainer (File file, String pathPrefix) throws IOException {
    super(getPath(file, pathPrefix), getContainerUrl(file));

    jar = new JarFile(file);
    this.pathPrefix = getNormalizedPathPrefix(pathPrefix);
  }
  
  /**
   * make sure the return value ends with '/', and does NOT start with '/'. If
   * the supplied pathPrefix only contains '/' or an empty string, return null
   */
  static String getNormalizedPathPrefix(String pathPrefix){
    if (pathPrefix != null){
      int len = pathPrefix.length();
      if (len > 0){
        if (pathPrefix.charAt(0) == '/'){
          if (len == 1){
            return null; // no need for storing a single '/' prefix
          } else {
            pathPrefix = pathPrefix.substring(1); // skip the heading '/'
            len--;
          }
        }
        
        if (pathPrefix.charAt(len-1) != '/'){
          pathPrefix += '/';
        }
        
        return pathPrefix;
        
      } else {
        return null; // empty prefix
      }
    } else {
      return null; // null prefix
    }
  }

  /**
   * return our string representation of the complete spec, which is
   * 
   *   <jar-pathname>/pathPrefix
   */
  static String getPath(File file, String pathPrefix){
    String pn = file.getPath();
   
    if (pathPrefix != null){
      int len = pathPrefix.length();
      if (len > 0){
        if (pathPrefix.charAt(0) == '/'){
          if (len == 1){
            return pn; // no need to store a single '/'
          }
        } else {
          pn += '/';
        }
        
        pn += pathPrefix;
      }
    }
    
    return pn;
  }
    
  @Override
  public ClassFileMatch getMatch(String clsName) throws ClassParseException {
    String pn = clsName.replace('.', '/') + ".class";
    
    if (pathPrefix != null){
      pn = pathPrefix + pn;
    }
    
    JarEntry e = jar.getJarEntry(pn);

    if (e != null) {
      InputStream is = null;
      try {
        long len = e.getSize();
        if (len > Integer.MAX_VALUE) {
          error("classfile too big: " + e.getName());
        }

        is = jar.getInputStream(e);

        byte[] data = new byte[(int) len];
        FileUtils.getContents(is, data);

        return new JVMClassFileMatch(clsName, getClassURL(clsName), data);

      } catch (IOException iox) {
        error("error reading jar entry " + e.getName());

      } finally {
        if (is != null) {
          try {
            is.close();
          } catch (IOException iox) {
            error("cannot close input stream for file " + e.getName());
          }
        }
      }
    }

    return null;
  }

}
