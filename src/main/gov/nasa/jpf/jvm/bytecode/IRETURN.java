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

import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Return int from method
 * ..., value => [empty]
 */
public class IRETURN extends JVMReturnInstruction {

  int ret;
  
  @Override
  public int getReturnTypeSize() {
    return 1;
  }
  
  @Override
  protected Object getReturnedOperandAttr (StackFrame frame) {
    return frame.getOperandAttr();
  }
  
  @Override
  protected void getAndSaveReturnValue (StackFrame ti) {
    ret = ti.pop();
  }
  
  @Override
  protected void pushReturnValue (StackFrame ti) {
    ti.push(ret);
  }
  
  public int getReturnValue () {
    return ret;
  }
  
  @Override
  public Object getReturnValue(ThreadInfo ti) {
    if (!isCompleted(ti)) { // we have to pull it from the operand stack
      StackFrame frame = ti.getTopFrame();
      ret = frame.peek();
    }

    return new Integer(ret);
  }
  
  @Override
  public int getByteCode () {
    return 0xAC;
  }
  
  @Override
  public String toString() {
    return "ireturn " + mi.getFullName();
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
