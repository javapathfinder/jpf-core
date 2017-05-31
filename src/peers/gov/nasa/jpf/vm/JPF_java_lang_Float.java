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
 * MJI NativePeer class for java.lang.Float library abstraction
 */
public class JPF_java_lang_Float extends NativePeer {
  @MJI
  public int floatToIntBits__F__I (MJIEnv env, int rcls, float v0) {
    return Float.floatToIntBits(v0);
  }

  @MJI
  public int floatToRawIntBits__F__I (MJIEnv env, int rcls, float v0) {
    return Float.floatToRawIntBits(v0);
  }

  @MJI
  public float intBitsToFloat__I__F (MJIEnv env, int rcls, int v0) {
    return Float.intBitsToFloat(v0);
  }
  
  // we need to intercept this because it compares double values, which might
  // cause an ArithmeticException to be raised if -check-fp-compare is set (default)
  // but -check-fp isn't, and Double.isInfinit is used to handle the cases
  // explicitly in the program (which is supposed to be the right way)
  @MJI
  public boolean isInfinite__F__Z (MJIEnv env, int rcls, float v) {
    return Float.isInfinite(v);
  }
  
  // ditto (see isInfinite)
  @MJI
  public boolean isNaN__F__Z (MJIEnv env, int rcls, float v) {
    return Float.isNaN(v);
  }
}
