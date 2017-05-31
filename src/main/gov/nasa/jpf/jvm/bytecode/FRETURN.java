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
 * Return float from method
 * ..., value => [empty]
 */
public class FRETURN extends JVMReturnInstruction {

  float ret;
  
  @Override
  public int getReturnTypeSize() {
    return 1;
  }

  @Override
  protected Object getReturnedOperandAttr (StackFrame frame) {
    return frame.getOperandAttr();
  }
  
  @Override
  protected void getAndSaveReturnValue (StackFrame frame) {
    ret = frame.popFloat();
  }
  
  @Override
  protected void pushReturnValue (StackFrame frame) {
    frame.pushFloat(ret);
  }
  
  public float getReturnValue () {
    return ret;
  }
  
  @Override
  public Float getReturnValue (ThreadInfo ti) {
    if (!isCompleted(ti)) { // we have to pull it from the operand stack
      StackFrame frame = ti.getTopFrame();
      ret = frame.peekFloat();
    }
    
    return new Float(ret);
  }
  
  @Override
  public int getByteCode () {
    return 0xAE;
  }
  
  @Override
  public String toString() {
    return "freturn " + mi.getFullName();
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
