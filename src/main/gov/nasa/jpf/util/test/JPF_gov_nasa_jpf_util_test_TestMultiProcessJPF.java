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

package gov.nasa.jpf.util.test;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * This is a native peer for multiprocess test class root
 */
public class JPF_gov_nasa_jpf_util_test_TestMultiProcessJPF 
  extends JPF_gov_nasa_jpf_util_test_TestJPF {

  @MJI
  public int getProcessId____I (MJIEnv env, int objRef) {
    return ThreadInfo.getCurrentThread().getApplicationContext().getId();
  }

  @MJI
  public static boolean mpVerifyAssertionErrorDetails__ILjava_lang_String_2_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int rString1, int rString2) {
    return true;
  }

  @MJI
  public static boolean mpVerifyAssertionError__I_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int argsRef) {
    return true;
  }

  @MJI
  public static boolean mpVerifyNoPropertyViolation__I_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int argsRef) {
    return true;
  }

  @MJI
  public static boolean mpVerifyUnhandledExceptionDetails__ILjava_lang_String_2Ljava_lang_String_2_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int clsRef, int details, int argsRef) {
    return true;
  }

  @MJI
  public static boolean mpVerifyUnhandledException__ILjava_lang_String_2_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int clsRef, int argsRef) {
    return true;
  }

  @MJI
  public static boolean mpVerifyJPFException__ILgov_nasa_jpf_util_TypeRef_2_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int typeRef, int argsRef) {
    return true;
  }

  @MJI
  public static boolean mpVerifyPropertyViolation__ILgov_nasa_jpf_util_TypeRef_2_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int typeRef, int argsRef) {
    return true;
  }

  @MJI
  public static boolean mpVerifyDeadlock__I_3Ljava_lang_String_2__Z (MJIEnv env, int objRef, int numOfPrc, int argsRef) {
    return true;
  }
}
