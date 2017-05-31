/*
 * Copyright (C) 2015, United States Government, as represented by the
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

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;

/**
 * a immutable list that just contains a single element
 * 
 * This is just an optimization for constructs such as CGs that inherently can
 * contain lists, but frequently don't have more than a single element
 * 
 * While java.util.Collections provides optimizations for empty lists, there is
 * no optimization for single element lists
 */
public class SingleElementList<E> implements List<E> {
  
  protected E elem;

  class SingleElemIterator implements ListIterator<E>{
    boolean done;

    @Override
    public boolean hasNext() {
      return !done;
    }

    @Override
    public E next() {
      if (!done){
        done = true;
        return elem;
      } else {
        throw new NoSuchElementException();
      }
    }

    @Override
    public boolean hasPrevious() {
      return false;
    }

    @Override
    public E previous() {
      throw new NoSuchElementException();
    }

    @Override
    public int nextIndex() {
      if (!done){
        return 0;
      } else {
        return 1;
      }
    }

    @Override
    public int previousIndex() {
      if (done){
        return 0;
      } else {
        return -1;
      }
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException("list is immutable");
    }

    @Override
    public void set(E e) {
      throw new UnsupportedOperationException("list is immutable");
    }

    @Override
    public void add(E e) {
      throw new UnsupportedOperationException("list is immutable");
    }
  }
  
  public SingleElementList (E e){
    elem = e;
  }

  @Override
  public int size() {
    return 1;
  }

  @Override
  public boolean isEmpty() {
    return false;
  }

  @Override
  public boolean contains(Object o) {
    if (elem != null){
      return elem.equals(o);
    } else {
      return o == null;
    }
  }

  @Override
  public Iterator<E> iterator() {
    return new SingleElemIterator();
  }

  @Override
  public Object[] toArray() {
    Object[] a = { elem };
    return a;
  }

  @Override
  public <T> T[] toArray(T[] a) {
    a[0] = (T)elem;
    return a;
  }

  @Override
  public boolean add(E e) {
    return false;
  }

  @Override
  public boolean remove(Object o) {
    return false;
  }

  @Override
  public boolean containsAll(Collection<?> c) {
    for (Object o : c){
      if (!contains(o)){
        return false;
      }
    }
    return true;
  }

  @Override
  public boolean addAll(Collection<? extends E> c) {
    return false;
  }

  @Override
  public boolean addAll(int index, Collection<? extends E> c) {
    return false;
  }

  @Override
  public boolean removeAll(Collection<?> c) {
    return false;
  }

  @Override
  public boolean retainAll(Collection<?> c) {
    for (Object o : c){
      if (!contains(o)){
        return false;
      }
    }
    return true;
  }

  @Override
  public void clear() {
    throw new UnsupportedOperationException("list is immutable");
  }

  @Override
  public E get(int index) {
    if (index == 0){
      return elem;
    } else {
      throw new IndexOutOfBoundsException(Integer.toString(index));
    }
  }

  @Override
  public E set(int index, E element) {
    throw new UnsupportedOperationException("list is immutable");
  }

  @Override
  public void add(int index, E element) {
    throw new UnsupportedOperationException("list is immutable");
  }

  @Override
  public E remove(int index) {
    throw new UnsupportedOperationException("list is immutable");
  }

  @Override
  public int indexOf(Object o) {
    if (elem.equals(o)){
      return 0;
    } else {
      return -1;
    }
  }

  @Override
  public int lastIndexOf(Object o) {
    return indexOf(o);
  }

  @Override
  public ListIterator<E> listIterator() {
    return new SingleElemIterator();
  }

  @Override
  public ListIterator<E> listIterator(int index) {
    if (index == 0){
      return new SingleElemIterator();
    } else {
      throw new IndexOutOfBoundsException(Integer.toString(index));      
    }
  }

  @Override
  public List<E> subList(int fromIndex, int toIndex) {
    throw new UnsupportedOperationException("single element list");
  }
  
}
