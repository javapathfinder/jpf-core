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
/**
 * JPF_java_lang_Shortjava.java
 *
 * @author Created by Omnicore CodeGuide
 */
package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

/**
 * MJI NativePeer class for java.lang.Short library abstraction
 */
public class JPF_java_lang_Short extends NativePeer {
  // <2do> at this point we deliberately do not override clinit

  @MJI
  public short parseShort__Ljava_lang_String_2__S (MJIEnv env, 
                                                          int clsObjRef, 
                                                          int strRef) {
    try {
      return Short.parseShort(env.getStringObject(strRef));
    } catch (NumberFormatException e) {
      env.throwException("java.lang.NumberFormatException");

      return 0;
    }
  }

  @MJI
  public short parseShort__Ljava_lang_String_2I__S (MJIEnv env, 
                                                            int clsObjRef, 
                                                            int strRef, int radix) {
    try {
      return Short.parseShort(env.getStringObject(strRef), radix);
    } catch (NumberFormatException e) {
      env.throwException("java.lang.NumberFormatException");

      return 0;
    }
  }

  @MJI
  public int toString__S__Ljava_lang_String_2 (MJIEnv env, int objref, short val) {
    return env.newString(Short.toString(val));
  }

  @MJI
  public int valueOf__S__Ljava_lang_Short_2 (MJIEnv env, int clsRef, short val) {
    return env.valueOfShort(val);
  }
}
