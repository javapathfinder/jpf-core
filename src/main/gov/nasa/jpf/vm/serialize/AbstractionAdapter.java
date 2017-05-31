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
package gov.nasa.jpf.vm.serialize;

import gov.nasa.jpf.vm.MJIEnv;

/**
 * (mostly) pass-through Abstraction
 */
public class AbstractionAdapter implements Abstraction {

  @Override
  public int getAbstractValue(int v) {
    return v;
  }

  @Override
  public int getAbstractValue(float v) {
    return Float.floatToIntBits(v);
  }

  @Override
  public int getAbstractValue(long v) {
    return (int)(v^(v>>>32));  // Long.hashValue
  }

  @Override
  public int getAbstractValue(double v) {
    long l = Double.doubleToLongBits(v);
    return (int)(l^(l>>>32)); // Double.hashValue
  }

  @Override
  public int getAbstractObject(int ref) {
    return ref;
  }

  @Override
  public boolean traverseObject(int ref) {
    return (ref != MJIEnv.NULL);
  }
  
}
