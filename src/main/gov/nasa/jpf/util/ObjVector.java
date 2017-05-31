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

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * more customizable alternative to java.util.Vector. Other than Vector, it
 * supports dynamic growth on set() operations. While it supports list
 * functions such as append, ObjVector resembles mostly an array, i.e.
 * is meant to be a random-access collection
 * 
 * this collection does not keep a count of non-null elements, but does maintain the
 * highest set index as its size through set/add and remove operations. Note that size
 * only shrinks through remove operations, not by setting null values. This means there
 * is no guarantee that data[size-1] is not null. The converse however is true - there is no
 * non-null element at an index >= size.
 * 
 * @author pcd
 */
public class ObjVector<E> implements ReadOnlyObjList<E>, Cloneable {
  public static final int defaultInitCap = 40;  

  /** <i>size</i> as in a java.util.Vector. */
  protected int size;
  
  /** the backing array. */
  protected Object[] data;
  
  /** growth strategy. */
  protected Growth growth;
    
  
  //--- constructors
  
  public ObjVector(Growth initGrowth, int initCap) {
    growth = initGrowth;
    data = new Object[initCap];
    size = 0;
  }
  
  public ObjVector(Growth initGrowth) {
    this(initGrowth,defaultInitCap);
  }
  
  public ObjVector(int initCap) { 
    this(Growth.defaultGrowth, initCap);
  }
  
  public ObjVector() {
    this(Growth.defaultGrowth,defaultInitCap);
  }
  
  public <F extends E> ObjVector(F[] init) {
    this(init.length);
    append(init);
  }
  
  public <F extends E> ObjVector(ObjVector<F> from) {
    this.data = new Object[from.data.length];
    this.size = from.size;
    this.growth = from.growth;
    System.arraycopy(from.data, 0, this.data, 0, size);
  }
  
  //--- set/add/remove operations  
  
  public void add(E x) {
    if (size >= data.length) {
      ensureCapacity(size+1);
    }
    data[size] = x;
    size++;
  }
  
  public void addNulls (int length) {
    int newSize = size + length;
    if (newSize > data.length) {
      ensureCapacity(size + length);
    }
    for (int i = size; i < newSize; i++) {
      data[i] = null;
    }
    size = newSize;
  }

  public <F extends E> void append(F[] x) {
    if (size + x.length > data.length) {
      ensureCapacity(size + x.length);
    }
    System.arraycopy(x, 0, data, size, x.length);
    size += x.length;
  }

  public <F extends E> void append(F[] x, int pos, int len) {
    if (size + len > data.length) {
      ensureCapacity(size + len);
    }
    System.arraycopy(x, pos, data, size, len);
    size += len;
  }
  
  public <F extends E> void append(ObjVector<F> x) {
    if (size + x.size > data.length) {
      ensureCapacity(size + x.size);
    }
    System.arraycopy(x.data, 0, data, size, x.size);
    size += x.size;
  }
  
  @SuppressWarnings("unchecked")
  public <F extends E> void append(ObjArray<F> x) {
    append((F[])(x.data));
  }

  public <F extends E> void append(ArrayList<F> x){
    int n = x.size();
    int newSize = size + n;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }
    for (int i = size, j=0; i < newSize; i++,j++) {
      data[i] = x.get(j);
    }
    size = newSize;
  }

  public <F extends E> void addAll(Iterable<F> x) {
    if (x instanceof ObjVector) {
      append((ObjVector<F>) x);
      return;
    }
    // else
    if (x instanceof ObjArray) {
      append((ObjArray<F>) x);
      return;
    }
    // else
    if (x == null) return;
    // else
    for (F e : x) {
      add(e);
    }
  }

  public int nextNull (int fromIndex){
    for (int i=fromIndex; i<size; i++){
      if (data[i] == null){
        return i;
      }
    }

    ensureCapacity(size+1);
    return size;
  }

  @Override
@SuppressWarnings("unchecked")
  public E get(int idx) {
    if (idx >= size) {
      return null;
    } else {
      return (E) data[idx];
    }
  }
  
  public void set(int idx, E v) {
    ensureSize(idx+1);
    data[idx] = v;
  }

  /**
   * set range of values
   * @param fromIndex first index (inclusive)
   * @param toIndex last index (exclusive)
   * @param val value to set
   */
  public void setRange (int fromIndex, int toIndex, E val) {
    ensureSize(toIndex);
    Arrays.fill(data, fromIndex, toIndex, val);
  }
  
  public <F> F[] toArray (F[] dst) {
    System.arraycopy(data,0,dst,0,size);
    return dst;
  }

  public ObjArray<E> toObjArray () {
    ObjArray<E> dst = new ObjArray<E>(size);
    System.arraycopy(data,0,dst.data,0,size);
    return dst;
  }

  public int dumpTo (Object[] dst, int pos) {
    System.arraycopy(data,0,dst,pos,size);
    return pos + size;
  }

  @Override
  public ObjVector<E> clone() {
    return new ObjVector<E>(this);
  }
  
  public void squeeze() {
    while (size > 0 && data[size - 1] == null) size--;
  }
  
  public void setSize(int sz) {
    if (sz > size) {
      ensureCapacity(sz);
      size = sz;
    } else {
      while (size > sz) {
        size--;
        data[size] = null;
      }
    }
  }

  public void clear() { 
    // faster than iterating over the whole array
    data = new Object[data.length];
    size = 0;
  }
  
  @SuppressWarnings("unchecked")
  public void clearAllSatisfying (Predicate<E> pred) {
    Object[] d = data;
    int newSize = 0;
    for (int i=size-1; i>=0; i--) {
      E e = (E)d[i];
      if (e != null) {
        if (pred.isTrue(e)) {
          d[i] = null;
        } else {
          if (newSize == 0) {
            newSize = i+1;
          }
        }
      }
    }
    
    size = newSize;
  }
  
  public int size() { 
    return size; 
  }
  
  @Override
  public int length() {
    return size;
  }
  
  public void ensureSize(int sz) {
    if (size < sz) {
      ensureCapacity(sz);
      size = sz;
    }
  }
  
  public void ensureCapacity(int desiredCap) {
    if (data.length < desiredCap) {
      Object[] newData = new Object[growth.grow(data.length, desiredCap)];
      System.arraycopy(data, 0, newData, 0, size);
      data = newData;
    }
  }
  
  @SuppressWarnings("unchecked")
  public void sort(Comparator<? super E> comp) {
    Arrays.sort(data, 0, size, (Comparator<Object>) comp);
  }
  
  public static <E> void copy(ObjVector<? extends E> src, int srcPos,
                              ObjVector<E> dst, int dstPos, int len) {
    src.ensureCapacity(srcPos + len);
    dst.ensureSize(dstPos+len);
    System.arraycopy(src.data, srcPos, dst.data, dstPos, len);
  }
  
  public static <E> void copy(ObjVector<? extends E> src, int srcPos,
      E[] dst, int dstPos, int len) {
    src.ensureCapacity(srcPos + len);
    //dst.ensureSize(dstPos+len);
    System.arraycopy(src.data, srcPos, dst, dstPos, len);
  }

  /**
   * remove all non-null elements between 'fromIdx' (inclusive) and
   * 'toIdx' (exclusive)
   * throw IndexOutOfBoundsException if index values are out of range
   */
  public int removeRange(int fromIdx, int toIdx){
    int n = 0;
    Object[] data = this.data;

    // it's the callers responsibility to ensure proper index ranges
    //if (fromIdx < 0) fromIdx = 0;
    //if (toIdx > size) toIdx = size;

    for (int i=fromIdx; i<toIdx; i++){
      if (data[i] != null){
        data[i] = null;
        n++;
      }
    }

    if (toIdx >= size){
      int i=fromIdx-1;
      for (; i>=0 && (data[i] == null); i--);
      size = i+1;
    }

    return n;
  }

  public int removeFrom(int fromIdx){
    return removeRange(fromIdx,size);
  }

  public E remove (int i) {
    E e = (E) data[i];
    
    if (e != null) {
      data[i] = null;
      if (i+1 == size) {
        int j=i-1;
        for (; j>=0 && (data[j] == null); j--); 
        size = j+1;
      }
    }
    
    return e;
  }

  //--- store/restore snapshot operations
    
  static final int DEFAULT_MAX_GAP = 10;
  
  /**
   * this is a block operation snapshot that stores chunks of original data with
   * not more than DEFAULT_MAX_GAP consecutive null elements. Use this if 
   * elements can be stored directly
   */
  public static class Snapshot<E> {
    static class Block {
      int baseIndex;
      Object[] data;
      Block next;
      
      Block (int baseIndex, Object[] data, Block next){
        this.baseIndex = baseIndex;
        this.data = data;
        this.next = next;
      }
    }
    
    // the ObjVector state we directly store
    int size;
    Growth growth;
    
    // where we keep the data
    Block head;
    
    int saveBlock (Object[] d, int start, int end) {
      int len = end-start+1;
      Object[] bd = new Object[len];
      System.arraycopy(d, start, bd, 0, len);
      head = new Block(start, bd, head);      
      
      return len;
    }
    
    Snapshot (ObjVector<E> v, int maxGap){
      int n = v.size;
      size = n;
      growth = v.growth;      
      Object[] d = v.data;
      
      int end = -1, start = -1;
      
      for (int i=n-1; (i>=0) && (n>0); i--) {
        if (d[i] != null) {
          if (start > 0 && (start - i) > maxGap ) { // store prev block
            n -= saveBlock( d, start, end);              
            end = i;
            start = i;
            
          } else {
            if (end < 0) {
              end = i;
            }
            start = i;
          }
        }
      }
      
      if (end >=0 && end >= start) {
        saveBlock( d, start, end);
      }
    }    
    
    public void restore (ObjVector<E> v) {
      // this is faster than iterating through the array
      Object[] d = new Object[size];
      v.data = d;

      for (Block block = head; block != null; block = block.next) {
        Object[] bd = block.data;
        System.arraycopy(bd, 0, d, block.baseIndex, bd.length);
      }
      
      v.size = size;
      v.growth = growth;
    }
  }

  
  public Snapshot<E> getSnapshot(){
    return new Snapshot<E>(this, DEFAULT_MAX_GAP);
  }
  
  /**
   * create a snapshot that doesn't store more than maxGap consecutive null values
   */
  public Snapshot<E> getSnapshot( int maxGap){
    return new Snapshot<E>(this, maxGap);
  }
  
  public void restore (Snapshot<E> snap) {
    snap.restore(this);
  }

  
  /**
   *  snapshot that can mutate element values, but therefore can't use block operations.
   *  This is slower to store/restore, but can be more memory efficient if the elements
   *  are fragmented (lots of small holes in data)
   */
  
  public static class MutatingSnapshot<E,T>{
    T[] values;
    int[] indices;
    
    @SuppressWarnings("unchecked")
    MutatingSnapshot (ObjVector<E> vec, Transformer<E,T> transformer){
      E[] d = (E[])vec.data;
      int size = vec.size;
      int len = 0;
      
      //--- get number of non-null elements
      for (int i=0; i<size; i++) {
        if (d[i] != null) {
          len++;
        }
      }
      
      //--- allocate data
      T[] values = (T[])new Object[len];
      int[] indices = new int[len];
      
      //--- fill it
      int j=0;
      for (int i=0; j < len; i++) {
        if (d[i] != null) {
          indices[j] = i;
          values[j] = transformer.transform(d[i]);
          j++;
        }
      }
      
      this.values = values;
      this.indices = indices;
    }
    
    @SuppressWarnings("unchecked")
    protected void restore (ObjVector<E> vec, Transformer<T,E> transformer) {
      T[] values = this.values;
      int[] indices = this.indices;
      int len = indices.length;
      int size = indices[len-1] +1;

      vec.clear();
      vec.ensureSize(size);
      E[] d = (E[])vec.data;

      for (int i=0; i<len; i++){
        E obj = transformer.transform(values[i]);
        int index = indices[i];
        d[index] = obj;
      }
      
      vec.size = size;
    }
  }
  
  public <T> MutatingSnapshot<E,T> getSnapshot( Transformer<E,T> transformer){
    return new MutatingSnapshot<E,T>(this, transformer);
  }
  
  public <T> void restore (MutatingSnapshot<E,T> snap, Transformer<T,E> transformer) {
    snap.restore(this, transformer);
  }
  

  //--- iterators
  
  /**
   * iterator that goes over all elements regardless of value (i.e. also includes null values)
   */
  protected class OVIterator implements Iterator<E> {
    int idx = 0;
    
    @Override
	public boolean hasNext () {
      return idx < size;
    }

    @Override
	@SuppressWarnings("unchecked")
    public E next () {
      if (idx >= data.length) throw new NoSuchElementException();
      E e = (E) data[idx];
      idx++;
      return e;
    }

    @Override
	public void remove () {
      throw new UnsupportedOperationException();
    }
  }

  @Override
  public Iterator<E> iterator () {
    return new OVIterator();
  }
  
  /**
   * iterator that only includes element values that are not null
   */
  protected class NonNullIterator implements Iterator<E>, Iterable<E> {
    int idx = 0;
    //int count = 0;

    @Override
	public boolean hasNext() {
      return (idx < size); // size is max set index
    }

    @Override
	@SuppressWarnings("unchecked")
    public E next () {
      int len = data.length;
      for (int i=idx; i<len; i++){
        Object o = data[i];
        if (o != null){
          //count++;
          idx = i+1;
          return (E)o;
        }
      }

      throw new NoSuchElementException();
    }

    @Override
	public void remove () {
      throw new UnsupportedOperationException();
    }

    @Override
	public Iterator<E> iterator() {
      return this;
    }
  }
  


  public Iterator<E> nonNullIterator() {
    return new NonNullIterator();
  }

  public Iterable<E> elements() {
    return new NonNullIterator();
  }

  public void process (Processor<E> processor) {
    for (int i=0; i<data.length; i++) {
      Object o = data[i];
      if (o != null) {
        processor.process( (E)o);
      }
    }
  }

  //--- misc (debugging etc.)
  
  public void printOn (PrintStream ps) {
    ps.println("ObjVector = [");
    for (int i=0; i<size; i++) {
      ps.print("  ");
      ps.print(i);
      ps.print(": ");
      ps.println(get(i));
    }
    ps.println(']');
  }

}
