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

import java.util.NoSuchElementException;
import gov.nasa.jpf.JPFException;

/**
 * common base for array based IntSet implementations
 */
public abstract class ArrayIntSet implements IntSet, Cloneable {
    
  protected int size;
  protected int[] elements;
  
  private class Iterator implements IntIterator {
    int next = 0;

    @Override
    public void remove() {
      int idx = next-1;
      if (idx >=0){
        if (idx < size-1){
          System.arraycopy(elements, next, elements, idx, size-idx);
        }
        size--;
        next = idx;
      }
    }

    @Override
    public boolean hasNext() {
      return (next < size);
    }

    @Override
    public int next() {
      if (next < size){
        return elements[next++];
      } else {
        throw new NoSuchElementException();
      }
    }
  }
  
  protected ArrayIntSet (){
    // nothing
  }
  
  protected ArrayIntSet (int initialCapacity){
    elements = new int[initialCapacity];
  }
  
  @Override
  public  boolean isEmpty(){
    return (size == 0);
  }
   
  @Override
  public int size(){
    return size;
  }
  
  @Override
  public void clear(){
    size = 0;
    elements = null;
  }
  
  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder(/*getClass().getName()*/);
    sb.append('{');
    for (int i=0; i<size; i++){
      if (i>0){
        sb.append(',');
      }
      sb.append(elements[i]);
    }
    sb.append('}');
    return sb.toString();
  }
  
  @Override
  public ArrayIntSet clone(){
    try {
      ArrayIntSet other = (ArrayIntSet) super.clone();
      other.size = size;
      if (elements != null) {
        other.elements = elements.clone();
      }
      return other;
      
    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("clone failed " + this);
    }
  }
  
  /**
   * this is probably a bad hash function, but we just need something that
   * is order independent
   */
  @Override
  public int hashCode(){
    int[] a = elements;
    int n = size;
    int h = (n << 16) + (n % 3);

    for (int i = 0; i < n; i++) {
      int e = a[i];
      if (e == 0){
        e = Integer.MAX_VALUE;
      }
      int rot = e % 31;
      h ^= (h << rot) | (h >>> (32 - rot)); // rotate left
    }
    
    return h;
  }
  
  @Override
  public boolean equals (Object o){
    if (o instanceof IntSet){
      IntSet other = (IntSet)o;
      if (size == other.size()){
        int len = size;
        int[] a = elements;
        for (int i=0; i<len; i++){
          if (!other.contains(a[i])){
            return false;
          }
        }
        return true;
      }
    }
    return false;
  }

  @Override
  public IntIterator intIterator (){
    return new Iterator();
  }
}
