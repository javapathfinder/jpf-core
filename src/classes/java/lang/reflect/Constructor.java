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

/**
 * (incomplete) support for consructor reflection
 * 
 * pretty stupid - this is almost identical to Method, but we can't derive,
 * and the delegation happens at the peer level anyways.
 * 
 * NOTE: 'regIdx' and 'name' need to be like Method, or the peer delegation
 * fails (this is the hack'ish part)
 * 
 * NOTE: we ditch the 'final' modifier so that we can provide our
 * own serialization ctor objects - that's probably going away
 * once we replace ObjectStreamClass
 */
public /*final*/ class Constructor <T> extends AccessibleObject implements Member {
  
  protected int regIdx;
  protected String name;

  @Override
  public native String getName();
  public native T newInstance (Object... args)
        throws IllegalAccessException, InvocationTargetException, InstantiationException;
  
  @Override
  public native int getModifiers();
  public native Class<?> getReturnType();
  public native Class<?>[] getParameterTypes();
  
  @Override
  public native Class<T> getDeclaringClass();
  
  @Override
  public native Annotation[] getAnnotations();
  @Override
  public native Annotation[] getDeclaredAnnotations();
  @Override
  public native <T extends Annotation> T getAnnotation( Class<T> annotationCls);
  public native Annotation[][] getParameterAnnotations();
  
  @Override
  public boolean isSynthetic () {
    return false;
  }
  
  @Override
  public native String toString();
  
  @Override
  public native boolean equals (Object obj);

  public boolean isVarArgs (){
    return (getModifiers() & Modifier.VARARGS) != 0;
  }

  @Override
  public native int hashCode ();

  public native String toGenericString ();
}
