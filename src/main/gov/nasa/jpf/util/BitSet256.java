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
 * a fixed size BitSet with 256 bits.
 *
 * The main motivation for this class is to minimize memory size while maximizing
 * performance and keeping a java.util.BitSet compatible interface. The only
 * deviation from the standard BitSet is that we assume more cardinality() calls
 * than set()/clear() calls, i.e. we want to cache this value
 *
 * Instances of this class do not allocate any additional memory, we keep all
 * data in builtin type fields
 */
public class BitSet256 extends AbstractFixedBitSet {

  public static final int INDEX_MASK = 0xffffff00;

  long l0, l1, l2, l3;

  public BitSet256 (){
    // nothing in here
  }

  public BitSet256 (int i){
    set(i);
  }

  public BitSet256 (int... idx){
    for (int i : idx){
      set(i);
    }
  }

  private final int computeCardinality (){
    int n= Long.bitCount(l0);
    n += Long.bitCount(l1);
    n += Long.bitCount(l2);
    n += Long.bitCount(l3);
    return n;
  }

  //--- public interface (much like java.util.BitSet)

  @Override
  public void set (int i){
    if ((i & INDEX_MASK) == 0) {
      long bitPattern = (1L << i);

      switch (i >> 6) {
        case 0:
          if ((l0 & bitPattern) == 0L) {
            cardinality++;
            l0 |= bitPattern;
          }
          break;
        case 1:
          if ((l1 & bitPattern) == 0L) {
            cardinality++;
            l1 |= bitPattern;
          }
          break;
        case 2:
          if ((l2 & bitPattern) == 0L) {
            cardinality++;
            l2 |= bitPattern;
          }
          break;
        case 3:
          if ((l3 & bitPattern) == 0L) {
            cardinality++;
            l3 |= bitPattern;
          }
      }
    } else {
      throw new IndexOutOfBoundsException("BitSet256 index out of range: " + i);
    }
  }

  @Override
  public void clear (int i){
    if ((i & INDEX_MASK) == 0) {
      long bitPattern = (1L << i);

      switch (i >> 6) {
        case 0:
          if ((l0 & bitPattern) != 0L) {
            cardinality--;
            l0 &= ~bitPattern;
          }
          break;
        case 1:
          if ((l1 & bitPattern) != 0L) {
            cardinality--;
            l1 &= ~bitPattern;
          }
          break;
        case 2:
          if ((l2 & bitPattern) != 0L) {
            cardinality--;
            l2 &= ~bitPattern;
          }
          break;
        case 3:
          if ((l3 & bitPattern) != 0L) {
            cardinality--;
            l3 &= ~bitPattern;
          }
      }
    } else {
      throw new IndexOutOfBoundsException("BitSet256 index out of range: " + i);
    }
  }

  @Override
  public boolean get (int i){
    if ((i & INDEX_MASK) == 0) {
      long bitPattern = (1L << i);

      switch (i >> 6) {
        case 0:
          return ((l0 & bitPattern) != 0);
        case 1:
          return ((l1 & bitPattern) != 0);
        case 2:
          return ((l2 & bitPattern) != 0);
        case 3:
          return ((l3 & bitPattern) != 0);
      }
    }

    throw new IndexOutOfBoundsException("BitSet256 index out of range: " + i);
  }

  @Override
  public int size() {
    return 256;
  }

  
  /**
   * number of bits we can store
   */
  @Override
  public int capacity() {
    return 256;
  }

  /**
   * index of highest set bit + 1
   */
  @Override
  public int length() {
    if (l3 != 0){
      return 256 - Long.numberOfLeadingZeros(l3);
    } else if (l2 != 0){
      return 192 - Long.numberOfLeadingZeros(l2);
    } else if (l1 != 0){
      return 128 - Long.numberOfLeadingZeros(l1);
    } else if (l1 != 0){
      return 64 - Long.numberOfLeadingZeros(l0);
    } else {
      return 0;
    }
  }

  @Override
  public void clear() {
    l0 = l1 = l2 = l3 = 0L;
    cardinality = 0;
  }


  @Override
  public int nextSetBit (int fromIdx){
    if ((fromIdx & INDEX_MASK) == 0) {
      int i;
      int i0 = fromIdx & 0x3f;
      switch (fromIdx >> 6){
        case 0:
          if ((i=Long.numberOfTrailingZeros(l0 & (0xffffffffffffffffL << i0))) <64) return i;
          if ((i=Long.numberOfTrailingZeros(l1)) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(l3)) <64) return i + 192;
          break;
        case 1:
          if ((i=Long.numberOfTrailingZeros(l1 & (0xffffffffffffffffL << i0))) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(l3)) <64) return i + 192;
          break;
        case 2:
          if ((i=Long.numberOfTrailingZeros(l2 & (0xffffffffffffffffL << i0))) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(l3)) <64) return i + 192;
          break;
        case 3:
          if ((i=Long.numberOfTrailingZeros(l3 & (0xffffffffffffffffL << i0))) <64) return i + 192;
      }

      return -1;

    } else {
      //throw new IndexOutOfBoundsException("BitSet256 index out of range: " + fromIdx);
      return -1;
    }
  }

  @Override
  public int nextClearBit (int fromIdx){
    if ((fromIdx & INDEX_MASK) == 0) {
      int i;
      int i0 = fromIdx & 0x3f;
      switch (fromIdx >> 6){
        case 0:
          if ((i=Long.numberOfTrailingZeros(~l0 & (0xffffffffffffffffL << i0))) <64) return i;
          if ((i=Long.numberOfTrailingZeros(~l1)) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(~l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(~l3)) <64) return i + 192;
          break;
        case 1:
          if ((i=Long.numberOfTrailingZeros(~l1 & (0xffffffffffffffffL << i0))) <64) return i + 64;
          if ((i=Long.numberOfTrailingZeros(~l2)) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(~l3)) <64) return i + 192;
          break;
        case 2:
          if ((i=Long.numberOfTrailingZeros(~l2 & (0xffffffffffffffffL << i0))) <64) return i + 128;
          if ((i=Long.numberOfTrailingZeros(~l3)) <64) return i + 192;
          break;
        case 3:
          if ((i=Long.numberOfTrailingZeros(~l3 & (0xffffffffffffffffL << i0))) <64) return i + 192;
      }

      return -1;

    } else {
      //throw new IndexOutOfBoundsException("BitSet256 index out of range: " + fromIdx);
      return -1;
    }
  }

  public void and (BitSet256 other){
    l0 &= other.l0;
    l1 &= other.l1;
    l2 &= other.l2;
    l3 &= other.l3;

    cardinality = computeCardinality();
  }

  public void andNot (BitSet256 other){
    l0 &= ~other.l0;
    l1 &= ~other.l1;
    l2 &= ~other.l2;
    l3 &= ~other.l3;

    cardinality = computeCardinality();
  }

  public void or (BitSet256 other){
    l0 |= other.l0;
    l1 |= other.l1;
    l2 |= other.l2;
    l3 |= other.l3;

    cardinality = computeCardinality();
  }

  @Override
  public boolean equals (Object o){
    if (o instanceof BitSet256){
      BitSet256 other = (BitSet256)o;
      if (l0 != other.l0) return false;
      if (l1 != other.l1) return false;
      if (l2 != other.l2) return false;
      if (l3 != other.l3) return false;
      return true;
    } else {
      // <2do> we could compare to a normal java.util.BitSet here
      return false;
    }
  }

  /**
   * answer the same hashCodes as java.util.BitSet
   */
  @Override
  public int hashCode() {
    long hc = 1234;
    hc ^= l0;
    hc ^= l1*2;
    hc ^= l2*3;
    hc ^= l3*4;
    return (int) ((hc >>32) ^ hc);
  }

  @Override
  public void hash (HashData hd){
    hd.add(l0);
    hd.add(l1);
    hd.add(l2);
    hd.add(l3);
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');

    boolean first = true;
    for (int i=nextSetBit(0); i>= 0; i = nextSetBit(i+1)){
      if (!first){
        sb.append(',');
      } else {
        first = false;
      }
      sb.append(i);
    }

    sb.append('}');

    return sb.toString();
  }
}
