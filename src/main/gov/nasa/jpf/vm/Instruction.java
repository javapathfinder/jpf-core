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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.util.ObjectList;
import gov.nasa.jpf.util.Source;
import gov.nasa.jpf.vm.bytecode.InstructionInterface;



/**
 * common root of all JPF bytecode instruction classes 
 * 
 */
public abstract class Instruction implements Cloneable, InstructionInterface {

  protected int insnIndex;        // code[] index of instruction
  protected int position;     // accumulated bytecode position (prev pos + prev bc-length)
  protected MethodInfo mi;    // the method this insn belongs to

  // property/mode specific attributes
  protected Object attr;
  
  // this is for changing from InstructionInterface types to Instruction types
  @Override
  public Instruction asInstruction(){
    return this;
  }
  
  // to allow a classname and methodname context for each instruction
  public void setContext(String className, String methodName, int lineNumber,
          int offset) {
  }

  /**
   * is this the first instruction in a method
   */
  @Override
  public boolean isFirstInstruction() {
    return (insnIndex == 0);
  }


  /**
   * answer if this is a potential loop closing jump
   */
  @Override
  public boolean isBackJump() {
    return false;
  }

  /**
   * is this instruction part of a monitorenter code pattern 
   */
  public boolean isMonitorEnterPrologue(){
    return false;
  }

  /**
   * is this one of our own, artificial insns?
   */
  @Override
  public boolean isExtendedInstruction() {
    return false;
  }


  @Override
  public MethodInfo getMethodInfo() {
    return mi;
  }


  /**
   * that's used for explicit construction of MethodInfos (synthetic methods)
   */
  public void setMethodInfo(MethodInfo mi) {
    this.mi = mi;
  }

  /**
   * this returns the instruction at the following code insnIndex within the same
   * method, which might or might not be the next one to enter (branches, overlay calls etc.).
   */
  @Override
  public Instruction getNext() {
    return mi.getInstruction(insnIndex + 1);
  }

  @Override
  public int getInstructionIndex() {
    return insnIndex;
  }

  @Override
  public int getPosition() {
    return position;
  }

  public void setLocation(int insnIdx, int pos) {
    insnIndex = insnIdx;
    position = pos;
  }

  /**
   * return the length in bytes of this instruction.
   * override if this is not 1
   */
  @Override
  public int getLength() {
    return 1;
  }

  @Override
  public Instruction getPrev() {
    if (insnIndex > 0) {
      return mi.getInstruction(insnIndex - 1);
    } else {
      return null;
    }
  }

  /**
   * this is for listeners that process instructionExecuted(), but need to
   * determine if there was a CG registration, an overlayed direct call
   * (like clinit) etc.
   * The easy case is the instruction not having been executed yet, in
   * which case ti.getNextPC() == null
   * There are two cases for re-execution: either nextPC was set to the
   * same insn (which is what CG creators usually use), or somebody just
   * pushed another stackframe that executes something which will return to the
   * same insn (that is what automatic <clinit> calls and the like do - we call
   * it overlays)
   */
  @Override
  public boolean isCompleted(ThreadInfo ti) {
    Instruction nextPc = ti.getNextPC();

    if (nextPc == null) {
      return ti.isTerminated();

    } else {

      return (nextPc != this) && (ti.getStackFrameExecuting(this, 1) == null);
    }

    // <2do> how do we account for exceptions? 
  }

  /**
   * this method can be overridden if instruction classes have to store
   * information for instructionExecuted() notifications, and this information
   * should not be stored persistent to avoid memory leaks (e.g. via traces).
   * Called by ThreadInfo.executeInstruction
   */
  public void cleanupTransients(){
    // nothing here
  }
  
  public boolean isSchedulingRelevant(SystemState ss, KernelState ks, ThreadInfo ti) {
    return false;
  }

  /**
   * this is the real workhorse
   * returns next instruction to enter in this thread
   * 
   * <2do> it's unfortunate we roll every side effect into this method, because
   * it diminishes the value of the 'executeInstruction' notification: all
   * insns that require some sort of late binding (InvokeVirtual, GetField, ..)
   * are not yet fully analyzable (e.g. the callee of InvokeVirtuals is not
   * known yet), putting the burden of duplicating the related code of
   * enter() in the listener. It would be better if we factor this
   * 'prepareExecution' out of enter()
   */
  @Override
  public abstract Instruction execute(ThreadInfo ti);

  @Override
  public String toString() {
    return getMnemonic();
  }

  /**
   * this can contain additional info that was gathered/cached during execution 
   */
  @Override
  public String toPostExecString(){
    return toString();
  }
  
  @Override
  public String getMnemonic() {
    String s = getClass().getSimpleName();
    return s.toLowerCase();
  }

  @Override
  public int getLineNumber() {
    return mi.getLineNumber(this);
  }

  @Override
  public String getSourceLine() {
    ClassInfo ci = mi.getClassInfo();
    if (ci != null) {
      int line = mi.getLineNumber(this);
      String fileName = ci.getSourceFileName();

      Source src = Source.getSource(fileName);
      if (src != null) {
        String srcLine = src.getLine(line);
        if (srcLine != null) {
          return srcLine;
        }
      }
    }
    
    return null;
  }

  /**
   * this is for debugging/logging if we always want something back telling
   * us where this insn came from
   */
  public String getSourceOrLocation(){
    ClassInfo ci = mi.getClassInfo();
    if (ci != null) {
      int line = mi.getLineNumber(this);
      String file = ci.getSourceFileName();

      Source src = Source.getSource(file);
      if (src != null) {
        String srcLine = src.getLine(line);
        if (srcLine != null) {
          return srcLine;
        }
      }

      return "(" + file + ':' + line + ')'; // fallback

    } else {
      return "[synthetic] " + mi.getName();
    }
  }
  
  
  /**
   * this returns a "pathname:line" string
   */
  @Override
  public String getFileLocation() {
    ClassInfo ci = mi.getClassInfo();
    if (ci != null) {
      int line = mi.getLineNumber(this);
      String fname = ci.getSourceFileName();
      return (fname + ':' + line);
    } else {
      return "[synthetic] " + mi.getName();
    }
  }

  /**
   * this returns a "filename:line" string
   */
  @Override
  public String getFilePos() {
    String file = null;
    int line = -1;
    ClassInfo ci = mi.getClassInfo();

    if (ci != null){
      line = mi.getLineNumber(this);
      file = ci.getSourceFileName();
      if (file != null){
        int i = file.lastIndexOf('/'); // ClassInfo.sourceFileName is using '/'
        if (i >= 0) {
          file = file.substring(i + 1);
        }
      }
    }

    if (file != null) {
      if (line != -1){
        return (file + ':' + line);
      } else {
        return file;
      }
    } else {
      return ("pc " + position);
    }
  }

  /**
   * this returns a "class.method(line)" string
   */
  @Override
  public String getSourceLocation() {
    ClassInfo ci = mi.getClassInfo();

    if (ci != null) {
      String s = ci.getName() + '.' + mi.getName() +
              '(' + getFilePos() + ')';
      return s;

    } else {
      return null;
    }
  }

  public void init(MethodInfo mi, int offset, int position) {
    this.mi = mi;
    this.insnIndex = offset;
    this.position = position;
  }

  /**
   * this is a misnomer - we actually push the clinit calls here in case
   * we need some. 'causedClinitCalls' might be more appropriate, but it is
   * used in a number of external projects
   */
  public boolean requiresClinitExecution(ThreadInfo ti, ClassInfo ci) {
    return ci.initializeClass(ti);
  }

  /**
   * this is returning the next Instruction to enter, to be called to obtain
   * the return value of enter() if this is not a branch insn
   *
   * Be aware of that we might have had exceptions caused by our execution
   * (-> lower frame), or we might have had overlaid calls (-> higher frame),
   * i.e. we can't simply assume it's the following insn. We have to
   * acquire this through the top frame of the ThreadInfo.
   *
   * note: the System.exit() problem should be gone, now that it is implemented
   * as ThreadInfo state (TERMINATED), rather than purged stacks
   */
  @Override
  public Instruction getNext (ThreadInfo ti) {
    return ti.getPC().getNext();
  }

  
  //--- the generic attribute API

  @Override
  public boolean hasAttr () {
    return (attr != null);
  }

  @Override
  public boolean hasAttr (Class<?> attrType){
    return ObjectList.containsType(attr, attrType);
  }

  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */
  @Override
  public Object getAttr(){
    return attr;
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  @Override
  public void setAttr (Object a){
    attr = ObjectList.set(attr, a);    
  }

  @Override
  public void addAttr (Object a){
    attr = ObjectList.add(attr, a);
  }

  @Override
  public void removeAttr (Object a){
    attr = ObjectList.remove(attr, a);
  }

  @Override
  public void replaceAttr (Object oldAttr, Object newAttr){
    attr = ObjectList.replace(attr, oldAttr, newAttr);
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  @Override
  public <T> T getAttr (Class<T> attrType) {
    return ObjectList.getFirst(attr, attrType);
  }

  @Override
  public <T> T getNextAttr (Class<T> attrType, Object prev) {
    return ObjectList.getNext(attr, attrType, prev);
  }

  @Override
  public ObjectList.Iterator attrIterator(){
    return ObjectList.iterator(attr);
  }
  
  @Override
  public <T> ObjectList.TypedIterator<T> attrIterator(Class<T> attrType){
    return ObjectList.typedIterator(attr, attrType);
  }

  // -- end attrs --

  /**
   * this is overridden by any Instruction that use a cache for class or 
   * method to provide a type safe cloning
   */
  public Instruction typeSafeClone(MethodInfo mi) {
    Instruction clone = null;

    try {
      clone = (Instruction) super.clone();

      // reset the method that this insn belongs to
      clone.mi = mi;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return clone;
  }
}
