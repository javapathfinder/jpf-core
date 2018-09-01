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

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.Types;

/**
 * MJI NativePeer class for java.lang.reflect.Array library abstraction
 */
public class JPF_java_lang_reflect_Array extends NativePeer {
  @MJI
  public int getLength__Ljava_lang_Object_2__I (MJIEnv env, int clsObjRef, 
                                                    int objRef) {
    if (objRef == MJIEnv.NULL) {
      env.throwException("java.lang.NullPointerException", "array argument is null");
      return 0;
    }
    if (!env.isArray(objRef)) {
      env.throwException("java.lang.IllegalArgumentException", "argument is not an array");
      return 0;
    }

    return env.getArrayLength(objRef);
  }
  
  @MJI
  public int newArray__Ljava_lang_Class_2I__Ljava_lang_Object_2 (MJIEnv env, int clsRef,
                                                                        int componentTypeRef, int length) {
    ClassInfo ci = env.getReferredClassInfo(componentTypeRef);
    String clsName = ci.getName();
    
    return createNewArray( env, clsName, length);
  }
  
  static int createNewArray (MJIEnv env, String clsName, int length) {
    int aRef = MJIEnv.NULL;
    
    if ("boolean".equals(clsName)) { aRef = env.newBooleanArray(length); }
    else if ("byte".equals(clsName)) { aRef = env.newByteArray(length); }
    else if ("char".equals(clsName)) { aRef = env.newCharArray(length); }
    else if ("short".equals(clsName)) { aRef = env.newShortArray(length); }
    else if ("int".equals(clsName)) { aRef = env.newIntArray(length); }
    else if ("long".equals(clsName)) { aRef = env.newLongArray(length); }
    else if ("float".equals(clsName)) { aRef = env.newFloatArray(length); }
    else if ("double".equals(clsName)) { aRef = env.newDoubleArray(length); }
    else { aRef = env.newObjectArray(clsName, length); }
    return aRef;    
  }
  
  @MJI
  public int multiNewArray__Ljava_lang_Class_2_3I__Ljava_lang_Object_2 (MJIEnv env, int clsRef,
                                                                               int componentTypeRef,
                                                                               int dimArrayRef) {
    ClassInfo ci = env.getReferredClassInfo(componentTypeRef);
    String clsName = ci.getName();
    int n = env.getArrayLength(dimArrayRef);
    int i;

    clsName = Types.getTypeSignature(clsName, true);
    
    String arrayType = "[";
    for (i=2; i<n; i++) arrayType += '[';
    arrayType += clsName;
    
    int[] dim = new int[n];
    for (i=0; i<n; i++) {
      dim[i] = env.getIntArrayElement(dimArrayRef, i);
    }
    
    int aRef = createNewMultiArray(env, arrayType, dim, 0); 
    return aRef;
  }
  
  static int createNewMultiArray (MJIEnv env, String arrayType, int[] dim, int level) {
    int aRef = MJIEnv.NULL;
    int len = dim[level];
    
    if (level < dim.length-1) {
      aRef = env.newObjectArray(arrayType, len);
    
      for (int i=0; i<len; i++) {
        int eRef = createNewMultiArray(env, arrayType.substring(1), dim, level+1);
        env.setReferenceArrayElement(aRef, i, eRef);
      }
    } else {
      aRef = createNewArray( env, arrayType, len);
    }
    
    return aRef;
  }

  @MJI
  public int get__Ljava_lang_Object_2I__Ljava_lang_Object_2 (MJIEnv env, int clsRef,
                                                                    int aref, int index){
    String at = Types.getTypeName(env.getArrayType(aref));
    if (at.equals("int")){
      int vref = env.newObject("java.lang.Integer");
      env.setIntField(vref, "value", env.getIntArrayElement(aref,index));
      return vref;
      
    } else if (at.equals("long")){
      int vref = env.newObject("java.lang.Long");
      env.setLongField(vref, "value", env.getLongArrayElement(aref,index));
      return vref;
      
    } else if (at.equals("double")){
      int vref = env.newObject("java.lang.Double");
      env.setDoubleField(vref, "value", env.getDoubleArrayElement(aref,index));
      return vref;
      
    } else if (at.equals("boolean")){
      int vref = env.newObject("java.lang.Boolean");
      env.setBooleanField(vref, "value", env.getBooleanArrayElement(aref,index));
      return vref;
      
    } else if (at.equals("char")){
      int vref = env.newObject("java.lang.Character");
      env.setCharField(vref, "value", env.getCharArrayElement(aref,index));
      return vref;
      
    } else if (at.equals("byte")){
      int vref = env.newObject("java.lang.Byte");
      env.setByteField(vref, "value", env.getByteArrayElement(aref,index));
      return vref;
      
    } else if (at.equals("short")){
      int vref = env.newObject("java.lang.Short");
      env.setShortField(vref, "value", env.getShortArrayElement(aref,index));
      return vref;

    } else if (at.equals("float")){
      int vref = env.newObject("java.lang.Float");
      env.setFloatField(vref, "value", env.getFloatArrayElement(aref,index));
      return vref;

    } else {
      return env.getReferenceArrayElement(aref, index);
    }
  }

  private static boolean check (MJIEnv env, int aref, int index) {
    if (aref == MJIEnv.NULL) {
      env.throwException("java.lang.NullPointerException", "array argument is null");
      return false;
    }
    if (!env.isArray(aref)) {
      env.throwException("java.lang.IllegalArgumentException", "argument is not an array");
      return false;
    }
    if (index < 0 || index >= env.getArrayLength(aref)) {
      env.throwException("java.lang.IndexOutOfBoundsException", "index " + index + " is out of bounds");
      return false;
    }
    return true;
  }

  @MJI
  public boolean getBoolean__Ljava_lang_Object_2I__Z (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getBooleanArrayElement(aref, index);
    }
    return false;
  }

  @MJI
  public static byte getByte__Ljava_lang_Object_2I__B (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getByteArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public char getChar__Ljava_lang_Object_2I__C (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getCharArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public short getShort__Ljava_lang_Object_2I__S (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getShortArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public int getInt__Ljava_lang_Object_2I__I (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getIntArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public long getLong__Ljava_lang_Object_2I__J (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getLongArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public float getFloat__Ljava_lang_Object_2I__F (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getFloatArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public double getDouble__Ljava_lang_Object_2I__D (MJIEnv env, int clsRef, int aref, int index) {
    if (check(env, aref, index)) {
      return env.getDoubleArrayElement(aref, index);
    }
    return 0;
  }

  @MJI
  public void setBoolean__Ljava_lang_Object_2IZ__V (MJIEnv env, int clsRef, int aref, int index, boolean val) {
    if (check(env, aref, index)) {
      env.setBooleanArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setByte__Ljava_lang_Object_2IB__V (MJIEnv env, int clsRef, int aref, int index, byte val) {
    if (check(env, aref, index)) {
      env.setByteArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setChar__Ljava_lang_Object_2IC__V (MJIEnv env, int clsRef, int aref, int index, char val) {
    if (check(env, aref, index)) {
      env.setCharArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setShort__Ljava_lang_Object_2IS__V (MJIEnv env, int clsRef, int aref, int index, short val) {
    if (check(env, aref, index)) {
      env.setShortArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setInt__Ljava_lang_Object_2II__V (MJIEnv env, int clsRef, int aref, int index, int val) {
    if (check(env, aref, index)) {
      env.setIntArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setLong__Ljava_lang_Object_2IJ__V (MJIEnv env, int clsRef, int aref, int index, long val) {
    if (check(env, aref, index)) {
      env.setLongArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setFloat__Ljava_lang_Object_2IF__V (MJIEnv env, int clsRef, int aref, int index, float val) {
    if (check(env, aref, index)) {
      env.setFloatArrayElement(aref, index, val);
    }
  }

  @MJI
  public void setDouble__Ljava_lang_Object_2ID__V (MJIEnv env, int clsRef, int aref, int index, double val) {
    if (check(env, aref, index)) {
      env.setDoubleArrayElement(aref, index, val);
    }
  }
}
