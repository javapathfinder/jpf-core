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
package sun.misc;

import java.lang.reflect.Field;

/**
 * Unsafe = unwanted. See comments in the native peer. We only have it because
 * it is required by java.util.concurrent.
 *
 * Note that in the real world, this class is only callable from the system library,
 * not application code
 */

public class Unsafe {
  private static final Unsafe theUnsafe = new Unsafe();

  public static final int INVALID_FIELD_OFFSET = -1;

  public static Unsafe getUnsafe() {
    return theUnsafe;
    //return new Unsafe();
  }

  // field offsets are completely useless between VMs, we just return
  // a numeric id for the corresponding FieldInfo here
  public native int fieldOffset (Field f);
  public native long objectFieldOffset (Field f);

  // those do the usual CAS magic
  public native boolean compareAndSwapObject (Object oThis, long offset, Object expect, Object update);
  public native boolean compareAndSwapInt (Object oThis, long offset, int expect, int update);
  public native boolean compareAndSwapLong (Object oThis, long offset, long expect, long update);

  // that looks like some atomic conditional wait
  public native void park (boolean isAbsolute, long timeout);
  public native void unpark (Object thread);

  // various accessors
  public native int getInt(Object obj, long l);
  public native int getIntVolatile(Object obj, long l);

  @Deprecated
  public int getInt(Object obj, int offset) {
    return getInt(obj, (long) offset);
  }

  public native void putInt(Object obj, long l, int i);
  public native void putIntVolatile(Object obj, long l, int i);

  @Deprecated
  public void putInt(Object obj, int offset, int i) {
    putInt(obj, (long) offset, i);
  }

  public native void putOrderedInt(Object obj, long l, int i);

  public native Object getObject(Object obj, long l);
  public native Object getObjectVolatile(Object obj, long l);

  @Deprecated
  public Object getObject(Object obj, int offset) {
    return getObject(obj, (long) offset);
  }

  public native void putObject(Object obj, long l, Object obj1);
  public native void putObjectVolatile(Object obj, long l, Object obj1);
  

  @Deprecated
  public void putObject(Object obj, int offset, Object obj1) {
    putObject(obj, (long) offset, obj1);
  }

  public native void putOrderedObject(Object obj, long l, Object obj1);

  public native boolean getBoolean(Object obj, long l);
  public native boolean getBooleanVolatile(Object obj, long l);

  @Deprecated
  public boolean getBoolean(Object obj, int offset) {
    return getBoolean(obj, (long) offset);
  }

  public native void putBoolean(Object obj, long l, boolean flag);
  public native void putBooleanVolatile(Object obj, long l, boolean flag);

  @Deprecated
  public void putBoolean(Object obj, int offset, boolean flag) {
    putBoolean(obj, (long) offset, flag);
  }

  public native byte getByte(Object obj, long l);
  public native byte getByteVolatile(Object obj, long l);

  @Deprecated
  public byte getByte(Object obj, int offset) {
    return getByte(obj, (long) offset);
  }

  public native void putByte(Object obj, long l, byte byte0);
  public native void putByteVolatile(Object obj, long l, byte byte0);

  @Deprecated
  public void putByte(Object obj, int offset, byte byte0) {
    putByte(obj, (long) offset, byte0);
  }

  public native short getShort(Object obj, long l);
  public native short getShortVolatile(Object obj, long l);

  @Deprecated
  public short getShort(Object obj, int offset) {
    return getShort(obj, (long) offset);
  }

  public native void putShort(Object obj, long l, short word0);
  public native void putShortVolatile(Object obj, long l, short word0);

  @Deprecated
  public void putShort(Object obj, int offset, short word0) {
    putShort(obj, (long) offset, word0);
  }

  public native char getChar(Object obj, long l);
  public native char getCharVolatile(Object obj, long l);

  @Deprecated
  public char getChar(Object obj, int offset) {
    return getChar(obj, (long) offset);
  }

  public native void putChar(Object obj, long l, char c);
  public native void putCharVolatile(Object obj, long l, char c);

  @Deprecated
  public void putChar(Object obj, int offset, char c) {
    putChar(obj, (long) offset, c);
  }

  public native long getLong(Object obj, long l);
  public native long getLongVolatile(Object obj, long l);

  @Deprecated
  public long getLong(Object obj, int offset) {
    return getLong(obj, (long) offset);
  }

  public native void putLong(Object obj, long l, long l1);
  public native void putLongVolatile(Object obj, long l, long l1);

  public native void putOrderedLong(Object obj, long l, long l1);

  @Deprecated
  public void putLong(Object obj, int offset, long l1) {
    putLong(obj, (long) offset, l1);
  }

  public native float getFloat(Object obj, long l);
  public native float getFloatVolatile(Object obj, long l);

  @Deprecated
  public float getFloat(Object obj, int offset) {
    return getFloat(obj, (long) offset);
  }

  public native void putFloat(Object obj, long l, float f);
  public native void putFloatVolatile(Object obj, long l, float f);

  @Deprecated
  public void putFloat(Object obj, int offset, float f) {
    putFloat(obj, (long) offset, f);
  }

  public native double getDouble(Object obj, long l);
  public native double getDoubleVolatile(Object obj, long l);

  @Deprecated
  public double getDouble(Object obj, int offset) {
    return getDouble(obj, (long) offset);
  }

  public native void putDouble(Object obj, long l, double d);
  public native void putDoubleVolatile(Object obj, long l, double d);

  @Deprecated
  public void putDouble(Object obj, int offset, double d) {
    putDouble(obj, (long) offset, d);
  }

  public native void ensureClassInitialized(Class<?> cls);

  public native int arrayBaseOffset(Class<?> clazz);

  public native int arrayIndexScale(Class<?> clazz);
  
  
  //--- java.nio finally breaks object boundaries  - hello, evil pointer arithmetic
  
  /**
   * this is really a byte[] allocation (used by java.nio.Bits). Note that
   * object has to be explicitly freed with freeMemory() (yikes!)
   */
  public native long allocateMemory (long bytes);
  
  /**
   * to be used to free allocateMemory() allocated array objects
   */
  public native void freeMemory (long byteArrayRef);
    
  /**
   * byte access of allocateMemory() objects. Note that 'address' has
   * to point into such an object
   */
  public native byte getByte (long address);
  public native void putByte (long address, byte val);
  
  public native char getChar (long address);
  public native void putChar (long address, char val);
  
  public native int getInt (long address);
  public native void putInt (long address, int val);
  
  public native long getLong (long address);
  public native void putLong (long address, long val);

  public native float getFloat (long address);
  public native void putFloat (long address, float val);

  public native double getDouble (long address);
  public native void putDouble (long address, double val);
  
}
