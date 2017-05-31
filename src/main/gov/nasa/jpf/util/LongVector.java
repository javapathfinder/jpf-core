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
 * (more efficient?) alternative to Vector<Integer>
 * @author pcd
 */
public final class LongVector {
  public static final int defaultInitCap = 40;

  
  /** <i>size</i> as in a java.util.Vector. */
  protected int size;
  
  /** the backing array. */
  protected long[] data;
  
  /** growth strategy. */
  protected Growth growth;
  
  
  public LongVector(Growth initGrowth, int initCap) {
    growth = initGrowth;
    data = new long[initCap];
    size = 0;
  }
  
  public LongVector(Growth initGrowth) { this(initGrowth,defaultInitCap); }
  
  public LongVector(int initCap) { this(Growth.defaultGrowth, initCap); }
  
  public LongVector() { this(Growth.defaultGrowth,defaultInitCap); }
  
  
  public void add(long x) {
    if (size+1 > data.length) {
      ensureCapacity(size+1);
    }
    data[size] = x;
    size++;
  }
  
  public long get(int idx) {
    if (idx >= size) {
      return 0;
    } else {
      return data[idx];
    }
  }
  
  public void set(int idx, long x) {
    ensureSize(idx + 1);
    data[idx] = x;
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
  
  public void ensureSize(int sz) {
    if (size < sz) {
      ensureCapacity(sz);
      size = sz;
    }
  }
  
  public void ensureCapacity(int desiredCap) {
    if (data.length < desiredCap) {
      long[] newData = new long[growth.grow(data.length, desiredCap)];
      System.arraycopy(data, 0, newData, 0, size);
      data = newData;
    }
  }
  
  public static void copy(LongVector src, int srcPos, LongVector dst, int dstPos, int len) {
    if (len == 0) return;
    src.ensureCapacity(srcPos + len);
    dst.ensureSize(dstPos+len);
    System.arraycopy(src.data, srcPos, dst.data, dstPos, len);
  }
}
