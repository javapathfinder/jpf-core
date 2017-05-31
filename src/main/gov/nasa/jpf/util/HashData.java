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
package gov.nasa.jpf.util;

/**
 * object to compute complex hash values that can be accumulated and
 * delegated (to aggregates etc.)
 * used to obtain hashcodes for states
 */
public class HashData {
  private static final int poly = 0x88888EEF;
  private int m = -1;

  public void reset() {
    m = -1;
  }
  
  public int getValue () {
    return (m >>> 4) ^ (m & 15);
  }

  public void add (int value) {
    if (m < 0) {
      m += m;
      m ^= poly;
    } else {
      m += m;
    }

    m ^= value;
  }

  public void add (long value){
    add((int)(value ^ (value >>> 32)));
  }

  public void add (Object o) {
    if (o != null) {
      add(o.hashCode());
    }
  }
  
  public void add (boolean b) {
    // a clear case of '42', but that's the "official" boolean hashing
    add(b ? 1231 : 1237);
  }
}
