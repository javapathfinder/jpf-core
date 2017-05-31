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
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativePeer;

public class JPF_java_io_ObjectStreamClass extends NativePeer {
  @MJI
  public void initNative____V (MJIEnv env, int clsObjRef) {
    // cut off
  }
  
  // why is this here??
  @MJI
  public boolean hasStaticInitializer__Ljava_lang_Class_2__Z (MJIEnv env, int objRef, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    MethodInfo mi = ci.getMethod("<clinit>()V", false);          
    return (mi != null);
  }

  // just a little accelerator
  @MJI
  public int getDeclaredSUID__Ljava_lang_Class_2__Ljava_lang_Long_2 (MJIEnv env, int objRef, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    FieldInfo fi = ci.getDeclaredStaticField("serialVersionUID");
    if (fi != null){
      ElementInfo ei = ci.getStaticElementInfo();
      long l = ei.getLongField(fi);
      return env.newLong(l);
    } else {
      return MJIEnv.NULL;
    }
  }
}
