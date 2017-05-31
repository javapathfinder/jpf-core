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

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;

/**
 * this is used to return from a DirectCallStackFrame
 *
 * Note that it is NOT a ReturnInstruction, in case listeners monitor these
 * and expect corresponding InvokeInstructions. Although this would seem intuitive, it
 * would be pointless to derive because the ReturnInstruction.enter() does
 * a lot of things we would have to cut off, i.e. it would require more effort
 * to undo this (no sync, no return value, no pc advance on the returned-to
 * stackframe etc.)
 *
 * However, having a dedicated direct call return instruction makes sense so
 * that the ReturnInstruction of the called method does not have to handle
 * direct calls specifically
 */
public class DIRECTCALLRETURN extends ReturnInstruction implements JVMInstruction  {

  @Override
  public boolean isExtendedInstruction() {
    return true;
  }

  public static final int OPCODE = 261;

  @Override
  public int getByteCode () {
    return OPCODE;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    if (ti.getStackDepth() == 1){ // thread exit point
      // this can execute several times because of the different locks involved
    
      if (!ti.exit()){
        return this; // repeat, we couldn't get the lock
      } else {
        return null;
      }      
      
    } else {
      // pop the current frame but do not advance the new top frame, and do
      // not touch its operand stack
    
      StackFrame frame = ti.popDirectCallFrame();
      return frame.getPC();
    }
  }
}
