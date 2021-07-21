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

package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.annotation.BitFlip;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;
import org.junit.Ignore;

/**
 * regression test for getBitFlip API and BitFlipListener
 */
public class BitFlipTest extends TestJPF {

  public static int fooStatic(@BitFlip int bar) {
    return bar;
  }

  public int fooInstance(@BitFlip int bar) {
    return bar;
  }

  public static void main(String[] args) {
    runTestsOfThisClass(args);
  }

  @Test
  public void testStaticMethodParameterBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for static method parameters test");
      int d = fooStatic(0);
      System.out.print("d = ");
      System.out.println(d);
      int seen = Verify.getCounter(0);
      assert ((seen & d) == 0);
      seen |= d;
      Verify.setCounter(0, seen);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @Test
  public void testInstanceMethodParameterBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for instance method parameters test");
      int d = fooInstance(0);
      System.out.print("d = ");
      System.out.println(d);
      int seen = Verify.getCounter(0);
      assert ((seen & d) == 0);
      seen |= d;
      Verify.setCounter(0, seen);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @BitFlip
  int instanceField;

  @Test
  public void testInstanceFieldBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for instance field test");
      instanceField = 0;
      System.out.print("instanceField = ");
      System.out.println(instanceField);
      int seen = Verify.getCounter(0);
      assert ((seen & instanceField) == 0);
      seen |= instanceField;
      Verify.setCounter(0, seen);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @BitFlip
  static int staticField;

  @Test
  public void testStaticFieldBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for static field test");
      staticField = 0;
      System.out.print("staticField = ");
      System.out.println(staticField);
      int seen = Verify.getCounter(0);
      assert ((seen & staticField) == 0);
      seen |= staticField;
      Verify.setCounter(0, seen);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @Test
  public void testLocalVariableBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for local variable test");
      @BitFlip int d = 0;
      System.out.print("d = ");
      System.out.println(d);
      int seen = Verify.getCounter(0);
      assert ((seen & d) == 0);
      seen |= d;
      Verify.setCounter(0, seen);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @Test
  public void testBitFlipAPI() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation()){
      System.out.println("getBitFlip API test");
      int d = Verify.getBitFlip(0, 1);
      System.out.print("d = ");
      System.out.println(d);
      int seen = Verify.getCounter(0);
      assert ((seen & d) == 0);
      seen |= d;
      Verify.setCounter(0, seen);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }
}
