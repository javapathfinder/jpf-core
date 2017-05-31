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
 * a simplistic IntSet implementation that uses an unsorted array to keep elements.
 * Obviously this is O(N) and therefore not a good choice if the list grows,
 * but if we know there are only a few elements then it isn't worth to
 * do any sorting or fancy lookup - the JIT would beat algorithm.
 * 
 * If the set is empty there is no memory allocated for the elements
 */
public class UnsortedArrayIntSet extends ArrayIntSet {

  static final int DEFAULT_CAPACITY = 4;
  static final int GROWTH = 8;
  
  public UnsortedArrayIntSet (){
    // nothing
  }
  
  public UnsortedArrayIntSet (int initialCapacity){
    super(initialCapacity);
  }

  
  
  @Override
  public boolean add (int v) {
    int len = size;
    if (len == 0){
      elements = new int[DEFAULT_CAPACITY];
      
    } else {
      int[] a = elements;
      int i=0;
      for (; i<len; i++){
        if (a[i] == v){
          return false; // was already there
        }
      }
      
      if (i == a.length){
        int[] newElements = new int[a.length + GROWTH];
        System.arraycopy(a, 0, newElements, 0, size);
        elements = newElements;
      }    
    }
    
    elements[size++] = v;
    return true;
  }

  @Override
  public boolean remove(int v) {
    int len = size;
    if (len > 0){
      int[] a = elements;
      for (int i=0; i<len; i++){
        if (a[i] == v){
          if (len == 1){
            elements = null;
          } else {
            i++;
            if (i < len) {
              System.arraycopy(a, i, a, i-1, len-i);
            }
          }
          
          size--;
          return true;
        }
      }
    }
    
    return false; // wasn't there
  }

  @Override
  public boolean contains(int v) {
    int len = size;
    if (len > 0){
      int[] a = elements;
      for (int i=0; i<len; i++){
        if (a[i] == v){
          return true;
        }
      }
    }
    
    return false;
  }
}
