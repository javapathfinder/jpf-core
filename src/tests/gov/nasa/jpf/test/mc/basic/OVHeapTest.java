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

package gov.nasa.jpf.test.mc.basic;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

/**
 * unit test for OVHeap
 */
public class OVHeapTest extends TestJPF {
  
  static int getReferenceValue (Object o) {
    int h = System.identityHashCode(o);
    return h ^ 0xABCD;  // revert the ABCD hash component
  }

  static void checkRef (String msg, String key, int ref) {
    System.out.printf("%s ,object: %s, ref: %d", msg, key, ref);
    
    int v = Verify.getValue(key);
    if (v == Verify.NO_VALUE) {
      Verify.putValue(key, ref);
      System.out.println(" new");
    } else {
      if (v == ref) {
        System.out.println(" seen");
      } else {
        fail("different reference values, had:" + v + ", new:" + ref);
      }
    }
  }
  
  static class X {
    String id;
    
    X (String id){
      this.id = id;
    }
  }
  
  static class Y extends X {
    Y (String id){
      super(id);
    }
  }
  
  X allocX (String id) {
    return new X(id);
  }
    
  @Test
  public void testSGOIDs() {
    if (verifyNoPropertyViolation("+vm.heap.class=.vm.OVHeap")) {
      Thread t = new Thread() {
        @Override
		public void run() {
          Class<?> cls = X.class;
          checkRef("from T ", "X.class", getReferenceValue(cls));
          
          X x1 = new X("t-x1");
          checkRef("from T ", x1.id, getReferenceValue(x1));
          
          Thread.yield(); // CG #3
          
          Y y1 = new Y("t-y1");
          checkRef("from T ", y1.id, getReferenceValue(y1));
        }
      };
      
      t.start();  // CG #1
      
      Class<?> clsY = Y.class;
      checkRef("from M ", "Y.class", getReferenceValue(clsY));
      
      Class<?> clsX = X.class;
      checkRef("from M ", "X.class", getReferenceValue(clsX));
      
      int n = Verify.getInt(1, 3); // CG #2
      System.out.println("-- M next X[] arraysize = " + n);
      X[] xs = new X[n];
      for (int i=0; i<xs.length; i++) {
        xs[i] = new X("xs-" + i);
        checkRef("from M ", xs[i].id, getReferenceValue(xs[i]));
      }
      
      Y y1 = new Y("m-y1");
      checkRef("from M ", y1.id, getReferenceValue(y1));
      
      X x1 = new Y("m-x1");
      checkRef("from M ", x1.id, getReferenceValue(x1));
    }
  }
}
