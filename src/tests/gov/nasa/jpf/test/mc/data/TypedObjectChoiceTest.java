/*
 * Copyright (C) 2015, United States Government, as represented by the
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
package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import org.junit.Test;

/**
 * regression test for TypedObjectChoice CGs
 */
public class TypedObjectChoiceTest extends TestJPF {

  static class A {
    int id;
    A (int i){
      id = i;
    }
    public String toString(){ return String.format("A(%d)", id); }
  }

  @Test
  public void testNoCG (){
    if (verifyNoPropertyViolation("+mycg.class=gov.nasa.jpf.vm.choice.TypedObjectChoice",
            "+mycg.include_null=false",
            "+mycg.type=" + getClass().getName() + "$A")){
      Object o = Verify.getObject("mycg");
      System.out.print("got object: ");
      System.out.println(o);

      assertTrue( o == null);
    }
  }

  @Test
  public void testNoObject (){
    if (verifyNoPropertyViolation("+mycg.class=gov.nasa.jpf.vm.choice.TypedObjectChoice",
                                  "+mycg.include_null=true",
                                  "+mycg.type=" + getClass().getName() + "$A")){
      Object o = Verify.getObject("mycg");
      System.out.print("got object: ");
      System.out.println(o);

      assertTrue( o == null);
    }
  }

  @Test
  public void testObject (){
    if (verifyNoPropertyViolation("+mycg.class=gov.nasa.jpf.vm.choice.TypedObjectChoice",
            "+mycg.include_null=false",
            "+mycg.type=" + getClass().getName() + "$A")) {
      A a = new A(42);

      Object o = Verify.getObject("mycg");
      System.out.print("got object: ");
      System.out.println(o);

      assertTrue(o instanceof A);
      a = (A) o;
      assertTrue(a.id == 42);
    }
  }

  @Test
  public void testMultipleObjects(){
    if (verifyNoPropertyViolation("+mycg.class=gov.nasa.jpf.vm.choice.TypedObjectChoice",
            "+mycg.include_null=false",
            "+mycg.type=" + getClass().getName() + "$A")) {
      A a1 = new A(1);
      A a2 = new A(2);

      Object o = Verify.getObject("mycg");
      System.out.print("got object: ");
      System.out.println(o);

      assertTrue(o instanceof A);
      A a = (A) o;
      Verify.setBitInBitSet(0, a.id, true);
      Verify.incrementCounter(0);
    }

    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
      assertTrue( Verify.getBitInBitSet(0, 1));
      assertTrue( Verify.getBitInBitSet(0, 2));
    }
  }

}
