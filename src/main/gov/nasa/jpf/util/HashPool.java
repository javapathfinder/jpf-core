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

import java.util.ArrayList;


/**
 * data structure used to do hash collapsing. All the major state components
 * (fields, Monitors, StackFrames, uThreadData) are stored in pools to
 * determine if they are new. Only the pool index values are used to
 * compute hash values.
 * <p>
 * 2006-06-14 - major rewrite by pcd
 */
public final class HashPool<V> {
  private IntTable<V> pool;
  private ArrayList<V> vect;
  
  public HashPool() {
    this(8); // default to 256 slots
  }
  
  public HashPool(int pow) {
    pool = new IntTable<V>(pow);
    vect = new ArrayList<V>(1 << pow);
  }

  /** optionally called only once after creation to link null to 0. */ 
  public HashPool<V> addNull() {
    if (size() == 0) {
      pool.add(null, 0);
      vect.add(null);
      return this;
    } else {
      throw new IllegalStateException();
    }
  }
  
  public IntTable.Entry<V> getEntry (V o) {
    int sz = pool.size(); // == vect.size();
    
    IntTable.Entry<V> e = pool.pool(o);
    if (e.val == sz) {
      vect.add(o);
    }
    return e;
  }

  public int getIndex (V o) {
    return getEntry(o).val;
  }

  public V get (V o) {
    return getEntry(o).key;
  }

  public V getObject (int idx) {
    return vect.get(idx);
  }

  public void print () {
    System.out.println("{");

    for (IntTable.Entry<V> entry : pool) {
      System.out.println("\t" + entry);
    }

    System.out.println("}");
  }

  public int size () {
    return pool.size();
  }
}
