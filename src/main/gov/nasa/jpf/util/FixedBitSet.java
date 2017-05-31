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

import gov.nasa.jpf.JPFException;
import java.util.NoSuchElementException;

/**
 * BitSet like interface for fixed size bit sets
 * 
 * We keep this as an interface so that we can have java.util.BitSet
 * subclasses as implementations
 */
public interface FixedBitSet extends Cloneable, IntSet {

  void set (int i);
  void set (int i, boolean val);
  boolean get (int i);
  void clear (int i);
  
  int nextClearBit (int fromIndex);
  int nextSetBit (int fromIndex);

  boolean isEmpty();
  int size();
  
  int cardinality();
  int length();
  int capacity();
  
  void clear();
  
  void hash (HashData hd);
  
  FixedBitSet clone();
}

/**
 * this is the base class for our non java.util.BitSet based FixedBitSet implementations
 */
abstract class AbstractFixedBitSet implements FixedBitSet {
  
  class SetBitIterator implements IntIterator {
    int cur = 0;
    int nBits;
    
    @Override
    public void remove() {
      if (cur >0){
        clear(cur-1);
      }
    }

    @Override
    public boolean hasNext() {
      return nBits < cardinality;
    }

    @Override
    public int next() {
      if (nBits < cardinality){
        int idx = nextSetBit(cur);
        if (idx >= 0){
          nBits++;
          cur = idx+1;
        }
        return idx;
        
      } else {
        throw new NoSuchElementException();
      }
    }
  }

  
  protected int cardinality;
  
  @Override
  public AbstractFixedBitSet clone(){
    try {
      return (AbstractFixedBitSet) super.clone();
    } catch (CloneNotSupportedException ex) {
      throw new JPFException("BitSet64 clone failed");
    }  
  }
  
  @Override
  public void set (int i, boolean val){
    if (val) {
      set(i);
    } else {
      clear(i);
    }
  }

  @Override
  public int cardinality() {
    return cardinality;
  }

  @Override
  public boolean isEmpty() {
    return (cardinality == 0);
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

  //--- IntSet interface
  
    
  @Override
  public boolean add(int i) {
    if (get(i)) {
      return false;
    } else {
      set(i);
      return true;
    }
  }

  @Override
  public boolean remove(int i) {
    if (get(i)) {
      clear(i);
      return true;
    } else {
      return false;
    }
  }

  @Override
  public boolean contains(int i) {
    return get(i);
  }

  @Override
  public IntIterator intIterator() {
    return new SetBitIterator();
  }

}
