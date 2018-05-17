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
 * basic method invocation test
 */


interface TMI {
  void gna();
}

class TestMethodBase extends TestJPF implements TMI {
  
  int baseData;

  static int sData;
  
  static {
    sData = -1;
  }
  
  static void taz () {
    sData = 24;
  }
  
  TestMethodBase (int a) {
    assert a == 42;
    baseData = 42;
  }

  boolean baz (boolean p, byte b, char c, short s, int i, long l, float f, double d, Object o) {
    assert p;
    assert b == 4;
    assert c == '?';
    assert s == 42;
    assert i == 4242;
    assert l == 424242;
    assert f == 4.2f;
    assert d == 4.242;
    assert o.equals(new Integer(42));

    baseData = 44;

    return p;
  }
  
  void faz () {
    gna();
  }
  
  @Override
  public void gna () {
    baseData = 0;
  }
  
  int har () {
    return priv();
  }
  
  private int priv () {
    return 7;
  }
}

public class MethodTest extends TestMethodBase {
  
  int data;
  
  static void taz () {
    sData = 9;
  }
  
  public MethodTest () {
    super(42);
    
    data = 42;
  }

  MethodTest (int a) {
    super(a);
    
    data = a;
  }
  
  double foo (boolean p, byte b, char c, short s, int i, long l, float f, double d, Object o) {
    assert p;
    assert b == 4;
    assert c == '?';
    assert s == 42;
    assert i == 4242;
    assert l == 424242;
    assert f == 4.2f;
    assert d == 4.242;
    assert o.equals(new Integer(42));

    data = 43;

    return d;
  }

  @Override
  public void gna () {
    baseData = 45;
  }
  
  int priv () {
    return 8;
  }
  
  @Test
  public void testCtor() {
    if (verifyNoPropertyViolation()) {
      MethodTest o1 = new MethodTest();
      assert o1.data == 42;
      assert o1.baseData == 42;

      MethodTest o2 = new MethodTest(42);
      assert o2.data == 42;
      assert o2.baseData == 42;
    }
  }

  @Test
  public void testCall() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();
      assert o.foo(true, (byte) 4, '?', (short) 42, 4242, 424242, 4.2f, 4.242, new Integer(42)) == 4.242;
      assert o.data == 43;
    }
  }

  @Test
  public void testInheritedCall() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();
      assert o.baz(true, (byte) 4, '?', (short) 42, 4242, 424242, 4.2f, 4.242, new Integer(42));
      assert o.baseData == 44;
    }
  }

  @Test
  public void testVirtualCall() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();
      TestMethodBase b = o;

      b.faz();
      assert o.baseData == 45;
    }
  }

  @Test
  public void testSpecialCall() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();
      assert o.har() == 7;
    }
  }

  @Test
  public void testStaticCall() {
    if (verifyNoPropertyViolation()) {
      assert TestMethodBase.sData == -1;

      MethodTest.taz();
      assert TestMethodBase.sData == 9;

      TestMethodBase.taz();
      assert TestMethodBase.sData == 24;

      // used to be:
      //TestMethod o = new TestMethod();
      //o.taz();
      // statically equiv. to this: (no warnings) - pcd
      new MethodTest();
      MethodTest.taz();

      assert TestMethodBase.sData == 9;
    }
  }

  @Test
  public void testInterfaceCall() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();
      TMI ifc = o;

      ifc.gna();
      assert o.baseData == 45;
    }
  }
  
  static class A {
    public int foo () {
      assert false : "should bever be invoked";
      return -1;
    }
  }
  
  static class A0 extends A {
    @Override
	public int foo () {
      return 0;
    }
  }
  
  static class A1 extends A {
    @Override
	public int foo () {
      return 1;
    }
  }
    
  /**
   * this is tricky - both the allocations and the foo() calls have to be
   * the same instructions, and we have to remove all 'a' references before
   * forcing a GC. Otherwise we don't allocate 'a's with the same reference values 
   *
   */
  @Test public void testCallAcrossGC () {

    if (verifyNoPropertyViolation()){
      System.out.println("testing virtual calls on GC boundaries");
      A a;

      a = new A0();
      assert a.foo() == 0 : "wrong A.foo() called for A0";

      // do a GC
      Runtime.getRuntime().gc(); // this should recycle 'a'

      a = new A1();
      assert a.foo() == 1 : "wrong A.foo() called for A1";
    }
  }


  interface InterfaceWithDefaultMethod {
    default int foo() {
      return 42;
    }
  }

  static class Base implements InterfaceWithDefaultMethod {
  }

  static class Child extends Base implements InterfaceWithDefaultMethod {
  }

  @Test
  public void testDefaultMethodCall() {
    if (verifyNoPropertyViolation()) {
      assertEquals(42, new Child().foo());
    }
  }
}
