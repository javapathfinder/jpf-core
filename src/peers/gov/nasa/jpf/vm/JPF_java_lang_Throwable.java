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
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * MJI NativePeer class for java.lang.Throwable library abstraction
 */
public class JPF_java_lang_Throwable extends NativePeer {    
  /**
   * return array of StackTraceElement elements from the snapshot stored in the Throwable
   */
  @MJI
  public int createStackTrace_____3Ljava_lang_StackTraceElement_2 (MJIEnv env, int objref) {
    int aref = env.getReferenceField(objref, "snapshot");
    int[] snap = env.getIntArrayObject(aref);
    
    return env.getThreadInfo().createStackTraceElements(snap);
  }
  
  @MJI
  public int fillInStackTrace____Ljava_lang_Throwable_2 (MJIEnv env, int objref) {
    ThreadInfo ti = env.getThreadInfo();
    int[] snap = ti.getSnapshot(objref);
    
    int aref = env.newIntArray(snap);
    env.setReferenceField(objref, "snapshot", aref);
    
    return objref;
  }
    
  // native because we don't want to waste states
  @MJI
  public void printStackTrace____V (MJIEnv env, int objRef) {
    env.getThreadInfo().printStackTrace(objRef);
  }
  
  // a helper method to get a string representation of the stacktrace
  @MJI
  public int getStackTraceAsString____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    ThreadInfo ti = env.getThreadInfo();
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    
    ti.printStackTrace(pw, objRef);
    String stackTrace = sw.toString();
    pw.close();
    
    return env.newString(stackTrace);
  }
  
  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objRef){
    ClassInfo ci = env.getClassInfo(objRef);
    int msgRef = env.getReferenceField(objRef, "detailMessage");
    
    String s = ci.getName();
    if (msgRef != MJIEnv.NULL){
      s += ": " + env.getStringObject(msgRef);
    }
    
    return env.newString(s);
  }
}
