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

import gov.nasa.jpf.vm.bytecode.StoreInstruction;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * abstraction for all array store instructions
 *
 *  ... array, index, <value> => ...
 */
public abstract class ArrayStoreInstruction extends JVMArrayElementInstruction implements StoreInstruction, JVMInstruction {


  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    int idx = peekIndex(ti);
    int aref = peekArrayRef(ti); // need to be polymorphic, could be LongArrayStore
    ElementInfo eiArray = ti.getElementInfo(aref);

    arrayOperandAttr = peekArrayAttr(ti);
    indexOperandAttr = peekIndexAttr(ti);

    if (!ti.isFirstStepInsn()){ // first execution, top half
      //--- runtime exceptions
      if (aref == MJIEnv.NULL) {
        return ti.createAndThrowException("java.lang.NullPointerException");
      }
    
      //--- shared access CG
      Scheduler scheduler = ti.getScheduler();
      if (scheduler.canHaveSharedArrayCG(ti, this, eiArray, idx)){
        eiArray = scheduler.updateArraySharedness(ti, eiArray, idx);
        if (scheduler.setsSharedArrayCG(ti, this, eiArray, idx)){
          return this;
        }
      }
    }
    
    try {
      setArrayElement(ti, frame, eiArray); // this pops operands
    } catch (ArrayIndexOutOfBoundsExecutiveException ex) { // at this point, the AIOBX is already processed
      return ex.getInstruction();
    }

    return getNext(ti);
  }

  protected void setArrayElement (ThreadInfo ti, StackFrame frame, ElementInfo eiArray) throws ArrayIndexOutOfBoundsExecutiveException {
    int esize = getElementSize();
    Object attr = esize == 1 ? frame.getOperandAttr() : frame.getLongOperandAttr();
    
    popValue(frame);
    index = frame.pop();
    // don't set 'arrayRef' before we do the CG checks (would kill loop optimization)
    arrayRef = frame.pop();

    eiArray = eiArray.getModifiableInstance();
    setField(eiArray, index);
    eiArray.setElementAttrNoClone(index,attr); // <2do> what if the value is the same but not the attr?
  }
  
  /**
   * this is for pre-exec use
   */
  @Override
  public int peekArrayRef(ThreadInfo ti) {
    return ti.getTopFrame().peek(2);
  }

  @Override
  public int peekIndex(ThreadInfo ti){
    return ti.getTopFrame().peek(1);
  }

  // override in LongArrayStoreInstruction
  @Override
  public Object  peekArrayAttr (ThreadInfo ti){
    return ti.getTopFrame().getOperandAttr(2);
  }

  @Override
  public Object peekIndexAttr (ThreadInfo ti){
    return ti.getTopFrame().getOperandAttr(1);
  }


  protected abstract void popValue(StackFrame frame);
 
  protected abstract void setField (ElementInfo e, int index)
                    throws ArrayIndexOutOfBoundsExecutiveException;


  @Override
  public boolean isRead() {
    return false;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

}
