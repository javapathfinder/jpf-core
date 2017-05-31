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

import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.bytecode.ReturnValueInstruction;


/**
 * Return reference from method
 * ..., objectref  => [empty]
 */
public class ARETURN extends JVMReturnInstruction implements ReturnValueInstruction {
  int ret;
  
  @Override
  public int getReturnTypeSize() {
    return 1;
  }
  
  @Override
  public int getValueSlot (StackFrame frame){
    return frame.getTopPos();
  }
  
  @Override
  protected Object getReturnedOperandAttr (StackFrame frame) {
    return frame.getOperandAttr();
  }
  
  @Override
  protected void getAndSaveReturnValue (StackFrame frame) {
    ret = frame.pop();
  }
  
  @Override
  protected void pushReturnValue (StackFrame frame) {
    frame.pushRef(ret);
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
    
    if (ret == MJIEnv.NULL) {
      return null;
    } else {
      return ti.getElementInfo(ret);
    }
  }
  
  @Override
  public int getByteCode () {
    return 0xB0;
  }
    
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

}
