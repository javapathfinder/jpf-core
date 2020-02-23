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

/**
* native peer for java.util.concurrent.atomic.AtomicReference
* this implementation just cuts off native methods
*/
public class JPF_java_util_concurrent_atomic_AtomicReference extends NativePeer {

  @MJI
  public void $clinit____V (MJIEnv env, int rcls) {
    // don't let this one pass, it calls native methods from non-public Sun classes
  }

  @MJI
  public int getAndSet__Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int objRef, int newValue) {
    int value = env.getReferenceField(objRef, "value");
    env.setReferenceField(objRef, "value", newValue);
    return value;
  }


  @MJI
  public boolean compareAndSet__Ljava_lang_Object_2Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int expect, int update){
    int value = env.getReferenceField(objRef, "value");
    if (value == expect){
      env.setReferenceField(objRef, "value", update);
      return true;
    } else {
      return false;
    }
  }
}
