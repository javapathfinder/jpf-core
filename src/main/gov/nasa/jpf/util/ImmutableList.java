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
 * utility class for JPF internal linked lists that are tail-immutable 
 */
public class ImmutableList<E> implements Iterable<E> {

  static class IteratorImpl<E> implements Iterator<E> {

    private ImmutableList<E> next;
    
    private IteratorImpl(ImmutableList<E> list){
      next = list;
    }
    
    @Override
	public boolean hasNext() {
      return (next != null);
    }

    @Override
	public E next() {
      if (next != null){
        E elem = next.head;
        next = next.tail;
        return elem;
        
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
	public void remove() {
      throw new UnsupportedOperationException("can't remove elements from ImmutableList");
    }
    
  }
  
  public final E head;
  public final ImmutableList<E> tail;
  
  
  public ImmutableList(E data, ImmutableList<E> tail) {
    this.head = data;
    this.tail = tail;
  }
  
  @Override
  public Iterator<E> iterator() {
    return new IteratorImpl(this);
  }
  
  public boolean contains (E object){
    for (E e : this){
      if (e.equals(object)){
        return true;
      }
    }
    
    return false;
  }
}
