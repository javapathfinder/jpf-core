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
package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * test cast operations
 */
public class CastTest extends TestJPF {
  
  @SuppressWarnings("cast")
  @Test public void testCast () {
    if (verifyNoPropertyViolation()){
      B b = new B();
      A a = b;

      B bb = (B) a;
      K k = a;
      I i = (I) a;

      C c = new C();
      k = c;
    }
  }

  @Test public void testCastFail () {
    if (verifyUnhandledException("java.lang.ClassCastException")){
      A a = new A();
      I i = (I) a;
    }
  }

  @Test public void testArrayCast () {
    if (verifyNoPropertyViolation()){
      String[] sa = new String[1];
      Object o = sa;
      Object[] ol = (Object[])o; // that should succeed
    }
  }
  
  @Test public void testArrayCastFail() {
    if (verifyUnhandledException("java.lang.ClassCastException")){
      String[] sa = new String[1];
      Object o = sa ;
      Number[] na = (Number[])o; // that should fail
    }
  }
  
  @Test public void testPrimitiveArrayCast() {
    if (verifyNoPropertyViolation()){
      int[] a = new int[10];
      Object o = a;
      int[] b = (int[]) o;
    }
  }
  
  //--- helper types and methods
  
  static interface I {
  }

  static interface J extends I {
  }

  static interface K {
  }
  
  static class A implements K {
  }

  static class B extends A implements J {
  }
  
  static class C extends B {
  }
}
