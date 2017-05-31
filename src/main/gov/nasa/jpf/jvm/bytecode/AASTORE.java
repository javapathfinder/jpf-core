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

import gov.nasa.jpf.util.InstructionState;
import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Store into reference array
 * ..., arrayref, index, value  => ...
 */
public class AASTORE extends ArrayStoreInstruction {

  int value;

  @Override
  public boolean isReferenceArray() {
    return true;
  }
  
  @Override
  protected void popValue(StackFrame frame){
    value = frame.pop();
  }

  @Override
  protected void setField (ElementInfo ei, int index) throws ArrayIndexOutOfBoundsExecutiveException {
    ei.checkArrayBounds(index);
    ei.setReferenceElement(index, value);
  }
  
  /**
   * overridden because AASTORE can cause ArrayStoreExceptions and exposure CGs 
   */
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    int refValue = frame.peek();
    int idx = frame.peek(1);
    int aref = frame.peek(2);
    
    value = aref;
    index = idx;
    
    if (aref == MJIEnv.NULL) {
      return ti.createAndThrowException("java.lang.NullPointerException");
    }
    
    ElementInfo eiArray = ti.getModifiableElementInfo(aref);
        
    if (!ti.isFirstStepInsn()){ // we only need to check this once
      Instruction xInsn = checkArrayStoreException(ti, frame, eiArray);
      if (xInsn != null){
        return xInsn;
      }
    }
    
    boolean checkExposure = false;
    Scheduler scheduler = ti.getScheduler();
    if (scheduler.canHaveSharedArrayCG(ti, this, eiArray, idx)){
      checkExposure = true;
      eiArray = scheduler.updateArraySharedness(ti, eiArray, index);
      if (scheduler.setsSharedArrayCG(ti, this, eiArray, idx)){
        return this;
      }
    }

    // check if this gets re-executed from an exposure CG
    if (frame.getAndResetFrameAttr(InstructionState.class) == null){
      try {
        Object attr = frame.getOperandAttr();
        eiArray.checkArrayBounds(idx);
        eiArray.setReferenceElement(idx, refValue);
        eiArray.setElementAttrNoClone(idx, attr);
        
      } catch (ArrayIndexOutOfBoundsExecutiveException ex) { // at this point, the AIOBX is already processed
        return ex.getInstruction();
      }

      if (checkExposure) {
        if (refValue != MJIEnv.NULL) {
          ElementInfo eiExposed = ti.getElementInfo(refValue);
          if (scheduler.setsSharedObjectExposureCG(ti, this, eiArray, null, eiArray)) {
            frame.addFrameAttr( InstructionState.processed);
            return this;
          }
        }
      }
    }
    
    frame.pop(3);
    
    return getNext(ti);      
  }

  protected Instruction checkArrayStoreException(ThreadInfo ti, StackFrame frame, ElementInfo ei){
    ClassInfo c = ei.getClassInfo();
    int refVal = frame.peek();
    
    if (refVal != MJIEnv.NULL) { // no checks for storing 'null'
      ClassInfo elementCi = ti.getClassInfo(refVal);
      ClassInfo arrayElementCi = c.getComponentClassInfo();
      if (!elementCi.isInstanceOf(arrayElementCi)) {
        String exception = "java.lang.ArrayStoreException";
        String exceptionDescription = elementCi.getName();
        return ti.createAndThrowException(exception, exceptionDescription);
      }
    }

    return null;
  }


  @Override
  public int getByteCode () {
    return 0x53;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
