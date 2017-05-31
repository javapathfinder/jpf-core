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

/**
 * regression test for ObjVector
 */
public class ObjVectorTest extends TestJPF {

  void assertEquals( ObjVector<Integer> v1, ObjVector<Integer> v2) {
    assertTrue( v1.size() == v2.size());

    int n = v1.size();
    for (int i=0; i<n; i++) {
      Object a = v1.get(i);
      Object b = v2.get(i);
      if (a == null) {
        assertTrue( b == null);
      } else {
        assertTrue( a.equals(b));
      }
    }
  }
  
  @Test
  public void testSnapshot () {
    ObjVector<Integer> v = new ObjVector<Integer>(100);
    
    // all empty snapshot
    ObjVector.Snapshot<Integer> snap = v.getSnapshot();
    Integer val = Integer.valueOf(42);
    v.set(0,  val);
    assertTrue(v.size() == 1 && v.get(0) == val);
    v.restore(snap);
    assertTrue(v.size() == 0 && v.get(0) == null);
    
    //--- all full snapshot
    for (int i=0; i<100; i++) {
      v.set(i, i);
    }
    ObjVector<Integer> v0 = v.clone();
    ObjVector.Snapshot<Integer> snap0 = v.getSnapshot();
    v.clear();
    v.restore(snap0);
    assertEquals( v0, v);
    
    //--- punch holes into it
    v.setRange(11,  20, null);
    v.set( 25,null);
    v.set( 26, null);
    v.set( 42, null);
    v.setRange(70, 85, null);
    ObjVector.Snapshot<Integer> snap1 = v.getSnapshot();    
    ObjVector<Integer> v1 = v.clone();
    v.clear();
    v.restore(snap1);
    //v.printOn( System.out);
    assertEquals( v1, v);
    
    //--- chop off the ends
    v.restore(snap0);
    v.setRange(81, 99, null);
    v.setRange(0, 19, null);
    ObjVector.Snapshot<Integer> snap2 = v.getSnapshot();    
    ObjVector<Integer> v2 = v.clone();
    v.clear();
    v.restore(snap2);
    assertEquals( v2, v); 
  }
  
  //--- mutating snapshot
  
  static class X {
    int val;
    X (Integer o) { val = o.intValue(); }
  }
  
  static class IXTransformer implements Transformer<Integer,X> {
    @Override
	public X transform( Integer obj) {
      return new X(obj);
    }
  }
  
  static class XITransformer implements Transformer<X,Integer>{
    @Override
	public Integer transform( X obj) {
      return Integer.valueOf(obj.val);
    }
  }
  
  @Test
  public void testMutatingSnapshot() {
    ObjVector<Integer> v = new ObjVector<Integer>(100);

    for (int i=0; i<100; i+=2) {
      v.set(i, Integer.valueOf(i));
    }
    print(v);
    
    System.out.println("now storing snapshot for ObjVector of size " + v.size());
    ObjVector.MutatingSnapshot<Integer,X> snap = v.getSnapshot(new IXTransformer());
    
    System.out.println("now modifying ObjVector");
    v.clear();
    v.setRange(0, 30, Integer.valueOf(42));
    print(v);
    
    System.out.println("now restoring ObjVector");
    v.restore(snap, new XITransformer());
    
    int n = print(v);
    assert n == 50 : "got wrong number of non-null elements: " + n;
  }
  
  // utilities
  
  int print(ObjVector<Integer> v) {
    int n = 0;
    for (int i=0; i<=v.size(); i++) {
      Integer e = v.get(i);
      if (e != null) {
        if (n++ > 0) {
          System.out.print(',');
        }
        System.out.print(e);
      }
    }
    System.out.println();
    
    return n;
  }
}
