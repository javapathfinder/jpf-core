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
package java.lang.reflect;

import java.lang.annotation.Annotation;

public final class Field extends AccessibleObject implements Member {
  int regIdx; // the link to the corresponding FieldInfo
  String name; // deferred set by the NativePeer getName()

  public String toGenericString() {
	  // TODO: return real generic string
	  return toString();
  }
  
  public native boolean getBoolean (Object o) throws IllegalAccessException;
  public native void setBoolean (Object o, boolean v) throws IllegalAccessException;

  public native byte getByte (Object o) throws IllegalAccessException;
  public native void setByte (Object o, byte v) throws IllegalAccessException;

  public native short getShort (Object o) throws IllegalAccessException;
  public native void setShort (Object o, short v) throws IllegalAccessException;
  
  public native char getChar (Object o) throws IllegalAccessException;
  public native void setChar (Object o, char v) throws IllegalAccessException;

  public native int getInt (Object o) throws IllegalAccessException;
  public native void setInt (Object o, int val) throws IllegalAccessException;

  public native long getLong (Object o) throws IllegalAccessException;
  public native void setLong (Object o, long v) throws IllegalAccessException;

  public native float getFloat (Object o) throws IllegalAccessException;
  public native void setFloat (Object o, float v) throws IllegalAccessException;

  public native double getDouble (Object o) throws IllegalAccessException;
  public native void setDouble (Object o, double v) throws IllegalAccessException;

  public native Class<?> getType ();
  
  public native Object get (Object o) throws IllegalAccessException;
    
  public native void set (Object o, Object v) throws IllegalArgumentException, IllegalAccessException;
  // the member interface
  @Override
  public native String getName();
  
  @Override
  public native int getModifiers();
  
  @Override
  public native Annotation[] getAnnotations();

  @Override
  public native <T extends Annotation> T getAnnotation( Class<T> annotationCls);
  
  @Override
  public native Class<?> getDeclaringClass ();
  
  @Override
  public native boolean isSynthetic ();

  @Override
  public native boolean equals (Object obj);

  @Override
  public native String toString ();

  public boolean isEnumConstant (){
    return (getModifiers() & Modifier.ENUM) != 0;
  }

  @Override
  public native int hashCode ();

  @Override
  public native Annotation[] getDeclaredAnnotations ();
}
