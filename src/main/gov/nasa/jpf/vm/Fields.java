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

import gov.nasa.jpf.util.HashData;
import gov.nasa.jpf.util.IntVector;
import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.ObjectList;


/**
 * Represents the variable, hash-collapsed pooled data associated with an object
 * that is related to the object values (as opposed to synchronization ->Monitor).
 * Contains the values of the fields, not their descriptors.  Descriptors are represented
 * by gov.nasa.jpf.vm.FieldInfo objects, which are stored in the ClassInfo structure.
 *
 * @see gov.nasa.jpf.vm.FieldInfo
 * @see gov.nasa.jpf.vm.Monitor
 */
public abstract class Fields implements Cloneable {

  /**
   * we use this to store arbitrary field attributes (like symbolic values),
   * but only create this on demand
   */
  protected Object[] fieldAttrs;

  /**
   * attribute attached to the object as a whole
   */
  protected Object objectAttr;


  protected Fields() {}

  public boolean hasFieldAttr() {
    return fieldAttrs != null;
  }

  public boolean hasFieldAttr (Class<?> attrType){    
    Object[] fa = fieldAttrs;
    if (fa != null){
      for (int i=0; i<fa.length; i++){
        Object a = fa[i];
        if (a != null && ObjectList.containsType(a, attrType)){
          return true;
        }
      }
    }
    return false;
  }


  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */
  public Object getFieldAttr(int fieldOrElementIndex){
    if (fieldAttrs != null){
      return fieldAttrs[fieldOrElementIndex];
    }
    return null;
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  public void setFieldAttr (int nFieldsOrElements, int fieldOrElementIndex, Object attr){
    if (fieldAttrs == null){
      if (attr == null){
        return; // no need to waste an array object for storing null
      }
      fieldAttrs = new Object[nFieldsOrElements];
    }
    fieldAttrs[fieldOrElementIndex] = attr;
  }
  
  public void addFieldAttr (int nFieldsOrElements, int fieldOrElementIndex, Object attr){
    if (attr != null){
      if (fieldAttrs == null) {
        fieldAttrs = new Object[nFieldsOrElements];
      }
      fieldAttrs[fieldOrElementIndex] = ObjectList.add(fieldAttrs[fieldOrElementIndex], attr);
    }
  }
  
  public void removeFieldAttr (int fieldOrElementIndex, Object attr){
    if (fieldAttrs != null){
     fieldAttrs[fieldOrElementIndex] = ObjectList.remove(fieldAttrs[fieldOrElementIndex], attr);
    }
  }
  
  public void replaceFieldAttr (int fieldOrElementIndex, Object oldAttr, Object newAttr){
    if (fieldAttrs != null){
     fieldAttrs[fieldOrElementIndex] = ObjectList.replace(fieldAttrs[fieldOrElementIndex], oldAttr, newAttr);
    }
  }
  
  public boolean hasFieldAttr (int fieldOrElementIndex, Class<?> type){
    if (fieldAttrs != null){
     return ObjectList.containsType(fieldAttrs[fieldOrElementIndex], type);
    }
    return false;
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getFieldAttr (int fieldOrElementIndex, Class<T> type){
    if (fieldAttrs != null){
     return ObjectList.getFirst(fieldAttrs[fieldOrElementIndex], type);
    }
    return null;    
  }
  
  public <T> T getNextFieldAttr (int fieldOrElementIndex, Class<T> type, Object prev){
    if (fieldAttrs != null){
     return ObjectList.getNext(fieldAttrs[fieldOrElementIndex], type, prev);
    }
    return null;    
  }
  
  public ObjectList.Iterator fieldAttrIterator(int fieldOrElementIndex){
    Object a = (fieldAttrs != null) ? fieldAttrs[fieldOrElementIndex] : null;
    return ObjectList.iterator(a);
  }
  
  public <T> ObjectList.TypedIterator<T> fieldAttrIterator(int fieldOrElementIndex, Class<T> type){
    Object a = (fieldAttrs != null) ? fieldAttrs[fieldOrElementIndex] : null;
    return ObjectList.typedIterator(a, type);
  }
  

  //--- object attributes
  public boolean hasObjectAttr () {
    return (objectAttr != null);
  }

  public boolean hasObjectAttr (Class<?> attrType){
    return ObjectList.containsType(objectAttr, attrType);
  }

  /**
   * this returns all of them - use either if you know there will be only
   * one attribute at a time, or check/process result with ObjectList
   */
  public Object getObjectAttr(){
    return objectAttr;
  }

  /**
   * this replaces all of them - use only if you know 
   *  - there will be only one attribute at a time
   *  - you obtained the value you set by a previous getXAttr()
   *  - you constructed a multi value list with ObjectList.createList()
   */
  public void setObjectAttr (Object attr){
    objectAttr = attr;    
  }

  public void addObjectAttr (Object attr){
    objectAttr = ObjectList.add(objectAttr, attr);
  }

  public void removeObjectAttr (Object attr){
    objectAttr = ObjectList.remove(objectAttr, attr);
  }

  public void replaceObjectAttr (Object oldAttr, Object newAttr){
    objectAttr = ObjectList.replace(objectAttr, oldAttr, newAttr);
  }

  /**
   * this only returns the first attr of this type, there can be more
   * if you don't use client private types or the provided type is too general
   */
  public <T> T getObjectAttr (Class<T> attrType) {
    return ObjectList.getFirst(objectAttr, attrType);
  }

  public <T> T getNextObjectAttr (Class<T> attrType, Object prev) {
    return ObjectList.getNext(objectAttr, attrType, prev);
  }

  public ObjectList.Iterator objectAttrIterator(){
    return ObjectList.iterator(objectAttr);
  }
  
  public <T> ObjectList.TypedIterator<T> objectAttrIterator(Class<T> attrType){
    return ObjectList.typedIterator(objectAttr, attrType);
  }


  public abstract int[] asFieldSlots();

  /**
   * give an approximation of the heap size in bytes - we assume fields are word
   * aligned, hence the number of values*4 should be good. Note that this is
   * overridden by ArrayFields (arrays would be packed)
   */
  public abstract int getHeapSize ();


  public boolean isReferenceArray () {
    return false;
  }

  // our low level getters and setters
  public abstract int getIntValue (int index);

  // same as getIntValue(), just here to make intentions clear
  public abstract int getReferenceValue (int index);

  public abstract long getLongValue (int index);

  public abstract boolean getBooleanValue (int index);

  public abstract byte getByteValue (int index);

  public abstract char getCharValue (int index);

  public abstract short getShortValue (int index);

  public abstract float getFloatValue (int index);

  public abstract double getDoubleValue (int index);

  //--- the field modifier methods (both instance and static)

  public abstract void setReferenceValue (int index, int newValue);

  public abstract void setBooleanValue (int index, boolean newValue);

  public abstract void setByteValue (int index, byte newValue);

  public abstract void setCharValue (int index, char newValue);

  public abstract void setShortValue (int index, short newValue);

  public abstract void setFloatValue (int index, float newValue);

  public abstract void setIntValue (int index, int newValue);

  public abstract void setLongValue (int index, long newValue);

  public abstract void setDoubleValue (int index, double newValue);

  @Override
  public abstract Fields clone ();

  protected Fields cloneFields() {
    try {
      Fields f = (Fields)super.clone();

      if (fieldAttrs != null) {
        f.fieldAttrs = fieldAttrs.clone();
      }

      if (objectAttr != null) {
        f.objectAttr = objectAttr; //
      }

      return f;
    } catch (CloneNotSupportedException cnsx){
      return null;
    }
  }

  @Override
  public abstract boolean equals(Object o);

  // <2do> not multi-attr aware yet
  protected boolean compareAttrs(Fields f) {
    if (fieldAttrs != null || f.fieldAttrs != null) {
      if (!Misc.equals(fieldAttrs, f.fieldAttrs)) {
        return false;
      }
    }

    if (!ObjectList.equals(objectAttr, f.objectAttr)){
      return false;
    }

    return true;
  }

  // serialization interface
  public abstract void appendTo(IntVector v);


  @Override
  public int hashCode () {
    HashData hd = new HashData();
    hash(hd);
    hashAttrs(hd);
    return hd.getValue();
  }

  public abstract void hash(HashData hd);

  /**
   * Adds some data to the computation of an hashcode.
   */
  public void hashAttrs (HashData hd) {

    // it's debatable if we add the attributes to the state, but whatever it
    // is, it should be kept consistent with the StackFrame.hash()
    Object[] a = fieldAttrs;
    if (a != null) {
      for (int i=0, l=a.length; i < l; i++) {
        ObjectList.hash(a[i], hd);
      }
    }

    if (objectAttr != null){
      ObjectList.hash(objectAttr, hd);
    }
  }


  // <2do> not multi-attr aware yet
  public void copyAttrs(Fields other) {
    if (other.fieldAttrs != null){
      if (fieldAttrs == null){
        fieldAttrs = other.fieldAttrs.clone();
      } else {
        System.arraycopy(other.fieldAttrs, 0, fieldAttrs, 0, fieldAttrs.length);
      }
    }

    objectAttr = other.objectAttr;
  }
}
