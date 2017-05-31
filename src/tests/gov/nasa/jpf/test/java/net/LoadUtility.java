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

package gov.nasa.jpf.test.java.net;

import java.io.File;

import gov.nasa.jpf.util.test.TestJPF;

/**
 * This is used to hide the package "classloader_specific_tests" from JPF 
 * which is need to test costum class loaders
 * 
 * @author Nastaran Shafiei
 */
public class LoadUtility extends TestJPF{

  protected String user_dir = System.getProperty("user.dir");
  protected String pkg = "classloader_specific_tests";

  protected String originalPath = user_dir + "/build/tests/" + pkg;
  protected String tempPath = user_dir + "/build/" + pkg;

  protected String jarUrl = "jar:file:" + user_dir + "/build/" + pkg + ".jar!/";
  protected String dirUrl = "file:" + user_dir + "/build";

  /**
   * move the package, to avoid systemClassLoader loading its classes
   */
  protected void movePkgOut() {
    if(!TestJPF.isJPFRun()) {
      movePkg(originalPath, tempPath);
    }
  }

  /**
   * move the package back to its original place
   */
  protected void movePkgBack() {
    if(!TestJPF.isJPFRun()) {
      movePkg(tempPath, originalPath);
    }
  }

  protected void movePkg(String from, String to) {
    File dstFile = new File(to);
    if(!dstFile.exists()) {
      dstFile = new File(from);
      assertTrue(dstFile.renameTo(new File(to)));
    } else {
      File srcFile = new File(from);
      if(srcFile.exists()) {
        // empty the directory
        for(String name: srcFile.list()) {
          assertTrue((new File(from + "/" + name)).delete());
        }
        // remove the directory
        assertTrue(srcFile.delete());
      }
    }
  }
}
