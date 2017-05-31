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
 * dynamically growing, cyclic array buffer queue for object references
 */
public class ArrayObjectQueue<E> implements ObjectQueue<E> {

  static final int DEFAULT_CAPACITY = 256;
  
  int size = 0;
  int first;  // next index we will remove
  int last;   // last index we did add
  
  Object[] buffer = null;
  
  class FIFOIterator implements Iterator<E> {
    int next = first;
    int remaining = size;
    
    @Override
	public boolean hasNext() {
      return (remaining > 0);
    }

    @Override
	public E next() {
      if (remaining == 0){
        throw new NoSuchElementException();
      } else {
        E e = (E) buffer[next];
        next = (next+1) % buffer.length;
        remaining--;
        return e;
      }
    }

    @Override
	public void remove() { // its a queue
      throw new UnsupportedOperationException();
    }
  }
  
  // just for debugging purposes
  class StorageIterator implements Iterator<E> {
    int next = 0;
    
    @Override
	public boolean hasNext(){
      return (next < buffer.length); 
    }
    
    @Override
	public E next(){
      if (next == buffer.length){
        throw new NoSuchElementException();
      }
      
      E e = (E) buffer[next];
      next++;
      return e;      
    }
    
    @Override
	public void remove() { // its a queue
      throw new UnsupportedOperationException();
    }
  }
  
  public ArrayObjectQueue (){
    buffer = new Object[DEFAULT_CAPACITY];
  }
  
  public ArrayObjectQueue (int initialCapacity){
    buffer = new Object[initialCapacity];
  }
  
  protected void grow(){
    Object[] newBuffer = new Object[buffer.length * 3 / 2];
    
    if (first < last){
      System.arraycopy(buffer, first, newBuffer, 0, last - first +1);
    } else if (first > last){
      int nRight = buffer.length - first;
      System.arraycopy(buffer, first, newBuffer, 0, nRight);
      System.arraycopy(buffer, 0, newBuffer, nRight, last+1);      
    } else { // just 1 element
      newBuffer[0] = buffer[first];
    }
    
    first = 0;
    last = size-1;
    buffer = newBuffer;
  }
  
  @Override
  public boolean isEmpty() {
    return (size == 0);
  }
  
  public int getCurrentCapacity(){
    return buffer.length;
  }
  
  @Override
  public int size() {
    return size;
  }
  
  @Override
  public boolean offer (E e){
    return add(e);
  }
  
  @Override
  public boolean add (E e){
    if (size == 0){  // first element
      first = last = 0;
      buffer[0] = e;
      size = 1;
      
    } else {
      int i = (last + 1) % buffer.length;
      if (i == first) {
        grow();
        i = size;
      }
      
      last = i;
      buffer[i] = e;
      size++;
    }
    
    return true; // this is a dynamic queue, we never run out of space
  }
  
  @Override
  public E poll (){
    if (size == 0){
      return null;
      
    } else {
      int i = first;
      
      first = (first+1) % buffer.length;
      size--;
      
      E e = (E) buffer[i];
      buffer[i] = null; // avoid memory leaks
      return e;
    }
    
  }
  
  @Override
  public E remove () throws NoSuchElementException {
    if (size == 0){
      throw new NoSuchElementException();
    } else {
      return poll();
    }
  }

  @Override
  public E peek () {
    if (size == 0){
      return null;
    } else {
      return (E)buffer[first];
    }
  }
  
  @Override
  public Iterator<E> iterator() {
    return new FIFOIterator();
  }
 
  public Iterator<E> storageIterator(){
    return new StorageIterator();
  }
  
  
  @Override
  public void clear(){
    buffer = new Object[buffer.length]; // cheaper than iterating over the old one
    size = 0;
    first = last = -1;
  }
  
  /**
   * call Processor.process(e) on each queued object
   * 
   * This method does not return before the queue is empty, which makes it
   * suitable for graph traversal. It also avoids iterator objects, allows
   * adding new objects while processing the queue, and enables to keep
   * processing state in the processor
   */
  @Override
  public void process (Processor<E> processor){
    while (size > 0){
      E e = remove();
      processor.process(e);
    }
  }
}
