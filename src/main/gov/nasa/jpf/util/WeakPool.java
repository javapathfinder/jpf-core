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

import java.lang.ref.WeakReference;

/**
 * This is a simplified hash pool that does not support removal or
 * numbering of elements.
 */
public class WeakPool<E> {
  private static final boolean DEBUG = false; 
  
  static final double MAX_LOAD_WIPE = 0.6;
  static final double MAX_LOAD_REHASH = 0.4;
  static final int DEFAULT_POW = 10;

  WeakReference<E>[] table;
  
  int count;
  int pow;
  int mask;
  int nextWipe;
  int nextRehash;
  
  /**
   * Creates a SimplePool that holds about 716 elements before first
   * rehash.
   */
  public WeakPool() {
    this(DEFAULT_POW);
  }
  
  /**
   * Creates a SimplePool that holds about 0.7 * 2**pow elements before
   * first rehash.
   */
  public WeakPool(int pow) {
    this.pow = pow;
    newTable();
    count = 0;
    mask = table.length - 1;
    nextWipe = (int)(MAX_LOAD_WIPE * mask);
    nextRehash = (int)(MAX_LOAD_REHASH * mask);
  }

  @SuppressWarnings("unchecked")
  protected void newTable() {
    table = new WeakReference[1 << pow];
  }
  
  // ********************** API as simple hash pool ******************* //
  
  /**
   * Asks whether a particular element is already pooled.  NOT A TYPICAL
   * OPERATION.
   */
  public boolean isPooled(E e) {
    return e == null || query(e) != null;
  }
  
  /**
   * Returns the matching element if there is one, null if not.
   */
  public E query(E e) {
    if (e == null) return null;
    int code = e.hashCode();
    int idx = code & mask;
    int delta = (code >> (pow - 1)) | 1; // must be odd!
    int oidx = idx;

    for(;;) {
      WeakReference<E> r = table[idx];
      if (r == null) break;
      E o = r.get();
      if (o != null && e.equals(o)) {
        return o; // seen before!
      }
      idx = (idx + delta) & mask;
      assert (idx != oidx); // should never wrap around
    }
    return null;
  }

  /**
   * Returns a pooled element matching e, which will be e if no match
   * has been previously pooled.
   */
  public E pool(E e) {
    if (e == null) return null;
    int code = e.hashCode();
    int idx = code & mask;
    int delta = (code >> (pow - 1)) | 1; // must be odd!
    int oidx = idx;

    for(;;) {
      WeakReference<E> r = table[idx];
      if (r == null) break;
      E o = r.get();
      if (o != null && e.equals(o)) {
        return o; // seen before!
      }
      idx = (idx + delta) & mask;
      assert (idx != oidx); // should never wrap around
    }
    assert (table[idx] == null); // should never fill up
    // not seen before; add it
    
    count++;
    if (count >= nextWipe) { // too full
      // determine if size needs to be increased or just wipe unused weak refs
      int oldCount = count;
      count = 0;
      for (int i = 0; i < table.length; i++) {
        if (table[i] != null && table[i].get() != null) {
          count++;
        }
      }
      if (DEBUG && oldCount > count) {
        System.out.println("Weak references collected: " + (oldCount - count));
      }
      if (count >= nextRehash) {
        pow++; // needs to be increased in size
      }
      WeakReference<E>[] oldTable = table;
      newTable();
      mask = table.length - 1;
      nextWipe = (int)(MAX_LOAD_WIPE * mask);
      nextRehash = (int)(MAX_LOAD_REHASH * mask);

      int oldLen = oldTable.length;
      for (int i = 0; i < oldLen; i++) {
        WeakReference<E> r = oldTable[i];
        if (r == null) continue;
        E o = r.get();
        if (o == null) continue;
        // otherwise:
        code = o.hashCode();
        idx = code & mask;
        delta = (code >> (pow - 1)) | 1; // must be odd!
        while (table[idx] != null) { // we know enough slots exist
          idx = (idx + delta) & mask;
        }
        table[idx] = r;
      }
      // done with rehash; now get idx to empty slot
      code = e.hashCode();
      idx = code & mask;
      delta = (code >> (pow - 1)) | 1; // must be odd!
      while (table[idx] != null) { // we know enough slots exist & new element
        idx = (idx + delta) & mask;
      }
    } else {
      // idx already pointing to empty slot
    }

    table[idx] = new WeakReference<E>(e);
    return e;
  }
  
  
  // ******************* API as add-only weak hash set *************** //
  
  public boolean isMember(E e) {
    return query(e) != null;
  }
  
  public void add(E e) {
    /*(void)*/ pool(e);
  }
  
  
  // ************************** Test main ************************ //
  
  /**
   * BROKEN Test main.
   */
  public static void main(String[] args) {
    WeakPool<Integer> pool = new WeakPool<Integer>(4);
    for (int i = 0; i < 1000000; i += 42) {
      Integer o = new Integer(i);
      Integer p = pool.pool(o);
      if (o != p) throw new RuntimeException();
      Integer q = pool.pool(p);
      if (q != p) throw new RuntimeException();
    }
    for (int i = 0; i < 1000000; i += 42) {
      Integer o = new Integer(i);
      Integer p = pool.pool(o);
      if (o == p) throw new RuntimeException();
      if (!o.equals(p)) throw new RuntimeException();
    }
    for (int i = 1; i < 1000000; i += 42) {
      Integer o = new Integer(i);
      Integer p = pool.pool(o);
      if (o != p) throw new RuntimeException();
    }
  }
}
