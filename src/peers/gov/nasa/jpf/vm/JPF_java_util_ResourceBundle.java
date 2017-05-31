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
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.List;

/**
 * native peer for ResourceBundle
 */
public class JPF_java_util_ResourceBundle extends NativePeer {

  @MJI
  public int getClassContext_____3Ljava_lang_Class_2 (MJIEnv env, int clsRef){
    ThreadInfo ti = env.getThreadInfo();

    List<StackFrame> list = ti.getInvokedStackFrames();
    int aRef = env.newObjectArray("java.lang.Class", list.size());

    int j=0;
    for (StackFrame frame : list){
      MethodInfo mi = frame.getMethodInfo();
      ClassInfo ci = mi.getClassInfo();
      int clsObjRef = ci.getClassObjectRef();
      env.setReferenceArrayElement(aRef, j++, clsObjRef);
    }

    return aRef;
  }
}
