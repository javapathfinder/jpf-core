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
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Branch always
 * No change
 *
 * <2do> store this as code insnIndex, not as bytecode position
 */
public class GOTO extends Instruction implements JVMInstruction {
  protected int targetPosition;
  Instruction target;

  public GOTO (int targetPosition){
    this.targetPosition = targetPosition;
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    if (isBackJump() && ti.maxTransitionLengthExceeded()){
      // this is a rather simplistic attempt to terminate the search in
      // endless loops that do not change program state.
      // <2do> this does not handle nested loops yet
      if (ti.breakTransition("MAX_TRANSITION_LENGTH")){
        return this; // re-execute after giving state matching a chance to prune the search
      }
    }
    
    return getTarget();
  }

  @Override
  public boolean isBackJump () {
    return (targetPosition <= position);
  }
  
  public Instruction getTarget() {
    if (target == null) {
      target = mi.getInstructionAt(targetPosition);
    }
    return target;
  }

  @Override
  public int getLength() {
    return 3; // opcode, bb1, bb2
  }
  
  @Override
  public int getByteCode () {
    return 0xA7;
  }
  
  @Override
  public String toString () {
    return getMnemonic() + " " + targetPosition;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction typeSafeClone(MethodInfo mi) {
    GOTO clone = null;

    try {
      clone = (GOTO) super.clone();

      // reset the method that this insn belongs to
      clone.mi = mi;

      clone.target = null;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return clone;
  }
}
