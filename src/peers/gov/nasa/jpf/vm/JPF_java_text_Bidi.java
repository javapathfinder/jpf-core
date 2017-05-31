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

public class JPF_java_text_Bidi extends NativePeer {

  @MJI
  public void $clinit____V (MJIEnv env, int clsObjRef) {
    // do nothing
  }
  
  @MJI
  public void nativeBidiChars (MJIEnv env, int clsObjRef,
                                      int bidiRef, int textRef, int textStart,
                                      int embeddingsRef, int embeddingsStart,
                                      int length, int flags) {
    // <2do> need to forward
  }

  @MJI
  public boolean requiresBidi (MJIEnv env, int clsObjRef,
                                      int textRef, int start, int limit) {
    // not supported for now
    return false;
  }
}
