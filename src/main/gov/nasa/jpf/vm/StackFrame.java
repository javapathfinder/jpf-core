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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.BitSetN;
import gov.nasa.jpf.util.BitSet1024;
import gov.nasa.jpf.util.BitSet256;
import gov.nasa.jpf.util.BitSet64;
import gov.nasa.jpf.util.FixedBitSet;
import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.OATHash;
import gov.nasa.jpf.util.ObjectList;
import gov.nasa.jpf.util.PrintUtils;
import gov.nasa.jpf.vm.bytecode.InvokeInstruction;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Iterator;


/**
 * Describes callerSlots stack frame.
 *
 * Java methods always have bounded local and operand stack sizes, computed
 * at compile time, stored in the classfile, and checked at runtime by the
 * bytecode verifier. Consequently, we combine locals and operands in one
 * data structure with the following layout
 *
 *   slot[0]                : 'this'
 *   ..                          .. local vars
 *   slot[stackBase-1]      : last local var
 *   slot[stackBase]        : first operand slot
 *   ..    ^
 *   ..    | operand stack range
 *   ..    v
 *   slot[top]              : highest used operand slot
 *
 */
public abstract class StackFrame implements Cloneable {
  
  /**
   * this StackFrame is not allowed to be modified anymore because it has been state stored.
   * Set during state storage and checked upon each modification, causing exceptions on attempts
   * to modify callerSlots frozen instance. The flag is reset in clones
   */
  public static final int   ATTR_IS_FROZEN  = 0x100;  
  static final int ATTR_IS_REFLECTION = 0x1000;  
   /**
    * the previous StackFrame (usually the caller, null if first). To be set when
    * the frame is pushed on the ThreadInfo callstack
    */
  protected StackFrame prev;

  /**
   * state management related attributes similar to ElementInfo. The lower 16 bits
   * are stored/restored, the upper 16 bits are for transient use
   */
  protected int attributes;

  
  protected int top;                // top index of the operand stack (NOT size)
                                    // this points to the last pushed value

  protected int thisRef = MJIEnv.NULL;       // slots[0] can change, but we have to keep 'this'
  protected int stackBase;          // index where the operand stack begins

  protected int[] slots;            // the combined local and operand slots
  protected FixedBitSet isRef;      // which slots contain references

  protected Object frameAttr;       // optional user attrs for the whole frame
  
  /*
   * This array can be used to store attributes (e.g. variable names) for
   * operands. We don't do anything with this except of preserving it (across
   * dups etc.), so it's pretty much up to the VM listeners/peers what's stored
   *
   * NOTE: attribute values are not restored upon backtracking per default, but
   * attribute references are. If you need restoration of values, use copy-on-write
   * in your clients
   *
   * these are set on demand
   */
  protected Object[] attrs = null;  // the combined user-defined callerSlots (set on demand)

  protected Instruction pc;         // the next insn to execute (program counter)
  protected MethodInfo mi;          // which method is executed in this frame

  static final int[] EMPTY_ARRAY = new int[0];
  static final FixedBitSet EMPTY_BITSET = new BitSet64();

  protected StackFrame (MethodInfo callee, int nLocals, int nOperands){
    mi = callee;
    pc = mi.getInstruction(0);

    stackBase = nLocals;
    top = nLocals-1;

    int nSlots = nLocals + nOperands;
    if (nSlots > 0){
      slots = new int[nLocals + nOperands];
      isRef = createReferenceMap(slots.length);
    } else {
      // NativeStackFrames don't use locals or operands, but we
      // don't want to add tests to all our methods
      slots = EMPTY_ARRAY;
      isRef = EMPTY_BITSET;
    }
  }
  
  public StackFrame (MethodInfo callee){
    this( callee, callee.getMaxLocals(), callee.getMaxStack());
  }



  /**
   * Creates an empty stack frame. Used by clone.
   */
  protected StackFrame () {
  }

  /**
   * creates callerSlots dummy Stackframe for testing of operand/local operations
   * NOTE - TESTING ONLY! this does not have a MethodInfo
   */
  protected StackFrame (int nLocals, int nOperands){
    stackBase = nLocals;
    slots = new int[nLocals + nOperands];
    isRef = createReferenceMap(slots.length);
    top = nLocals-1;  // index, not size!
  }
  
  /**
   * re-execute method from the beginning - use with care
   */
  public void reset() {
    pc = mi.getInstruction(0);
  }  
  


  protected FixedBitSet createReferenceMap (int nSlots){
    if (nSlots <= 64){
      return new BitSet64();
    } else if (nSlots <= 256){
      return new BitSet256();  
    } else if (nSlots <= 1024) {
      return new BitSet1024();
    } else {
      return new BitSetN(nSlots);
    }
  }

  public boolean isNative() {
    return false;
  }
  
  public StackFrame getCallerFrame (){
    MethodInfo callee = mi;
    for (StackFrame frame = getPrevious(); frame != null; frame = frame.getPrevious()){
      Instruction insn = frame.getPC();
      if (insn instanceof InvokeInstruction){
        InvokeInstruction call = (InvokeInstruction)insn;
        if (call.getInvokedMethod() == callee){
          return frame;
        }
      }
    }
    
    return null;
  }
  
  /**
   * return the object reference for an instance method to be called (we are still in the
   * caller's frame). This only makes sense after all params have been pushed, before the
   * INVOKEx insn is executed
   */
  public int getCalleeThis (MethodInfo mi) {
    return getCalleeThis(mi.getArgumentsSize());
  }

  /**
   * return reference of called object in the context of the caller
   * (i.e. we are in the caller frame)
   */
  public int getCalleeThis (int size) {
    // top is the topmost index
    int i = size-1;
    if (top < i) {
      return MJIEnv.NULL;
    }

    return slots[top-i];
  }

  public StackFrame getPrevious() {
    return prev;
  }
  
  /**
   * to be set (by ThreadInfo) when the frame is pushed. Can also be used
   * for non-local gotos, but be warned - that's tricky
   */
  public void setPrevious (StackFrame frame){
    prev = frame;
  }

  public Object getLocalOrFieldValue (String id) {
    // try locals first
    LocalVarInfo lv = mi.getLocalVar(id, pc.getPosition());
    if (lv != null){
      return getLocalValueObject(lv);
    }

    // then fields
    return getFieldValue(id);
  }

  public Object getLocalValueObject (LocalVarInfo lv) {
    if (lv != null) { // might not have been compiled with debug info
      String sig = lv.getSignature();
      int slotIdx = lv.getSlotIndex();
      int v = slots[slotIdx];

      switch (sig.charAt(0)) {
        case 'Z':
          return Boolean.valueOf(v != 0);
        case 'B':
          return new Byte((byte) v);
        case 'C':
          return new Character((char) v);
        case 'S':
          return new Short((short) v);
        case 'I':
          return new Integer(v);
        case 'J':
          return new Long(Types.intsToLong(slots[slotIdx + 1], v)); // Java is big endian, Types expects low,high
        case 'F':
          return new Float(Float.intBitsToFloat(v));
        case 'D':
          return new Double(Double.longBitsToDouble(Types.intsToLong(slots[slotIdx + 1], v)));
        default:  // reference
          if (v >= 0) {
            return VM.getVM().getHeap().get(v);
          }
      }
    }

    return null;
  }

  public Object getFieldValue (String id) {
    // try instance fields first
    if (thisRef != MJIEnv.NULL) {  // it's an instance method
      ElementInfo ei = VM.getVM().getHeap().get(thisRef);
      Object v = ei.getFieldValueObject(id);
      if (v != null) {
        return v;
      }
    }

    // check static fields (in method class and its superclasses)
    return mi.getClassInfo().getStaticFieldValueObject(id);
  }

  public ClassInfo getClassInfo () {
    return mi.getClassInfo();
  }

  public String getClassName () {
    return mi.getClassInfo().getName();
  }

  public String getSourceFile () {
    return mi.getClassInfo().getSourceFileName();
  }

  /**
   * does any of the 'nTopSlots' hold callerSlots reference value of 'objRef'
   * 'nTopSlots' is usually obtained from MethodInfo.getNumberOfCallerStackSlots()
   */
  public boolean includesReferenceOperand (int nTopSlots, int objRef){

    for (int i=0, j=top-nTopSlots+1; i<nTopSlots && j>=0; i++, j++) {
      if (isRef.get(j) && (slots[j] == objRef)){
        return true;
      }
    }

    return false;
  }

  /**
   * does any of the operand slots hold callerSlots reference value of 'objRef'
   */
  public boolean includesReferenceOperand (int objRef){

    for (int i=stackBase; i<=top; i++) {
      if (isRef.get(i) && (slots[i] == objRef)){
        return true;
      }
    }

    return false;
  }

  /**
   * is this StackFrame modifying the KernelState
   * this is true unless this is callerSlots NativeStackFrame
   */
  public boolean modifiesState() {
    return true;
  }

  public boolean isDirectCallFrame () {
    return false;
  }

  public boolean isSynthetic() {
    return false;
  }

  // gets and sets some derived information
  public int getLine () {
    return mi.getLineNumber(pc);
  }


  /**
   * generic visitor for reference arguments
   */
  public void processRefArguments (MethodInfo miCallee, ReferenceProcessor visitor){
    int nArgSlots = miCallee.getArgumentsSize();

    for (int i=top-1; i>=top-nArgSlots; i--){
      if (isRef.get(i)){
        visitor.processReference(slots[i]);
      }
    }
  }

  public int getSlot(int idx){
    return slots[idx];
  }
  public boolean isReferenceSlot(int idx){
    return isRef.get(idx);
  }


  public void setOperand (int offset, int v, boolean isRefValue){
    int i = top-offset;
    slots[i] = v;
    isRef.set(i, isRefValue);
  }

  
  //----------------------------- various attribute accessors

  public boolean hasAttrs () {
    return attrs != null;
  }

  public boolean hasFrameAttr(){
    return frameAttr != null;
  }
  
  public boolean hasFrameAttr (Class<?> attrType){
    return ObjectList.containsType(frameAttr, attrType);
  }
  
  public boolean hasFrameAttrValue (Object a){
    return ObjectList.contains(frameAttr, a);
  }
  
  //--- the frame attr accessors 
  
 /**
   * this returns all of them - use either if you know there will be only
   * one attribute at callerSlots time, or check/process result with ObjectList
   */
  public Object getFrameAttr(){
    return frameAttr;
  }

  /**
   * this replaces all of them - use only if you know there are no 
   * SystemAttributes in the list (which would cause an exception)
   */
  public void setFrameAttr (Object attr){
    frameAttr = ObjectList.set(frameAttr, attr);    
  }

  public void addFrameAttr (Object attr){
    frameAttr = ObjectList.add(frameAttr, attr);
  }

  public void removeFrameAttr (Object attr){
    frameAttr = ObjectList.remove(frameAttr, attr);
  }

  public void replaceFrameAttr (Object oldAttr, Object newAttr){
    frameAttr = ObjectList.replace(frameAttr, oldAttr, newAttr);
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getFrameAttr (Class<T> attrType) {
    return ObjectList.getFirst(frameAttr, attrType);
  }
  
  public <T> T getAndResetFrameAttr (Class<T> attrType) {
    T attr = ObjectList.getFirst(frameAttr, attrType);
    if (attr != null){
      frameAttr = ObjectList.remove(frameAttr, attr);
    }
    return attr;
  }
  

  public <T> T getNextFrameAttr (Class<T> attrType, Object prev) {
    return ObjectList.getNext(frameAttr, attrType, prev);
  }

  public ObjectList.Iterator frameAttrIterator(){
    return ObjectList.iterator(frameAttr);
  }
  
  public <T> ObjectList.TypedIterator<T> frameAttrIterator(Class<T> attrType){
    return ObjectList.typedIterator(frameAttr, attrType);
  }
  
  //--- the top single-slot operand attrs

  public boolean hasOperandAttr(){
    if ((top >= stackBase) && (attrs != null)){
      return (attrs[top] != null);
    }
    return false;
  }
  public boolean hasOperandAttr(Class<?> type){
    if ((top >= stackBase) && (attrs != null)){
      return ObjectList.containsType(attrs[top], type);
    }
    return false;
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at callerSlots time, or check/process result with ObjectList
   */
  public Object getOperandAttr () {
    if ((top >= stackBase) && (attrs != null)){
      return attrs[top];
    }
    return null;
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at callerSlots time
   *  - you obtained the value you set by callerSlots previous getXAttr()
   *  - you constructed callerSlots multi value list with ObjectList.createList()
   */
  public void setOperandAttr (Object a){
    assert (top >= stackBase);
    if (attrs == null) {
      if (a == null) return;
      attrs = new Object[slots.length];
    }
    attrs[top] = a;
  }

  
  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getOperandAttr (Class<T> attrType){
    assert (top >= stackBase);
    
    if ((attrs != null)){
      return ObjectList.getFirst(attrs[top], attrType);
    }
    return null;
  }
  public <T> T getNextOperandAttr (Class<T> attrType, Object prev){
    assert (top >= stackBase);
    if (attrs != null){
      return ObjectList.getNext( attrs[top], attrType, prev);
    }
    return null;
  }
  public Iterator operandAttrIterator(){
    assert (top >= stackBase);
    Object a = (attrs != null) ? attrs[top] : null;
    return ObjectList.iterator(a);
  }
  public <T> Iterator<T> operandAttrIterator(Class<T> attrType){
    assert (top >= stackBase);
    Object a = (attrs != null) ? attrs[top] : null;
    return ObjectList.typedIterator(a, attrType);
  }
  

  public void addOperandAttr (Object a){
    assert (top >= stackBase);
    if (a != null){
      if (attrs == null) {
        attrs = new Object[slots.length];
      }

      attrs[top] = ObjectList.add(attrs[top], a);
    }        
  }
  
  public void removeOperandAttr (Object a){
    assert (top >= stackBase) && (a != null);
    if (attrs != null){
      attrs[top] = ObjectList.remove(attrs[top], a);
    }        
  }
  
  public void replaceOperandAttr (Object oldAttr, Object newAttr){
    assert (top >= stackBase) && (oldAttr != null) && (newAttr != null);
    if (attrs != null){
      attrs[top] = ObjectList.replace(attrs[top], oldAttr, newAttr);
    }        
  }
  
  
  //--- offset operand attrs

  public boolean hasOperandAttr(int offset){
    int i = top-offset;
    assert (i >= stackBase);
    if (attrs != null){
      return (attrs[i] != null);
    }
    return false;
  }
  public boolean hasOperandAttr(int offset, Class<?> type){
    int i = top-offset;
    assert (i >= stackBase);
    if (attrs != null){
      return ObjectList.containsType(attrs[i], type);
    }
    return false;
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at callerSlots time, or check/process result with ObjectList
   */
  public Object getOperandAttr (int offset) {
    int i = top-offset;
    assert (i >= stackBase);
    
    if (attrs != null) {
      return attrs[i];
    }
    return null;
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at callerSlots time
   *  - you obtained the value you set by callerSlots previous getXAttr()
   *  - you constructed callerSlots multi value list with ObjectList.createList()
   */  
  public void setOperandAttr (int offset, Object a){
    int i = top-offset;
    assert (i >= stackBase);

    if (attrs == null) {
      if (a == null) return;
      attrs = new Object[slots.length];
    }
    attrs[i] = a;
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getOperandAttr (int offset, Class<T> attrType){
    int i = top-offset;
    assert (i >= stackBase) : this;
    if (attrs != null){
      return ObjectList.getFirst(attrs[i], attrType);
    }
    return null;
  }
  public <T> T getNextOperandAttr (int offset, Class<T> attrType, Object prev){
    int i = top-offset;
    assert (i >= stackBase);
    if (attrs != null){
      return ObjectList.getNext( attrs[i], attrType, prev);
    }
    return null;
  }
  public ObjectList.Iterator operandAttrIterator(int offset){
    int i = top-offset;
    assert (i >= stackBase);
    Object a = (attrs != null) ? attrs[i] : null;
    return ObjectList.iterator(a);
  }
  public <T> ObjectList.TypedIterator<T> operandAttrIterator(int offset, Class<T> attrType){
    int i = top-offset;
    assert (i >= stackBase);
    Object a = (attrs != null) ? attrs[i] : null;
    return ObjectList.typedIterator(a, attrType);
  }


  public void addOperandAttr (int offset, Object a){
    int i = top-offset;
    assert (i >= stackBase);

    if (a != null){
      if (attrs == null) {
        attrs = new Object[slots.length];
      }
      attrs[i] = ObjectList.add(attrs[i],a);
    }    
  }

  public void removeOperandAttr (int offset, Object a){
    int i = top-offset;
    assert (i >= stackBase) && (a != null);
    if (attrs != null){
      attrs[i] = ObjectList.remove(attrs[i], a);
    }        
  }
  
  public void replaceOperandAttr (int offset, Object oldAttr, Object newAttr){
    int i = top-offset;
    assert (i >= stackBase) && (oldAttr != null) && (newAttr != null);
    if (attrs != null){
      attrs[i] = ObjectList.replace(attrs[i], oldAttr, newAttr);
    }        
  }
  
  
  //--- top double-slot operand attrs
  // we store attributes for double slot values at the local var index,
  // which is the lower one. The ..LongOperand.. APIs are handling this offset
 
  public boolean hasLongOperandAttr(){
    return hasOperandAttr(1);
  }
  public boolean hasLongOperandAttr(Class<?> type){
    return hasOperandAttr(1, type);
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at callerSlots time, or check/process result with ObjectList
   */
  public Object getLongOperandAttr () {
    return getOperandAttr(1);
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at callerSlots time
   *  - you obtained the value you set by callerSlots previous getXAttr()
   *  - you constructed callerSlots multi value list with ObjectList.createList()
   */  
  public void setLongOperandAttr (Object a){
    setOperandAttr(1, a);
  }
  
  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getLongOperandAttr (Class<T> attrType) {
    return getOperandAttr(1, attrType);
  }
  public <T> T getNextLongOperandAttr (Class<T> attrType, Object prev) {
    return getNextOperandAttr(1, attrType, prev);
  }
  public ObjectList.Iterator longOperandAttrIterator(){
    return operandAttrIterator(1);
  }
  public <T> ObjectList.TypedIterator<T> longOperandAttrIterator(Class<T> attrType){
    return operandAttrIterator(1, attrType);
  }
    
  public void addLongOperandAttr (Object a){
    addOperandAttr(1, a);
  }

  public void removeLongOperandAttr (Object a){
    removeOperandAttr(1, a);
  }

  public void replaceLongOperandAttr (Object oldAttr, Object newAttr){
    replaceOperandAttr(1, oldAttr, newAttr);
  }


  //--- local attrs
  // single- or double-slot - you have to provide the var index anyways)
  
  public boolean hasLocalAttr(int index){
    assert index < stackBase;
    if (attrs != null){
      return (attrs[index] != null);
    }
    return false;
  }
  public boolean hasLocalAttr(int index, Class<?> type){
    assert index < stackBase;
    if (attrs != null){
      return ObjectList.containsType(attrs[index], type);
    }
    return false;
  }

  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at callerSlots time, or check/process result with ObjectList
   */
  public Object getLocalAttr (int index){
    assert index < stackBase;
    if (attrs != null){
      return attrs[index];
    }
    return null;
  }
   
  public Object getLongLocalAttr (int index){
    return getLocalAttr( index);
  }
  
  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at callerSlots time
   *  - you obtained the value you set by callerSlots previous getXAttr()
   *  - you constructed callerSlots multi value list with ObjectList.createList()
   */  
  public void setLocalAttr (int index, Object a) {
    assert index < stackBase;
    if (attrs == null){
      if (a == null) return;
      attrs = new Object[slots.length];
    }
    attrs[index] = a;
  }

  public void setLongLocalAttr (int index, Object a){
    setLocalAttr( index, a);
  }
  
  public void addLongLocalAttr (int index, Object a){
    addLocalAttr( index, a);
  }
  
  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getLocalAttr (int index, Class<T> attrType){
    assert index < stackBase;
    if (attrs != null){
      return ObjectList.getFirst( attrs[index], attrType);
    }
    return null;
  }
  public <T> T getNextLocalAttr (int index, Class<T> attrType, Object prev){
    assert index < stackBase;
    if (attrs != null){
      return ObjectList.getNext( attrs[index], attrType, prev);
    }
    return null;
  }
  public ObjectList.Iterator localAttrIterator(int index){
    assert index < stackBase;
    Object a = (attrs != null) ? attrs[index] : null;
    return ObjectList.iterator(a);
  }
  public <T> ObjectList.TypedIterator<T> localAttrIterator(int index, Class<T> attrType){
    assert index < stackBase;
    Object a = (attrs != null) ? attrs[index] : null;
    return ObjectList.typedIterator(a, attrType);
  }
  

  public void addLocalAttr (int index, Object attr){
    assert index < stackBase;
    if (attrs == null){
      if (attr == null) return;
      attrs = new Object[slots.length];
    }
    attrs[index] = ObjectList.add(attrs[index], attr);
  }
  
  public void removeLocalAttr (int index, Object attr){
    assert index < stackBase && attr != null;
    if (attr != null){
      attrs[index] = ObjectList.remove(attrs[index], attr);    
    }
  }

  public void replaceLocalAttr (int index, Object oldAttr, Object newAttr){
    assert index < stackBase && oldAttr != null && newAttr != null;
    if (attrs != null){
      attrs[index] = ObjectList.replace(attrs[index], oldAttr, newAttr);    
    }
  }
  
  //--- various special attr accessors

  /**
   * helper to quickly find out if any of the locals slots holds
   * an attribute of the provided type
   * 
   * @param attrType type of attribute to look for
   * @param startIdx local index to start from
   * @return index of local slot with attribute, -1 if none found
   */
  public int getLocalAttrIndex (Class<?> attrType, int startIdx){
    if (attrs != null){
      for (int i=startIdx; i<stackBase; i++){
        Object a = attrs[i];
        if (ObjectList.containsType(a, attrType)){
          return i;
        }
      }
    }

    return -1;
  }
  
  // <2do> this is machine dependent since it uses the operand stack. Only here because there
  // is no suitable place to factor this out between xStackFrame, xNativeStackFrame and xDirectCallStackFrame
  // (another example of missing multiple inheritance)
  // Needs to be overridden for Dalvik
  
  /**
   * this retrieves the argument values from the caller, i.e. the previous stackframe 
   * 
   * references are returned as ElementInfos or null
   * primitive values are returned as box objects (e.g. int -> Integer)
   */
  public Object[] getArgumentValues (ThreadInfo ti){
    StackFrame callerFrame = getCallerFrame();
    if (callerFrame != null){
      return callerFrame.getCallArguments(ti);
    } else {
      // <2do> what about main(String[] args) ?
    }
    
    return null;
  }
  
  /**
   * get the arguments of the executed call
   * Note - this throws an exception if the StackFrame pc is not an InvokeInstruction
   */
  public Object[] getCallArguments (ThreadInfo ti){
    if (pc == null || !(pc instanceof InvokeInstruction)){
      throw new JPFException("stackframe not executing invoke: " + pc);
    }
    
    InvokeInstruction call = (InvokeInstruction) pc;    
    MethodInfo callee = call.getInvokedMethod();

    byte[] argTypes = callee.getArgumentTypes();

    return getArgumentsValues(ti, argTypes);
  }

  public Object[] getArgumentsValues (ThreadInfo ti, byte[] argTypes){
    int n = argTypes.length;
    Object[] args = new Object[n];

    for (int i=n-1, off=0; i>=0; i--) {
      switch (argTypes[i]) {
      case Types.T_ARRAY:
      //case Types.T_OBJECT:
      case Types.T_REFERENCE:
        int ref = peek(off);
        if (ref != MJIEnv.NULL) {
          args[i] = ti.getElementInfo(ref);
        } else {
          args[i] = null;
        }
        off++;
        break;

      case Types.T_LONG:
        args[i] = new Long(peekLong(off));
        off+=2;
        break;
      case Types.T_DOUBLE:
        args[i] = new Double(Types.longToDouble(peekLong(off)));
        off+=2;
        break;

      case Types.T_BOOLEAN:
        args[i] = new Boolean(peek(off) != 0);
        off++;
        break;
      case Types.T_BYTE:
        args[i] = new Byte((byte)peek(off));
        off++;
        break;
      case Types.T_CHAR:
        args[i] = new Character((char)peek(off));
        off++;
        break;
      case Types.T_SHORT:
        args[i] = new Short((short)peek(off));
        off++;
        break;
      case Types.T_INT:
        args[i] = new Integer(peek(off));
        off++;
        break;
      case Types.T_FLOAT:
        args[i] = new Float(Types.intToFloat(peek(off)));
        off++;
        break;
      default:
        // error, unknown argument type
      }
    }
    return args;
  }
  
  /**
   * return an array of all argument attrs, which in turn can be lists. If
   * you have to retrieve values, use the ObjectList APIs
   * 
   * this is here (and not in ThreadInfo) because we might call it
   * on callerSlots cached/cloned StackFrame (caller stack might be already
   * modified, e.g. for callerSlots native method).
   * to be used from listeners.
   */
  public Object[] getArgumentAttrs (MethodInfo miCallee) {
    if (attrs != null) {
      int nArgs = miCallee.getNumberOfArguments();
      byte[] at = miCallee.getArgumentTypes();
      Object[] a;

      if (!miCallee.isStatic()) {
        a = new Object[nArgs+1];
        a[0] = getOperandAttr(miCallee.getArgumentsSize()-1);
      } else {
        a = new Object[nArgs];
      }

      for (int i=nArgs-1, off=0, j=a.length-1; i>=0; i--, j--) {
        byte argType = at[i];
        if (argType == Types.T_LONG || argType == Types.T_DOUBLE) {
          a[j] = getOperandAttr(off+1);
          off +=2;
        } else {
          a[j] = getOperandAttr(off);
          off++;
        }
      }

      return a;

    } else {
      return null;
    }
  }

  /**
   * check if there is any argument attr of the provided type on the operand stack
   * this is far more efficient than retrieving attribute values (we don't
   * care for argument types)
   */
  public boolean hasArgumentAttr (MethodInfo miCallee, Class<?> attrType){
    if (attrs != null) {
      int nArgSlots = miCallee.getArgumentsSize();

      for (int i=0; i<nArgSlots; i++){
        Object a = getOperandAttr(i);
        if (ObjectList.containsType(a, attrType)){
          return true;
        }
      }
    }

    return false;
  }

  public boolean hasArgumentObjectAttr (ThreadInfo ti, MethodInfo miCallee, Class<?> type){
    int nArgSlots = miCallee.getArgumentsSize();
    for (int i=0; i<nArgSlots; i++){
      if (isOperandRef(i)){
        int objRef = peek(i);
        if (objRef != MJIEnv.NULL){
          ElementInfo ei = ti.getElementInfo(objRef);
          if (ei.getObjectAttr(type) != null) {
            return true;
          }
        }
      }
    }

    return false;
  }
  
  
  // -- end attrs --
  
  public void setLocalReferenceVariable (int index, int ref){
    if (slots[index] != MJIEnv.NULL){
      VM.getVM().getSystemState().activateGC();
    }
    
    slots[index] = ref;
    isRef.set(index);
  }

  public void setLocalVariable (int index, int v){
    // Hmm, should we treat this an error?
    if (isRef.get(index) && slots[index] != MJIEnv.NULL){
      VM.getVM().getSystemState().activateGC();      
    }
    
    slots[index] = v;
    isRef.clear(index);
  }
  
  public void setFloatLocalVariable (int index, float f){
    setLocalVariable( index, Float.floatToIntBits(f));
  }

  public void setDoubleLocalVariable (int index, double f){
    setLongLocalVariable( index, Double.doubleToLongBits(f));
  }

  
  // <2do> replace with non-ref version
  public void setLocalVariable (int index, int v, boolean ref) {
    // <2do> activateGc should be replaced by local refChanged
    boolean activateGc = ref || (isRef.get(index) && (slots[index] != MJIEnv.NULL));

    slots[index] = v;
    isRef.set(index,ref);

    if (activateGc) {
        VM.getVM().getSystemState().activateGC();
    }
  }

  public int getLocalVariable (int i) {
    return slots[i];
  }

  public int getLocalVariable (String name) {
    int idx = getLocalVariableSlotIndex(name);
    if (idx >= 0) {
      return getLocalVariable(idx);
    } else {
      throw new JPFException("local variable not found: " + name);
    }
  }

  public int getLocalVariableCount() {
    return stackBase;
  }

  /**
   * <2do> - this should return only LocalVarInfo for the current pc
   */
  public LocalVarInfo[] getLocalVars () {
    return mi.getLocalVars();
  }


  public boolean isLocalVariableRef (int idx) {
    return isRef.get(idx);
  }

  public String getLocalVariableType (String name) {
    LocalVarInfo lv = mi.getLocalVar(name, pc.getPosition()+pc.getLength());
    if (lv != null){
      return lv.getType();
    }

    return null;
  }

  public String getLocalVariableType (int idx){
    LocalVarInfo lv = mi.getLocalVar(idx, pc.getPosition()+pc.getLength());
    if (lv != null){
      return lv.getType();
    }

    return null;
  }

  public LocalVarInfo getLocalVarInfo (String name){
    return mi.getLocalVar(name, pc.getPosition()+pc.getLength());
  }

  public LocalVarInfo getLocalVarInfo (int idx){
    return mi.getLocalVar(idx, pc.getPosition()+pc.getLength());
  }

  public void setThis (int objRef){
    thisRef = objRef;
  }
  
  public FixedBitSet getReferenceMap(){
    return isRef;
  }

  //--- direct slot access - provided for machine-independent clients
  
  public int[] getSlots () {
    return slots; // we should probably clone
  }
  public Object[] getSlotAttrs(){
    return attrs;
  }
  public Object getSlotAttr (int i){
    if (attrs != null){
      return attrs[i];
    }
    return null;
  }
  public <T> T getSlotAttr (int i, Class<T> attrType){
    if (attrs != null){
      return ObjectList.getFirst( attrs[i], attrType);
    }
    return null;
  }  
  public void setSlotAttr (int i, Object a){
    if (attrs == null){
      attrs = new Object[slots.length];
    }
    attrs[i] = a;
  }
  public void addSlotAttr (int i, Object a){
    if (a != null){
      if (attrs == null) {
        attrs = new Object[slots.length];
      }

      attrs[i] = ObjectList.add(attrs[i], a);
    }        
  }  
  public void replaceSlotAttr (int i, Object oldAttr, Object newAttr){
    if (attrs != null){
      attrs[i] = ObjectList.replace(attrs[i], oldAttr, newAttr);
    }        
  }
  
  
  
  public void visitReferenceSlots (ReferenceProcessor visitor){
    for (int i=isRef.nextSetBit(0); i>=0 && i<=top; i=isRef.nextSetBit(i+1)){
      visitor.processReference(slots[i]);
    }
  }

  public void setLongLocalVariable (int index, long v) {
    // WATCH OUT: apparently, slots can change type, so we have to
    // reset the reference flag (happened in JavaSeq)

    slots[index] = Types.hiLong(v);
    isRef.clear(index);

    index++;
    slots[index] = Types.loLong(v);
    isRef.clear(index);
  }

  public long getLongLocalVariable (int idx) {
    return Types.intsToLong(slots[idx + 1], slots[idx]);
  }
  
  public double getDoubleLocalVariable (int idx) {
    return Types.intsToDouble(slots[idx + 1], slots[idx]);
  }

  public float getFloatLocalVariable (int idx) {
    int bits = slots[idx];
    return Float.intBitsToFloat(bits);
  }

  public double getDoubleLocalVariable (String name) {
    int idx = getLocalVariableSlotIndex(name);
    if (idx >= 0) {
      return getDoubleLocalVariable(idx);
    } else {
      throw new JPFException("long local variable not found: " + name);
    }    
  }
  
  public long getLongLocalVariable (String name) {
    int idx = getLocalVariableSlotIndex(name);

    if (idx >= 0) {
      return getLongLocalVariable(idx);
    } else {
      throw new JPFException("long local variable not found: " + name);
    }
  }

  public MethodInfo getMethodInfo () {
    return mi;
  }

  public String getMethodName () {
    return mi.getName();
  }

  public boolean isOperandRef (int offset) {
    return isRef.get(top-offset);
  }

  public boolean isOperandRef () {
    return isRef.get(top);
  }

  //--- direct pc modification
  // NOTE: this is dangerous, caller has to guarantee stack consistency
  public void setPC (Instruction newpc) {
    pc = newpc;
  }

  public Instruction getPC () {
    return pc;
  }

  public void advancePC() {
    int i = pc.getInstructionIndex() + 1;
    if (i < mi.getNumberOfInstructions()) {
      pc = mi.getInstruction(i);
    } else {
      pc = null;
    }
  }

  public int getTopPos() {
    return top;
  }

  ExceptionHandler getHandlerFor (ClassInfo ciException){
    return mi.getHandlerFor (ciException, pc);
  }
  
  public boolean isFirewall (){
    return mi.isFirewall();
  }
  
  public String getStackTraceInfo () {
    StringBuilder sb = new StringBuilder(128);

    if(!mi.isJPFInternal()) {
    	sb.append(mi.getStackTraceName());
    	
    	if(pc != null) {
    		sb.append('(');
            sb.append( pc.getFilePos());
            sb.append(')');
    	}
    } else {
    	sb.append(mi.getName());
    	
    	if(mi.isMJI()) {
    		sb.append("(Native)");
    	} else {
    		sb.append("(Synthetic)");
    	}
    }

    return sb.toString();
  }

  /**
   * if this is an instance method, return the reference of the corresponding object
   * (note this only has to be in slot 0 upon entry)
   */
  public int getThis () {
    return thisRef;
  }

  // stack operations
  public void clearOperandStack () {
    if (attrs != null){
      for (int i=stackBase; i<= top; i++){
        attrs[i] = null;
      }
    }
    
    top = stackBase-1;
  }
  
  // this is callerSlots deep copy
  @Override
  public StackFrame clone () {
    try {
      StackFrame sf = (StackFrame) super.clone();

      sf.defreeze();
      
      sf.slots = slots.clone();
      sf.isRef = isRef.clone();

      if (attrs != null){
        sf.attrs = attrs.clone();
      }

      // frameAttr is not cloned to allow search global use 

      return sf;
    } catch (CloneNotSupportedException cnsx) {
      throw new JPFException(cnsx);
    }
  }
  
  //--- change management
  
  protected void checkIsModifiable() {
    if ((attributes & ATTR_IS_FROZEN) != 0) {
      throw new JPFException("attempt to modify frozen stackframe: " + this);
    }
  }
  
  public void freeze() {
    attributes |= ATTR_IS_FROZEN;
  }

  public void defreeze() {
    attributes &= ~ATTR_IS_FROZEN;
  }
  
  public boolean isFrozen() {
    return ((attributes & ATTR_IS_FROZEN) != 0);    
  }
  
  
  public void setReflection(){
    attributes |= ATTR_IS_REFLECTION;
  }
  
  public boolean isReflection(){
    return ((attributes & ATTR_IS_REFLECTION) != 0);
  }

  // all the dupses don't have any GC side effect (everything is already
  // on the stack), so skip the GC requests associated with push()/pop()

  public void dup () {
    // .. A     =>
    // .. A A
    //    ^

    int t= top;

    int td=t+1;
    slots[td] = slots[t];
    isRef.set(td, isRef.get(t));

    if (attrs != null){
      attrs[td] = attrs[t];
    }

    top = td;
  }

  public void dup2 () {
    // .. A B        =>
    // .. A B A B
    //      ^

    int ts, td;
    int t=top;

    // duplicate A
    td = t+1; ts = t-1;
    slots[td] = slots[ts];
    isRef.set(td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // duplicate B
    td++; ts=t;
    slots[td] = slots[ts];
    isRef.set(td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    top = td;
  }

  public void dup2_x1 () {
    // .. A B C       =>
    // .. B C A B C
    //        ^

    int b, c;
    boolean bRef, cRef;
    Object bAnn = null, cAnn = null;
    int ts, td;
    int t = top;

    // duplicate C
    ts=t; td = t+2;                              // ts=top, td=top+2
    slots[td] = c = slots[ts];
    cRef = isRef.get(ts);
    isRef.set(td,cRef);
    if (attrs != null){
      attrs[td] = cAnn = attrs[ts];
    }

    // duplicate B
    ts--; td--;                                  // ts=top-1, td=top+1
    slots[td] = b = slots[ts];
    bRef = isRef.get(ts);
    isRef.set(td, bRef);
    if (attrs != null){
      attrs[td] = bAnn = attrs[ts];
    }

    // shuffle A
    ts=t-2; td=t;                                // ts=top-2, td=top
    slots[td] = slots[ts];
    isRef.set(td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // shuffle B
    td = ts;                                     // td=top-2
    slots[td] = b;
    isRef.set(td, bRef);
    if (attrs != null){
      attrs[td] = bAnn;
    }

    // shuffle C
    td++;                                        // td=top-1
    slots[td] = c;
    isRef.set(td, cRef);
    if (attrs != null){
      attrs[td] = cAnn;
    }

    top += 2;
  }

  public void dup2_x2 () {
    // .. A B C D       =>
    // .. C D A B C D
    //          ^

    int c, d;
    boolean cRef, dRef;
    Object cAnn = null, dAnn = null;
    int ts, td;
    int t = top;

    // duplicate C
    ts = t-1; td = t+1;                          // ts=top-1, td=top+1
    slots[td] = c = slots[ts];
    cRef = isRef.get(ts);
    isRef.set(td, cRef);
    if (attrs != null){
      attrs[td] = cAnn = attrs[ts];
    }

    // duplicate D
    ts=t; td++;                                  // ts=top, td=top+2
    slots[td] = d = slots[ts];
    dRef = isRef.get(ts);
    isRef.set(td, dRef);
    if (attrs != null){
      attrs[td] = dAnn = attrs[ts];
    }

    // shuffle A
    ts = t-3; td = t-1;                          // ts=top-3, td=top-1
    slots[td] = slots[ts];
    isRef.set( td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // shuffle B
    ts++; td = t;                                // ts = top-2
    slots[td] = slots[ts];
    isRef.set( td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // shuffle D
    td = ts;                                     // td = top-2
    slots[td] = d;
    isRef.set( td, dRef);
    if (attrs != null){
      attrs[td] = dAnn;
    }

    // shuffle C
    td--;                                        // td = top-3
    slots[td] = c;
    isRef.set(td, cRef);
    if (attrs != null){
      attrs[td] = cAnn;
    }

    top += 2;
  }

  public void dup_x1 () {
    // .. A B     =>
    // .. B A B
    //      ^

    int b;
    boolean bRef;
    Object bAnn = null;
    int ts, td;
    int t = top;

    // duplicate B
    ts = t; td = t+1;
    slots[td] = b = slots[ts];
    bRef = isRef.get(ts);
    isRef.set(td, bRef);
    if (attrs != null){
      attrs[td] = bAnn = attrs[ts];
    }

    // shuffle A
    ts--; td = t;       // ts=top-1, td = top
    slots[td] = slots[ts];
    isRef.set( td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // shuffle B
    td = ts;            // td=top-1
    slots[td] = b;
    isRef.set( td, bRef);
    if (attrs != null){
      attrs[td] = bAnn;
    }

    top++;
  }

  public void dup_x2 () {
    // .. A B C     =>
    // .. C A B C
    //        ^

    int c;
    boolean cRef;
    Object cAnn = null;
    int ts, td;
    int t = top;

    // duplicate C
    ts = t; td = t+1;
    slots[td] = c = slots[ts];
    cRef = isRef.get(ts);
    isRef.set( td, cRef);
    if (attrs != null){
      attrs[td] = cAnn = attrs[ts];
    }

    // shuffle B
    td = ts; ts--;               // td=top, ts=top-1
    slots[td] = slots[ts];
    isRef.set( td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // shuffle A
    td=ts; ts--;                 // td=top-1, ts=top-2
    slots[td] = slots[ts];
    isRef.set( td, isRef.get(ts));
    if (attrs != null){
      attrs[td] = attrs[ts];
    }

    // shuffle C
    td = ts;                     // td = top-2
    slots[td] = c;
    isRef.set(td, cRef);
    if (attrs != null){
      attrs[td] = cAnn;
    }

    top++;
  }

  /**
   * to be used to check if a StackFrame got cloned due to its execution
   * changing attributes and/or slots, but otherwise represents the same
   * execution
   */
  public boolean originatesFrom (StackFrame other){
    if (other == this){
      return true;
    } else {
      return ((mi == other.mi) &&
              (prev == other.prev) &&
              (top == other.top) &&
              (getClass() == other.getClass()));
    }
  }
  
  
  // <2do> pcm - I assume this compares snapshots, not types. Otherwise it
  // would be pointless to equals stack/local values
  @Override
  public boolean equals (Object o) {
    if (o instanceof StackFrame){
      StackFrame other = (StackFrame)o;

      if (prev != other.prev) {
        return false;
      }
      if (pc != other.pc) {
        return false;
      }
      if (mi != other.mi) {
        return false;
      }
      if (top != other.top){
        return false;
      }

      int[] otherSlots = other.slots;
      FixedBitSet otherIsRef = other.isRef;
      for (int i=0; i<=top; i++){
        if ( slots[i] != otherSlots[i]){
          return false;
        }
        if ( isRef.get(i) != otherIsRef.get(i)){
          return false;
        }
      }

      if (!Misc.compare(top,attrs,other.attrs)){
        return false;
      }
      
      if (!ObjectList.equals(frameAttr, other.frameAttr)){
        return false;
      }

      return true;
    }

    return false;
  }
  
  public boolean hasAnyRef () {
    return isRef.cardinality() > 0;
  }
  
  public int mixinExecutionStateHash (int h) {
    h = OATHash.hashMixin( h, mi.getGlobalId());
    
    if (pc != null){
      h = OATHash.hashMixin(h, pc.getInstructionIndex());
      // we don't need the bytecode since there can only be one insn with this index in this method
    }
    
    for (int i=0; i<top; i++) {
      h = OATHash.hashMixin(h, slots[i]);
    }
   
    return h;
  }

  protected void hash (HashData hd) {
    if (prev != null){
      hd.add(prev.objectHashCode());
    }
    hd.add(mi.getGlobalId());

    if (pc != null){
      hd.add(pc.getInstructionIndex());
    }

    for (int i=0; i<=top; i++){
      hd.add(slots[i]);
    }

    isRef.hash(hd);

    // it's debatable if we add the attributes to the state, but whatever it
    // is, it should be kept consistent with the Fields.hash()
    if (attrs != null){
      for (int i=0; i<=top; i++){
        ObjectList.hash( attrs[i], hd);
      }
    }
    
    if (frameAttr != null){
      ObjectList.hash(frameAttr, hd);
    }
  }

  // computes an hash code for the hash table
  // the default hash code is different for each object
  // we need to redifine it to make the hash table work
  @Override
  public int hashCode () {
    HashData hd = new HashData();
    hash(hd);
    return hd.getValue();
  }

  /**
   * mark all objects reachable from local or operand stack positions containing
   * references. Done during phase1 marking of threads (the stack is one of the
   * Thread gc roots)
   */
  public void markThreadRoots (Heap heap, int tid) {

    /**
    for (int i = isRef.nextSetBit(0); i>=0 && i<=top; i = isRef.nextSetBit(i + 1)) {
      int objref = slots[i];
      if (objref != MJIEnv.NULL) {
        heap.markThreadRoot(objref, tid);
      }
    }
    **/
    for (int i = 0; i <= top; i++) {
      if (isRef.get(i)) {
        int objref = slots[i];
        if (objref != MJIEnv.NULL) {
          heap.markThreadRoot(objref, tid);
        }
      }
    }
  }

  //--- debugging methods

  public void printOperands (PrintStream pw){
    pw.print("operands = [");
    for (int i=stackBase; i<=top; i++){
      if (i>0){
        pw.print(',');
      }
      if (isOperandRef(i)){
        pw.print('^');
      }
      pw.print(slots[i]);
      Object a = getOperandAttr(top-i);
      if (a != null){
        pw.print(" {");
        pw.print(a);
        pw.print('}');
      }
    }
    pw.println(']');
  }

  /**
   * this includes locals and pc
   */
  public void printStackContent () {
    PrintStream pw = System.out;

    pw.print( "\tat ");
    pw.print( mi.getFullName());

    if (pc != null) {
      pw.println( ":" + pc.getPosition());
    } else {
      pw.println();
    }

    pw.print("\t slots: ");
    for (int i=0; i<=top; i++){
      if (i == stackBase){
        pw.println("\t      ----------- operand stack");
      }

      pw.print( "\t    [");
      pw.print(i);
      pw.print("] ");
      if (isRef.get(i)) {
        pw.print( "@");
      }
      pw.print( slots[i]);

      if (attrs != null){
        pw.print("  attr=");
        pw.print(attrs[i]);
      }

      pw.println();
    }
  }

  public void printStackTrace () {
    System.out.println( getStackTraceInfo());
  }

  public void swap () {
    int t = top-1;

    int v = slots[top];
    boolean isTopRef = isRef.get(top);

    slots[top] = slots[t];
    isRef.set( top, isRef.get(t));

    slots[t] = v;
    isRef.set( t, isTopRef);

    if (attrs != null){
      Object a = attrs[top];
      attrs[top] = attrs[t];
      attrs[t] = a;
    }
  }

  protected void printContentsOn(PrintWriter pw){
    pw.print("isFrozen=");
    pw.print(isFrozen());
    pw.print(",mi=");
    pw.print( mi != null ? mi.getUniqueName() : "null");
    pw.print(",top="); pw.print(top);
    pw.print(",slots=[");

    for (int i = 0; i <= top; i++) {
      if (i == stackBase){
        pw.print("||");
      } else {
        if (i != 0) {
          pw.print(',');
        }
      }

      if (isRef.get(i)){
        pw.print('@');
      }
      pw.print(slots[i]);

      if (attrs != null && attrs[i] != null) {
        pw.print('(');
        pw.print(attrs[i]);
        pw.print(')');
      }
    }

    pw.print("],pc=");
    pw.print(pc != null ? pc.getPosition() : "null");

    pw.print(']');

  }
  
  // <2do> there are way too many different print/debug methods here
  public void printSlots (PrintStream ps){
    for (int i = 0; i <= top; i++) {
      if (i == stackBase){
        ps.print("||");
      } else {
        if (i != 0) {
          ps.print(',');
        }
      }

      if (isRef.get(i)){
        PrintUtils.printReference(ps, slots[i]);
      } else {
        ps.print(slots[i]);
      }
    }    
  }

  public int getDepth(){
    int depth = 0;
    
    for (StackFrame frame = prev; frame != null; frame = frame.prev){
      depth++;
    }
    
    return depth;
  }
  
  protected int objectHashCode() {
    return super.hashCode();
  }

  @Override
  public String toString () {
    StringWriter sw = new StringWriter(128);
    PrintWriter pw = new PrintWriter(sw);

    pw.print(getClass().getSimpleName() + '{');
    //pw.print(Integer.toHexString(objectHashCode()));
    printContentsOn(pw);
    pw.print('}');

    return sw.toString();
  }

  public float peekFloat() {
    return Float.intBitsToFloat(slots[top]);
  }

  public float peekFloat (int offset){
    return Float.intBitsToFloat(slots[top-offset]);    
  }
  
  public double peekDouble() {
    int i = top;
    return Types.intsToDouble( slots[i], slots[i-1]);
  }
  
  public double peekDouble (int offset){
    int i = top-offset;
    return Types.intsToDouble( slots[i], slots[i-1]);
  }
  
  public long peekLong () {
    int i = top;
    return Types.intsToLong( slots[i], slots[i-1]);
  }

  public long peekLong (int offset) {
    int i = top - offset;
    return Types.intsToLong( slots[i], slots[i-1]);
  }

  public void pushLong (long v) {
    push( (int) (v>>32));
    push( (int) v);
  }

  public void pushDouble (double v) {
    long l = Double.doubleToLongBits(v);
    push( (int) (l>>32));
    push( (int) l);
  }

  public void pushFloat (float v) {
    push( Float.floatToIntBits(v));
  }
  
  public double popDouble () {
    int i = top;

    int lo = slots[i--];
    int hi = slots[i--];

    if (attrs != null){
      i = top;
      attrs[i--] = null; // not really required
      attrs[i--] = null; // that's where the attribute should be
    }

    top = i;
    return Types.intsToDouble(lo, hi);
  }

  public long popLong () {
    int i = top;

    int lo = slots[i--];
    int hi = slots[i--];

    if (attrs != null){
      i = top;
      attrs[i--] = null; // not really required
      attrs[i--] = null; // that's where the attribute should be
    }

    top = i;
    return Types.intsToLong(lo, hi);
  }

  public int peek () {
    return slots[top];
  }

  public int peek (int offset) {
    return slots[top-offset];
  }

  public void removeArguments (MethodInfo mi) {
    int i = mi.getArgumentsSize();

    if (i != 0) {
      pop(i);
    }
  }
  
  public void pop (int n) {
    //assert (top >= stackBase) : "stack empty";

    int t = top - n;

    // <2do> get rid of this !
    for (int i=top; i>t; i--) {
      if (isRef.get(i) && (slots[i] != MJIEnv.NULL)) {
        VM.getVM().getSystemState().activateGC();
        break;
      }
    }

    if (attrs != null){  // just to avoid memory leaks
      for (int i=top; i>t; i--){
        attrs[i] = null;
      }
    }

    top = t;
  }

  public float popFloat() {    
    int v = slots[top];

    if (attrs != null){ // just to avoid memory leaks
      attrs[top] = null;
    }

    top--;

    return Float.intBitsToFloat(v);
  }
  
  public int pop () {
    //assert (top >= stackBase) : "stack empty";
    
    int v = slots[top];

    // <2do> get rid of this
    if (isRef.get(top)) {
      if (v != MJIEnv.NULL) {
        VM.getVM().getSystemState().activateGC();
      }
    }

    if (attrs != null){ // just to avoid memory leaks
      attrs[top] = null;
    }

    top--;

    // note that we don't reset the operands or oRefs values, so that
    // we can still access them after the insn doing the pop got executed
    // (e.g. useful for listeners)

    return v;
  }
  
  public void pushLocal (int index) {
    top++;
    slots[top] = slots[index];
    isRef.set(top, isRef.get(index));

    if (attrs != null){
      attrs[top] = attrs[index];
    }
  }

  public void pushLongLocal (int index){
    int t = top;

    slots[++t] = slots[index];
    isRef.clear(t);
    slots[++t] = slots[index+1];
    isRef.clear(t);

    if (attrs != null){
      attrs[t-1] = attrs[index];
      attrs[t] = null;
    }

    top = t;
  }

  public void storeOperand (int index){
    slots[index] = slots[top];
    isRef.set( index, isRef.get(top));

    if (attrs != null){
      attrs[index] = attrs[top];
      attrs[top] = null;
    }

    top--;
  }

  public void storeLongOperand (int index){
    int t = top-1;
    int i = index;

    slots[i] = slots[t];
    isRef.clear(i);

    slots[++i] = slots[t+1];
    isRef.clear(i);

    if (attrs != null){
      attrs[index] = attrs[t]; // its in the lower word
      attrs[i] = null;

      attrs[t] = null;
      attrs[t+1] = null;
    }

    top -=2;
  }

  public void push (int v){
    top++;
    slots[top] = v;
    isRef.clear(top);

    //if (attrs != null){ // done on pop
    //  attrs[top] = null;
    //}
  }

  public void pushRef (int ref){
    top++;
    slots[top] = ref;
    isRef.set(top);

    //if (attrs != null){ // done on pop
    //  attrs[top] = null;
    //}

    if (ref != MJIEnv.NULL) {
      VM.getVM().getSystemState().activateGC();
    }
  }

  public void push (int v, boolean ref) {
    top++;
    slots[top] = v;
    isRef.set(top, ref);

    //if (attrs != null){ // done on pop
    //  attrs[top] = null;
    //}

    if (ref && (v != MJIEnv.NULL)) {
      VM.getVM().getSystemState().activateGC();
    }
  }

  // return the value of callerSlots variable given the name
  public int getLocalVariableSlotIndex (String name) {
    LocalVarInfo lv = mi.getLocalVar(name, pc.getPosition());

    if (lv != null){
      return lv.getSlotIndex();
    }

    return -1;
  }

  //--- abstract argument & return passing that can have VM dependend implementation
  
  public void setReferenceResult (int ref, Object attr){
    pushRef(ref);
    if (attr != null){
      setOperandAttr(attr);
    }
  }
  
  public void setResult (int r, Object attr){
    push(r);
    if (attr != null){
      setOperandAttr(attr);
    }    
  }
  
  public void setResult (long r, Object attr){
    pushLong(r);
    if (attr != null){
      setLongOperandAttr(attr);
    }    
  }
  
  public int getResult(){
    return pop();
  }
  
  public long getLongResult(){
    return popLong();
  }

  public int getReferenceResult () {
    return pop();
  }
  
  public Object getResultAttr () {
    return getOperandAttr();
  }

  public Object getLongResultAttr () {
    return getLongOperandAttr();
  }
  
  public float getFloatResult(){
    return Float.intBitsToFloat(getResult());    
  }
  public double getDoubleResult(){
    return Double.longBitsToDouble(getLongResult());
  }
  public Object getFloatResultAttr(){
    return getResultAttr();
  }
  public Object getDoubleResultAttr(){
    return getLongResultAttr();
  }

  
  //--- VM independent exception handler setup
  
  public void setExceptionReference (int exRef){
    pushRef(exRef);
  }
  
  public int getExceptionReference (){
    return pop();
  }
  
  public void setExceptionReferenceAttribute (Object attr){
    setOperandAttr(attr);
  }
  
  public Object getExceptionReferenceAttribute (){
    return getOperandAttr();
  }
  
  
  // those set the local vars that are normally initialized from call arguments
  public abstract void setArgumentLocal (int idx, int value, Object attr);
  public abstract void setLongArgumentLocal (int idx, long value, Object attr);
  public abstract void setReferenceArgumentLocal (int idx, int ref, Object attr);

  public void setFloatArgumentLocal (int idx, float value, Object attr){
    setArgumentLocal( idx, Float.floatToIntBits(value), attr);
  }
  public void setDoubleArgumentLocal (int idx, double value, Object attr){
    setLongArgumentLocal( idx, Double.doubleToLongBits(value), attr);
  }
}
