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

import static java.lang.Integer.MIN_VALUE;

import java.util.Arrays;

/**
 * This has approximately the interface of ObjVector but uses a hash table
 * instead of an array.  Also, does not require allocation with each add. 
 */
public class SparseObjVector<E> {
  private static final boolean DEBUG = false; 
  
  static final double MAX_LOAD_WIPE = 0.6;
  static final double MAX_LOAD_REHASH = 0.4;
  static final int DEFAULT_POW = 10;

  int[]    idxTable;  // MIN_VALUE => unoccupied
  Object[] valTable;  // can be bound to null
  
  int count;
  int pow;
  int mask;
  int nextWipe;
  int nextRehash;
  
  
  /**
   * Creates a SimplePool that holds about 716 elements before first
   * rehash.
   */
  public SparseObjVector() {
    this(DEFAULT_POW);
  }
  
  /**
   * Creates a SimplePool that holds about 0.7 * 2**pow elements before
   * first rehash.
   */
  public SparseObjVector(int pow) {
    this.pow = pow;
    newTable();
    count = 0;
    mask = valTable.length - 1;
    nextWipe = (int)(MAX_LOAD_WIPE * mask);
    nextRehash = (int)(MAX_LOAD_REHASH * mask);
  }

  public void clear() {
    pow = DEFAULT_POW;
    newTable();
    count = 0;
    mask = valTable.length - 1;
    nextWipe = (int) (MAX_LOAD_WIPE * mask);
    nextRehash = (int) (MAX_LOAD_REHASH * mask);
  }
  
  // INTERNAL //
  
  @SuppressWarnings("unchecked")
  protected void newTable() {
    valTable = new Object[1 << pow];
    idxTable = new int[1 << pow];
    Arrays.fill(idxTable, MIN_VALUE);
  }
  
  protected int mix(int x) {
    int y = 0x9e3779b9;
    x ^= 0x510fb60d;
    y += (x >> 8) + (x << 3);
    x ^= (y >> 5) + (y << 2);
    return y - x;
  }
  
  
  // ********************* Public API ******************** //
  
  
  @SuppressWarnings("unchecked")
  public E get(int idx) {
    int code = mix(idx);
    int pos = code & mask;
    int delta = (code >> (pow - 1)) | 1; // must be odd!
    int oidx = pos;

    for(;;) {
      int tidx = idxTable[pos];
      if (tidx == MIN_VALUE) {
        return null;
      }
      if (tidx == idx) {
        return (E) valTable[pos];
      }
      pos = (pos + delta) & mask;
      assert (pos != oidx); // should never wrap around
    }
  }

  public void set(int idx, E e) {
    int code = mix(idx);
    int pos = code & mask;
    int delta = (code >> (pow - 1)) | 1; // must be odd!
    int oidx = pos;

    for(;;) {
      int tidx = idxTable[pos];
      if (tidx == MIN_VALUE) {
        break;
      }
      if (tidx == idx) {
        valTable[pos] = e; // update
        return;            // and we're done
      }
      pos = (pos + delta) & mask;
      assert (pos != oidx); // should never wrap around
    }
    // idx not in table; add it
    
    if ((count+1) >= nextWipe) { // too full
      if (count >= nextRehash) {
        pow++;
      }
      
      /**
      // determine if size needs to be increased or just wipe null blocks
      int oldCount = count;
      count = 0;
      for (int i = 0; i < idxTable.length; i++) {
        if (idxTable[i] != MIN_VALUE && valTable[i] != null) {
          count++;
        }
      }
      if (count >= nextRehash) {
        pow++; // needs to be increased in size
        if (DEBUG) {
          System.out.println("Rehash to capacity: 2**" + pow);
        }
      } else {
        if (DEBUG) {
          System.out.println("Rehash reclaiming this many nulls: " + (oldCount - count));
        }
      }
      **/
      
      Object[] oldValTable = valTable;
      int[] oldIdxTable = idxTable;
      newTable();
      mask = idxTable.length - 1;
      nextWipe = (int)(MAX_LOAD_WIPE * mask);
      nextRehash = (int)(MAX_LOAD_REHASH * mask);

      int oldLen = oldIdxTable.length;
      for (int i = 0; i < oldLen; i++) {
        int tidx = oldIdxTable[i];
        if (tidx == MIN_VALUE) continue;
        Object o = oldValTable[i];
        //if (o == null) continue;
        // otherwise:
        code = mix(tidx);
        pos = code & mask;
        delta = (code >> (pow - 1)) | 1; // must be odd!
        while (idxTable[pos] != MIN_VALUE) { // we know enough slots exist
          pos = (pos + delta) & mask;
        }
        idxTable[pos] = tidx;
        valTable[pos] = o;
      }
      // done with rehash; now get idx to empty slot
      code = mix(idx);
      pos = code & mask;
      delta = (code >> (pow - 1)) | 1; // must be odd!
      while (idxTable[pos] != MIN_VALUE) { // we know enough slots exist
        pos = (pos + delta) & mask;
      }
    } else {
      // pos already pointing to empty slot
    }

    count++;
    
    idxTable[pos] = idx;
    valTable[pos] = e;
  }
  
  public void remove(int idx) {
    set(idx, null);
  }
  
  
  // ************************** Test main ************************ //
  
  public static void main(String[] args) {
    SparseObjVector<Integer> vect = new SparseObjVector<Integer>(3);
    
    // add some
    for (int i = -4200; i < 4200; i += 10) {
      vect.set(i, new Integer(i));
    }
    
    // check for added & non-added
    for (int i = -4200; i < 4200; i += 10) {
      Integer v = vect.get(i);
      if (v.intValue() != i) {
        throw new IllegalStateException();
      }
    }
    for (int i = -4205; i < 4200; i += 10) {
      Integer v = vect.get(i);
      if (v != null) {
        throw new IllegalStateException();
      }
    }
    
    // add some more
    for (int i = -4201; i < 4200; i += 10) {
      vect.set(i, new Integer(i));
    }

    // check all added
    for (int i = -4200; i < 4200; i += 10) {
      Integer v = vect.get(i);
      if (v.intValue() != i) {
        throw new IllegalStateException();
      }
    }
    for (int i = -4201; i < 4200; i += 10) {
      Integer v = vect.get(i);
      if (v.intValue() != i) {
        throw new IllegalStateException();
      }
    }
    
    // "remove" some
    for (int i = -4200; i < 4200; i += 10) {
      vect.remove(i);
    }
    
    // check for added & non-added
    for (int i = -4201; i < 4200; i += 10) {
      Integer v = vect.get(i);
      if (v.intValue() != i) {
        throw new IllegalStateException();
      }
    }
    for (int i = -4200; i < 4200; i += 10) {
      Integer v = vect.get(i);
      if (v != null) {
        throw new IllegalStateException();
      }
    }

    // add even more
    for (int i = -4203; i < 4200; i += 10) {
      vect.set(i, new Integer(i));
    }
    for (int i = -4204; i < 4200; i += 10) {
      vect.set(i, new Integer(i));
    }
  }
}
