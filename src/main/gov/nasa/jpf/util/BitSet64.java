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
 *
 */
public class BitSet64 extends AbstractFixedBitSet implements Cloneable {

  static final int INDEX_MASK = 0xffffffc0; // ( i>=0 && i<64)

  long l0;

  public BitSet64 (){
    // nothing in here
  }

  public BitSet64 (int i){
    set(i);
  }

  public BitSet64 (int... idx){
    for (int i : idx){
      set(i);
    }
  }

  @Override
  public void hash (HashData hd){
    hd.add(l0);
  }


  private final int computeCardinality (){
    return Long.bitCount(l0);
  }

  //--- public interface (much like java.util.BitSet)

  @Override
  public void set (int i){
    if ((i & INDEX_MASK) == 0){
      long bitPattern = (1L << i);
      if ((l0 & bitPattern) == 0L) {
        cardinality++;
        l0 |= bitPattern;
      }
    } else {
      throw new IndexOutOfBoundsException("BitSet64 index out of range: " + i);
    }
  }

  @Override
  public void clear (int i){
    if ((i & INDEX_MASK) == 0){
      long bitPattern = (1L << i);
      if ((l0 & bitPattern) != 0L) { // bit is set
        cardinality--;
        l0 &= ~bitPattern;
      }
    } else {
      throw new IndexOutOfBoundsException("BitSet64 index out of range: " + i);
    }
  }


  @Override
  public boolean get (int i){
    if ((i & INDEX_MASK) == 0){
      long bitPattern = (1L << i);
      return ((l0 & bitPattern) != 0);
    } else {
      throw new IndexOutOfBoundsException("BitSet64 index out of range: " + i);
    }
  }

  @Override
  public int capacity(){
    return 64;
  }
  
  /**
   * number of bits we can store
   */
  @Override
  public int size() {
    return 64;
  }

  /**
   * index of highest set bit + 1
   */
  @Override
  public int length() {
    return 64 - Long.numberOfLeadingZeros(l0);
  }


  @Override
  public void clear() {
    l0 = 0L;
    cardinality = 0;
  }

  @Override
  public int nextSetBit (int fromIdx){
    if ((fromIdx & INDEX_MASK) == 0){
      //int n = Long.numberOfTrailingZeros(l0 & (0xffffffffffffffffL << fromIdx));
      int n = Long.numberOfTrailingZeros(l0 >> fromIdx) + fromIdx;
      if (n < 64) {
        return n;
      } else {
        return -1;
      }
    } else {
      //throw new IndexOutOfBoundsException("BitSet64 index out of range: " + fromIdx);
      return -1;
    }
  }

  @Override
  public int nextClearBit (int fromIdx){
    if ((fromIdx & INDEX_MASK) == 0){
      //int n = Long.numberOfTrailingZeros(~l0 & (0xffffffffffffffffL << fromIdx));
      int n = Long.numberOfTrailingZeros(~l0 >> fromIdx) + fromIdx;
      if (n < 64) {
        return n;
      } else {
        return -1;
      }
    } else {
      //throw new IndexOutOfBoundsException("BitSet64 index out of range: " + fromIdx);
      return -1;
    }
  }

  public void and (BitSet64 other){
    l0 &= other.l0;

    cardinality = computeCardinality();
  }

  public void andNot (BitSet64 other){
    l0 &= ~other.l0;

    cardinality = computeCardinality();
  }

  public void or (BitSet64 other){
    l0 |= other.l0;

    cardinality = computeCardinality();
  }

  @Override
  public boolean equals (Object o){
    if (o instanceof BitSet64){
      BitSet64 other = (BitSet64)o;
      if (l0 != other.l0) return false;
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
    return (int) ((hc >>32) ^ hc);
  }

}
