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

import java.util.Arrays;

/**
 * Faster version of BitSet for those that never change.
 */
public class FinalBitSet {
  final byte[] data;
  
  FinalBitSet(byte[] in) {
    int len = in.length;
    while (len > 0 && in[len - 1] == 0) len--;
    this.data = new byte[len];
    System.arraycopy(in, 0, this.data, 0, len);
  }
  
  public final boolean get(int idx) {
    int a = idx >> 3;
    return a < data.length && a >= 0 && (data[a] & (1 << (idx & 7))) != 0;
  }
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (! (o instanceof FinalBitSet)) return false;
    byte[] thatData = ((FinalBitSet)o).data;
    byte[] thisData = this.data;
    return Arrays.equals(thisData, thatData);
  }
  
  
  /*======= Static Stuff ========*/
  static final SimplePool<FinalBitSet> pool = new SimplePool<FinalBitSet>();
  
  public static final FinalBitSet empty = create(BitArray.empty);
  
  /**
   * Creates a pooled FinalBitSet.
   */
  public static FinalBitSet create(BitArray in) {
    return pool.pool(new FinalBitSet(in.data));
  }

  /**
   * Creates a pooled FinalBitSet.
   */
  public static FinalBitSet create(byte[] in) {
    return pool.pool(new FinalBitSet(in));
  }
}
