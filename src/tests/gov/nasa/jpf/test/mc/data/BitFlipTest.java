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

  public static void main(String[] args) {
    runTestsOfThisClass(args);
  }

  public void checkBitFlip(int data) {
    int seen = Verify.getCounter(0);
    assert ((seen & data) == 0);
    seen |= data;
    Verify.setCounter(0, seen);
  }

  public static int staticMethod1(@BitFlip int bar) {
    return bar;
  }

  @Test
  public void testAnnotatedStaticMethodParameterBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for static method parameters test");
      int d = staticMethod1(0);
      System.out.print("d = ");
      System.out.println(d);
      checkBitFlip(d);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  public static int staticMethod2(int bar) {
    return bar;
  }

  @Test
  public void testCommandLineSpecifiedStaticMethodParameterBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener",
                "+bitflip.params=foo",
                "+bitflip.foo.method=gov.nasa.jpf.test.mc.data.BitFlipTest.staticMethod2(int)",
                "+bitflip.foo.name=bar")){
      System.out.println("command line specified bit flip for static method parameters test");
      int d = staticMethod2(0);
      System.out.print("d = ");
      System.out.println(d);
      checkBitFlip(d);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  public int instanceMethod1(@BitFlip int bar) {
    return bar;
  }

  @Test
  public void testAnnotatedInstanceMethodParameterBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for instance method parameters test");
      int d = instanceMethod1(0);
      System.out.print("d = ");
      System.out.println(d);
      checkBitFlip(d);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  public int instanceMethod2(int bar) {
    return bar;
  }

  @Test
  public void testCommandLineSpecifiedInstanceMethodParameterBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener",
                "+bitflip.params=foo",
                "+bitflip.foo.method=gov.nasa.jpf.test.mc.data.BitFlipTest.instanceMethod2(int)",
                "+bitflip.foo.name=bar",
                "+bitflip.foo.nbit=1")){
      System.out.println("command line specified bit flip for instance method parameters test");
      int d = instanceMethod2(0);
      System.out.print("d = ");
      System.out.println(d);
      checkBitFlip(d);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @BitFlip
  int annotatedInstanceField;

  @Test
  public void testAnnotatedInstanceFieldBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for instance field test");
      annotatedInstanceField = 0;
      System.out.print("annotatedInstanceField = ");
      System.out.println(annotatedInstanceField);
      checkBitFlip(annotatedInstanceField);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  int instanceField;

  @Test
  public void testCommandLineSpecifiedInstanceFieldBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener",
                "+bitflip.fields=foo",
                "+bitflip.foo.field=gov.nasa.jpf.test.mc.data.BitFlipTest.instanceField")){
      System.out.println("command line specified bit flip for instance field test");
      instanceField = 0;
      System.out.print("instanceField = ");
      System.out.println(instanceField);
      checkBitFlip(instanceField);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @BitFlip
  static int annotatedStaticField;

  @Test
  public void testAnnotatedStaticFieldBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for static field test");
      annotatedStaticField = 0;
      System.out.print("annotatedStaticField = ");
      System.out.println(annotatedStaticField);
      checkBitFlip(annotatedStaticField);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  static int staticField;

  @Test
  public void testCommandLineSpecifiedStaticFieldBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener",
                "+bitflip.fields=foo",
                "+bitflip.foo.field=gov.nasa.jpf.test.mc.data.BitFlipTest.staticField",
                "+bitflip.foo.nbit=1")){
      System.out.println("command line specified bit flip for static field test");
      staticField = 0;
      System.out.print("staticField = ");
      System.out.println(staticField);
      checkBitFlip(staticField);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @Test
  public void testAnnotatedLocalVariableBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener")){
      System.out.println("@BitFlip annotation for local variable test");
      @BitFlip int d = 0;
      System.out.print("d = ");
      System.out.println(d);
      checkBitFlip(d);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }

  @Test
  public void testCommandLineSpecifiedLocalVariableBitFlip() {

    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.listener.BitFlipListener",
                "+bitflip.localvars=bar",
                "+bitflip.bar.method=gov.nasa.jpf.test.mc.data.BitFlipTest.testCommandLineSpecifiedLocalVariableBitFlip()",
                "+bitflip.bar.name=d")){
      System.out.println("command line specified bit flip for local variable test");
      int d = 0;
      System.out.print("d = ");
      System.out.println(d);
      checkBitFlip(d);
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
      checkBitFlip(d);
    } else {
      assert Verify.getCounter(0) == -1;
    }
  }
}
