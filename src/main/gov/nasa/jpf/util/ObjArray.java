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

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * Wrapper for arrays of objects which provides proper equals() and hashCode()
 * methods, and behaves nicely with Java 1.5 generics. 
 */
public final class ObjArray<E> implements ReadOnlyObjList<E>, Iterable<E>, Cloneable  {
  final Object[] data;

  public ObjArray(int size) {
    data = new Object[size];
  }
  
  public ObjArray(E[] data) {
    this.data = data;
  }

  @Override
  public ObjArray<E> clone() {
    return new ObjArray( data.clone());
  }


  public E[] toArray (E[] a) {
    if (a.length >= data.length) {
      System.arraycopy(data,0,a,0,data.length);
      return a;
    } else {
      return null;
    }
  }
  
  
  @Override
@SuppressWarnings("unchecked")
  public E get(int idx) {
    return (E) data[idx];
  }
  
  public void set(int idx, E e) {
    data[idx] = e;
  }
  
  @Override
  public int length() {
    return data.length;
  }
  
  @Override
  public int hashCode() {
    return Arrays.hashCode(data);
  }
  
  @Override
  public boolean equals(Object o) {
    if (this == o) return true;
    if (! (o instanceof ObjArray)) return false;
    Object[] thatData = ((ObjArray)o).data;
    Object[] thisData = this.data;
    
    // could cause NullPointerException for non-robust .equals 
    // return Arrays.equals(thisData, thatData);
    
    if (thisData == thatData) return true;
    if (thisData.length != thatData.length) return false;
    for (int i = 0; i < thisData.length; i++) {
      if (!Misc.equals(thisData[i], thatData[i])) {
        return false;
      }
    }
    return true;
  }

  public void fill(E e) {
    Arrays.fill(data, e);
  }
  
  public void nullify () {
    Arrays.fill(data, null);
  }
  
  public static <T> void copy(ObjArray<? extends T> src, int srcPos,
                              ObjArray<T> dst, int dstPos, int len) {
    System.arraycopy(src.data, srcPos, dst.data, dstPos, len);
  }

  static final ObjArray<Object> zero = new ObjArray<Object>(0);
  @SuppressWarnings("unchecked")
  public static <T> ObjArray<T> zeroLength() {
    return (ObjArray<T>) zero;
  }
  
  @Override
  public Iterator<E> iterator () {
    return new Iterator<E>() {
      int idx = 0;

      @Override
	public boolean hasNext () {
        return idx < data.length;
      }

      @Override
	@SuppressWarnings("unchecked")
      public E next () {
        if (idx >= data.length) throw new NoSuchElementException();
        return (E) data[idx++];
      }

      @Override
	public void remove () {
        throw new UnsupportedOperationException();
      }
    };
  }
}
