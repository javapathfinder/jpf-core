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

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativePeer;

/**
 * native peer for MemoryGoal tests
 */
public class JPF_gov_nasa_jpf_test_MemoryGoal extends NativePeer {

  Listener listener;
  
  // <2do> that's too simple, because we should only measure what is
  // allocated from the invoked method, not the MethodTester. Needs a listener
  
  static class Listener extends ListenerAdapter {
    
    MethodInfo mi;
    boolean active;
    
    long nAllocBytes;
    long nFreeBytes;
    long nAlloc;
    long nFree;
    
    Listener (MethodInfo mi){
      this.mi = mi;
    }
    
    @Override
    public void objectCreated (VM vm, ThreadInfo ti, ElementInfo ei){
      if (active){        
        nAlloc++;
        nAllocBytes += ei.getHeapSize(); // just an approximation
      }
    }
    
    @Override
    public void objectReleased (VM vm, ThreadInfo ti, ElementInfo ei){
      if (active){
        nFree++;
        nFreeBytes += ei.getHeapSize(); // just an approximation
      }      
    }

    @Override
    public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
      if (!active) {
        if (executedInsn.getMethodInfo() == mi){
          active = true;
        }
      } else {
        if ((executedInsn instanceof JVMReturnInstruction) && (executedInsn.getMethodInfo() == mi)){
          active = false;
        }
      }
    }
    
    long totalAllocBytes() {
      return nAllocBytes - nFreeBytes;
    }
  }
  
  @MJI
  public boolean preCheck__Lgov_nasa_jpf_test_TestContext_2Ljava_lang_reflect_Method_2__Z
                      (MJIEnv env, int objRef, int testContextRef, int methodRef){
    MethodInfo mi = JPF_java_lang_reflect_Method.getMethodInfo(env, methodRef);
    
    listener = new Listener(mi);
    env.addListener(listener);
    return true;
  }
  
  // what a terrible name!
  @MJI
  public boolean postCheck__Lgov_nasa_jpf_test_TestContext_2Ljava_lang_reflect_Method_2Ljava_lang_Object_2Ljava_lang_Throwable_2__Z 
           (MJIEnv env, int objRef, int testContextRef, int methdRef, int resultRef, int exRef){

    long nMax = env.getLongField(objRef, "maxGrowth");

    Listener l = listener;
    env.removeListener(l);
    listener = null;
    
    return (l.totalAllocBytes() <= nMax);
  }
}
