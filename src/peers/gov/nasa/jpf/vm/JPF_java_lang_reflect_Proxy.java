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

import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import gov.nasa.jpf.annotation.MJI;


public class JPF_java_lang_reflect_Proxy extends NativePeer {

  Set<ClassInfo> proxyCIs = new HashSet<>();

  @MJI
  public int defineClass0 (MJIEnv env, int clsObjRef, int classLoaderRef, int nameRef, int bufferRef, int offset, int length) {
    String clsName = env.getStringObject(nameRef);
    byte[] buffer = env.getByteArrayObject(bufferRef);

    try {
      ClassInfo ci = ClassLoaderInfo.getCurrentClassLoader().getResolvedClassInfo( clsName, buffer, offset, length);
      if (!ci.isRegistered()) {
        ThreadInfo ti = env.getThreadInfo();
        ci.registerClass(ti);
      }
      proxyCIs.add(ci);
      return ci.getClassObjectRef();

    } catch (ClassInfoException cix){
      env.throwException("java.lang.ClassFormatError", clsName); // <2do> check if this is the right one
      return MJIEnv.NULL;
    }
  }

  private List<Integer> getInterfaceList(MJIEnv env, int interfaceArrayRef) {
    int[] interfaces = env.getElementInfo(interfaceArrayRef).asReferenceArray();
    List<Integer> interfaceClassObjRefs = new ArrayList<>(interfaces.length);
    for (int interfaceClsObjRef : interfaces) {
      interfaceClassObjRefs.add(interfaceClsObjRef);
    }
    return interfaceClassObjRefs;
  }

  @MJI
  public int getCachedProxyClass__Ljava_lang_String_2__Ljava_lang_Class_2(MJIEnv env,
                                                                          int clsObjRef,
                                                                          int proxyNameRef) {
    String proxyName = env.getStringObject(proxyNameRef);
    try {
      ClassInfo ci = ClassLoaderInfo.getCurrentClassLoader().getAlreadyResolvedClassInfo(proxyName);
      // Case 1. This class is not resolved, need generate class file and resolve
      if (ci == null) {
        return MJIEnv.NULL;
      }

      // Case 2. This class has been resolved, create (if not created) and return its class object.
      if (!ci.isRegistered()) {
        ThreadInfo ti = env.getThreadInfo();
        ci.registerClass(ti);
      }
      return ci.getClassObjectRef();
    } catch (ClassInfoException cix){
      env.throwException("java.lang.ClassFormatError", proxyName);
      return MJIEnv.NULL;
    }
  }

  @MJI
  public int getProxyClassCanonicalName___3Ljava_lang_Class_2__Ljava_lang_String_2(
      MJIEnv env,
      int clsObjRef,
      int interfaceArrayRef) {
    List<Integer> interfaceClassObjRefs = getInterfaceList(env, interfaceArrayRef);
    StringBuilder interfaceNames = new StringBuilder();

    String pkgName = null;
    for (Integer interfaceRef : interfaceClassObjRefs) {
      ClassInfo intf = env.getReferredClassInfo(interfaceRef);

      // Concat interface names to generate unique identifier
      interfaceNames.append(intf.getName());

      // Put Proxy class in the same package of the
      // non-public interface for accessibility.
      // Throw IllegalArgumentException if non-public interfaces
      // reside in different packages.
      if ((intf.getModifiers() & Modifier.PUBLIC) == 0) {
        if (pkgName == null) {
          pkgName = intf.getPackageName();
        } else if (!pkgName.equals(intf.getPackageName())) {
          return MJIEnv.NULL;
        }
      }
    }
    if (pkgName == null) {
      pkgName = "com.sun.proxy";
    }
    String proxyId = interfaceNames.toString();
    String proxyName = pkgName + ".$Proxy$" + Integer.toHexString(proxyId.hashCode());
    return env.newString(proxyName);
  }

  @MJI
  public boolean isProxyClass__Ljava_lang_Class_2__Z(MJIEnv env,
                                                     int clsObjRef,
                                                     int targetClsObjRef) {
    ClassInfo ci = env.getReferredClassInfo(targetClsObjRef);
    return proxyCIs.contains(ci);
  }

  @MJI
  public int getInvocationHandler__Ljava_lang_Object_2__Ljava_lang_reflect_InvocationHandler_2(MJIEnv env,
                                                                                               int clsObjRef,
                                                                                               int proxyObjRef) {
    ElementInfo proxyObj = env.getElementInfo(proxyObjRef);
    return proxyObj.getReferenceField("h");
  }
}
