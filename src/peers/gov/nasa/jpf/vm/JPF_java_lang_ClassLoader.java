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
package gov.nasa.jpf.vm;

import java.util.Map;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.ClassPath;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ClassInfoException;
import gov.nasa.jpf.vm.ClinitRequired;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * Native peer for java.lang.ClassLoader
 */
public class JPF_java_lang_ClassLoader extends NativePeer {

  @MJI
  public void $init____V (MJIEnv env, int objRef) {
    ClassLoaderInfo systemCl = ClassLoaderInfo.getCurrentSystemClassLoader();
    $init__Ljava_lang_ClassLoader_2__V (env, objRef, systemCl.getClassLoaderObjectRef());
  }

  @MJI
  public void $init__Ljava_lang_ClassLoader_2__V (MJIEnv env, int objRef, int parentRef) {
    Heap heap = env.getHeap();

    //--- Retrieve the parent ClassLoaderInfo
    ClassLoaderInfo parent = env.getClassLoaderInfo(parentRef);

    //--- create the internal representation of the classloader
    ClassLoaderInfo cl = new ClassLoaderInfo(env.getVM(), objRef, new ClassPath(), parent);

    //--- initialize the java.lang.ClassLoader object
    ElementInfo ei = heap.getModifiable(objRef);
    ei.setIntField( ClassLoaderInfo.ID_FIELD, cl.getId());

    // we should use the following block if we ever decide to make systemClassLoader 
    // unavailable if(parent.isSystemClassLoader) {
    //  // we don't want to make the systemCLassLoader available through SUT
    //  parentRef = MJIEnv.NULL;
    // }

    ei.setReferenceField("parent", parentRef);
  }

  @MJI
  public int getSystemClassLoader____Ljava_lang_ClassLoader_2 (MJIEnv env, int clsObjRef) {
    return ClassLoaderInfo.getCurrentSystemClassLoader().getClassLoaderObjectRef();
  }

  @MJI
  public int getResource0__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int resRef){
    String rname = env.getStringObject(resRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String resourcePath = cl.findResource(rname);

    return env.newString(resourcePath);
  }

  @MJI
  public int getResources0__Ljava_lang_String_2___3Ljava_lang_String_2 (MJIEnv env, int objRef, int resRef) {
    String rname = env.getStringObject(resRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String[] resources = cl.findResources(rname);

    return env.newStringArray(resources);
  }

  @MJI
  public int findLoadedClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env, int objRef, int nameRef) {
    String cname = env.getStringObject(nameRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    ClassInfo ci = cl.getAlreadyResolvedClassInfo(cname);
    if(ci != null) {
      return ci.getClassObjectRef();
    }

    return MJIEnv.NULL;
  }

  @MJI
  public int findSystemClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env, int objRef, int nameRef) {
    String cname = env.getStringObject(nameRef);

    checkForIllegalName(env, cname);
    if(env.hasException()) {
      return MJIEnv.NULL;
    }

    ClassLoaderInfo cl = ClassLoaderInfo.getCurrentSystemClassLoader();

    ClassInfo ci = cl.getResolvedClassInfo(cname);

    if(!ci.isRegistered()) {
      ci.registerClass(env.getThreadInfo());
    }

    return ci.getClassObjectRef();
  }

  @MJI
  public int defineClass0__Ljava_lang_String_2_3BII__Ljava_lang_Class_2 
                                      (MJIEnv env, int objRef, int nameRef, int bufferRef, int offset, int length) {
    String cname = env.getStringObject(nameRef);
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    // determine whether that the corresponding class is already defined by this 
    // classloader, if so, this attempt is invalid, and loading throws a LinkageError
    if (cl.getDefinedClassInfo(cname) != null) {  // attempt to define twice
      env.throwException("java.lang.LinkageError"); 
      return MJIEnv.NULL;
    }
        
    byte[] buffer = env.getByteArrayObject(bufferRef);
    
    try {
      ClassInfo ci = cl.getResolvedClassInfo( cname, buffer, offset, length);

      // Note: if the representation is not of a supported major or minor version, loading 
      // throws an UnsupportedClassVersionError. But for now, we do not check for this here 
      // since we don't do much with minor and major versions

      ThreadInfo ti = env.getThreadInfo();
      ci.registerClass(ti);

      return ci.getClassObjectRef();
      
    } catch (ClassInfoException cix){
      env.throwException("java.lang.ClassFormatError");
      return MJIEnv.NULL;
    }
  }


  protected static boolean check(MJIEnv env, String cname, byte[] buffer, int offset, int length) {
    // throw SecurityExcpetion if the package prefix is java
    checkForProhibitedPkg(env, cname);

    // throw NoClassDefFoundError if the given class does name might 
    // not be a valid binary name
    checkForIllegalName(env, cname);

    // throw IndexOutOfBoundsException if buffer length is not consistent
    // with offset
    checkData(env, buffer, offset, length);

    return !env.hasException();
  }

  protected static void checkForProhibitedPkg(MJIEnv env, String name) {
    if(name != null && name.startsWith("java.")) {
      env.throwException("java.lang.SecurityException", "Prohibited package name: " + name);
    }
  }

  protected static void checkForIllegalName(MJIEnv env, String name) {
    if((name == null) || (name.length() == 0)) {
      return;
    }

    if((name.indexOf('/') != -1) || (name.charAt(0) == '[')) {
      env.throwException("java.lang.NoClassDefFoundError", "IllegalName: " + name);
    }
  }

  protected static void checkData(MJIEnv env, byte[] buffer, int offset, int length) {
    if(offset<0 || length<0 || offset+length > buffer.length) {
      env.throwException("java.lang.IndexOutOfBoundsException");
    }
  }

  static String pkg_class_name = "java.lang.Package";

  @MJI
  public int getPackages_____3Ljava_lang_Package_2 (MJIEnv env, int objRef) {
    ClassLoaderInfo sysLoader = ClassLoaderInfo.getCurrentSystemClassLoader();
    ClassInfo pkgClass = null; 
    try {
      pkgClass = sysLoader.getInitializedClassInfo(pkg_class_name, env.getThreadInfo());
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    // Returns all of the Packages defined by this class loader and its ancestors
    Map<String, ClassLoaderInfo> pkgs = cl.getPackages();
    int size = pkgs.size();
    // create an array of type java.lang.Package
    int pkgArr = env.newObjectArray(pkg_class_name, size);

    int i = 0;
    for(String name: cl.getPackages().keySet()) {
      int pkgRef = createPackageObject(env, pkgClass, name, cl);
      // place the object into the array
      env.setReferenceArrayElement(pkgArr, i++, pkgRef);
    }

    return pkgArr;
  }

  @MJI
  public int getPackage__Ljava_lang_String_2__Ljava_lang_Package_2 (MJIEnv env, int objRef, int nameRef) {
    ClassLoaderInfo sysLoader = ClassLoaderInfo.getCurrentSystemClassLoader();

    ClassInfo pkgClass = null; 
    try {
      pkgClass = sysLoader.getInitializedClassInfo(pkg_class_name, env.getThreadInfo());
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    String pkgName = env.getStringObject(nameRef);
    if(cl.getPackages().get(pkgName)!=null) {
      return createPackageObject(env, pkgClass, pkgName, cl);
    } else {
      return MJIEnv.NULL;
    }
  }

  public static int createPackageObject(MJIEnv env, ClassInfo pkgClass, String pkgName, ClassLoaderInfo cl) {
    int pkgRef = env.newObject(pkgClass);
    ElementInfo ei = env.getModifiableElementInfo(pkgRef);

    ei.setReferenceField("pkgName", env.newString(pkgName));
    ei.setReferenceField("loader", cl.getClassLoaderObjectRef());
    // the rest of the fields set to some dummy value
    ei.setReferenceField("specTitle", env.newString("spectitle"));
    ei.setReferenceField("specVersion", env.newString("specversion"));
    ei.setReferenceField("specVendor", env.newString("specvendor"));
    ei.setReferenceField("implTitle", env.newString("impltitle"));
    ei.setReferenceField("implVersion", env.newString("implversion"));
    ei.setReferenceField("sealBase", MJIEnv.NULL);

    return pkgRef;
  }

  @MJI
  public void setDefaultAssertionStatus__Z__V (MJIEnv env, int objRef, boolean enabled) {
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    cl.setDefaultAssertionStatus(enabled);
  }

  @MJI
  public void setPackageAssertionStatus__Ljava_lang_String_2Z__V (MJIEnv env, int objRef, int strRef, boolean enabled) {
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    String pkgName = env.getStringObject(strRef);
    cl.setPackageAssertionStatus(pkgName, enabled);
  }

  @MJI
  public void setClassAssertionStatus__Ljava_lang_String_2Z__V (MJIEnv env, int objRef, int strRef, boolean enabled) {
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    String clsName = env.getStringObject(strRef);
    cl.setClassAssertionStatus(clsName, enabled);
  }

  @MJI
  public void clearAssertionStatus____V (MJIEnv env, int objRef) {
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    cl.clearAssertionStatus();
  }
}
