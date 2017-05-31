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

import java.util.BitSet;
import java.util.NoSuchElementException;

/**
 * a FixedBitSet implementation that is based on java.util.BitSet
 */
public class BitSetN extends BitSet implements FixedBitSet {
  
  class SetBitIterator implements IntIterator {
    int cur = 0;
    int nBits;
    int cardinality;  // <2do> this should be lifted since it makes the iterator brittle
    
    SetBitIterator (){
      cardinality = cardinality();
    }
    
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
  
  
  public BitSetN (int nBits){
    super(nBits);
  }
  
  @Override
  public FixedBitSet clone() {
    return (FixedBitSet) super.clone();
  }

  @Override
  public int capacity() {
    return size();
  }


  @Override
  public void hash (HashData hd){
    long[] data = toLongArray();
    for (int i=0; i<data.length; i++){
      hd.add(data[i]);
    }
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
