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
import gov.nasa.jpf.vm.BooleanChoiceGenerator;
import gov.nasa.jpf.vm.KernelState;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * abstraction for all comparison instructions
 */
public abstract class IfInstruction extends Instruction implements JVMInstruction {
  protected int targetPosition;  // insn position at jump insnIndex
  protected Instruction target;  // jump target
  
  protected boolean conditionValue;  /** value of last evaluation of branch condition */

  protected IfInstruction(int targetPosition){
    this.targetPosition = targetPosition;
  }

  /**
   * return which branch was taken. Only useful after instruction got executed
   * WATCH OUT - 'true' means the jump condition is met, which logically is
   * the 'false' branch
   */
  public boolean getConditionValue() {
    return conditionValue;
  }
    
  /**
   *  Added so that SimpleIdleFilter can detect do-while loops when 
   * the while statement evaluates to true.
   */
  @Override
  public boolean isBackJump () { 
    return (conditionValue) && (targetPosition <= position);
  }
    
  /** 
   * retrieve value of jump condition from operand stack
   * (not ideal to have this public, but some listeners might need it for
   * skipping the insn, plus we require it for subclass factorization)
   */
  public abstract boolean popConditionValue(StackFrame frame);
  
  public Instruction getTarget() {
    if (target == null) {
      target = mi.getInstructionAt(targetPosition);
    }
    return target;
  }
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();

    conditionValue = popConditionValue(frame);
    if (conditionValue) {
      return getTarget();
    } else {
      return getNext(ti);
    }
  }

  /**
   * use this as a delegatee in overridden executes of derived IfInstructions
   * (e.g. for symbolic execution)
   */
  protected Instruction executeBothBranches (SystemState ss, KernelState ks, ThreadInfo ti){
    if (!ti.isFirstStepInsn()) {
      BooleanChoiceGenerator cg = new BooleanChoiceGenerator(ti.getVM().getConfig(), "ifAll");
      if (ss.setNextChoiceGenerator(cg)){
        return this;

      } else {
        StackFrame frame = ti.getModifiableTopFrame();
        // some listener did override the CG, fallback to normal operation
        conditionValue = popConditionValue(frame);
        if (conditionValue) {
          return getTarget();
        } else {
          return getNext(ti);
        }
      }
      
    } else {
      BooleanChoiceGenerator cg = ss.getCurrentChoiceGenerator("ifAll", BooleanChoiceGenerator.class);
      assert (cg != null) : "no BooleanChoiceGenerator";
      
      StackFrame frame = ti.getModifiableTopFrame();
      popConditionValue(frame); // we are not interested in concrete values
      
      conditionValue = cg.getNextChoice();
      
      if (conditionValue) {
        return getTarget();
      } else {
        return getNext(ti);
      }

    }
  }
  
  @Override
  public String toString () {
    return getMnemonic() + " " + targetPosition;
  }
  
  @Override
  public int getLength() {
    return 3; // usually opcode, bb1, bb2
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction typeSafeClone(MethodInfo mi) {
    IfInstruction clone = null;

    try {
      clone = (IfInstruction) super.clone();

      // reset the method that this insn belongs to
      clone.mi = mi;

      clone.target = null;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return clone;
  }
}
