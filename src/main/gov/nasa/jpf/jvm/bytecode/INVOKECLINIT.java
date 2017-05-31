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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * this is an artificial bytecode that we use to deal with the particularities of 
 * <clinit> calls, which are never in the loaded bytecode but always directly called by
 * the VM. The most obvious difference is that <clinit> execution does not trigger
 * class initialization.
 * A more subtle difference is that we save a wait() - if a class
 * is concurrently initialized, both enter INVOKECLINIT (i.e. compete and sync for/on
 * the class object lock), but once the second thread gets resumed and detects that the
 * class is now initialized (by the first thread), it skips the method execution and
 * returns right away (after deregistering as a lock contender). That's kind of hackish,
 * but we have no method to do the wait in, unless we significantly complicate the
 * direct call stubs, which would obfuscate observability (debugging dynamically
 * generated code isn't very appealing). 
 */
public class INVOKECLINIT extends INVOKESTATIC {

  public INVOKECLINIT (ClassInfo ci){
    super(ci.getSignature(), "<clinit>", "()V");
  }

  @Override
  public Instruction execute (ThreadInfo ti) {    
    MethodInfo callee = getInvokedMethod(ti);
    ClassInfo ciClsObj = callee.getClassInfo();
    ElementInfo ei = ciClsObj.getClassObject();

    if (ciClsObj.isInitialized()) { // somebody might have already done it if this is re-executed
      if (ei.isRegisteredLockContender(ti)){
        ei = ei.getModifiableInstance();
        ei.unregisterLockContender(ti);
      }
      return getNext();
    }

    // not much use to update sharedness, clinits are automatically synchronized
    if (reschedulesLockAcquisition(ti, ei)){     // this blocks or registers as lock contender
      return this;
    }
    
    // if we get here we still have to execute the clinit method
    setupCallee( ti, callee); // this creates, initializes & pushes the callee StackFrame, then acquires the lock
    ciClsObj.setInitializing(ti);

    return ti.getPC(); // we can't just return the first callee insn if a listener throws an exception
  }

  @Override
  public boolean isExtendedInstruction() {
    return true;
  }

  public static final int OPCODE = 256;

  @Override
  public int getByteCode () {
    return OPCODE;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
