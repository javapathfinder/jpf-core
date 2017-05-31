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

import gov.nasa.jpf.util.test.TestJPF;
import java.util.Arrays;
import java.util.BitSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;
import org.junit.Test;

/**
 * regression test for PSIntMap
 */
public class PSIntMapTest extends TestJPF {

  PSIntMap<Integer> createPersistentIntMap(){
    return new PSIntMap<Integer>();  
  }
  
  PSIntMap<Integer> set (PSIntMap<Integer> m, int i){
    return m.set(i, Integer.valueOf(i));
  }
  
  PSIntMap<Integer> set (PSIntMap<Integer> m, int[] data){
    for (int i=0; i<data.length; i++){
      int v = data[i];
      if (v >= 0){
        m = m.set( v, Integer.valueOf(v));
      } else {
        m = m.remove(-v);
      }
    }
    return m;
  }
  
  static class IntegerProcessor implements Processor<Integer>{
    int count=0;
    
    @Override
	public void process( Integer i){
      if (count++ > 0){
        System.out.print(',');
      }
      System.out.print(i);
    }
    
    public int getCount(){
      return count;
    }
  }
  
  static void dump (String prefix, PSIntMap<Integer> map, String postfix){
    if (prefix != null) {
      System.out.print(prefix);
      System.out.print(' ');
    }
    
    System.out.print(map.getClass().getSimpleName() + " {");
    map.process( new IntegerProcessor());
    System.out.print('}');
    
    if (postfix != null){
      System.out.print(' ');
      System.out.print(postfix);
    }    
  }
  
  static void dump (PSIntMap<Integer> map){
    dump(null,map, "\n");
  }
  
  static void assertNullValue (PSIntMap<Integer> map, int key){
    Integer v = map.get(key);
    if (v != null){
      fail("non-null value for: " + key + " = " + v);
    }
  }

  static void assertNonNullValue (PSIntMap<Integer> map, int key){
    Integer v = map.get(key);
    if (v == null || v.intValue() != key){
      fail("wrong value for: " + key + " = " + v);
    }
  }

  static void assertEquals( PSIntMap<Integer> map, int[] data){
    int[] a = data.clone();
    Arrays.sort(a);
    
    System.out.print("assertEquals {");
    for (int i=0; i<data.length; i++){
      if (i > 0){ 
        System.out.print(',');
      }
      System.out.print(a[i]);
    }
    System.out.println('}');
    
    assertTrue( map.size() == data.length);
    
    int[] b = new int[data.length];
    int j=0;
    for (Integer v : map){
      int i = v.intValue();
      b[j++] = i;
    }
    Arrays.sort(b);
    
    for (int i=0; i<a.length; i++){
      if (a[i] != b[i]){
        fail("different values : " + a[i] + ',' + b[i]);
      }
    }
  }
  
  //--- the tests
  
  @Test
  public void testSingleAdd(){
    PSIntMap<Integer> m = createPersistentIntMap();
    assertTrue( m.size() == 0);
    
    m = set( m, 0);
    dump("0: ", m, "\n");
    assertTrue( m.size() == 1);
    assertNonNullValue( m, 0);
    assertNullValue( m, 42);
    
    m = new PSIntMap<Integer>();
    m = set( m, 42);
    dump("42: ", m, "\n");
    assertTrue( m.size() == 1);
    assertNullValue( m, 0);
    assertNonNullValue( m, 42);

    int k = 32*32*32*32 + 1;
    m = new PSIntMap<Integer>();
    m = set( m, k);    
    dump("32**4 + 1: ", m, "\n");
    assertTrue( m.size() == 1);
    assertNullValue( m, 0);
    assertNonNullValue( m, k);
    m.printOn(System.out);    
  }  
  
  @Test
  public void testMultiAdd(){
    PSIntMap<Integer> m = createPersistentIntMap();
    
    int[] data = { 0,1, 32, 4,10, 666,669, 36, 37 }; 
    m = set( m, data);
    
    dump(m);
    //m.printOn(System.out);
    
    assertEquals( m, data);    
  }
  
  @Test
  public void testConsecutiveAdd(){
    int len = 32*32*32;
    PSIntMap<Integer> m = createPersistentIntMap();
    for (int i=0; i<len; i++){
      m = set(m, i);
    }
        
    for (int i=0; i<len; i++){
      Integer v = m.get(i);
      assertNonNullValue( m, i);
    }
    
    System.out.println("m.size() = " + m.size());
    assertTrue(m.size() == len);
  }

  @Test
  public void testConsecutiveAddRemove(){
    int len = 32*32*32;
    PSIntMap<Integer> m = createPersistentIntMap();
    for (int i=0; i<len; i++){
      m = set(m, i);
    }
    
    for (int i=0; i<len; i++){
      Integer v = m.get(i);
      assertNonNullValue( m, i);
    }
    
    for (int i=len-1; i>= 0; i--){
      m = m.remove(i);
    }
    
    System.out.println("m.size() = " + m.size());
    assertTrue(m.size() == 0);
  }
  
  @Test
  public void testPredicateRemoval(){
    PSIntMap<Integer> m = createPersistentIntMap();
    
    int[] data = { 0,1, 32, 4,10, 666,669, 36, 37, 95,97 }; 
    m = set( m, data);

    dump("before removal:", m, "\n");
    
    Predicate<Integer> pred = new Predicate<Integer>(){
      @Override
	public boolean isTrue(Integer i){
        return ((i & 1) != 0);
      }
    };
    
    m = m.removeAllSatisfying(pred);
    
    dump("after removal:", m, "\n");
    m.printOn(System.out);
  }

  @Test
  public void testRangePredicateRemoval(){
    PSIntMap<Integer> m = createPersistentIntMap();
    int len = 20000;
    for (int i=0; i<len; i++){
      m = set(m, i);
    }

    // completely remove first value node
    Predicate<Integer> pred = new Predicate<Integer>(){
      @Override
	public boolean isTrue (Integer n){
        return (n <= 31);
      }
    };
    m = m.removeAllSatisfying(pred);
    
    System.out.println("m.size() = " + m.size());
    assertTrue( m.size() == (len - 32));
    for (int i=0; i<32; i++){
      assertTrue( m.get(i) == null);
    }
    len -= 32;
    
    // remove all but one value from the second node
    pred = new Predicate<Integer>(){
      @Override
	public boolean isTrue (Integer n){
        return (n >32 && n <= 63);
      }
    };
    m = m.removeAllSatisfying(pred);
    System.out.println("m.size() = " + m.size());
    assertTrue( m.size() == (len - 31));
    assertTrue( m.get(32) != null);
    for (int i=33; i<64; i++){
      assertTrue( m.get(i) == null);
    }
    len -= 31;
    
    // remove all but one from bitmap node
    pred = new Predicate<Integer>(){
      @Override
	public boolean isTrue (Integer n){
        return (n == 64);
      }
    };
    m = m.removeAllSatisfying(pred);
    pred = new Predicate<Integer>(){
      @Override
	public boolean isTrue (Integer n){
        return (n >= 64 && n < 95);
      }
    };
    m = m.removeAllSatisfying(pred);
    for (int i=64; i<95; i++){
      assertTrue( m.get(i) == null);
    }    
    assertTrue( m.get(95) != null);
  }  
  
  @Test
  public void testHeapPattern(){
    Random r = new Random(42);
    final BitSet removed = new BitSet();
    
    Predicate<Integer> pred = new Predicate<Integer>(){
      @Override
	public boolean isTrue (Integer n){
        return removed.get(n.intValue());
      }
    };
    
    PSIntMap<Integer> m = createPersistentIntMap();
    int max = 20000;
    for (int i=0; i<max; i++){
      m = set(m, i);
      
      if ((i > 0) && (i % 500) == 0){
         for (int j=0; j<120; j++){
           int k = r.nextInt(i);
           removed.set(k);
         }
         
         m = m.removeAllSatisfying(pred);
      }
    }
    
    System.out.println("m.size() = " + m.size());
    int nRemoved = removed.cardinality();
    assertTrue( m.size() == (max - nRemoved));
    
    int n = 0;
    for (int i=0; i<max; i++){
      if (removed.get(i)){
        assertTrue( m.get(i) == null);
      } else {
        assertTrue( m.get(i) != null);
        n++;
      }
    }
    assertTrue( n == (max - nRemoved));
  }
    
  
  //--- benchmarks
  
  final static int NSTATES = 20000;
  final static int NOBJECTS = 2000;
  final static int NGC = 400;
  
  
  public void benchmark (){
    long t1, t2;

    //--- PersistentIntMap
    Predicate<Integer> pred = new Predicate<Integer>() {
      @Override
	public boolean isTrue (Integer o) {
        int i = o.intValue();
        return (i < NGC);
      }
    };
    
    Runtime.getRuntime().gc();
    t1 = System.currentTimeMillis();
    for (int l=0; l<NSTATES; l++) {
      PSIntMap<Integer> t = createPersistentIntMap();
      
      //--- allocations
      for (int i=0; i<NOBJECTS; i++){
        t = t.set(i,  Integer.valueOf(i));
      }

      //--- lookup
      for (int i=0; i<NOBJECTS; i++) {
        Integer o = t.get(i);
      }
      
      //--- gc
      t = t.removeAllSatisfying(pred);
      
      //--- no store/backtrack costs for container
    }
    t2 = System.currentTimeMillis();
    System.out.println("PersistentIntMap (" + NSTATES + " cycles): " + (t2 - t1));
  

    //--- HashMap
    Runtime.getRuntime().gc();
    t1 = System.currentTimeMillis();
    for (int l=0; l<NSTATES; l++) {
      HashMap<Integer,Integer> m = new HashMap<Integer,Integer>();
      //--- allocations
      for (int i=0; i<NOBJECTS; i++){
        m.put(i, i);
      }

      //--- lookup
      for (int i=0; i<NOBJECTS; i++) {
        Integer o = m.get(i);
      }
      
      //--- gc
      for (Iterator<Map.Entry<Integer,Integer>> it = m.entrySet().iterator(); it.hasNext();) {
        Map.Entry<Integer, Integer> e = it.next();
        if (pred.isTrue(e.getValue())) {
          it.remove();
        }      
      }
      
      //--- 2 x clone (upon store and backtrack)
      m = (HashMap<Integer,Integer>)m.clone();
      m = (HashMap<Integer,Integer>)m.clone();
    }
    t2 = System.currentTimeMillis();
    System.out.println("HashMap (" + NSTATES + " cycles): " + (t2 - t1));

    //--- ObjVector (needs to be adjusted for holes -> increased size)
    Runtime.getRuntime().gc();
    t1 = System.currentTimeMillis();
    for (int l=0; l<NSTATES; l++) {
      ObjVector<Integer> v = new ObjVector<Integer>();
      //--- allocations
      for (int i=0; i<NOBJECTS; i++){
        v.set(i, i);
      }

      //--- lookup
      for (int i=0; i<NOBJECTS; i++) {
        Integer o = v.get(i);
      }
      
      //--- gc
      v.clearAllSatisfying(pred);
      
      //--- snap & restore
      ObjVector.Snapshot<Integer> snap = v.getSnapshot();
      v.restore(snap);
    }
    t2 = System.currentTimeMillis();
    System.out.println("ObjVector (" + NSTATES + " cycles): " + (t2 - t1));

  }

}
