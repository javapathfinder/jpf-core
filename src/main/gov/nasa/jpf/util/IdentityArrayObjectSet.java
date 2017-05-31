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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * simple identity set for objects 
 * we don't sort&bisect, assuming the number of entries will be small
 * be aware this doesn't scale to large sets
 */
public class IdentityArrayObjectSet<E> implements IdentityObjectSet<E> {

  static final int DEFAULT_CAPACITY = 4;
  
  private class StoreOrderIterator implements Iterator<E> {
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
    public E next() {
      if (next < size){
        return (E) elements[next++];
      } else {
        throw new NoSuchElementException();
      }
    }
  }
  
  protected int size;
  protected Object[] elements;
  
  public IdentityArrayObjectSet(){
    // nothing, elements allocated on demand
  }
  
  public IdentityArrayObjectSet (int initialCapacity){
    elements = new Object[initialCapacity];
  }
  
  public IdentityArrayObjectSet (E initialElement){
    elements = new Object[DEFAULT_CAPACITY];
    
    elements[0] = initialElement;
    size = 1;
  }
  
  @Override
  public int size(){
    return size;
  }
  
  public boolean isEmpty(){
    return (size == 0);
  }
  
  @Override
  public boolean add (E obj){
    for (int i=0; i<size; i++){
      if (elements[i] == obj){
        return false;
      }
    }
    
    if (size == 0){
      elements = new Object[DEFAULT_CAPACITY];
    } else if (size == elements.length){
      Object[] newElements = new Object[elements.length * 3 / 2];
      System.arraycopy(elements, 0, newElements, 0, size);
      elements = newElements;
    }
    
    elements[size] = obj;
    size++;
    return true;
  }
  
  @Override
  public boolean contains (E obj){
    for (int i=0; i<size; i++){
      if (elements[i] == obj){
        return true;
      }
    }
    
    return false;
  }
  
  @Override
  public boolean remove (E obj){
    int len = size;
    for (int i=0; i<len; i++){
      if (elements[i] == obj){
        len--;
        if (len == 0){
          size = 0;
          elements = null;
          
        } else if (i < len){
          System.arraycopy(elements, i+1, elements, i, len-i);
        } else {
          elements[len] = null; // avoid memory leak
        }
        
        size = len;
        return true;
      }
    }
    
    return false;    
  }
  
  @Override
  public ObjectSet<E> clone(){
    try {
      return (ObjectSet<E>)super.clone();
    } catch (CloneNotSupportedException x){
      // can't happen
      return null;
    }
  }
  
  @Override
  public Iterator<E> iterator(){
    return new StoreOrderIterator();
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
}
