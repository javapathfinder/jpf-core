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
 * object queue that uses cached link entries
 */
public class LinkedObjectQueue<E> implements ObjectQueue<E> {

  static class Entry {
    Entry next; // single linked list
    Object obj;  // referenced object
  }

  protected Entry last;
  protected Entry first;

  protected int size;
  
  protected int maxCache;
  protected int nFree;
  protected Entry free;

  class FIFOIterator implements Iterator<E> {
    Entry e = first;

    @Override
	public boolean hasNext() {
      return e != null;
    }

    @Override
	public E next() {
      if (e == null){
        throw new NoSuchElementException();
      } else {
        E obj = (E)e.obj;
        e = e.next;
        return obj;
      }
    }

    @Override
	public void remove() {
      throw new UnsupportedOperationException("arbitrary remove from queue not supported");
    }
  }
  
  public LinkedObjectQueue (){
    maxCache = 256;
  }
  
  public LinkedObjectQueue (int maxCache){
    this.maxCache = maxCache;
  }
  
  @Override
  public int size() {
    return size;
  }
  
  @Override
  public boolean add(E obj) {
    Entry e;

    if (nFree > 0){ // reuse a cached Entry object
      e = free;
      free = e.next;
      nFree--;

    } else {
      e = new Entry();
    }

    e.obj = obj;
    e.next = null;

    if (last != null) {
      last.next = e;
    } else {
      first = e;
    }

    last = e;
    
    size++;
    return true;
  }
  
  @Override
  public boolean offer( E obj){
    return add(obj);
  }

  @Override
  public boolean isEmpty(){
    return size > 0;
  }
  
  @Override
  public E peek (){
    if (size == 0){
      return null;
    } else {
      return (E)first.obj;
    }
  }
  
  @Override
  public E poll(){
    if (size == 0){
      return null;
      
    } else {
      Entry e = first;
      first = first.next;
      size--;
      
      E obj = (E)e.obj;
      
      if (nFree < maxCache){
        Entry next = e.next;
        e.next = (nFree++ > 0) ? free : null;
        e.obj = null;
        free = e;
      }
      
      return obj;
    }
  }
  
  @Override
  public E remove() throws NoSuchElementException {
    if (size == 0){
      throw new NoSuchElementException();
    } else {
      return poll();
    }
  }
  
  @Override
  public Iterator<E> iterator(){
    return new FIFOIterator();
  }
  
  @Override
  public void process( Processor<E> proc) {
    for (Entry e = first; e != null; ) {
      proc.process( (E)e.obj);

      e.obj = null; // avoid memory leaks

      if (nFree < maxCache){
        // recycle to save some allocation and a lot of shortliving garbage
        Entry next = e.next;
        e.next = (nFree++ > 0) ? free : null;
        free = e;
        e = next;

      } else {
        e = e.next;
      }
    }
    clear();
  }

  @Override
  public void clear () {
    first = null;
    last = null;
    size = 0;

    // don't reset nFree and free since we limit the memory size of our cache
    // and the Entry object do not reference anything
  }
}
