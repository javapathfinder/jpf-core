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

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;

import java.util.Iterator;


/**
 * abstraction for the various return instructions
 */
public abstract class JVMReturnInstruction extends ReturnInstruction implements JVMInstruction {

  // to store where we came from
  protected StackFrame returnFrame;

  abstract public int getReturnTypeSize();
  abstract protected Object getReturnedOperandAttr(StackFrame frame);
  
  // note these are only callable from within the same enter - thread interleavings
  // would cause races
  abstract protected void getAndSaveReturnValue (StackFrame frame);
  abstract protected void pushReturnValue (StackFrame frame);

  public abstract Object getReturnValue(ThreadInfo ti);

  public StackFrame getReturnFrame() {
    return returnFrame;
  }

  public void setReturnFrame(StackFrame frame){
    returnFrame = frame;
  }

  /**
   * this is important since keeping the StackFrame alive would be a major
   * memory leak
   */
  @Override
  public void cleanupTransients(){
    returnFrame = null;
  }
  
  //--- attribute accessors
  
  // the accessors are here to save the client some effort regarding the
  // return type (slot size).
  // Since these are all public methods that can be called by listeners,
  // we stick to the ThreadInfo argument
  
  public boolean hasReturnAttr (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    return frame.hasOperandAttr();
  }
  public boolean hasReturnAttr (ThreadInfo ti, Class<?> type){
    StackFrame frame = ti.getTopFrame();
    return frame.hasOperandAttr(type);
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   * 
   * obviously, this only makes sense from an instructionExecuted(), since
   * the value is pushed during the enter(). Use ObjectList to access values
   */
  public Object getReturnAttr (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    return frame.getOperandAttr();
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   * 
   * we don't clone since pushing a return value already changed the caller frame
   */
  public void setReturnAttr (ThreadInfo ti, Object a){
    StackFrame frame = ti.getModifiableTopFrame();
    frame.setOperandAttr(a);
  }
  
  public void addReturnAttr (ThreadInfo ti, Object attr){
    StackFrame frame = ti.getModifiableTopFrame();
    frame.addOperandAttr(attr);
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getReturnAttr (ThreadInfo ti, Class<T> type){
    StackFrame frame = ti.getTopFrame();
    return frame.getOperandAttr(type);
  }
  public <T> T getNextReturnAttr (ThreadInfo ti, Class<T> type, Object prev){
    StackFrame frame = ti.getTopFrame();
    return frame.getNextOperandAttr(type, prev);
  }
  public Iterator<?> returnAttrIterator (ThreadInfo ti){
    StackFrame frame = ti.getTopFrame();
    return frame.operandAttrIterator();
  }
  public <T> Iterator<T> returnAttrIterator (ThreadInfo ti, Class<T> type){
    StackFrame frame = ti.getTopFrame();
    return frame.operandAttrIterator(type);
  }
  
  // -- end attribute accessors --
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    boolean didUnblock = false;
    
    if (!ti.isFirstStepInsn()) {
      didUnblock = ti.leave();  // takes care of unlocking before potentially creating a CG
    }
    
    if (mi.isSynchronized()) {
      int objref = mi.isStatic() ? mi.getClassInfo().getClassObjectRef() : ti.getThis();
      ElementInfo ei = ti.getElementInfo(objref);

      if (ei.getLockCount() == 0) {
        if (ti.getScheduler().setsLockReleaseCG(ti, ei, didUnblock)){
          return this;
        }
      }
    }

    StackFrame frame = ti.getModifiableTopFrame();
    returnFrame = frame;
    Object attr = getReturnedOperandAttr(frame); // the return attr - get this before we pop
    getAndSaveReturnValue(frame);
    
    // note that this is never the first frame, since we start all threads (incl. main)
    // through a direct call
    frame = ti.popAndGetModifiableTopFrame();

    // remove args, push return value and continue with next insn
    // (DirectCallStackFrames don't use this)
    frame.removeArguments(mi);
    pushReturnValue(frame);

    if (attr != null) {
      setReturnAttr(ti, attr);
    }

    return frame.getPC().getNext();
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
  
  @Override
  public String toPostExecString() {
    return getMnemonic() + " [" + mi.getFullName() + ']';
  }
}
