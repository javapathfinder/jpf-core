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
package gov.nasa.jpf.util;

import gov.nasa.jpf.JPFException;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

/**
 * reflection utilities
 */
public class Reflection {

  /**
   * find callers class
   *
   * @param up levels upwards from our caller (NOT counting ourselves)
   * @return caller class, null if illegal 'up' value
   */
  public static Class<?> getCallerClass(int up) {
    int idx = up + 1; // don't count this stackframe

    StackTraceElement[] st = (new Throwable()).getStackTrace();
    if ((up < 0) || (idx >= st.length)) {
      return null;
    } else {
      try {
        return Class.forName(st[idx].getClassName());
      } catch (Throwable t) {
        return null;
      }
    }
  }

  public static Class<?> getCallerClass () {
    return getCallerClass(2);
  }

  public static <T> Class<? extends T>  getCallerClass (Class<T> type){
    Class<?> cls = getCallerClass(2);

    if (cls != null) {
      if (type.isAssignableFrom(cls)) {
        return cls.asSubclass(type);
      } else {
        throw new JPFException("caller class: " + cls.getName() + " not of type: " + type.getName());
      }
    }
    return null;
  }

  public static StackTraceElement getCallerElement (int up){
    int idx = up + 1; // don't count this stackframe

    StackTraceElement[] st = (new Throwable()).getStackTrace();
    if ((up < 0) || (idx >= st.length)) {
      return null;
    } else {
      return st[idx];
    }
  }
  public static StackTraceElement getCallerElement () {
    StackTraceElement[] st = (new Throwable()).getStackTrace();
    if (st.length > 2){
      return st[2]; // '0' is this method itself
    } else {
      return null;
    }
  }

  public static boolean tryCallMain(Class<?> cls, String[] args) throws InvocationTargetException {
    try {
      Method method = cls.getDeclaredMethod("main", String[].class);
      int modifiers = method.getModifiers();

      if ((modifiers & (Modifier.PUBLIC | Modifier.STATIC | Modifier.ABSTRACT)) == (Modifier.PUBLIC | Modifier.STATIC)) {
        method.invoke(null, (Object)args);
        return true;
      }

    } catch (NoSuchMethodException nsmx) {
      //System.out.println(nsmx);
      // just return false
    } catch (IllegalAccessException iax){
      //System.out.println(iax);
      // can't happen, we checked for it before invoking
    } catch (IllegalArgumentException iargx){
      //System.out.println(iargx);
      // can't happen, we checked for it before invoking
    }

    return false;
  }

}
