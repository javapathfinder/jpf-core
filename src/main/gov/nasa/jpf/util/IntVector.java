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

import gov.nasa.jpf.JPFException;


/**
 * (more efficient?) alternative to Vector<Integer>
 * @author pcd
 */
public final class IntVector implements Comparable<IntVector>, Cloneable {
  public static final int defaultInitCap = 40;

  
  /** <i>size</i> as in a java.util.Vector. */
  protected int size;
  
  /** the backing array. */
  protected int[] data;
  
  /** growth strategy. */
  protected Growth growth;
  
  
  public IntVector(Growth initGrowth, int initCap) {
    growth = initGrowth;
    data = new int[initCap];
    size = 0;
  }
  
  public IntVector(int... init) {
    this(Growth.defaultGrowth, init.length);
    size = init.length;
    System.arraycopy(init, 0, data, 0, size);
  }
  
  public IntVector(Growth initGrowth) { this(initGrowth,defaultInitCap); }
  
  public IntVector(int initCap) { this(Growth.defaultGrowth, initCap); }
  
  public IntVector() { this(Growth.defaultGrowth,defaultInitCap); }

  @Override
  public IntVector clone() {
    try {
      IntVector v = (IntVector)super.clone();
      v.data = data.clone();
      return v;

    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("IntVector clone failed");
    }
  }

  public void add(int x) {
    try {
      data[size] = x;
      size++;
    } catch (ArrayIndexOutOfBoundsException aobx){
      ensureCapacity(size+1);
      data[size] = x;
      size++;
    }
  }

  public boolean addIfAbsent (int x) {
    for (int i=0; i<size; i++){
      if (data[i] == x){
        return false;
      }
    }
    add(x);
    return true;
  }

  public void add (long x) {
    if (size+2 > data.length) {
      ensureCapacity(size+2);
    }
    data[size] = (int)(x >> 32);
    size++;
    data[size] = (int)x;
    size++;    
  }
  
  public void add(int x1, int x2) {
    if (size+2 > data.length) {
      ensureCapacity(size+2);
    }
    data[size] = x1;
    size++;
    data[size] = x2;
    size++;
  }
  
  public void add(int x1, int x2, int x3) {
    if (size+3 > data.length) {
      ensureCapacity(size+3);
    }
    data[size] = x1;
    size++;
    data[size] = x2;
    size++;
    data[size] = x3;
    size++;
  }
  
  public void addZeros (int length) {
    int newSize = size + length;
    if (newSize > data.length) {
      ensureCapacity(size + length);
    }
    for (int i = size; i < newSize; i++) {
      data[i] = 0;
    }
    size = newSize;
  }

  public void append(int[] a) {
    int newSize = size + a.length;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }
    System.arraycopy(a, 0, data, size, a.length);
    size = newSize;
  }

  public void append(int[] x, int pos, int len) {
    int newSize = size + len;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }
    System.arraycopy(x, pos, data, size, len);
    size = newSize;
  }

  public void append(boolean[] a){
    int newSize = size + a.length;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }
    for (int i = size; i < newSize; i++) {
      data[i] = a[i] ? 1 : 0;
    }
    size = newSize;
  }

  public void appendPacked(boolean[] a){
    int len = (a.length+31) >> 5;  // new data length, 32 booleans per word
    int n = a.length >> 5; // number of full data words
    int len1 = n << 5;
    int rem = a.length % 32;

    int newSize = size + len;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }

    int k=size;
    int x=0;
    int i=0;
    while (i<len1){ // store full data words
      x = a[i++] ? 1 : 0;
      for (int j=1; j<32; j++){
        x <<= 1;
        if (a[i++]){
          x |= 1;
        }
      }
      data[k++] = x;
    }

    if (rem > 0) { // store partial word
      if (i>0){
        x = a[i-1] ? 1 : 0;
      } else {
        x = a[i++] ? 1 : 0;
      }
      while (i < a.length) {
        x <<= 1;
        if (a[i++]) {
          x |= 1;
        }
      }
      x <<= (32-rem);
      data[k] = x;
    }

    size = newSize;
  }

  public void appendPacked(byte[] a){
    int len = (a.length+3)/4;  // new data length, 4 bytes per word
    int n = a.length >> 2; // number of full data words
    int len1 = n << 2;

    int newSize = size + len;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }

    int j=0;
    int k=size;
    int x;
    while (j<len1){
      x = a[j++] & 0xff;  x <<= 8;
      x |= a[j++] & 0xff; x <<= 8;
      x |= a[j++] & 0xff; x <<= 8;
      x |= a[j++] & 0xff;
      data[k++] = x;
    }

    switch (a.length % 4){
      case 0:
        break;
      case 1:
        data[k] = (a[j] << 24);
        break;
      case 2:
        x = a[j++] & 0xff;  x <<= 8;
        x |= a[j] & 0xff;   x <<= 16;
        data[k] = x;
        break;
      case 3:
        x = a[j++] & 0xff;  x <<= 8;
        x |= a[j++] & 0xff; x <<= 8;
        x |= a[j] & 0xff;   x <<= 8;
        data[k] = x;
        break;
    }

    size = newSize;
  }


  public void appendPacked(char[] a){
    int len = (a.length+1)/2;  // new data length, 2 chars per word
    int n = a.length >> 1; // number of full data words
    int len1 = n << 1;

    int newSize = size + len;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }

    int j=0;
    int k=size;
    while (j<len1){
      int x = a[j++] & 0xffff;  x <<= 16;
      x |= a[j++] & 0xffff;
      data[k++] = x;
    }

    if (a.length % 2 > 0){
      data[k] = (a[j] & 0xffff) << 16;
    }

    size = newSize;
  }

  public void appendPacked(short[] a){
    int len = (a.length+1)/2;  // new data length, 2 chars per word
    int n = a.length >> 1; // number of full data words
    int len1 = n << 1;

    int newSize = size + len;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }

    int j=0;
    int k=size;
    while (j<len1){
      int x = a[j++] & 0xffff;  x <<= 16;
      x |= a[j++] & 0xffff;
      data[k++] = x;
    }

    if (a.length % 2 > 0){
      data[k] = (a[j] & 0xffff) << 16;
    }

    size = newSize;
  }

  public void appendRawBits(float[] a){
    int newSize = size + a.length;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }
    int k=size;
    for (int i = 0; i < a.length; i++) {
      data[k++] = Float.floatToRawIntBits(a[i]);
    }
    size = newSize;
  }


  public void appendBits(long[] a){
    int newSize = size + a.length * 2;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }
    
    int k = size;
    for (int i = 0; i<a.length; i++){
      long l = a[i];
      data[k++] = (int) (l >>> 32);
      data[k++] = (int) (l & 0xffffffff);
    }
    
    size = newSize;    
  }

  public void appendRawBits(double[] a){
    int newSize = size + a.length * 2;
    if (newSize > data.length) {
      ensureCapacity(newSize);
    }

    int k = size;
    for (int i = 0; i<a.length; i++){
      long l = Double.doubleToRawLongBits(a[i]);
      data[k++] = (int) (l >>> 32);
      data[k++] = (int) (l & 0xffffffff);
    }

    size = newSize;
  }

  
  public void append(IntVector x) {
    if (x == null) return;
    if (size + x.size > data.length) {
      ensureCapacity(size + x.size);
    }
    System.arraycopy(x.data, 0, data, size, x.size);
    size += x.size;
  }

  public boolean removeFirst (int x){
    for (int i=0; i<size; i++){
      if (data[i] == x){
        System.arraycopy(data,i+1, data,i, size-i);
        size--;
        return true;
      }
    }

    return false;
  }

  public int get(int idx) {
    if (idx >= size) {
      return 0;
    } else {
      return data[idx];
    }
  }
  
  public void set(int idx, int x) {
    ensureSize(idx + 1);
    data[idx] = x;
  }

  public int getFirstIndexOfValue(int x) {
    for (int i=0; i<size; i++){
      if (data[i] == x){
        return i;
      }
    }
    return -1;
  }

  public boolean contains (int x) {
    for (int i=0; i<size; i++){
      if (data[i] == x){
        return true;
      }
    }
    return false;
  }


  public int[] toArray (int[] dst) {
    System.arraycopy(data,0,dst,0,size);
    return dst;
  }

  public int dumpTo (int[] dst, int pos) {
    System.arraycopy(data,0,dst,pos,size);
    return pos + size;
  }

  public void squeeze() {
    while (size > 0 && data[size - 1] == 0) size--;
  }
  
  public void setSize(int sz) {
    if (sz > size) {
      ensureCapacity(sz);
      size = sz;
    } else {
      while (size > sz) {
        size--;
        data[size] = 0;
      }
    }
  }

  public void clear() { setSize(0); }
  
  public int size() { return size; }
  
  public int[] toArray() {
    int[] out = new int[size];
    System.arraycopy(data, 0, out, 0, size);
    return out;
  }

  public void ensureSize(int sz) {
    if (size < sz) {
      ensureCapacity(sz);
      size = sz;
    }
  }
  
  public void ensureCapacity(int desiredCap) {
    if (data.length < desiredCap) {
      int[] newData = new int[growth.grow(data.length, desiredCap)];
      System.arraycopy(data, 0, newData, 0, size);
      data = newData;
    }
  }
  
  public static void copy(IntVector src, int srcPos, IntVector dst, int dstPos, int len) {
    if (len == 0) return;
    src.ensureCapacity(srcPos + len);
    dst.ensureSize(dstPos+len);
    System.arraycopy(src.data, srcPos, dst.data, dstPos, len);
  }

  public static void copy(int[] src, int srcPos, IntVector dst, int dstPos, int len) {
    if (len == 0) return;
    dst.ensureSize(dstPos+len);
    System.arraycopy(src, srcPos, dst.data, dstPos, len);
  }

  public static void copy(IntVector src, int srcPos, int[] dst, int dstPos, int len) {
    if (len == 0) return;
    src.ensureCapacity(srcPos + len);
    System.arraycopy(src.data, srcPos, dst, dstPos, len);
  }

  /** dictionary/lexicographic ordering */
  @Override
  public int compareTo (IntVector that) {
    if (that == null) return this.size; // consider null and 0-length the same
    
    int comp;
    int smaller = Math.min(this.size, that.size);
    for (int i = 0; i < smaller; i++) {
      comp = this.data[i] - that.data[i];
      if (comp != 0) return comp;
    }
    // same up to shorter length => compare sizes
    return this.size - that.size;
  }
}
