/*
 * Copyright (C) 2021 Pu Yi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */


package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.annotation.BitFlip;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;
import org.junit.Ignore;

/**
 * regression tests for the getBitFlip API and the BitFlipListener
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
