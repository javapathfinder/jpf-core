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

import static gov.nasa.jpf.util.SparseClusterArray.S1;
import gov.nasa.jpf.util.SparseClusterArray.Entry;
import gov.nasa.jpf.util.SparseClusterArray.Snapshot;
import gov.nasa.jpf.util.test.TestJPF;

import java.util.HashMap;
import java.util.Random;

import org.junit.Test;

/**
 * unit test for gov.nasa.jpf.util.SparseClusterArray
 */
public class SparseClusterArrayTest extends TestJPF {

  public static void main (String[] args){

    // our performance evals
    if (args.length == 1){
      String mthName = args[0];
      if (mthName.equals("evalHashMap")){
        evalHashMap();
        return;
      } else if (mthName.equals("evalSparseClusterArray")){
        evalSparseClusterArray();
        return;
      }
    }

    // the regression tests
    runTestsOfThisClass(args);
  }


  @Test
  public void testBasic() {
    SparseClusterArray<Object> arr = new SparseClusterArray<Object>();
    Object v;
    int ref;

    ref = (1 << S1) | 42;
    arr.set(ref, (v = new Integer(ref)));

    Object o = arr.get(ref);
    System.out.println(o);
    assert o.equals(v);

    ref = (2 << S1);
    arr.set(ref, new Integer(ref));

    System.out.println("cardinality = " + arr.cardinality());
    assert arr.cardinality() == 2;

    for (Object e : arr) {
      System.out.println(e);
    }
  }

  @Test
  public void testNextNull () {
    Object e = new Integer(42);
    SparseClusterArray<Object> arr = new SparseClusterArray<Object>();
    int k;
    int limit = 10000000;

    arr.set(0, e);
    k = arr.firstNullIndex(0, limit);
    System.out.println("k=" + k);  // 1
    assert k == 1;

    arr.set(0,null);
    k = arr.firstNullIndex(0, limit);
    System.out.println("k=" + k);  // 0
    assert k == 0;

    arr.set(511, 511);

    int i=0;
    for (;i<512; i++) {
      arr.set(i, e);
    }
    System.out.println(arr.get(511));
    System.out.println(arr.get(512));
    k = arr.firstNullIndex(0, limit);
    assert k == 512;
    
    long t1 = System.currentTimeMillis();
    for (int j=0; j<100000; j++) {
      k = arr.firstNullIndex(0, limit);
    }
    long t2 = System.currentTimeMillis();
    System.out.println("k=" + k + ", 100000 lookups in: " + (t2 - t1)); // 512

    for (;i<2048;i++) {
      arr.set(i, e);
    }
    k = arr.firstNullIndex(0, limit);
    System.out.println("k=" + k);  // 2048 (no chunk)
    assert k == 2048;

    k = arr.firstNullIndex(0, 2048);
    System.out.println("k=" + k); // -1
    assert k == -1;

    arr.set(2048, e);
    arr.set(2048,null);
    k = arr.firstNullIndex(0, limit);
    System.out.println("k=" + k);  // 2048 (new chunk)
    assert k == 2048;

    for (; i<2500; i++) {
      arr.set(i, e);
    }
    k = arr.firstNullIndex(0, limit);
    System.out.println("k=" + k);  // 2500
    assert k == 2500;
  }

  @Test
  public void testClusterNextNull () {
    SparseClusterArray<Integer> arr = new SparseClusterArray<Integer>();

    arr.set(0, 0); // have a lower chunk

    int tid = 2;
    int base = (tid << SparseClusterArray.S1);
    int r = arr.firstNullIndex(base, SparseClusterArray.MAX_CLUSTER_ENTRIES);
    assert r == 0x02000000;
    System.out.println(Integer.toHexString(r));
    arr.set(r, 42);

    r = arr.firstNullIndex(base, SparseClusterArray.MAX_CLUSTER_ENTRIES);
    assert r == 0x02000001;
    System.out.println(Integer.toHexString(r));

    for (int i=r; i < 0x0200ffff; i++){
      arr.set(i,42);
    }

    arr.set(0x200f0ff, null);
    r = arr.firstNullIndex(base, SparseClusterArray.MAX_CLUSTER_ENTRIES);
    assert r == 0x200f0ff;
    System.out.println(Integer.toHexString(r));
    arr.set(0x200f0ff, 42);

    r = arr.firstNullIndex(base, SparseClusterArray.MAX_CLUSTER_ENTRIES);
    assert r == 0x200ffff;
    System.out.println(Integer.toHexString(r));

  }

  @Test
  public void testClone() {
    SparseClusterArray<Integer> arr = new SparseClusterArray<Integer>();

    arr.set(0, new Integer(0));
    arr.set(42, new Integer(42));
    arr.set(6762, new Integer(6762));
    arr.set(6762, null);

    Cloner<Integer> cloner = new Cloner<Integer>() {
      @Override
	public Integer clone (Integer other) {
        return new Integer(other);
      }
    };
    SparseClusterArray<Integer> newArr = arr.deepCopy(cloner);
    for (Integer i : newArr) {
      System.out.println(i);
    }

    assert newArr.cardinality() == 2;
    assert newArr.get(0) == 0;
    assert newArr.get(42) == 42;
    assert newArr.get(6762) == null;
  }

  @Test
  public void testSnapshot() {
    SparseClusterArray<Integer> arr = new SparseClusterArray<Integer>();

    arr.set(0, new Integer(0));
    arr.set(42, new Integer(42));
    arr.set(4095, new Integer(4095));
    arr.set(4096, new Integer(4096));
    arr.set(7777, new Integer(7777));
    arr.set(67620, new Integer(67620));
    arr.set(67620, null);
    arr.set(7162827, new Integer(7162827));

    Transformer<Integer,String> i2s = new Transformer<Integer,String>() {
      @Override
	public String transform (Integer n) {
        return n.toString();
      }
    };

    Transformer<String,Integer> s2i = new Transformer<String,Integer>() {
      @Override
	public Integer transform (String s) {
        return new Integer( Integer.parseInt(s));
      }
    };

    Snapshot<Integer,String> snap = arr.getSnapshot(i2s);
    // just for debugging purposes
    int len = snap.size();
    for (int i=0; i<len; i++){
      System.out.println("a[" + snap.getIndex(i) + "] = " + snap.getValue(i));
    }

    arr.set(42,null);
    arr.set(87, new Integer(87));
    arr.set(7162827, new Integer(-1));

    arr.restore(snap, s2i);
    for (Integer i : arr) {
      System.out.println(i);
    }

    assert arr.cardinality() == 6;
    assert arr.get(0) == 0;
    assert arr.get(42) == 42;
    assert arr.get(4095) == 4095;
    assert arr.get(4096) == 4096;
    assert arr.get(7777) == 7777;
    assert arr.get(7162827) == 7162827;
  }

  @Test
  public void testChanges() {
    SparseClusterArray<Integer> arr = new SparseClusterArray<Integer>();

    arr.set(42, new Integer(42));
    arr.set(6276, new Integer(6276));

    arr.trackChanges();

    arr.set(0, new Integer(0));
    arr.set(42, new Integer(-1));
    arr.set(4095, new Integer(4095));
    arr.set(4096, new Integer(4096));
    arr.set(7777, new Integer(7777));
    arr.set(7162827, new Integer(7162827));

    Entry<Integer> changes = arr.getChanges();
    arr.revertChanges(changes);

    for (Integer i : arr) {
      System.out.println(i);
    }

    assert arr.cardinality() == 2;
    assert arr.get(42) == 42;
    assert arr.get(6276) == 6276;
  }

  @Test
  public void testIterator() {
    SparseClusterArray<Integer> arr = new SparseClusterArray<Integer>();

    for (int i=0; i<300; i++){
      arr.set(i,i);
    }
    for (int i=700; i < 1000; i++){
      arr.set(i,i);
    }
    
    // remove some while we iterate
    boolean lastSeen = false;
    int n = 0;
    for (Integer i : arr){
      if (i == 200){
        for (int j = 150; j<750; j++){
          arr.set(j, null);
        }
      } else if (i == 999){
        lastSeen = true;
      }
      n++;
    }
    
    assert n == 451 : "wrong number of visited elements: " + n; // [0-200] + [750-999]
    assert lastSeen : "last element not seen";
  }

  @Test
  public void testIndexIterator() {
    SparseClusterArray<Integer> arr = new SparseClusterArray<Integer>();

    for (int i=0; i<300; i++){
      arr.set(i,i);
    }
    for (int i=700; i < 1000; i++){
      arr.set(i,i);
    }

    // remove some while we iterate
    boolean lastSeen = false;
    int n = 0;
    IndexIterator it = arr.getElementIndexIterator(100);
    for (int i=it.next(); i>= 0; i = it.next()){
System.out.println(i);
      if (i == 200){
        for (int j = 150; j<750; j++){
          arr.set(j, null);
        }
      } else if (i == 999){
        lastSeen = true;
      }
      n++;
    }

    assert n == 351 : "wrong number of visited elements: " + n; // [100-200] + [750-999]
    assert lastSeen : "last element not seen";
  }


   //--- the performance sectopm

  final static int MAX_ROUNDS = 1000;
  final static int MAX_N = 10000;
  final static int MAX_T = 8;


  static void evalSparseClusterArray() {
    Random random = new Random(0);
    Object elem = new Object();
    long t1, t2;
    int n = 0;

    t1 = System.currentTimeMillis();
    SparseClusterArray<Object> arr = new SparseClusterArray<Object>();

    for (int i=0; i<MAX_ROUNDS; i++) {
      int seg = random.nextInt(MAX_T) << S1;
      for (int j=0; j<MAX_N; j++) {
        int ref = seg | random.nextInt(MAX_N);
        //ref |= j;
        arr.set(ref, elem);
        if (arr.get(ref) == null) throw new RuntimeException("element not set: " + i);
      }
    }
    t2 = System.currentTimeMillis();
    System.out.println("SparseArray random write/read of " + arr.cardinality() + " elements: "+ (t2 - t1));

    n=0;
    t1 = System.currentTimeMillis();
    for (Object e : arr) {
      n++;
    }
    t2 = System.currentTimeMillis();
    System.out.println("SparseArray iteration over " + n + " elements: " + (t2 - t1));
  }

  static void evalHashMap() {
    Random random = new Random(0);
    Object elem = new Object();
    long t1, t2;

    t1 = System.currentTimeMillis();
    HashMap<Integer,Object> arr = new HashMap<Integer,Object>();

    for (int i=0; i<MAX_ROUNDS; i++) {
      int seg = random.nextInt(MAX_T) << S1;
      for (int j=0; j<MAX_N; j++) {
        int ref = seg | random.nextInt(MAX_N);
        //ref |= j;
        arr.put(ref, elem);
        if (arr.get(ref) == null) throw new RuntimeException("element not set: " + i);
      }
    }
    t2 = System.currentTimeMillis();
    System.out.println("HashMap random write/read of " + arr.size() + " elements: " + (t2 - t1));

    int n=0;
    t1 = System.currentTimeMillis();
    for (Object e : arr.values()) {
      n++;
    }
    t2 = System.currentTimeMillis();
    System.out.println("HashMap iteration over " + n + " elements: " + (t2 - t1));
  }
}
