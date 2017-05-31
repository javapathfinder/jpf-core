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
package gov.nasa.jpf.vm;
//
// Contributed by Peter C. Dillinger and the Georgia Tech Research Corporation
//
// Portions drawn from public domain work by Bob Jenkins, May 2006
//
// Modified by Peter C. Dillinger working under Mission Critical Technologies
//

//import gov.nasa.jpf.util.LongVector;

/**
 * Implements StateSet based on Jenkins hashes.
 */
public class JenkinsStateSet extends SerializingStateSet {
  static final double MAX_LOAD = 0.7;
  static final int INIT_SIZE = 65536;

  
  int lastStateId = -1;

  //LongVector fingerprints;
  long[] fingerprints;

  int[] hashtable;

  int nextRehash;

  public JenkinsStateSet() {
    lastStateId = -1;
    hashtable = new int[INIT_SIZE];
    nextRehash = (int) (MAX_LOAD * INIT_SIZE);
    
    //fingerprints = new LongVector(nextRehash / 2);
    fingerprints = new long[nextRehash/2];
    
  }
  
  @Override
  public int size () {
    return lastStateId + 1;
  }
 
  public static long longLookup3Hash(int[] val) {
    // Jenkins' LOOKUP3 hash  (May 2006)
    int a = 0x510fb60d;
    int b = 0xa4cb30d9 + (val.length);
    int c = 0x9e3779b9;

    int i;
    int max = val.length - 2;
    for (i = 0; i < max; i += 3) {
      a += val[i];
      b += val[i + 1];
      c += val[i + 2];
      a -= c;  a ^= (c << 4) ^ (c >>> 28);  c += b;
      b -= a;  b ^= (a << 6) ^ (a >>> 26);  a += c;
      c -= b;  c ^= (b << 8) ^ (b >>> 24);  b += a;
      a -= c;  a ^= (c << 16)^ (c >>> 16);  c += b;
      b -= a;  b ^= (a << 19)^ (a >>> 13);  a += c;
      c -= b;  c ^= (b << 4) ^ (b >>> 28);  b += a;
    }
    switch (val.length - i) {
    case 2:
      c += val[val.length - 2];
      b += val[val.length - 1];
      break;
    case 1:
      b += val[val.length - 1];
      break;
    }
    c ^= b; c -= (b << 14) ^ (b >>> 18);
    a ^= c; a -= (c << 11) ^ (c >>> 21);
    b ^= a; b -= (a << 25) ^ (a >>>  7);
    c ^= b; c -= (b << 16) ^ (b >>> 16);
    a ^= c; a -= (c <<  4) ^ (c >>> 28);
    b ^= a; b -= (a << 14) ^ (a >>> 18);
    c ^= b; c -= (b << 24) ^ (b >>>  8);
    
    return ((long)c << 32) ^ b ^ a;
  }
  
  
  @Override
  public int add (int[] val) {
    long hash = longLookup3Hash(val); // this is the expensive part
    int i;
    
    // hash table lookup & add; open-addressed, double hashing
    int mask = hashtable.length - 1;
    int idx = (int)(hash >> 32) & mask;
    int delta = (int)hash | 1; // must be odd!
    int oidx = idx;

    while (hashtable[idx] != 0) {
      int id = hashtable[idx] - 1; // in table, 1 higher
      //if (fingerprints.get(id) == hash) {
      if (fingerprints[id] == hash){
        return id;
      }
      idx = (idx + delta) & mask;
      assert (idx != oidx); // should never wrap around
    }
    assert (hashtable[idx] == 0); // should never fill up

    if (lastStateId >= nextRehash) { // too full
      hashtable = null;
      // run GC here?
      hashtable = new int[(mask + 1) << 1];
      mask = hashtable.length - 1;
      nextRehash = (int) (MAX_LOAD * mask);

      for (i = 0; i <= lastStateId; i++) {
        //long h = fingerprints.get(i);
        long h = fingerprints[i];
        idx = (int)(h >> 32) & mask;
        delta = (int)h | 1;
        while (hashtable[idx] != 0) { // we know enough slots exist
          idx = (idx + delta) & mask;
        }
        hashtable[idx] = i + 1; // in table, add 1
      }
      // done with rehash; now get idx to empty slot
      idx = (int)(hash >> 32) & mask;
      delta = (int)hash | 1; // must be odd!
      while (hashtable[idx] != 0) { // we know enough slots exist and state is
                                    // new
        idx = (idx + delta) & mask;
      }
    } else {
      // idx already pointing to empty slot
    }

    //--- only reached if state is new
    
    lastStateId++;
    hashtable[idx] = lastStateId + 1; // in table, add 1

    //fingerprints.set(lastStateId, hash);
    try { // this happens rarely enough to save on nominal branch instructions
      fingerprints[lastStateId] = hash;
    } catch (ArrayIndexOutOfBoundsException ix){
      growFingerprint(lastStateId);
      fingerprints[lastStateId] = hash;      
    }
    
    return lastStateId;
  }
  
  void growFingerprint (int minSize){
    // we don't try to be fancy here
    int newSize = fingerprints.length *2;
    if (newSize < minSize) {
      newSize = minSize;
    }
    
    long[] newFingerprints = new long[newSize];
    System.arraycopy( fingerprints, 0, newFingerprints, 0, fingerprints.length);
    fingerprints = newFingerprints;
  }
  
  /**
   * Main for testing speed, mostly.
   */
  public static void main(String[] args) {
    try {
      int vlen = Integer.parseInt(args[0]);
      int adds = Integer.parseInt(args[1]);
      int queries = Integer.parseInt(args[2]);
      if (queries > adds) {
        queries = adds;
        System.err.println("Truncating queries to " + queries);
      }
      
      int[] v = new int[vlen];
      int i;
      for (i = 0; i < vlen; i++) {
        v[i] = i - 42;
      }
      
      JenkinsStateSet set = new JenkinsStateSet();
      
      long t1 = System.currentTimeMillis();
      for (i = 0; i < adds; i++) {
        v[0] = i * 3;
        set.add(v);
        assert set.size() == i+1;
      }
      
      for (i = 0; i < queries; i++) {
        v[0] = i * 3;
        set.add(v);
        assert set.size() == adds;
      }
      long t2 = System.currentTimeMillis();
      System.out.println("duration: " + (t2 - t1));
      
      
    } catch (RuntimeException re) {
      re.printStackTrace();
      System.err.println("args:  vector_length  #adds  #queries");
    }
  }
}
