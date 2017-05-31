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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Access jump table by index and jump
 *   ..., index  => ...
 */
public class TABLESWITCH extends SwitchInstruction implements gov.nasa.jpf.vm.bytecode.TableSwitchInstruction {

  int min, max;

  public TABLESWITCH(int defaultTarget, int min, int max){
    super(defaultTarget, (max - min +1));
    this.min = min;
    this.max = max;
  }
  
  public int getMin(){
	  return min;
  }
  
  public int getMax(){
	  return max;
  }

  @Override
  public void setTarget (int value, int target){
    int i = value-min;

    if (i>=0 && i<targets.length){
      targets[i] = target;
    } else {
      throw new JPFException("illegal tableswitch target: " + value);
    }
  }

  @Override
  protected Instruction executeConditional (ThreadInfo ti){
    StackFrame frame = ti.getModifiableTopFrame();

    int value = frame.pop();
    int i = value-min;
    int pc;

    if (i>=0 && i<targets.length){
      lastIdx = i;
      pc = targets[i];
    } else {
      lastIdx = -1;
      pc = target;
    }

    // <2do> this is BAD - we should compute the target insns just once
    return mi.getInstructionAt(pc);
  }


  @Override
  public int getLength() {
    return 13 + 2*(matches.length); // <2do> NOT RIGHT: padding!!
  }
  
  @Override
  public int getByteCode () {
    return 0xAA;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
