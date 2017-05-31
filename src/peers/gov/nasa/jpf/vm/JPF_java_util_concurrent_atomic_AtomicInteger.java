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
* native peer for java.util.concurrent.atomic.AtomicInteger
* this implementation just cuts off native methods
*/
public class JPF_java_util_concurrent_atomic_AtomicInteger extends NativePeer {

  @MJI
  public void $clinit____V (MJIEnv env, int rcls) {
    // don't let this one pass, it calls native methods from non-public Sun classes
  }
 
  @MJI
  public boolean compareAndSet__II__Z (MJIEnv env, int objRef, int expect, int update){
    int value = env.getIntField(objRef, "value");
    if (value == expect){
      env.setIntField(objRef, "value", update);
      return true;
    } else {
      return false;
    }
  }
  
  @MJI
  public int getAndAdd__I__I (MJIEnv env, int objRef, int delta) {
    int value = env.getIntField(objRef, "value");
    env.setIntField(objRef, "value", value + delta);
    return value;
  }
  
  @MJI
  public int getAndIncrement____I (MJIEnv env, int objRef) {
    int value = env.getIntField(objRef, "value");
    env.setIntField(objRef, "value", value + 1);
    return value;
  }
  
  @MJI
  public int getAndDecrement____I (MJIEnv env, int objRef) {
    int value = env.getIntField(objRef, "value");
    env.setIntField(objRef, "value", value - 1);
    return value;
  }
  
  @MJI
  public void lazySet__I__V (MJIEnv env, int objRef, int newValue) {
    env.setIntField(objRef, "value", newValue);
  }

  @MJI
  public int getAndSet__I__I (MJIEnv env, int objRef, int newValue) {
    int value = env.getIntField(objRef, "value");
    env.setIntField(objRef, "value", newValue);
    return value;
  }

  @MJI
  public boolean weakCompareAndSet__II__Z (MJIEnv env, int objRef, int expect, int update) {
    int value = env.getIntField(objRef, "value");
    if (value == expect){
      env.setIntField(objRef, "value", update);
      return true;
    } else {
      return false;
    }
  }
  
  @MJI
  public int incrementAndGet____I (MJIEnv env, int objRef) {
    int value = env.getIntField(objRef, "value");
    value++;
    env.setIntField(objRef, "value", value);
    return value;
  }
  
  @MJI
  public int decrementAndGet____I (MJIEnv env, int objRef) {
    int value = env.getIntField(objRef, "value");
    value--;
    env.setIntField(objRef, "value", value);
    return value;
  }
  
  @MJI
  public int addAndGet__I__I (MJIEnv env, int objRef, int delta) {
    int value = env.getIntField(objRef, "value");
    value += delta;
    env.setIntField(objRef, "value", value);
    return value;
  }
}
