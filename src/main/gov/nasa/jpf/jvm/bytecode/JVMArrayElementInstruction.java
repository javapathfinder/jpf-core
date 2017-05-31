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
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.bytecode.ArrayElementInstruction;

/**
 * abstract class for operations that access elements of arrays
 */
public abstract class JVMArrayElementInstruction extends  ArrayElementInstruction {
  
  protected int arrayRef;
  protected int index; // the accessed element

  // we cache these to avoid the need for executeInstruction() listening
  // if attrs are processed in instructionExecuted()
  protected Object arrayOperandAttr;
  protected Object indexOperandAttr;
  
  // we need this to be abstract because of the LongArrayStore insns
  @Override
  abstract public int peekIndex (ThreadInfo ti);
  abstract public int peekArrayRef (ThreadInfo ti);

  abstract public Object peekIndexAttr (ThreadInfo ti);
  abstract public Object peekArrayAttr (ThreadInfo ti);

  public boolean isReferenceArray() {
    return false;
  }
  
  @Override
  public ElementInfo getElementInfo (ThreadInfo ti){
    if (isCompleted(ti)){
      return ti.getElementInfo(arrayRef);
    } else {
      int ref = peekArrayRef(ti);
      return ti.getElementInfo(arrayRef);
    }
  }
  
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

  public Object getArrayOperandAttr (ThreadInfo ti){
    if (ti.isPreExec()) {
      return peekArrayAttr(ti);
    } else {
      return arrayOperandAttr;
    }
  }

  public Object getIndexOperandAttr (ThreadInfo ti){
    if (ti.isPreExec()) {
      return peekIndexAttr(ti);
    } else {
      return indexOperandAttr;
    }
  }


  @Override
  public ElementInfo peekArrayElementInfo (ThreadInfo ti){
    int aref = getArrayRef(ti);
    return ti.getElementInfo(aref);
  }
  
  public int getIndex (ThreadInfo ti){
    if (!isCompleted(ti)){
      return peekIndex(ti);
    } else {
      return index;
    }
  }
  
  /**
   * return size of array elements in stack words (long,double: 2, all other: 1)
   * e.g. used to determine where the object reference is on the stack
   * 
   * should probably be abstract, but there are lots of subclasses and only LongArrayLoad/Store insns have different size
   */
  protected int getElementSize () {
    return 1;
  }
  
  @Override
  public abstract boolean isRead();
  
}
