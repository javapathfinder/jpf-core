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

/**
 * A specialized version of ElementInfo that represents heap objects
 */
public class DynamicElementInfo extends ElementInfo {
  
  public DynamicElementInfo () {
    // for restoration
  }

  public DynamicElementInfo (int objref, ClassInfo ci, Fields f, Monitor m, ThreadInfo ti) {
    super(objref, ci, f, m, ti);

    attributes = ci.getElementInfoAttrs();

    ti.getScheduler().initializeObjectSharedness(ti, this);
  }

  @Override
  public ElementInfo getModifiableInstance() {
    if (!isFrozen()) {
      return this;
    } else {
      return VM.getVM().getHeap().getModifiable( objRef);
    }
  }
    
  @Override
  public boolean isObject(){
    return true;
  }
  
  @Override
  public boolean hasFinalizer() {
    return (ci.getFinalizer()!=null);
  }
  
  @Override
  protected int getNumberOfFieldsOrElements(){
    if (fields instanceof ArrayFields){
      return ((ArrayFields)fields).arrayLength();
    } else {
      return ci.getNumberOfInstanceFields();
    }
  }

  @Override
  public int getNumberOfFields () {
    return getClassInfo().getNumberOfInstanceFields();
  }

  @Override
  public FieldInfo getFieldInfo (int fieldIndex) {
    return getClassInfo().getInstanceField(fieldIndex);
  }

  @Override
  public FieldInfo getFieldInfo (String fname) {
    return getClassInfo().getInstanceField(fname);
  }
  @Override
  protected FieldInfo getDeclaredFieldInfo (String clsBase, String fname) {
    return getClassInfo().getClassLoaderInfo().getResolvedClassInfo(clsBase).getDeclaredInstanceField(fname);
  }

  @Override
  public ElementInfo getEnclosingElementInfo(){
    for (FieldInfo fi : getClassInfo().getDeclaredInstanceFields()){
      if (fi.getName().startsWith("this$")){
        int objref = getReferenceField(fi);
        return VM.getVM().getElementInfo(objref);
      }
    }
    return null;
  }

  @Override
  public String asString() {
    char[] data = getStringChars();
    if (data != null){
      return new String(data);
      
    } else {
      return "";
    }
  }

  @Override
  public char[] getStringChars(){
    if (!ClassInfo.isStringClassInfo(ci)) {
      throw new JPFException("object is not of type java.lang.String");
    }

    int vref = getDeclaredReferenceField("value", "java.lang.String");    
    if (vref != MJIEnv.NULL){
      ElementInfo eVal = VM.getVM().getHeap().get(vref);
      char[] value = eVal.asCharArray();
      return value;
      
    } else {
      return null;
    }    
  }
  
  /**
   * just a helper to avoid creating objects just for the sake of comparing
   */
  @Override
  public boolean equalsString (String s) {
    if (!ClassInfo.isStringClassInfo(ci)) {
      return false;
    }

    int vref = getDeclaredReferenceField("value", "java.lang.String");
    ElementInfo e = VM.getVM().getHeap().get(vref);
    CharArrayFields cf = (CharArrayFields)e.getFields();
    char[] v = cf.asCharArray();
    
    return new String(v).equals(s);
  }

  @Override
  public boolean isBoxObject(){
    String cname = ci.getName();
    if (cname.startsWith("java.lang.")){
      cname = cname.substring(10);
      return ("Boolean".equals(cname) ||
          "Character".equals(cname) ||
          "Byte".equals(cname) ||
          "Short".equals(cname) ||
          "Integer".equals(cname) ||
          "Float".equals(cname) ||
          "Long".equals(cname) ||
          "Double".equals(cname) );
        
    } else {
      return false;
    }
  }
  
  @Override
  public Object asBoxObject(){
    String cname = ci.getName();
    if (cname.startsWith("java.lang.")){
      cname = cname.substring(10);
      if ("Boolean".equals(cname)){
        return Boolean.valueOf( getBooleanField("value"));
      } else if ("Character".equals(cname)){
        return Character.valueOf(getCharField("value"));
      } else if ("Byte".equals(cname)){
        return Byte.valueOf( getByteField("value"));
      } else if ("Short".equals(cname)){
        return Short.valueOf( getShortField("value"));
      } else if ("Integer".equals(cname)){
        return Integer.valueOf( getIntField("value"));
      } else if ("Float".equals(cname)){
        return Float.valueOf( getFloatField("value"));
      } else if ("Long".equals(cname)){
        return Long.valueOf( getLongField("value"));
      } else if ("Double".equals(cname)){
        return Double.valueOf( getDoubleField("value"));
      }
    }
    
    throw new JPFException("object is not a box object: " + this);    
  }
}
