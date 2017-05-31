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

import gov.nasa.jpf.annotation.MJI;


public class JPF_java_lang_reflect_Proxy extends NativePeer {
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
      return ci.getClassObjectRef();
      
    } catch (ClassInfoException cix){
      env.throwException("java.lang.ClassFormatError", clsName); // <2do> check if this is the right one
      return MJIEnv.NULL;
    }
  }
}

