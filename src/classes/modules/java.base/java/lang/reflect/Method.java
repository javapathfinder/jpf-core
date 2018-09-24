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
 * minimal Method reflection support.
 * Note that we share peer code between Method and Constructor (which aren't
 * really different on the JPF side), so don't change field names!
 */
public final class Method extends AccessibleObject implements Member {
  int regIdx; // the link to the corresponding MethodInfo
  String name; // deferred set by the NativePeer getName()

  @Override
  public native String getName();
  public String toGenericString() {
	  // TODO: return real generic string
	  return toString();
  }
  public native Object invoke (Object object, Object... args)
        throws IllegalAccessException, InvocationTargetException;

  @Override
  public native int getModifiers();
  public native Class<?> getReturnType();
  public native Class<?>[] getParameterTypes();
  public native Type[] getGenericParameterTypes();
  public native Class<?>[] getExceptionTypes();

  @Override
  public native Class<?> getDeclaringClass();

  @Override
  public native Annotation[] getAnnotations();
  @Override
  public native Annotation[] getDeclaredAnnotations();
  @Override
  public native <T extends Annotation> T getAnnotation( Class<T> annotationCls);
  public native Annotation[][] getParameterAnnotations();

  @Override
  public boolean isSynthetic (){
    return Modifier.isSynthetic(getModifiers());
  }

  @Override
  public native String toString();

  // for Annotations - return the default value of the annotation member
  // represented by this method
  public native Object getDefaultValue();

  @Override
  public native boolean equals (Object obj);

  public boolean isVarArgs (){
    return (getModifiers() & Modifier.VARARGS) != 0;
  }

  @Override
  public native int hashCode ();

  public boolean isBridge (){
    return (getModifiers() & Modifier.BRIDGE) != 0;
  }
}
