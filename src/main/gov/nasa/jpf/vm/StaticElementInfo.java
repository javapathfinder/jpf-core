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
import gov.nasa.jpf.util.HashData;

/**
 * A specialized version of ElementInfo that is used for static fields. Each
 * registered ClassInfo instance has its own StaticElementInfo instance
 */
public final class StaticElementInfo extends ElementInfo {

  // this is kind of dangerous - make sure these flags are still unused in ElementInfo
  static final int ATTR_COR_CHANGED    = 0x100000;
  static final int ATTR_STATUS_CHANGED = 0x200000;

  static final int ATTR_ANY_CHANGED = ElementInfo.ATTR_ANY_CHANGED | ATTR_COR_CHANGED | ATTR_STATUS_CHANGED;
  
  protected int classObjectRef = MJIEnv.NULL;
  protected int status = ClassInfo.UNINITIALIZED;

  
  public StaticElementInfo () {
  }

  public StaticElementInfo (int id, ClassInfo ci, Fields f, Monitor m, ThreadInfo ti, ElementInfo eiClsObj) {
    super(id, ci, f, m, ti);

    // startup classes don't have a class object yet
    if (eiClsObj != null) {
      classObjectRef = eiClsObj.getObjectRef();
    }

    ti.getScheduler().initializeClassSharedness(ti, this);
  }
  
  @Override
  public ElementInfo getModifiableInstance() {
    if (!isFrozen()) {
      return this;
    } else {
      Statics statics = ci.getStatics();
      return statics.getModifiable( objRef);
    }
  }
  
  @Override
  public boolean isObject(){
    return false;
  }

  @Override
  public boolean isArray(){
    return false;
  }
  
  @Override
  public boolean hasFinalizer() {
    return false;
  }
  
  @Override
  protected int getNumberOfFieldsOrElements(){
    // static fields can't be arrays, those are always heap objects
    return ci.getNumberOfStaticFields();
  }

  @Override
  public boolean hasChanged() {
    return (attributes & ATTR_ANY_CHANGED) != 0;
  }

  @Override
  public void markUnchanged() {
    attributes &= ~ATTR_ANY_CHANGED;
  }
  
  @Override
  public void hash(HashData hd) {
    super.hash(hd);
    hd.add(classObjectRef);
    hd.add(status);
  }

  @Override
  public boolean equals(Object o) {
    if (super.equals(o) && o instanceof StaticElementInfo) {
      StaticElementInfo other = (StaticElementInfo) o;

      if (classObjectRef != other.classObjectRef) {
        return false;
      }
      if (status != other.status) {
        return false;
      }

      return true;

    } else {
      return false;
    }
  }


  /**
  public boolean isShared() {
    // static fields are always thread global
    return true;
  }
  **/

  public int getStatus() {
    return status;
  }
  
  void setStatus (int newStatus) {
    checkIsModifiable();
    
    if (status != newStatus) {
      status = newStatus;
      attributes |= ATTR_STATUS_CHANGED;
    }
  }

  
  @Override
  protected FieldInfo getDeclaredFieldInfo (String clsBase, String fname) {
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsBase); // <2do> should use CL argument
    FieldInfo fi = ci.getDeclaredStaticField(fname);
    
    if (fi == null) {
      throw new JPFException("class " + ci.getName() +
                                         " has no static field " + fname);
    }
    return fi;
  }

  @Override
  public FieldInfo getFieldInfo (String fname) {
    ClassInfo ci = getClassInfo();
    return ci.getStaticField(fname);
  }
  
  protected void checkFieldInfo (FieldInfo fi) {
    if (getClassInfo() != fi.getClassInfo()) {
      throw new JPFException("wrong static FieldInfo : " + fi.getName()
          + " , no such field in class " + getClassInfo().getName());
    }
  }

  @Override
  public int getNumberOfFields () {
    return getClassInfo().getNumberOfStaticFields();
  }
  
  @Override
  public FieldInfo getFieldInfo (int fieldIndex) {
    return getClassInfo().getStaticField(fieldIndex);
  }
    
  /**
   * gc mark all objects stored in static reference fields
   */
  void markStaticRoot (Heap heap) {
    ClassInfo ci = getClassInfo();
    int n = ci.getNumberOfStaticFields();
    
    for (int i=0; i<n; i++) {
      FieldInfo fi = ci.getStaticField(i);
      if (fi.isReference()) {
        int objref = fields.getIntValue(fi.getStorageOffset());
        heap.markStaticRoot(objref);
      }
    }
    
    // don't forget the class object itself (which is not a field)
    heap.markStaticRoot(classObjectRef);
  }
      
  public int getClassObjectRef () {
    return classObjectRef;
  }
  
  public void setClassObjectRef(int r) {
    checkIsModifiable();
    
    classObjectRef = r;
    attributes |= ATTR_COR_CHANGED;
  }

  @Override
  public String toString() {
    return getClassInfo().getName(); // don't append objRef (useless and misleading for statics)
  }

  protected ElementInfo getReferencedElementInfo (FieldInfo fi){
    assert fi.isReference();
    Heap heap = VM.getVM().getHeap();
    return heap.get(getIntField(fi));
  }

}

