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

public class JPF_sun_reflect_Reflection extends NativePeer {

  @MJI
  public int getCallerClass__I__Ljava_lang_Class_2(MJIEnv env, int clsObjRef, int offset){
    ThreadInfo ti = env.getThreadInfo();
    
    StackFrame frame = ti.getTopFrame();
    MethodInfo mi = frame.getMethodInfo();
    
    while (offset > 0){
      frame = frame.getPrevious();
      if (frame == null){
        return MJIEnv.NULL; // <2do> maybe this throws an exception
      }
      
      if (frame.isDirectCallFrame()){
        continue; // does not count
      }
      
      mi = frame.getMethodInfo();
      if (mi.getName().equals("invoke") && mi.getClassName().equals("java.lang.reflect.Method")){
        continue; // does not count
      }
      
      offset--;
    }
 
    ClassInfo ci = mi.getClassInfo();
    return ci.getClassObjectRef();
  }
  
  @MJI
  public int getCallerClass____Ljava_lang_Class_2(MJIEnv env, int clsObjRef){
    return getCallerClass__I__Ljava_lang_Class_2( env, clsObjRef, 2);
  }
}
