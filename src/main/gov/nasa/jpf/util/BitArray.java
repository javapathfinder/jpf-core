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
import java.util.BitSet;

/**
 * Faster version of BitSet for fixed size.
 */
public class BitArray {
  public final int length;
  final byte[] data;
  
  public BitArray(int len) {
    length = len;
    data = new byte[(len + 7) >> 3];
  }

  public void fromBitSet(BitSet in) {
    int max = Math.min(data.length, (in.length() + 7) >> 3);
    int i;
    for (i = 0; i < max; i++) {
      int j = i << 3;
      data[i] = (byte)
        ((in.get(j + 0) ? 1 : 0) |
         (in.get(j + 1) ? 2 : 0) |
         (in.get(j + 2) ? 4 : 0) |
         (in.get(j + 3) ? 8 : 0) |
         (in.get(j + 4) ? 16 : 0) |
         (in.get(j + 5) ? 32 : 0) |
         (in.get(j + 6) ? 64 : 0) |
         (in.get(j + 7) ? 128 : 0));
    }
    Arrays.fill(data, max, data.length, (byte) 0);
  }

  public final void set(int idx, boolean val) {
    if (idx >= length) throw new ArrayIndexOutOfBoundsException("" + idx + " >= " + length);
    if (val) {
      data[idx >> 3] |= (1 << (idx & 7));
    } else {
      data[idx >> 3] &= ~(1 << (idx & 7));
    }
  }

  public final void set(int idx, int val) {
    set(idx, val != 0);
  }
  
  public final void set(int idx) {
    if (idx >= length) throw new ArrayIndexOutOfBoundsException("" + idx + " >= " + length);
    data[idx >> 3] |= (1 << (idx & 7));
  }

  public final void clear(int idx) {
    if (idx >= length) throw new ArrayIndexOutOfBoundsException("" + idx + " >= " + length);
    data[idx >> 3] &= ~(1 << (idx & 7));
  }

  public final void setAll() {
    Arrays.fill(data, (byte) 0xff);
    cleanup();
  }

  public final void clearAll() {
    Arrays.fill(data, (byte) 0);
  }
  
  public final void invert() {
    int i;
    for (i = 0; i < data.length; i++) {
      data[i] = (byte) ~ data[i];
    }
    cleanup();
  }
  
  // to keep all unused bits at 0
  final void cleanup() {
    if ((length & 7) != 0) {
      int idx = data.length - 1;
      data[idx] &= ~(0xff << (length & 7));
    }
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
    if (! (o instanceof BitArray)) return false;
    byte[] thatData = ((BitArray)o).data;
    byte[] thisData = this.data;
    return Arrays.equals(thisData, thatData);
  }
  
  public static final BitArray empty = new BitArray(0);
}
