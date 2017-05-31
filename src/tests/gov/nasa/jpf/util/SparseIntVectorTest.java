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

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class SparseIntVectorTest extends TestJPF {
  
  void assertEquals( SparseIntVector v1, SparseIntVector v2) {
    assertTrue( v1.size() == v2.size());

    int n = v1.size();
    for (int i=0; i<n; i++) {
      int a = v1.get(i);
      int b = v2.get(i);
      assertTrue(a == b);
    }
  }
  
  @Test
  public void testSetGet () {
    SparseIntVector v = new SparseIntVector();

    v.set(0, 10);
    v.set(42, 11);
    v.set(111111111, 12);
    
    assertTrue( v.get(0) == 10);
    assertTrue( v.get(42) == 11);
    assertTrue( v.get(111111111) == 12);
    
    assertTrue( v.get(10) == 0);
    
    v.clear(42);
    assertTrue( v.get(42) == 0);
  }
  
  @Test
  public void testSnapshot () {
    SparseIntVector v = new SparseIntVector();

    // all empty snapshot
    SparseIntVector.Snapshot snap = v.getSnapshot();
    int val = 42;
    v.set(0,  val);
    assertTrue(v.size() == 1 && v.get(0) == val);
    v.restore(snap);
    assertTrue(v.size() == 0);

    //--- all full snapshot
    for (int i=0; i<100; i++) {
      v.set(i, i);
      assertTrue( "size out of sync: " + i, v.size() == (i+1));
    }
    SparseIntVector.Snapshot snap0 = v.getSnapshot();
    v.clear();
    v.restore(snap0);
    for (int i=0; i<100; i++) {
      assertTrue( i == v.get(i));
    }
    
    //--- punch holes into it
    v.setRange(11,  20, 0);
    v.set( 25,0);
    v.set( 26, 0);
    v.set( 42, 0);
    v.setRange(70, 85, 0);
    SparseIntVector.Snapshot snap1 = v.getSnapshot();    
    SparseIntVector v1 = v.clone();
    v.clear();
    v.restore(snap1);
    //v.printOn( System.out);
    assertEquals( v1, v);
    
    //--- chop off the ends
    v.restore(snap0);
    v.setRange(81, 99, 0);
    v.setRange(0, 19, 0);
    SparseIntVector.Snapshot snap2 = v.getSnapshot();    
    SparseIntVector v2 = v.clone();
    v.clear();
    v.restore(snap2);
    assertEquals( v2, v); 
  }
  
  static final int MAX_SIZE = 10000;
  static final int MAX_ROUNDS = 1000;
  
  public void benchmark() {
    long t1, t2;

    for (int rep = 0; rep < 2; rep++) {
      Runtime.getRuntime().gc();
      SparseIntVector siv = new SparseIntVector();
      t1 = System.currentTimeMillis();
      for (int i = 0; i < MAX_ROUNDS; i++) {
        SparseIntVector.Snapshot snap = siv.getSnapshot();
        for (int j = 0; j < MAX_SIZE; j++) {
          siv.set(j, j);
          assert siv.get(j) == j;
          // assert siv.size() == (j+1) : "size differs: " + siv.size() + " / "
          // + (j+1);
        }
        assert siv.size() == MAX_SIZE : "wrong size: " + siv.size();
        siv.restore(snap);
      }
      t2 = System.currentTimeMillis();
      System.out.printf("SparseIntVector size %d, rounds %d: %d\n", MAX_SIZE,
          MAX_ROUNDS, (t2 - t1));

      Runtime.getRuntime().gc();
      IntTable<Integer> tbl = new IntTable<Integer>();
      t1 = System.currentTimeMillis();
      for (int i = 0; i < MAX_ROUNDS; i++) {
        IntTable.Snapshot<Integer> snap = tbl.getSnapshot();
        for (int j = 0; j < MAX_SIZE; j++) {
          tbl.put(j, j);

          IntTable.Entry<Integer> e = tbl.get(j);
          assert e != null && e.val == j  : "wrong IntTable entry for index: " + j + " : " + e + " in round: " + i;
        }
        assert tbl.size() == MAX_SIZE : "wrong size: " + tbl.size();
        tbl.restore(snap);
      }
      t2 = System.currentTimeMillis();
      System.out.printf("IntTable size %d, rounds %d: %d\n", MAX_SIZE,
          MAX_ROUNDS, (t2 - t1));
    }
  }
}
