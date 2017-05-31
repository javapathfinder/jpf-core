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
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Get length of array 
 * ..., arrayref => ..., length
 */
public class ARRAYLENGTH extends Instruction implements JVMInstruction {
    
  protected int arrayRef;

  /**
   * only makes sense from an executeInstruction() or instructionExecuted() listener,
   * it is undefined outside of insn exec notifications
   */
  public int getArrayRef (ThreadInfo ti){
    if (ti.isPreExec()){
      return peekArrayRef(ti);
    } else {
      return arrayRef;
    }
  }

  public ElementInfo peekArrayElementInfo (ThreadInfo ti){
    int aref = getArrayRef(ti);
    return ti.getElementInfo(aref);
  }
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();

    arrayRef = frame.pop();

    if (arrayRef == MJIEnv.NULL){
      return ti.createAndThrowException("java.lang.NullPointerException",
                                        "array length of null object");
    }

    ElementInfo ei = ti.getElementInfo(arrayRef);
    frame.push(ei.arrayLength(), false);

    return getNext(ti);
  }
  
  @Override
  public int getByteCode () {
    return 0xBE;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  protected int peekArrayRef (ThreadInfo ti) {
    return ti.getTopFrame().peek();
  }
}
