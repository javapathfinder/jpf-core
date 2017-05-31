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
 * This has approximately the interface of IntVector but uses a hash table
 * instead of an array.  Also, does not require allocation with each add.
 * Configurable default value. 
 */
public class SparseIntVector implements Cloneable {
  private static final boolean DEBUG = false;
  
  static final double MAX_LOAD_WIPE = 0.6;
  static final double MAX_LOAD_REHASH = 0.4;
  static final int DEFAULT_POW = 10;
  static final int DEFAULT_VAL = 0;

  /**
   * a simplistic snapshot implementation that stores set indices/values in order to save space
   */
  public static class Snapshot {
    private final int length;
    private final int pow, mask, nextWipe, nextRehash;
    
    private final int[] positions;
    private final int[] indices;
    private final int[] values;
    
    Snapshot (SparseIntVector v){
      int len = v.idxTable.length;
      
      length = len;
      pow = v.pow;
      mask = v.mask;
      nextWipe = v.nextWipe;
      nextRehash = v.nextRehash;
      
      int size = v.count;
      positions = new int[size];
      indices = new int[size];
      values = new int[size];
      
      int[] idxTable = v.idxTable;
      int[] valTable = v.valTable;
      int j=0;
      for (int i=0; i<len; i++) {
        if (idxTable[i] != MIN_VALUE) {
          positions[j] = i;
          indices[j] = idxTable[i];
          values[j] = valTable[i];
          j++;
        }
      }
    }
    
    void restore (SparseIntVector v) {
      int size = indices.length;
      
      v.count = size;
      v.pow = pow;
      v.mask = mask;
      v.nextWipe = nextWipe;
      v.nextRehash = nextRehash;
      
      int len = length;
      int[] idxTable = new int[len];
      int[] valTable = new int[len];
      
      Arrays.fill(idxTable, MIN_VALUE);
      
      for (int i=0; i<size; i++) {
        int j = positions[i];        
        idxTable[j] = indices[i];
        valTable[j] = values[i];
      }
      
      v.idxTable = idxTable;
      v.valTable = valTable;
    }
  }
  
  int[] idxTable;  // MIN_VALUE => unoccupied
  int[] valTable;  // can be bound to null
  
  int count;
  int pow;
  int mask;
  int nextWipe;
  int nextRehash;
  
  int defaultValue;
    
  /**
   * Creates a SimplePool that holds about 716 elements before first
   * rehash.
   */
  public SparseIntVector() {
    this(DEFAULT_POW,DEFAULT_VAL);
  }
  
  /**
   * Creates a SimplePool that holds about 0.7 * 2**pow elements before
   * first rehash.
   */
  public SparseIntVector(int pow, int defValue) {
    this.pow = pow;
    newTable();
    count = 0;
    mask = valTable.length - 1;
    nextWipe = (int)(MAX_LOAD_WIPE * mask);
    nextRehash = (int)(MAX_LOAD_REHASH * mask);
    defaultValue = defValue;
  }  
  
  // INTERNAL //
  
  @SuppressWarnings("unchecked")
  protected void newTable() {
    valTable = new int[1 << pow];
    idxTable = new int[1 << pow];
    if (defaultValue != 0) {
      Arrays.fill(valTable, defaultValue);
    }
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

  public Snapshot getSnapshot() {
    return new Snapshot(this);
  }
  
  public void restore (Snapshot snap) {
    snap.restore(this);
  }
  
  @Override
  public SparseIntVector clone() {
    try {
      SparseIntVector o = (SparseIntVector) super.clone();
      o.idxTable = idxTable.clone();
      o.valTable = valTable.clone();
      
      return o;
      
    } catch (CloneNotSupportedException cnsx) {
      // can't happen
      return null;
    }
  }
  
  public int size() {
    return count;
  }
  
  public void clear() {
    Arrays.fill(valTable, defaultValue);
    Arrays.fill(idxTable, MIN_VALUE);
    count = 0;
  }
  
  public void clear(int idx) {
    int code = mix(idx);
    int pos = code & mask;
    int delta = (code >> (pow - 1)) | 1; // must be odd!
    int oidx = pos;

    for(;;) {
      int tidx = idxTable[pos];
      if (tidx == MIN_VALUE) {
        return; // nothing to clear
      }
      if (tidx == idx) {
        count--;
        idxTable[pos] = MIN_VALUE;
        valTable[pos] = defaultValue;
        return;
      }
      pos = (pos + delta) & mask;
      assert (pos != oidx); // should never wrap around
    }
  }
  
  @SuppressWarnings("unchecked")
  public int get(int idx) {
    int code = mix(idx);
    int pos = code & mask;
    int delta = (code >> (pow - 1)) | 1; // must be odd!
    int oidx = pos;

    for(;;) {
      int tidx = idxTable[pos];
      if (tidx == MIN_VALUE) {
        return defaultValue;
      }
      if (tidx == idx) {
        return valTable[pos];
      }
      pos = (pos + delta) & mask;
      assert (pos != oidx); // should never wrap around
    }
  }

  // for debug only
  int count() {
    int count = 0;
    for (int i = 0; i < idxTable.length; i++) {
      if (idxTable[i] != MIN_VALUE /*&& valTable[i] != defaultValue*/) {
        count++;
      }
    }
    return count;
  }
  
  public void set(int idx, int val) {
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
        valTable[pos] = val; // update
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
        //if (idxTable[i] != MIN_VALUE && valTable[i] != defaultValue) {
        if (idxTable[i] != MIN_VALUE) {
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
      
      int[] oldValTable = valTable;
      int[] oldIdxTable = idxTable;
      newTable();
      mask = idxTable.length - 1;
      nextWipe = (int)(MAX_LOAD_WIPE * mask);
      nextRehash = (int)(MAX_LOAD_REHASH * mask);

      int oldLen = oldIdxTable.length;
      for (int i = 0; i < oldLen; i++) {
        int tidx = oldIdxTable[i];
        if (tidx == MIN_VALUE) continue;
        int o = oldValTable[i];
        //if (o == defaultValue) continue;
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
    valTable[pos] = val;
  }
  
  
  public void setRange (int fromIndex, int toIndex, int val) {
    for (int i=fromIndex; i<toIndex; i++) {
      set(i, val);
    }
  }
  
  // ************************** Test main ************************ //
  
  public static void main(String[] args) {
    SparseIntVector vect = new SparseIntVector(3, MIN_VALUE);
    
    // add some
    for (int i = -4200; i < 4200; i += 10) {
      vect.set(i, i);
    }
    
    // check for added & non-added
    for (int i = -4200; i < 4200; i += 10) {
      int v = vect.get(i);
      if (v != i) {
        throw new IllegalStateException();
      }
    }
    for (int i = -4205; i < 4200; i += 10) {
      int v = vect.get(i);
      if (v != MIN_VALUE) {
        throw new IllegalStateException();
      }
    }
    
    // add some more
    for (int i = -4201; i < 4200; i += 10) {
      vect.set(i, i);
    }

    // check all added
    for (int i = -4200; i < 4200; i += 10) {
      int v = vect.get(i);
      if (v != i) {
        throw new IllegalStateException();
      }
    }
    for (int i = -4201; i < 4200; i += 10) {
      int v = vect.get(i);
      if (v != i) {
        throw new IllegalStateException();
      }
    }
    
    // "remove" some
    for (int i = -4200; i < 4200; i += 10) {
      vect.set(i,MIN_VALUE);
    }
    
    // check for added & non-added
    for (int i = -4201; i < 4200; i += 10) {
      int v = vect.get(i);
      if (v != i) {
        throw new IllegalStateException();
      }
    }
    for (int i = -4200; i < 4200; i += 10) {
      int v = vect.get(i);
      if (v != MIN_VALUE) {
        throw new IllegalStateException();
      }
    }

    // add even more
    for (int i = -4203; i < 4200; i += 10) {
      vect.set(i, i);
    }
    for (int i = -4204; i < 4200; i += 10) {
      vect.set(i, i);
    }
  }
}
