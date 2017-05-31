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
 * native peer for java.util.concurrent.atomic.AtomicLongArray
 */
public class JPF_java_util_concurrent_atomic_AtomicLongArray extends NativePeer {

  @MJI
  public long getNative__I__J (MJIEnv env, int objRef, int index) {
    int arrayRef = env.getReferenceField(objRef, "array");
    return env.getLongArrayElement(arrayRef, index);
  }

  @MJI
  public boolean compareAndSetNative__IJJ__Z (MJIEnv env, int objRef, int index, long expect, long update) {
    int arrayRef = env.getReferenceField(objRef, "array");
    long value = env.getLongArrayElement(arrayRef, index);
    if (value == expect) {
      env.setLongArrayElement(arrayRef, index, update);
      return true;
    } else {
      return false;
    }
  }
}
