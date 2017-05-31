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

import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * abstraction for all array load instructions
 *
 * ..., array, index => ..., value
 */
public abstract class ArrayLoadInstruction extends JVMArrayElementInstruction implements JVMInstruction {
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();

    index = frame.peek();
    arrayRef = frame.peek(1); // ..,arrayRef,idx
    if (arrayRef == MJIEnv.NULL) {
      return ti.createAndThrowException("java.lang.NullPointerException");
    }
    ElementInfo eiArray = ti.getElementInfo(arrayRef);

    indexOperandAttr = peekIndexAttr(ti);
    arrayOperandAttr = peekArrayAttr(ti);

    Scheduler scheduler = ti.getScheduler();
    if (scheduler.canHaveSharedArrayCG( ti, this, eiArray, index)){ // don't modify the frame before this
      eiArray = scheduler.updateArraySharedness(ti, eiArray, index);
      if (scheduler.setsSharedArrayCG( ti, this, eiArray, index)){
        return this;
      }
    }
    
    frame.pop(2); // now we can pop index and array reference
    
    try {
      push(frame, eiArray, index);

      Object elementAttr = eiArray.getElementAttr(index);
      if (elementAttr != null) {
        if (getElementSize() == 1) {
          frame.setOperandAttr(elementAttr);
        } else {
          frame.setLongOperandAttr(elementAttr);
        }
      }
      
      return getNext(ti);
      
    } catch (ArrayIndexOutOfBoundsExecutiveException ex) {
      return ex.getInstruction();
    }
  }

  protected boolean isReference () {
    return false;
  }

  /**
   * only makes sense pre-exec
   */
  @Override
  public int peekArrayRef (ThreadInfo ti){
    return ti.getTopFrame().peek(1);
  }

  @Override
  public Object peekArrayAttr (ThreadInfo ti){
    return ti.getTopFrame().getOperandAttr(1);
  }

  // wouldn't really be required for loads, but this is a general
  // ArrayInstruction API
  @Override
  public int peekIndex (ThreadInfo ti){
    return ti.getTopFrame().peek();
  }

  @Override
  public Object peekIndexAttr (ThreadInfo ti){
    return ti.getTopFrame().getOperandAttr();
  }

  protected abstract void push (StackFrame frame, ElementInfo e, int index)
                throws ArrayIndexOutOfBoundsExecutiveException;

  
  @Override
  public boolean isRead() {
    return true;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
 }
