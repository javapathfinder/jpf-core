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

import gov.nasa.jpf.jvm.JVMClassFileContainer;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.vm.ClassFileMatch;
import gov.nasa.jpf.vm.ClassParseException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.net.MalformedURLException;

/**
 *
 */
public class DirClassFileContainer extends JVMClassFileContainer {

  protected File dir;

  static String getContainerURL(File dir){
    try {
      return dir.toURI().toURL().toString();
    } catch (MalformedURLException e) {
      return dir.getPath();
    }
  }

  public DirClassFileContainer(File dir) {
    super(dir.getPath(), getContainerURL(dir));

    this.dir = dir;
  }

  @Override
  public ClassFileMatch getMatch(String clsName) throws ClassParseException {
    String pn = clsName.replace('.', File.separatorChar) + ".class";
    File f = new File(dir, pn);

    if (f.isFile()) {
      FileInputStream fis = null;

      try {
        fis = new FileInputStream(f);
        long len = f.length();
        if (len > Integer.MAX_VALUE) {
          error("classfile too big: " + f.getPath());
        }
        byte[] data = new byte[(int) len];
        FileUtils.getContents(fis, data);

        return new JVMClassFileMatch( clsName, getClassURL(clsName), data);

      } catch (IOException iox) {
        error("cannot read " + f.getPath());

      } finally {
        if (fis != null) {
          try {
            fis.close();
          } catch (IOException iox) {
            error("cannot close input stream for file " + f.getPath());
          }
        }
      }
    }

    return null;
  }
}
