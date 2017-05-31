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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * Exit monitor for object 
 * ..., objectref => ... 
 */
public class MONITOREXIT extends LockInstruction {

  @Override
  public Instruction execute (ThreadInfo ti) {
    boolean didUnblock = false;
    StackFrame frame = ti.getTopFrame();
    Scheduler scheduler = ti.getScheduler();
    
    int objref = frame.peek();
    if (objref == MJIEnv.NULL) {
      return ti.createAndThrowException("java.lang.NullPointerException", "attempt to release lock for null object");
    }

    lastLockRef = objref;
    ElementInfo ei = ti.getElementInfo(objref);
    ei = scheduler.updateObjectSharedness(ti, ei, null); // locks most likely belong to shared objects
    
    if (!ti.isFirstStepInsn()){
      ei = ei.getModifiableInstance();
      // we only do this in the top half of the first execution, but before potentially creating
      // a CG so that blocked threads are runnable again
      didUnblock = ei.unlock(ti);
    }
    
    if (ei.getLockCount() == 0) { // might still be recursively locked, which wouldn't be a release
      if (scheduler.setsLockReleaseCG(ti, ei, didUnblock)){ // scheduling point
        return this;
      }
    }

    // bottom half or monitorexit proceeded
    frame = ti.getModifiableTopFrame();
    frame.pop();

    return getNext(ti);
  }

  @Override
  public int getByteCode () {
    return 0xC3;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
