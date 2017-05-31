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

import java.util.Iterator;

/**
 * common base for DRETURN and LRETURN
 */
public abstract class LongReturn extends JVMReturnInstruction {

  protected long ret;
  
  @Override
  public int getReturnTypeSize() {
    return 2;
  }
  
  @Override
  protected Object getReturnedOperandAttr (StackFrame frame) {
    return frame.getLongOperandAttr();
  }
  
  @Override
  protected void getAndSaveReturnValue (StackFrame frame) {
    ret = frame.popLong();
  }

  @Override
  protected void pushReturnValue (StackFrame frame) {
    frame.pushLong(ret);
  }

  //--- attribute accessors 
  
  @Override
  public boolean hasReturnAttr (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    return frame.hasLongOperandAttr();
  }
  @Override
  public boolean hasReturnAttr (ThreadInfo ti, Class<?> type){
    StackFrame frame = ti.getTopFrame();
    return frame.hasLongOperandAttr(type);
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   * 
   * obviously, this only makes sense from an instructionExecuted(), since
   * the value is pushed during the enter(). Use ObjectList to access values
   */
  @Override
  public Object getReturnAttr (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    return frame.getLongOperandAttr();
  }
  
  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   * 
   * we don't clone since pushing a return value already changed the caller frame
   */
  @Override
  public void setReturnAttr (ThreadInfo ti, Object a){
    StackFrame frame = ti.getModifiableTopFrame();
    frame.setLongOperandAttr(a);
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  @Override
  public <T> T getReturnAttr (ThreadInfo ti, Class<T> type){
    StackFrame frame = ti.getTopFrame();
    return frame.getLongOperandAttr(type);
  }
  @Override
  public <T> T getNextReturnAttr (ThreadInfo ti, Class<T> type, Object prev){
    StackFrame frame = ti.getTopFrame();
    return frame.getNextLongOperandAttr(type, prev);
  }
  @Override
  public Iterator returnAttrIterator (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    return frame.longOperandAttrIterator();
  }
  @Override
  public <T> Iterator<T> returnAttrIterator (ThreadInfo ti, Class<T> type){
    StackFrame frame = ti.getTopFrame();
    return frame.longOperandAttrIterator(type);
  }
  
  @Override
  public void addReturnAttr (ThreadInfo ti, Object attr){
    StackFrame frame = ti.getModifiableTopFrame();
    frame.addLongOperandAttr(attr);
  }


}
