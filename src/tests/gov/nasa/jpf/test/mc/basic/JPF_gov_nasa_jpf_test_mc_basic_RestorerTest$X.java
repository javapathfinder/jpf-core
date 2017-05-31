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
package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.ClosedMemento;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * peer for the regression test for on-demand state restoration by means of
 * ClosedMementos
 */
public class JPF_gov_nasa_jpf_test_mc_basic_RestorerTest$X extends NativePeer {

  static class InsnExecCount {
    int count;    
  }
  
  static class InsnCountRestorer implements ClosedMemento {
    InsnExecCount insnAttr;
    int count; // the value to restore
    
    InsnCountRestorer (InsnExecCount insnAttr){
      this.insnAttr = insnAttr;
      this.count = insnAttr.count;
      
      System.out.println("## storing: " + count);
    }
    
    @Override
	public void restore(){
      System.out.println("## restoring: " + count);
      insnAttr.count = count;
    }
  }

  @MJI
  public void $init (MJIEnv env, int objref){
    ThreadInfo ti = env.getThreadInfo();
    StackFrame caller = ti.getCallerStackFrame();
    Instruction insn = caller.getPC();
        
    InsnExecCount a = insn.getAttr(InsnExecCount.class);
    if (a == null){
      a = new InsnExecCount();
      insn.addAttr( a);
    }

    SystemState ss = env.getSystemState();
    if (!ss.hasRestorer(a)){
      env.getSystemState().putRestorer( a, new InsnCountRestorer(a));      
    }
    
    a.count++;
    env.setIntField(objref, "id", a.count);
  }
}
