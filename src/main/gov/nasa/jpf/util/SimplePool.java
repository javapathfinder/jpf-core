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
 * This is a simplified hash pool that does not support removal or
 * numbering of elements.
 */
@SuppressWarnings("unchecked")
public class SimplePool<E> {
  static final double MAX_LOAD = 0.7;
  static final int DEFAULT_POW = 10;

  Object[] table;
  
  int count;
  int pow;
  int mask;
  int nextRehash;
  
  /**
   * Creates a SimplePool that holds about 716 elements before first
   * rehash.
   */
  public SimplePool() {
    this(DEFAULT_POW);
  }
  
  /**
   * Creates a SimplePool that holds about 0.7 * 2**pow elements before
   * first rehash.
   */
  public SimplePool(int pow) {
    table = new Object[1 << pow];
    count = 0;
    this.pow = pow;
    mask = table.length - 1;
    nextRehash = (int)(MAX_LOAD * mask);
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
      Object o = table[idx];
      if (o == null) break;
      if (e.equals(o)) {
        return (E) o; // seen before!
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
      Object o = table[idx];
      if (o == null) break;
      if (e.equals(o)) {
        return (E) o; // seen before!
      }
      idx = (idx + delta) & mask;
      assert (idx != oidx); // should never wrap around
    }
    assert (table[idx] == null); // should never fill up
    // not seen before; add it
    
    count++;
    if (count >= nextRehash) { // too full
      Object[] oldTable = table;
      pow++;
      table = new Object[1 << pow];
      mask = table.length - 1;
      nextRehash = (int)(MAX_LOAD * mask);

      int oldLen = oldTable.length;
      for (int i = 0; i < oldLen; i++) {
        Object o = oldTable[i];
        if (o != null) {
          code = o.hashCode();
          idx = code & mask;
          delta = (code >> (pow - 1)) | 1; // must be odd!
          while (table[idx] != null) { // we know enough slots exist
            idx = (idx + delta) & mask;
          }
          table[idx] = o;
        }
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

    table[idx] = e;
    return e;
  }
  
  
  // ******************* API as add-only hash set *************** //
  
  public boolean isMember(E e) {
    return query(e) != null;
  }
  
  public void add(E e) {
    /*(void)*/ pool(e);
  }
  
  
  // ************************** Test main ************************ //
  
  /**
   * Test main.
   */
  public static void main(String[] args) {
    SimplePool<Integer> pool = new SimplePool<Integer>(4);
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
