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

import com.sun.org.apache.bcel.internal.generic.InstructionConstants;
import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.JPFListener;
import gov.nasa.jpf.vm.serialize.UnknownJPFClass;

import java.util.Date;
import java.util.Locale;


/**
 * MJIEnv is the call environment for "native" methods, i.e. code that
 * is executed by the VM, not by JPF.
 *
 * Since library abstractions are supposed to be "user code", we provide
 * this class as a (little bit of) insulation towards the inner JPF workings.
 *
 * There are two APIs exported by this class. The public methods (like
 * getStringObject) don't expose JPF internals, and can be used from non
 * gov.nasa.jpf.vm NativePeer classes). The rest is package-default
 * and can be used to fiddle around as much as you like to (if you are in
 * the ..jvm package)
 *
 * Note that MJIEnv objects are now per-ThreadInfo (i.e. the variable
 * call envionment only includes MethodInfo and ClassInfo), which means
 * MJIEnv can be used in non-native methods (but only carefully, if you
 * don't need mi or ciMth).
 *
 * Note also this only works because we are not getting recursive in
 * native method calls. In fact, the whole DirectCallStackFrame / repeatTopInstruction
 * mechanism is there to turn logial recursion (JPF calling native, calling
 * JPF, calling native,..) into iteration. Otherwise we couldn't backtrack
 */
public class MJIEnv {
  public static final int NULL = 0;

  VM                     vm;
  ClassInfo               ciMth;  // the ClassInfo of the method this is called from
  MethodInfo              mi;
  ThreadInfo              ti;
  Heap                    heap;

  // those are various attributes set by the execution. note that
  // NativePeer.invoke never gets recursive in a roundtrip (at least if
  // used correctly, so we don't have to be afraid to overwrite any of these
  boolean                 repeat;
  Object                  returnAttr;

  // exception to be thrown upon return from native method
  // NOTE: this is only transient - don't expect this to be preserved over
  // transition boundaries
  int                     exceptionRef;

  MJIEnv (ThreadInfo ti) {
    this.ti = ti;

    // set those here so that we don't have an inconsistent state between
    // creation of an MJI object and the first native method call in
    // this thread (where any access to the heap or sa would bomb)
    vm = ti.getVM();
    heap = vm.getHeap();

    exceptionRef = NULL;
  }

  public VM getVM () {
    return vm;
  }

  public JPF getJPF () {
    return vm.getJPF();
  }

  public boolean isBigEndianPlatform(){
    return vm.isBigEndianPlatform();
  }
  
  public void addListener (JPFListener l){
    vm.getJPF().addListener(l);
  }

  public void removeListener (JPFListener l){
    vm.getJPF().removeListener(l);
  }

  public Config getConfig() {
    return vm.getConfig();
  }

  public void gc() {
    heap.gc();
  }

  public void forceState (){
    getSystemState().setForced(true);
  }

  public void ignoreTransition () {
    getSystemState().setIgnored(true);
  }

  public boolean isArray (int objref) {
    return heap.get(objref).isArray();
  }

  public int getArrayLength (int objref) {
    if (isArray(objref)) {
      return heap.get(objref).arrayLength();
    } else {
      throwException("java.lang.IllegalArgumentException");

      return 0;
    }
  }

  public String getArrayType (int objref) {
    return heap.get(objref).getArrayType();
  }

  public int getArrayTypeSize (int objref) {
    return Types.getTypeSize(getArrayType(objref));
  }

  //=== various attribute accessors ============================================
  // we only support some attribute APIs here, since MJIEnv adds little value
  // other than hiding the ElementInfo access. If the client already has
  // an ElementInfo reference, it should use that one to retrieve/enumerate/set
  // attributes since this avoids repeated Heap.get() calls
  
  //--- object attributes

  public boolean hasObjectAttr (int objref){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.hasObjectAttr();
    }

    return false;
  }

  public boolean hasObjectAttr (int objref, Class<?> type){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.hasObjectAttr(type);
    }

    return false;    
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */  
  public Object getObjectAttr (int objref){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.getObjectAttr();
    }
    return null;
  }
  
  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  public void setObjectAttr (int objref, Object a){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      ei.setObjectAttr(a);
    }
  }

  public void addObjectAttr (int objref, Object a){
    if (objref != NULL){
      ElementInfo ei = heap.getModifiable(objref);
      ei.addObjectAttr(a);
    }
  }

  
  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getObjectAttr (int objref, Class<T> attrType){
    ElementInfo ei = heap.get(objref);
    return ei.getObjectAttr(attrType);
  }
  
  //--- field attributes

  public boolean hasFieldAttr (int objref){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.hasFieldAttr();
    }

    return false;
  }
  
  public boolean hasFieldAttr (int objref, Class<?> type){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.hasFieldAttr(type);
    }

    return false;    
  }
  
  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */  
  public Object getFieldAttr (int objref, String fname){
    ElementInfo ei = heap.get(objref);
    FieldInfo fi = ei.getFieldInfo(fname);
    if (fi != null){
      return ei.getFieldAttr(fi);
    } else {
      throw new JPFException("no such field: " + fname);
    }
  }
  
  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  public void setFieldAttr (int objref, String fname, Object a){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      FieldInfo fi = ei.getFieldInfo(fname);
      ei.setFieldAttr(fi, a);
    }
  }

  public void addFieldAttr (int objref, String fname, Object a){
    if (objref != NULL){
      ElementInfo ei = heap.getModifiable(objref);
      FieldInfo fi = ei.getFieldInfo(fname);
      ei.addFieldAttr(fi, a);
    }
  }

  
  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getFieldAttr (int objref, String fname, Class<T> attrType){
    ElementInfo ei = heap.get(objref);
    FieldInfo fi = ei.getFieldInfo(fname);
    if (fi != null){
      return ei.getFieldAttr(fi, attrType);
    } else {
      throw new JPFException("no such field: " + fname);
    }
  }

  
  //--- element attrs

  public boolean hasElementdAttr (int objref){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.hasElementAttr();
    }

    return false;
  }
  
  public boolean hasElementAttr (int objref, Class<?> type){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.hasElementAttr(type);
    }

    return false;    
  }

  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */  
  public Object getElementAttr (int objref, int idx){
    ElementInfo ei = heap.get(objref);
    return ei.getElementAttr(idx);
  }
  
  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  public void setElementAttr (int objref, int idx, Object a){
    ElementInfo ei = heap.get(objref);
    ei.setElementAttr(idx, a);
  }

  public void addElementAttr (int objref, int idx, Object a){
    ElementInfo ei = heap.getModifiable(objref);
    ei.addElementAttr(idx, a);
  }

  
  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getElementAttr (int objref, int idx, Class<T> attrType){
    if (objref != NULL){
      ElementInfo ei = heap.get(objref);
      return ei.getElementAttr(idx, attrType);
    }
    return null;
  }

  

  // == end attrs ==  


  
  // the instance field setters
  public void setBooleanField (int objref, String fname, boolean val) {
    heap.getModifiable(objref).setBooleanField(fname, val);
  }

  public boolean getBooleanField (int objref, String fname) {
    return heap.get(objref).getBooleanField(fname);
  }

  public boolean getBooleanArrayElement (int objref, int index) {
    return heap.get(objref).getBooleanElement(index);
  }

  public void setBooleanArrayElement (int objref, int index, boolean value) {
    heap.getModifiable(objref).setBooleanElement(index, value);
  }


  public void setByteField (int objref, String fname, byte val) {
    heap.getModifiable(objref).setByteField(fname, val);
  }

  public byte getByteField (int objref, String fname) {
    return heap.get(objref).getByteField(fname);
  }

  public void setCharField (int objref, String fname, char val) {
    heap.getModifiable(objref).setCharField(fname, val);
  }

  public char getCharField (int objref, String fname) {
    return heap.get(objref).getCharField(fname);
  }

  public void setDoubleField (int objref, String fname, double val) {
    heap.getModifiable(objref).setDoubleField(fname, val);
  }

  public double getDoubleField (int objref, String fname) {
    return heap.get(objref).getDoubleField(fname);
  }

  public void setFloatField (int objref, String fname, float val) {
    heap.getModifiable(objref).setFloatField(fname, val);
  }

  public float getFloatField (int objref, String fname) {
    return heap.get(objref).getFloatField(fname);
  }


  public void setByteArrayElement (int objref, int index, byte value) {
    heap.getModifiable(objref).setByteElement(index, value);
  }

  public byte getByteArrayElement (int objref, int index) {
    return heap.get(objref).getByteElement(index);
  }

  public void setCharArrayElement (int objref, int index, char value) {
    heap.getModifiable(objref).setCharElement(index, value);
  }

  public void setIntArrayElement (int objref, int index, int value) {
    heap.getModifiable(objref).setIntElement(index, value);
  }

  public void setShortArrayElement (int objref, int index, short value) {
    heap.getModifiable(objref).setShortElement(index, value);
  }

  public void setFloatArrayElement (int objref, int index, float value) {
    heap.getModifiable(objref).setFloatElement(index, value);
  }

  public float getFloatArrayElement (int objref, int index) {
    return heap.get(objref).getFloatElement(index);
  }

  public double getDoubleArrayElement (int objref, int index) {
    return heap.get(objref).getDoubleElement(index);
  }
  public void setDoubleArrayElement (int objref, int index, double value) {
    heap.getModifiable(objref).setDoubleElement(index, value);
  }

  public short getShortArrayElement (int objref, int index) {
    return heap.get(objref).getShortElement(index);
  }

  public int getIntArrayElement (int objref, int index) {
    return heap.get(objref).getIntElement(index);
  }

  public char getCharArrayElement (int objref, int index) {
    return heap.get(objref).getCharElement(index);
  }

  public void setIntField (int objref, String fname, int val) {
    ElementInfo ei = heap.getModifiable(objref);
    ei.setIntField(fname, val);
  }

  // these two are the workhorses
  public void setDeclaredIntField (int objref, String refType, String fname, int val) {
    ElementInfo ei = heap.getModifiable(objref);
    ei.setDeclaredIntField(fname, refType, val);
  }

  public int getIntField (int objref, String fname) {
    ElementInfo ei = heap.get(objref);
    return ei.getIntField(fname);
  }

  public int getDeclaredIntField (int objref, String refType, String fname) {
    ElementInfo ei = heap.get(objref);
    return ei.getDeclaredIntField(fname, refType);
  }

  // these two are the workhorses
  public void setDeclaredReferenceField (int objref, String refType, String fname, int val) {
    ElementInfo ei = heap.getModifiable(objref);
    ei.setDeclaredReferenceField(fname, refType, val);
  }

  public void setReferenceField (int objref, String fname, int ref) {
     ElementInfo ei = heap.getModifiable(objref);
     ei.setReferenceField(fname, ref);
  }

  public int getReferenceField (int objref, String fname) {
    ElementInfo ei = heap.get(objref);
    return ei.getReferenceField(fname);
  }

  // we need this in case of a masked field
  public int getReferenceField (int objref, FieldInfo fi) {
    ElementInfo ei = heap.get(objref);
    return ei.getReferenceField(fi);
  }

  public String getStringField (int objref, String fname){
    int ref = getReferenceField(objref, fname);
    return getStringObject(ref);
  }

  // the box object accessors (should probably test for the appropriate class)
  public boolean getBooleanValue (int objref) {
    return getBooleanField(objref, "value");
  }

  public byte getByteValue (int objref) {
    return getByteField(objref, "value");
  }

  public char getCharValue (int objref) {
    return getCharField(objref, "value");
  }

  public short getShortValue (int objref) {
    return getShortField(objref, "value");
  }

  public int getIntValue (int objref) {
    return getIntField(objref, "value");
  }

  public long getLongValue (int objref) {
    return getLongField(objref, "value");
  }

  public float getFloatValue (int objref) {
    return getFloatField(objref, "value");
  }

  public double getDoubleValue (int objref) {
    return getDoubleField(objref, "value");
  }


  public void setLongArrayElement (int objref, int index, long value) {
    heap.getModifiable(objref).setLongElement(index, value);
  }

  public long getLongArrayElement (int objref, int index) {
    return heap.get(objref).getLongElement(index);
  }

  public void setLongField (int objref, String fname, long val) {
    ElementInfo ei = heap.getModifiable(objref);
    ei.setLongField(fname, val);
  }

//  public void setLongField (int objref, String refType, String fname, long val) {
//    ElementInfo ei = heap.get(objref);
//    ei.setLongField(fname, refType, val);
//  }

  public long getLongField (int objref, String fname) {
    ElementInfo ei = heap.get(objref);
    return ei.getLongField(fname);
  }

//  public long getLongField (int objref, String refType, String fname) {
//    ElementInfo ei = heap.get(objref);
//    return ei.getLongField(fname, refType);
//  }

  public void setReferenceArrayElement (int objref, int index, int eRef) {
    heap.getModifiable(objref).setReferenceElement(index, eRef);
  }

  public int getReferenceArrayElement (int objref, int index) {
    return heap.get(objref).getReferenceElement(index);
  }

  public void setShortField (int objref, String fname, short val) {
    setIntField(objref, fname, /*(int)*/ val);
  }

  public short getShortField (int objref, String fname) {
    return (short) getIntField(objref, fname);
  }

  /**
   * NOTE - this doesn't support element type checks or overlapping in-array copy 
   */
  public void arrayCopy (int srcRef, int srcPos, int dstRef, int dstPos, int len){
    ElementInfo eiSrc = heap.get(srcRef);
    ElementInfo eiDst = heap.get(dstRef);
    
    eiDst.arrayCopy(eiSrc, srcPos, dstPos, len);
  }
  
  public String getTypeName (int objref) {
    return heap.get(objref).getType();
  }

  public boolean isInstanceOf (int objref, String clsName) {
    ClassInfo ci = getClassInfo(objref);
    return ci.isInstanceOf(clsName);
  }
  
  public boolean isInstanceOf (int objref, ClassInfo cls) {
    ClassInfo ci = getClassInfo(objref);
    return ci.isInstanceOf(cls);
  }

  //--- the static field accessors
  // NOTE - it is the callers responsibility to ensure the class is
  // properly initialized, since calling <clinit> requires a roundtrip
  // (i.e. cannot be done synchronously from one of the following methods)
  
  // <2do> this uses the current system CL, we should probably use an explicit CL argument
  
  public void setStaticBooleanField (String clsName, String fname,
                                     boolean value) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setBooleanField(fname, value);
  }
  public void setStaticBooleanField (int clsObjRef, String fname, boolean val) {
    ElementInfo cei = getStaticElementInfo(clsObjRef);
    cei.setBooleanField(fname, val);
  }
  
  public boolean getStaticBooleanField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getBooleanField(fname);
  }

  public void setStaticByteField (String clsName, String fname, byte value) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setByteField(fname, value);  }

  public byte getStaticByteField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getByteField(fname);
  }

  public void setStaticCharField (String clsName, String fname, char value) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setCharField(fname, value);  }

  public char getStaticCharField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getCharField(fname);
  }

  public void setStaticDoubleField (String clsName, String fname, double val) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setDoubleField(fname, val);
  }

  public double getStaticDoubleField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getDoubleField(fname);
  }
  
  public double getStaticDoubleField (int clsObjRef, String fname) {
    ElementInfo cei = getStaticElementInfo(clsObjRef);
    return cei.getDoubleField(fname);
  }

  public double getStaticDoubleField (ClassInfo ci, String fname) {
    ElementInfo ei = ci.getStaticElementInfo();
    return ei.getDoubleField(fname);
  }
  
  public void setStaticFloatField (String clsName, String fname, float val) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setFloatField(fname, val);
  }

  public float getStaticFloatField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getFloatField(fname);
  }

  public void setStaticIntField (String clsName, String fname, int val) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setIntField(fname, val);
  }

  public void setStaticIntField (int clsObjRef, String fname, int val) {
    ElementInfo cei = getStaticElementInfo(clsObjRef);
    cei.setIntField(fname, val);
  }

  public int getStaticIntField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getIntField(fname);
  }
  
  public int getStaticIntField (int clsObjRef, String fname) {
    ElementInfo cei = getStaticElementInfo(clsObjRef);
    return cei.getIntField(fname);
  }

  public int getStaticIntField (ClassInfo ci, String fname) {
    ElementInfo ei = ci.getStaticElementInfo();
    return ei.getIntField(fname);
  }

  public void setStaticLongField (String clsName, String fname, long value) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    ci.getStaticElementInfo().setLongField(fname, value);
  }

  public void setStaticLongField (int clsObjRef, String fname, long val) {
    ElementInfo cei = getModifiableStaticElementInfo(clsObjRef);
    cei.setLongField(fname, val);
  }

  public long getStaticLongField (int clsRef, String fname) {
    ClassInfo ci = getReferredClassInfo(clsRef);
    return getStaticLongField(ci, fname);
  }

  public long getStaticLongField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return getStaticLongField(ci, fname);
  }

  public long getStaticLongField (ClassInfo ci, String fname){
    ElementInfo ei = ci.getStaticElementInfo();
    return ei.getLongField(fname);
  }

  public void setStaticReferenceField (String clsName, String fname, int objref) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);

    // <2do> - we should REALLY check for type compatibility here
    ci.getModifiableStaticElementInfo().setReferenceField(fname, objref);
  }

  public void setStaticReferenceField (int clsObjRef, String fname, int objref) {
    ElementInfo cei = getModifiableStaticElementInfo(clsObjRef);

    // <2do> - we should REALLY check for type compatibility here
    cei.setReferenceField(fname, objref);
  }

  public int getStaticReferenceField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getReferenceField(fname);
  }

  public int getStaticReferenceField (int clsObjRef, String fname) {
    ElementInfo cei = getStaticElementInfo(clsObjRef);
    return cei.getReferenceField(fname);
  }

  public int getStaticReferenceField (ClassInfo ci, String fname){
    return ci.getStaticElementInfo().getReferenceField(fname);
  }

  public short getStaticShortField (String clsName, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    return ci.getStaticElementInfo().getShortField(fname);
  }

  public char[] getStringChars (int objRef){
    if (objRef != MJIEnv.NULL) {
      ElementInfo ei = getElementInfo(objRef);
      return ei.getStringChars();
      
    } else {
      return null;
    }
    
  }
  
  /**
   * turn JPF String object into a VM String object
   * (this is a method available for non gov..jvm NativePeer classes)
   */
  public String getStringObject (int objRef) {
    if (objRef != MJIEnv.NULL) {
      ElementInfo ei = getElementInfo(objRef);
      return ei.asString();
      
    } else {
      return null;
    }
  }

  public String[] getStringArrayObject (int aRef){
    String[] sa = null;
     
    if (aRef == NULL) return sa;

    ClassInfo aci = getClassInfo(aRef);
    if (aci.isArray()){
      ClassInfo eci = aci.getComponentClassInfo();
      if (eci.getName().equals("java.lang.String")){
        int len = getArrayLength(aRef);
        sa = new String[len];

        for (int i=0; i<len; i++){
          int sRef = getReferenceArrayElement(aRef,i);
          sa[i] = getStringObject(sRef);
        }

        return sa;
        
      } else {
        throw new IllegalArgumentException("not a String[] array: " + aci.getName());
      }
    } else {
      throw new IllegalArgumentException("not an array reference: " + aci.getName());
    }
  }
  
  public Date getDateObject (int objref) {
    if (objref != MJIEnv.NULL) {
      ElementInfo ei = getElementInfo(objref);
      if (ei.getClassInfo().getName().equals("java.util.Date")) {
        // <2do> this is not complete yet
        long fastTime = ei.getLongField("fastTime");
        Date d = new Date(fastTime);
        return d;
      } else {
        throw new JPFException("not a Date object reference: " + ei);
      }
    } else {
      return null;
    }
    
  }

  public Object[] getArgumentArray (int argRef) {
    Object[] args = null;
    if (argRef == NULL) return args;

    int nArgs = getArrayLength(argRef);
    args = new Object[nArgs];

    for (int i=0; i<nArgs; i++){
      int aref = getReferenceArrayElement(argRef,i);
      ClassInfo ci = getClassInfo(aref);
      String clsName = ci.getName();
      if (clsName.equals("java.lang.Boolean")){
        args[i] = Boolean.valueOf(getBooleanField(aref,"value"));
      } else if (clsName.equals("java.lang.Integer")){
        args[i] = Integer.valueOf(getIntField(aref,"value"));
      } else if (clsName.equals("java.lang.Double")){
        args[i] = Double.valueOf(getDoubleField(aref,"value"));
      } else if (clsName.equals("java.lang.String")){
        args[i] = getStringObject(aref);
      }
    }

    return args;
  }

  public Boolean getBooleanObject (int objref){
    return Boolean.valueOf(getBooleanField(objref, "value"));
  }

  public Byte getByteObject (int objref){
    return new Byte(getByteField(objref, "value"));
  }

  public Character getCharObject (int objref){
    return new Character(getCharField(objref, "value"));
  }

  public Short getShortObject (int objref){
    return new Short(getShortField(objref, "value"));
  }

  public Integer getIntegerObject (int objref){
    return new Integer(getIntField(objref, "value"));
  }

  public Long getLongObject (int objref){
    return new Long(getLongField(objref, "value"));
  }

  public Float getFloatObject (int objref){
    return new Float(getFloatField(objref, "value"));
  }

  public Double getDoubleObject (int objref){
    return new Double(getDoubleField(objref, "value"));
  }

  // danger - the returned arrays could be used to modify contents of stored objects

  public byte[] getByteArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    byte[] a = ei.asByteArray();

    return a;
  }

  public char[] getCharArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    char[] a = ei.asCharArray();

    return a;
  }

  public short[] getShortArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    short[] a = ei.asShortArray();

    return a;
  }

  public int[] getIntArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    int[] a = ei.asIntArray();

    return a;
  }

  public long[] getLongArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    long[] a = ei.asLongArray();

    return a;
  }

  public float[] getFloatArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    float[] a = ei.asFloatArray();

    return a;
  }

  public double[] getDoubleArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    double[] a = ei.asDoubleArray();

    return a;
  }

  public boolean[] getBooleanArrayObject (int objref) {
    ElementInfo ei = getElementInfo(objref);
    boolean[] a = ei.asBooleanArray();

    return a;
  }
  
  public int[] getReferenceArrayObject (int objref){
    ElementInfo ei = getElementInfo(objref);
    int[] a = ei.asReferenceArray();

    return a;    
  }

  public boolean canLock (int objref) {
    ElementInfo ei = getElementInfo(objref);

    return ei.canLock(ti);
  }

  public int newBooleanArray (int size) {
    return heap.newArray("Z", size, ti).getObjectRef();
  }

  public int newByteArray (int size) {
    return heap.newArray("B", size, ti).getObjectRef();
  }

  public int newByteArray (byte[] buf){
    ElementInfo eiArray = heap.newArray("B", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setByteElement( i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newCharArray (int size) {
    return heap.newArray("C", size, ti).getObjectRef();
  }

  public int newCharArray (char[] buf){
    ElementInfo eiArray = heap.newArray("C", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setCharElement( i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newShortArray (int size) {
    return heap.newArray("S", size, ti).getObjectRef();
  }
  
  public int newShortArray (short[] buf){
    ElementInfo eiArray = heap.newArray("S", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setShortElement(i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newDoubleArray (int size) {
    return heap.newArray("D", size, ti).getObjectRef();
  }

  public int newDoubleArray (double[] buf){
    ElementInfo eiArray =  heap.newArray("D", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setDoubleElement(i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newFloatArray (int size) {
    return heap.newArray("F", size, ti).getObjectRef();
  }
  
  public int newFloatArray (float[] buf){
    ElementInfo eiArray =  heap.newArray("F", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setFloatElement( i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newIntArray (int size) {
    return heap.newArray("I", size, ti).getObjectRef();
  }

  public int newIntArray (int[] buf){
    ElementInfo eiArray = heap.newArray("I", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setIntElement( i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newLongArray (int size) {
    return heap.newArray("J", size, ti).getObjectRef();
  }

  public int newLongArray (long[] buf){
    ElementInfo eiArray = heap.newArray("J", buf.length, ti);
    for (int i=0; i<buf.length; i++){
      eiArray.setLongElement( i, buf[i]);
    }
    return eiArray.getObjectRef();
  }

  public int newObjectArray (String elementClsName, int size) {
    if (!elementClsName.endsWith(";")) {
      elementClsName = Types.getTypeSignature(elementClsName, false);
    }

    return heap.newArray(elementClsName, size, ti).getObjectRef();
  }

  public ElementInfo newElementInfo (ClassInfo ci) throws ClinitRequired {
    if (ci.initializeClass(ti)){
      throw new ClinitRequired(ci);
    }

    return heap.newObject(ci, ti);
  }
  
  /**
   * check if the ClassInfo is properly initialized
   * if yes, create a new instance of it but don't call any ctor
   * if no, throw a ClinitRequired exception
   */
  public int newObject (ClassInfo ci)  throws ClinitRequired {
    ElementInfo ei = newElementInfo(ci);
    return ei.getObjectRef();
  }

  /**
   * this creates a new object without checking if the ClassInfo needs
   * initialization. This is useful in a context that already
   * is aware and handles re-execution
   */
  public int newObjectOfUncheckedClass (ClassInfo ci){
    ElementInfo ei = heap.newObject(ci, ti);
    return ei.getObjectRef();    
  }
  
  public ElementInfo newElementInfo (String clsName)  throws ClinitRequired, UnknownJPFClass {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
    if (ci != null){
      return newElementInfo(ci);
    } else {
      throw new UnknownJPFClass(clsName);
    }
  }
  
  public int newObject (String clsName)   throws ClinitRequired, UnknownJPFClass {
    ElementInfo ei = newElementInfo(clsName);
    return (ei != null) ? ei.getObjectRef() : NULL;
  }
  
  public int newString (String s) {
    if (s == null){
      return NULL;
    } else {
      return heap.newString(s, ti).getObjectRef();
    }
  }

  public int newStringArray (String[] a){
    int aref = newObjectArray("Ljava/lang/String;", a.length);

    for (int i=0; i<a.length; i++){
      setReferenceArrayElement(aref, i, newString(a[i]));
    }

    return aref;
  }

  public int newString (int arrayRef) {
    String t = getArrayType(arrayRef);
    String s = null;

    if ("C".equals(t)) {          // character array
      char[] ca = getCharArrayObject(arrayRef);
      s = new String(ca);
    } else if ("B".equals(t)) {   // byte array
      byte[] ba = getByteArrayObject(arrayRef);
      s = new String(ba);
    }

    if (s == null) {
      return NULL;
    }

    return newString(s);
  }
  
  public String format (int fmtRef, int argRef){
    String format = getStringObject(fmtRef);
    int len = getArrayLength(argRef);
    Object[] arg = new Object[len];

    for (int i=0; i<len; i++){
      int ref = getReferenceArrayElement(argRef,i);
      if (ref != NULL) {
        String clsName = getClassName(ref);
        if (clsName.equals("java.lang.String")) {
          arg[i] = getStringObject(ref);
        } else if (clsName.equals("java.lang.Boolean")){
          arg[i] = getBooleanObject(ref);
        } else if (clsName.equals("java.lang.Byte")) {
          arg[i] = getByteObject(ref);
        } else if (clsName.equals("java.lang.Char")) {
          arg[i] = getCharObject(ref);
        } else if (clsName.equals("java.lang.Short")) {
          arg[i] = getShortObject(ref);
        } else if (clsName.equals("java.lang.Integer")) {
          arg[i] = getIntegerObject(ref);
        } else if (clsName.equals("java.lang.Long")) {
          arg[i] = getLongObject(ref);
        } else if (clsName.equals("java.lang.Float")) {
          arg[i] = getFloatObject(ref);
        } else if (clsName.equals("java.lang.Double")) {
          arg[i] = getDoubleObject(ref);
        } else {
          // need a toString() here
          arg[i] = "??";
        }
      }
    }

    return String.format(format,arg);
  }

  public String format (Locale l,int fmtRef, int argRef){
	    String format = getStringObject(fmtRef);
	    int len = getArrayLength(argRef);
	    Object[] arg = new Object[len];

	    for (int i=0; i<len; i++){
	      int ref = getReferenceArrayElement(argRef,i);
	      if (ref != NULL) {
	        String clsName = getClassName(ref);
	        if (clsName.equals("java.lang.String")) {
	          arg[i] = getStringObject(ref);
	        } else if (clsName.equals("java.lang.Byte")) {
	          arg[i] = getByteObject(ref);
	        } else if (clsName.equals("java.lang.Char")) {
	          arg[i] = getCharObject(ref);
	        } else if (clsName.equals("java.lang.Short")) {
	          arg[i] = getShortObject(ref);
	        } else if (clsName.equals("java.lang.Integer")) {
	          arg[i] = getIntegerObject(ref);
	        } else if (clsName.equals("java.lang.Long")) {
	          arg[i] = getLongObject(ref);
	        } else if (clsName.equals("java.lang.Float")) {
	          arg[i] = getFloatObject(ref);
	        } else if (clsName.equals("java.lang.Double")) {
	          arg[i] = getDoubleObject(ref);
	        } else {
	          // need a toString() here
	          arg[i] = "??";
	        }
	      }
	    }

	    return String.format(l,format,arg);
	  }


  public int newBoolean (boolean b){
    return getStaticReferenceField("java.lang.Boolean", b ? "TRUE" : "FALSE");
  }

  public int newInteger (int n){
    ElementInfo ei = heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Integer"), ti);
    ei.setIntField("value",n);
    return ei.getObjectRef();
  }

  public int newLong (long l){
    ElementInfo ei = heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Long"), ti);
    ei.setLongField("value",l);
    return ei.getObjectRef();
  }

  public int newDouble (double d){
    ElementInfo ei = heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Double"), ti);
    ei.setDoubleField("value",d);
    return ei.getObjectRef();
  }

  public int newFloat (float f){
    ElementInfo ei = heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Float"), ti);
    ei.setFloatField("value",f);
    return ei.getObjectRef();
  }

  public int newByte (byte b){
    ElementInfo ei = heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Byte"), ti);
    ei.setByteField("value",b);
    return ei.getObjectRef();
  }

  public int newShort (short s){
    ElementInfo ei = heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Short"), ti);
    ei.setShortField("value",s);
    return ei.getObjectRef();
  }

  public int newCharacter (char c){
    ElementInfo ei =  heap.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Character"), ti);
    ei.setCharField("value",c);
    return ei.getObjectRef();
  }


  public boolean notify (int objref) {
    // objref can't be NULL since the corresponding INVOKE would have failed
    ElementInfo ei = getModifiableElementInfo(objref);
    return notify(ei);
  }

  public boolean notify (ElementInfo ei) {
    if (!ei.isLockedBy(ti)){
      throwException("java.lang.IllegalMonitorStateException",
                                 "un-synchronized notify");
      return false;
    }

    return ei.notifies(getSystemState(), ti); 
  }
  
  public boolean notifyAll (int objref) {
    // objref can't be NULL since the corresponding INVOKE would have failed
    ElementInfo ei = getElementInfo(objref);
    return notifyAll(ei);
  }

  public boolean notifyAll (ElementInfo ei) {
    if (!ei.isLockedBy(ti)){
      throwException("java.lang.IllegalMonitorStateException",
                                 "un-synchronized notifyAll");
      return false;
    }

    return ei.notifiesAll();    
  }
  
  public void registerPinDown(int objref){
    heap.registerPinDown(objref);
  }
  public void registerPinDown (ElementInfo ei){
    registerPinDown(ei.getObjectRef());
  }
  
  public void releasePinDown(int objref){
    heap.releasePinDown(objref);
  }
  public void releasePinDown (ElementInfo ei){
    releasePinDown(ei.getObjectRef());
  }
  
  /**
   *  use this whenever a peer performs an operation on a class that might not be initialized yet
   *  Do a repeatInvocation() in this case 
   */
  public boolean requiresClinitExecution(ClassInfo ci) {
    return ci.initializeClass(ti);
  }
  
  /**
   * repeat execution of the instruction that caused a native method call
   * NOTE - this does NOT mean it's the NEXT executed insn, since the native method
   * might have pushed direct call frames on the stack before asking us to repeat it.
   */
  public void repeatInvocation () {
    repeat = true;
  }

  public boolean isInvocationRepeated() {
    return repeat;
  }


  public boolean setNextChoiceGenerator (ChoiceGenerator<?> cg){
    return vm.getSystemState().setNextChoiceGenerator(cg);
  }

  public void setMandatoryNextChoiceGenerator(ChoiceGenerator<?> cg, String failMsg){
    vm.getSystemState().setMandatoryNextChoiceGenerator(cg, failMsg);
  }

  public ChoiceGenerator<?> getChoiceGenerator () {
    return vm.getSystemState().getChoiceGenerator();
  }

  // note this only makes sense if we actually do return something
  public void setReturnAttribute (Object attr) {
    returnAttr = attr;
  }

  /**
   * return attr list of all arguments. Use ObjectList to retrieve values
   * from this list
   * 
   * NOTE - this can only be called from a native method context, since
   * otherwise the top frame is the callee
   */
  public Object[] getArgAttributes () {
    StackFrame caller = getCallerStackFrame();
    return caller.getArgumentAttrs(mi);
  }

  public Object getReturnAttribute() {
    return returnAttr;
  }

  // if any of the next methods is called from the bottom
  // half of a CG method, you might want to check if another thread
  // or a listener has already set an exception you don't want to override
  // (this is for instance used in Thread.stop())

  public void throwException (int xRef){
    assert isInstanceOf(xRef, "java.lang.Throwable");
    exceptionRef = xRef;
  }

  public void throwException (String clsName) {
    ClassInfo ciX = ClassInfo.getInitializedClassInfo(clsName, ti);
    assert ciX.isInstanceOf("java.lang.Throwable");
    exceptionRef = ti.createException(ciX, null, NULL);
  }

  public void throwException (String clsName, String details) {
    ClassInfo ciX = ClassInfo.getInitializedClassInfo(clsName, ti);
    assert ciX.isInstanceOf("java.lang.Throwable");
    exceptionRef = ti.createException(ciX, details, NULL);
  }

  public void throwAssertion (String details) {
    throwException("java.lang.AssertionError", details);
  }

  public void throwInterrupt(){
    throwException("java.lang.InterruptedException");
  }

  public void stopThread(){
    stopThreadWithException(MJIEnv.NULL);
  }

  public void stopThreadWithException (int xRef){
    // this will call throwException(xRef) with the proper Throwable
    ti.setStopped(xRef);
  }

  void setCallEnvironment (MethodInfo mi) {
    this.mi = mi;

    if (mi != null){
      ciMth = mi.getClassInfo();
    } else {
      //ciMth = null;
      //mi = null;
    }

    repeat = false;
    returnAttr = null;

    // we should NOT reset exceptionRef here because it might have been set
    // at the beginning of the transition. It gets reset upon return from the
    // native method
    //exceptionRef = NULL;
  }

  void clearCallEnvironment () {
    setCallEnvironment(null);
  }

  ElementInfo getStaticElementInfo (int clsObjRef) {
    ClassInfo ci = getReferredClassInfo( clsObjRef);
    if (ci != null) {
      return ci.getStaticElementInfo();
    }
    
    return null;
  }
  
  ElementInfo getModifiableStaticElementInfo (int clsObjRef) {
    ClassInfo ci = getReferredClassInfo( clsObjRef);
    if (ci != null) {
      return ci.getModifiableStaticElementInfo();
    }
    
    return null;
  }
  

  ClassInfo getClassInfo () {
    return ciMth;
  }

  public ClassInfo getReferredClassInfo (int clsObjRef) {
    ElementInfo ei = getElementInfo(clsObjRef);
    if (ei.getClassInfo().getName().equals("java.lang.Class")) {
      int ciId = ei.getIntField( ClassInfo.ID_FIELD);
      int clref = ei.getReferenceField("classLoader");
      
      ElementInfo eiCl = getElementInfo(clref);
      int cliId = eiCl.getIntField(ClassLoaderInfo.ID_FIELD);
      
      ClassLoaderInfo cli = getVM().getClassLoader(cliId);
      ClassInfo referredCi = cli.getClassInfo(ciId);
      
      return referredCi;
      
    } else {
      throw new JPFException("not a java.lang.Class object: " + ei);
    }
  }

  public ClassInfo getClassInfo (int objref) {
    ElementInfo ei = getElementInfo(objref);
    if (ei != null){
      return ei.getClassInfo();
    } else {
      return null;
    }
  }

  public String getClassName (int objref) {
    return getClassInfo(objref).getName();
  }

  public Heap getHeap () {
    return vm.getHeap();
  }

  public ElementInfo getElementInfo (int objref) {
    return heap.get(objref);
  }

  public ElementInfo getModifiableElementInfo (int objref) {
    return heap.getModifiable(objref);
  }

  
  public int getStateId () {
    return VM.getVM().getStateId();
  }

  void clearException(){
    exceptionRef = MJIEnv.NULL;
  }

  public int peekException () {
    return exceptionRef;
  }

  public int popException () {
    int ret = exceptionRef;
    exceptionRef = NULL;
    return ret;
  }

  public boolean hasException(){
    return (exceptionRef != NULL);
  }

  public boolean hasPendingInterrupt(){
    return (exceptionRef != NULL && isInstanceOf(exceptionRef, "java.lang.InterruptedException"));
  }

  //-- time is managed by the VM
  public long currentTimeMillis(){
    return vm.currentTimeMillis();
  }
  
  public long nanoTime(){
    return vm.nanoTime();
  }
  
  //--- those are not public since they refer to JPF internals
  public KernelState getKernelState () {
    return VM.getVM().getKernelState();
  }

  public MethodInfo getMethodInfo () {
    return mi;
  }

  public Instruction getInstruction () {
    return ti.getPC();
  }

  /**
   * It returns the ClassLoaderInfo corresponding to the given classloader object
   * reference
   */
  public ClassLoaderInfo getClassLoaderInfo(int clObjRef) {
    if(clObjRef == MJIEnv.NULL) {
      return null;
    }

    int cliId = heap.get(clObjRef).getIntField(ClassLoaderInfo.ID_FIELD);
    return getVM().getClassLoader(cliId);
  }

  // <2do> that's not correct - it should return the current SystemClassLoader, NOT the startup SystemClassLoader
  // (we can instantiate them explicitly)
  public ClassLoaderInfo getSystemClassLoaderInfo() {
    return ti.getSystemClassLoaderInfo();
  }
  
  public SystemState getSystemState () {
    return ti.getVM().getSystemState();
  }
  
  public ApplicationContext getApplicationContext (){
    return ti.getApplicationContext();
  }

  public ThreadInfo getThreadInfo () {
    return ti;
  }

  /**
   * NOTE - callers have to be prepared this might return null in case
   * the thread got already terminated
   */
  public ThreadInfo getThreadInfoForId (int id){
    return vm.getThreadList().getThreadInfoForId(id);
  }

  public ThreadInfo getLiveThreadInfoForId (int id){
    ThreadInfo ti = vm.getThreadList().getThreadInfoForId(id);
    if (ti != null && ti.isAlive()){
      return ti;
    }
    
    return null;
  }
  
  /**
   * NOTE - callers have to be prepared this might return null in case
   * the thread got already terminated
   */
  public ThreadInfo getThreadInfoForObjRef (int id){
    return vm.getThreadList().getThreadInfoForObjRef(id);
  }
  
  public ThreadInfo getLiveThreadInfoForObjRef (int id){
    ThreadInfo ti = vm.getThreadList().getThreadInfoForObjRef(id);
    if (ti != null && ti.isAlive()){
      return ti;
    }
    
    return null;
  }

  
  
  public ThreadInfo[] getLiveThreads(){
    return getVM().getLiveThreads();
  }
  
  // <2do> - naming? not very intuitive
  void lockNotified (int objref) {
    ElementInfo ei = getModifiableElementInfo(objref);
    ei.lockNotified(ti);
  }

  void initAnnotationProxyField (int proxyRef, FieldInfo fi, Object v) throws ClinitRequired {
    String fname = fi.getName();
    String ftype = fi.getType();

    if (v instanceof String){
      setReferenceField(proxyRef, fname, newString((String)v));
    } else if (v instanceof Boolean){
      setBooleanField(proxyRef, fname, ((Boolean)v).booleanValue());
    } else if (v instanceof Integer){
      setIntField(proxyRef, fname, ((Integer)v).intValue());
    } else if (v instanceof Long){
      setLongField(proxyRef, fname, ((Long)v).longValue());
    } else if (v instanceof Float){
      setFloatField(proxyRef, fname, ((Float)v).floatValue());
    } else if (v instanceof Short){
      setShortField(proxyRef, fname, ((Short)v).shortValue());
    } else if (v instanceof Character){
      setCharField(proxyRef, fname, ((Character)v).charValue());
    } else if (v instanceof Byte){
      setByteField(proxyRef, fname, ((Byte)v).byteValue());
    } else if (v instanceof Double){
      setDoubleField(proxyRef, fname, ((Double)v).doubleValue());

    } else if (v instanceof AnnotationInfo.EnumValue){ // an enum constant
      AnnotationInfo.EnumValue ev = (AnnotationInfo.EnumValue)v;
      String eCls = ev.getEnumClassName();
      String eConst = ev.getEnumConstName();

      ClassInfo eci = ClassLoaderInfo.getCurrentResolvedClassInfo(eCls);
      if (!eci.isInitialized()){
        throw new ClinitRequired(eci);
      }

      StaticElementInfo sei = eci.getStaticElementInfo();
      int eref = sei.getReferenceField(eConst);
      setReferenceField(proxyRef, fname, eref);

    } else if (v instanceof AnnotationInfo.ClassValue){ // a class
      String clsName = v.toString();
      ClassInfo cci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
      // <2do> should throw ClassNotFoundError here if cci is null

      if (!cci.isInitialized()){
        throw new ClinitRequired(cci);
      }

      int cref = cci.getClassObjectRef();
      setReferenceField(proxyRef, fname, cref);

    } else if (v.getClass().isArray()){ // ..or arrays thereof
      Object[] a = (Object[])v;
      int aref = NULL;

      if (ftype.equals("java.lang.String[]")){
        aref = newObjectArray("Ljava/lang/String;", a.length);
        for (int i=0; i<a.length; i++){
          setReferenceArrayElement(aref,i,newString(a[i].toString()));
        }
      } else if (ftype.equals("int[]")){
        aref = newIntArray(a.length);
        for (int i=0; i<a.length; i++){
          setIntArrayElement(aref,i,((Number)a[i]).intValue());
        }
      } else if (ftype.equals("boolean[]")){
        aref = newBooleanArray(a.length);
        for (int i=0; i<a.length; i++){
          setBooleanArrayElement(aref,i,((Boolean)a[i]).booleanValue());
        }
      } else if (ftype.equals("long[]")){
        aref = newLongArray(a.length);
        for (int i=0; i<a.length; i++){
          setLongArrayElement(aref,i,((Number)a[i]).longValue());
        }
      } else if (ftype.equals("double[]")){
        aref = newDoubleArray(a.length);
        for (int i=0; i<a.length; i++){
          setDoubleArrayElement(aref,i,((Number)a[i]).doubleValue());
        }
      } else if (ftype.equals("java.lang.Class[]")){
        aref = newObjectArray("java.lang.Class", a.length);
        for (int i=0; i<a.length; i++){
          String clsName = ((AnnotationInfo.ClassValue)a[i]).getName();
          ClassInfo cci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);
          if (!cci.isInitialized()){
            throw new ClinitRequired(cci);
          }
          int cref = cci.getClassObjectRef();
          setReferenceArrayElement(aref,i,cref);
        }
      }

      if (aref != NULL){
        setReferenceField(proxyRef, fname, aref);
      } else {
        throwException("AnnotationElement type not supported: " + ftype);
      }

    } else {
      throwException("AnnotationElement type not supported: " + ftype);
    }
  }

  int newAnnotationProxy (ClassInfo aciProxy, AnnotationInfo ai) throws ClinitRequired {

    int proxyRef = newObject(aciProxy);

    // init fields of the new object from the AnnotationInfo
    for (AnnotationInfo.Entry e : ai.getEntries()){
      Object v = e.getValue();
      String fname = e.getKey();
      FieldInfo fi = aciProxy.getInstanceField(fname);

      initAnnotationProxyField(proxyRef, fi, v);
    }

    return proxyRef;
  }

  int newAnnotationProxies (AnnotationInfo[] ai) throws ClinitRequired {

    if ((ai != null) && (ai.length > 0)){
      int aref = newObjectArray("Ljava/lang/annotation/Annotation;", ai.length);
      for (int i=0; i<ai.length; i++){
        ClassInfo aci = ClassLoaderInfo.getCurrentResolvedClassInfo(ai[i].getName());
        ClassInfo aciProxy = aci.getAnnotationProxy();

        int ar = newAnnotationProxy(aciProxy, ai[i]);
        setReferenceArrayElement(aref, i, ar);
      }
      return aref;

    } else {
      // on demand init (not too many programs use annotation reflection)
      int aref = getStaticReferenceField("java.lang.Class", "emptyAnnotations");
      if (aref == NULL) {
        aref = newObjectArray("Ljava/lang/annotation/Annotation;", 0);
        setStaticReferenceField("java.lang.Class", "emptyAnnotations", aref);
      }
      return aref;
    }
  }

  public void handleClinitRequest (ClassInfo ci) {
    ThreadInfo ti = getThreadInfo();

    // NOTE: we have to repeat no matter what, since this is called from
    // a handler context (if we only had to create a class object w/o
    // calling clinit, we can't just go on)
    ci.initializeClass(ti);
    repeatInvocation();
  }

  public StackFrame getCallerStackFrame() {
    // since native methods are now executed within their own stack frames
    // we provide a little helper to get the caller
    return ti.getLastNonSyntheticStackFrame();
  }

  public StackFrame getModifiableCallerStackFrame() {
    // since native methods are now executed within their own stack frames
    // we provide a little helper to get the caller
    return ti.getModifiableLastNonSyntheticStackFrame();
  }

  
  public int valueOfBoolean(boolean b) {
    return BoxObjectCacheManager.valueOfBoolean(ti, b);
  }

  public int valueOfByte(byte b) {
    return BoxObjectCacheManager.valueOfByte(ti, b);
  }

  public int valueOfCharacter(char c) {
    return BoxObjectCacheManager.valueOfCharacter(ti, c);
  }

  public int valueOfShort(short s) {
    return BoxObjectCacheManager.valueOfShort(ti, s);
  }

  public int valueOfInteger(int i) {
    return BoxObjectCacheManager.valueOfInteger(ti, i);
  }

  public int valueOfLong(long l) {
    return BoxObjectCacheManager.valueOfLong(ti, l);
  }
}
