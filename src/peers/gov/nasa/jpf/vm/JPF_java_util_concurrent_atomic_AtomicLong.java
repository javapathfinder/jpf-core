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
 * native peer for java.util.concurrent.atomic.AtomicLong
 * this implementation just cuts off native methods
 */
public class JPF_java_util_concurrent_atomic_AtomicLong extends NativePeer {
  @MJI
  public void $clinit____V (MJIEnv env, int rcls) {
    // don't let this one pass, it calls native methods from non-public Sun classes
  }

  @MJI
  public boolean compareAndSet__JJ__Z (MJIEnv env, int objRef, long expect, long update){
    long value = env.getLongField(objRef, "value");
    if (value == expect){
      env.setLongField(objRef, "value", update);
      return true;
    } else {
      return false;
    }
  }
  
  @MJI
  public long getAndIncrement____J (MJIEnv env, int objRef){
    long value = env.getLongField(objRef, "value");
    env.setLongField(objRef, "value", value + 1);
    return value;
  }
  
  @MJI
  public long getAndDecrement____J (MJIEnv env, int objRef){
    long value = env.getLongField(objRef, "value");
    env.setLongField(objRef, "value", value - 1);
    return value;
  }

  @MJI
  public long getAndAdd__J__J (MJIEnv env, int objRef, long delta) {
    long value = env.getLongField(objRef, "value");
    env.setLongField(objRef, "value", value + delta);
    return value;
  }
  
  @MJI
  public long incrementAndGet____J (MJIEnv env, int objRef) {
    long value = env.getLongField(objRef, "value");
    value++;
    env.setLongField(objRef, "value", value);
    return value;
  }
  
  @MJI
  public long decrementAndGet____J (MJIEnv env, int objRef) {
    long value = env.getLongField(objRef, "value");
    value--;
    env.setLongField(objRef, "value", value);
    return value;
  }
  
  @MJI
  public long addAndGet__J__J (MJIEnv env, int objRef, long delta) {
    long value = env.getLongField(objRef, "value");
    value += delta;
    env.setLongField(objRef, "value", value);
    return value;
  }
}
