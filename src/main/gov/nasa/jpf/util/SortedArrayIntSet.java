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
 * a set of integers that uses a simple sorted int array and binary search
 * 
 * To be used in a context where
 * 
 *  - the number of elements does not have a hard limit
 *  - the number of elements is assumed to be small, but potentially sparse
 *  - the following operations are time critical
 *     + inclusion check
 *     + size check
 *     + cloning
 *     + iteration over elements
 *  - adding/removing should be better than O(N)
 *  
 */
public class SortedArrayIntSet extends ArrayIntSet {
  
  static final int DEFAULT_CAPACITY = 8;
  static final int GROWTH = 8;
      
  //--- private methods
  
  // returns the index where the match should be
  // caller has to make sure size > 0
  protected final int bisect (int val){
    int min = 0;
    int max = size-1;
    int[] a = elements;
    
    while (max > min) {
      int mid = (min + max) / 2;
      
      if (a[mid] < val) {
        min = mid + 1;
      } else {
        max = mid;
      }
    }
    
    return min;
  }
  
  
  // if we already have elements, idx has to be within range
  protected final void insertElement (int idx){
    if (elements == null){
      elements = new int[DEFAULT_CAPACITY];
     
    } else {
      int[] a = elements;      
      
      if (size == a.length){
        int newLength = a.length + GROWTH;
        int[] newElements = new int[newLength];
        if (idx > 0){
          System.arraycopy(a, 0, newElements, 0, idx);
        }
        if (idx < size){
          System.arraycopy(a, idx, newElements, idx+1, size-idx);
        }
        elements = newElements;
        
      } else {
        System.arraycopy(a, idx, a, idx+1, size-idx);
      }
    }
  }
  
  
  //--- public methods
  
  public SortedArrayIntSet (){
    // nothing
  }
  
  public SortedArrayIntSet (int initialCapacity){
    super(initialCapacity);
  }
  
  @Override
  public boolean contains (int v) {
    return ((size > 0) && elements[bisect(v)] == v);      
  }
  
  @Override
  public boolean add (int v){
    if (size == 0){
      elements = new int[DEFAULT_CAPACITY];
      elements[0] = v;
      size++;
      return true;
      
    } else {
      int i = bisect(v);
      int e = elements[i];
      if (e != v){
        if (e < v) {
          i++;
        }
        
        insertElement(i);
        elements[i] = v;
        size++;
        return true;
        
      } else {
        return false; // was already there
      }
    }
  }
    
  @Override
  public boolean remove (int v) {
    int len = size;
    
    if (len > 0){
      int[] a = elements;
      int i = bisect(v);
      if (a[i] == v) {
        len--;
        if (len == 0){
          elements = null;
          size = 0;
          
        } else {
          if (i < len){
            System.arraycopy(a, i + 1, a, i, (len - i));          
          }
          size = len;
        }
        
        return true;
      }
    }
    
    return false; // wasn't there
  }
  
}
