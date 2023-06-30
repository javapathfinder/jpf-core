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

  /**
   * @return the path to .class file including the source path of the container
   * eg:-
   *     jar:file:/path/to/jpf-classes.jar!/java.base/java/lang/Object.class
   *
   *     /path/to/build/tests/TypeNameTest.class
   *
   *     jrt:/java.base/java/lang/Class.class
   */
  @Override
  public String getClassURL (String typeName){
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
      return typeName.replace('.', File.separatorChar) + ".class";
    }
    //Strip eventual module prefix
    if (typeName.contains("$&$")) {
      typeName = typeName.split("\\$&\\$")[1];
    }
    return moduleName + File.separator + typeName.replace('.', File.separatorChar) + ".class";
  }

  /**
   * @return the module name for the given typeName. Returns null if the module is an unnamed module
   * or the Class object associated with the given typeName is not found.
   *
   * It is possible to hint the loader with the module name using $&$ as separator between module and class.
   */
  static String getModuleName(String typeName) {
    if (typeName.contains("$&$")) {
      return typeName.split("\\$&\\$")[0];
    }
    try {
      // In support of jdk.internal.reflect.ReflectionFactory,
      // we use a model class java.lang.reflect.Constructor (which is non-final
      // in our implementation but is final in OpenJDK's implementation)
      // and define gov.nasa.jpf.SerializationConstructor as a subclass of it.
      //
      // Here we try to load the class on the underlying JVM (OpenJDK) to
      // query its module name. But for gov.nasa.jpf.SerializationConstructor,
      // OpenJDK will see it inheriting a final class (j.l.r.Constructor) and throw
      // java.lang.VerifyError. Since we know it doesn't have a module name, we can
      // directly return null instead of loading it on the underlying OpenJDK
      // and doing the query.
      if (typeName.equals("gov.nasa.jpf.SerializationConstructor")) {
        return null;
      }
      return Class.forName(typeName.split("\\$")[0]).getModule().getName();
    } catch (ClassNotFoundException e) {
      return null;
    }
  }

}
