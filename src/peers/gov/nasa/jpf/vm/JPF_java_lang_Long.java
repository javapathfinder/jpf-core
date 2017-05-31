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
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

/**
 * MJI NativePeer class for java.lang.Long library abstraction
 */
public class JPF_java_lang_Long extends NativePeer {
  // <2do> at this point we deliberately do not override clinit

  @MJI
  public long parseLong__Ljava_lang_String_2I__J (MJIEnv env, int clsObjRef, 
                                                     int strRef, int radix) {
    try {
      return Long.parseLong(env.getStringObject(strRef), radix);
    } catch (NumberFormatException e) {
      env.throwException("java.lang.NumberFormatException");

      return 0;
    }
  }

  @MJI
  public long parseLong__Ljava_lang_String_2__J (MJIEnv env, int clsObjRef, 
                                                     int strRef) {
    try {
      return Long.parseLong(env.getStringObject(strRef));
    } catch (NumberFormatException e) {
      env.throwException("java.lang.NumberFormatException");

      return 0;
    }
  }

  @MJI
  public int toBinaryString__J__Ljava_lang_String_2 (MJIEnv env, int objref, long val) {
    return env.newString(Long.toBinaryString(val));
  }

  @MJI
  public int toHexString__J__Ljava_lang_String_2 (MJIEnv env, int objref, long val) {
    return env.newString(Long.toHexString(val));
  }

  @MJI
  public int toOctalString__J__Ljava_lang_String_2 (MJIEnv env, int objref, long val) {
    return env.newString(Long.toOctalString(val));
  }

  @MJI
  public int toString__J__Ljava_lang_String_2 (MJIEnv env, int objref, long val) {
    return env.newString(Long.toString(val));
  }

  @MJI
  public int toString__JI__Ljava_lang_String_2 (MJIEnv env, int objref, long val, int radix) {
    return env.newString(Long.toString(val, radix));
  }

  @MJI
  public int valueOf__J__Ljava_lang_Long_2 (MJIEnv env, int clsRef, long val) {
    return env.valueOfLong(val);
  }
}
