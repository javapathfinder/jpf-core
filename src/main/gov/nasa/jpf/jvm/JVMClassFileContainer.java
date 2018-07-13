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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.ClassFileContainer;
import gov.nasa.jpf.vm.ClassFileMatch;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ClassParseException;

import java.io.File;

/**
 * ClassFileContainer that holds Java classfiles
 */
public abstract class JVMClassFileContainer extends ClassFileContainer {
  
  // the VM and container type specific info we need to instantiate a ClassInfo from this container
  public class JVMClassFileMatch extends ClassFileMatch {
    byte[] data;
    
    JVMClassFileMatch (String typeName, String url, byte[] data) {
      super(typeName, url);
      
      this.data = data;
    }
    
    @Override
    public ClassFileContainer getContainer(){
      return JVMClassFileContainer.this;
    }
    
    public byte[] getData(){
      return data;
    }
    
    @Override
    public JVMClassInfo createClassInfo (ClassLoaderInfo loader) throws ClassParseException {
      JVMSystemClassLoaderInfo sysCli = (JVMSystemClassLoaderInfo)loader.getSystemClassLoader();
      
      JVMCodeBuilder cb = sysCli.getCodeBuilder(typeName);
      ClassFile cf = new ClassFile(data);
      
      return new JVMClassInfo( typeName, loader, cf, url, cb);
    }
    
    @Override
    public AnnotationInfo createAnnotationInfo (ClassLoaderInfo loader) throws ClassParseException {
      ClassFile cf = new ClassFile(data);
      JVMAnnotationParser parser = new JVMAnnotationParser(cf);

      return new AnnotationInfo(typeName, loader, parser);
    }
  }
  
  protected JVMClassFileContainer (String name, String url) {
    super(name, url);
  }
  
  @Override
  public String getClassURL (String typeName) {
    return getURL() + getClassEntryURL(typeName);
  }

  /**
   * @param typeName in the format java.lang.Object
   * @return Returns a path to .class file including the module name
   * in a format similar to java.base/java/lang/Object.class
   *
   * If the module for the typeName is an unnamed module, returns a path in a format similar to
   * java/lang/Object.class
   */
  static String getClassEntryURL(String typeName) {
    String moduleName = getModuleName(typeName);
    if (moduleName == null) {
      return typeName.replace('.', '/') + ".class";
    }
    return moduleName + File.separator + typeName.replace('.', '/') + ".class";
  }

  /**
   * @return the module name for the given typeName or null if the module is an unnamed module
   */
  static String getModuleName(String typeName) {
    try {
      return Class.forName(typeName.split("\\$")[0]).getModule().getName();
    } catch (ClassNotFoundException e) {
      throw new JPFException("class not found: " + typeName, e);
    }
  }

}
