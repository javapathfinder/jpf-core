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
package gov.nasa.jpf.test.vm.reflection;

import gov.nasa.jpf.util.test.TestJPF;

import java.lang.annotation.Annotation;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.junit.Test;

public class MethodTest extends TestJPF {

  private double m_data = 42.0;
  private Object m_arg;

  static class Boo {

    static int d = 42;
  }

  static class Faz {

    static int d = 4200;
    
    static private int foo (int a){
      return a + 42;
    }
  }

  static class SupC {

    private int privateMethod() {
      return -42;
    }
  }

  static class SubC extends SupC {

    public int privateMethod() {
      return 42;
    }
  }

  public Boo getBoo() {
    return null;
  }

  public double foo(int a, double d, String s) {
    System.out.printf("in foo( %d, %f, %s)\n", a,d,s);
    
    assert m_data == 42.0 : "wrong object data";
    assert a == 3 : "wrong int parameter value";
    assert d == 3.33 : "wrong double parameter value";
    assert "Blah".equals(s) : "wrong String parameter value";

    return 123.456;
  }

  @Test
  public void testInstanceMethodInvoke() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();

      try {
        Class<?> cls = o.getClass();
        Method m = cls.getMethod("foo", int.class, double.class, String.class);

        Object res = m.invoke(o, new Integer(3), new Double(3.33), "Blah");
        double d = ((Double) res).doubleValue();
        System.out.println("foo returned " + d);

        assert d == 123.456 : "wrong return value";

      } catch (Throwable t) {
        t.printStackTrace();

        assert false : " unexpected exception: " + t;
      }
    }
  }

  public static int harr (int a){
    System.out.printf("in harr(%d)\n", a);
    
    return a+1;
  }
  
  @Test
  public void testStaticMethodInvoke() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();

      try {
        Class<?> cls = o.getClass();
        Method m = cls.getMethod("harr", int.class);

        Object res = m.invoke(null, new Integer(41));
        int r = (Integer)res;
        System.out.println("harr returned " + r);

        assert r == 42 : "wrong return value";

      } catch (Throwable t) {
        t.printStackTrace();

        assert false : " unexpected exception: " + t;
      }
    }
  }
  
  public static void doAlmostNothing(){
    System.out.println("in doAlmostNothing");
  }
  
  @Test
  public void testNoArgStaticMethodInvoke() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();

      try {
        Class<?> cls = o.getClass();
        Method m = cls.getMethod("doAlmostNothing");

        Object res = m.invoke(null, (Object[])null);
        System.out.println("doAlmostNothing returned " + res);

        assert res == null : "wrong return value";

      } catch (Throwable t) {
        t.printStackTrace();

        assert false : " unexpected exception: " + t;
      }
    }
  }
  
  
  
  @Test
  public void getPrivateMethod() throws NoSuchMethodException {
    if (verifyUnhandledException(NoSuchMethodException.class.getName())) {
      Integer.class.getMethod("toUnsignedString0", int.class, int.class);   // Doesn't matter which class we use.  It just needs to be a different class and a private method.
    }
  }

  private static void privateStaticMethod() {
  }

  @Test
  public void invokePrivateSameClass() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    privateStaticMethod();   // Get rid of IDE warning

    if (verifyNoPropertyViolation()) {
      Method m = getClass().getDeclaredMethod("privateStaticMethod");

      m.invoke(null);
    }
  }

  @Test
  public void invokePrivateOtherClass() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyUnhandledException(IllegalAccessException.class.getName())) {
      Method m = Faz.class.getDeclaredMethod("foo", int.class);

      int res = (Integer)m.invoke(null, 5);
      fail("should never get here");
    }
  }

  @Test
  public void invokePrivateOtherClassAccessible() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyNoPropertyViolation()) {
      Method m = Faz.class.getDeclaredMethod("foo", int.class);

      m.setAccessible(true);
      int res = (Integer)m.invoke(null, 5);
      assertTrue( res == 47);
    }
  }

  @Test
  public void invokePrivateSuperclass() throws SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyNoPropertyViolation()) {
      Method aMethod = SupC.class.getDeclaredMethod("privateMethod");
      aMethod.setAccessible(true);
      assert ((Integer) aMethod.invoke(new SubC()) == -42) : "must call method from superclass";
    }
  }

  @Test
  public void getMethodCanFindNotify() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()) {
      Integer.class.getMethod("notify");
    }
  }

  @Test
  public void getDeclaredMethodCantFindNotify() throws NoSuchMethodException {
    if (verifyUnhandledException(NoSuchMethodException.class.getName())) {
      Integer.class.getDeclaredMethod("notify");
    }
  }

  public void publicMethod() {
  }

  @Test
  public void invokeWrongThisType() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyUnhandledException(IllegalArgumentException.class.getName())) {
      Method m = getClass().getMethod("publicMethod");

      m.invoke(new Object());
    }
  }

  @Test
  public void invokeNullObject() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyUnhandledException(NullPointerException.class.getName())) {
      Method m = getClass().getMethod("publicMethod");

      m.invoke(null);
    }
  }

  @Test
  public void invokeWrongNumberOfArguments() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyUnhandledException(IllegalArgumentException.class.getName())) {
      Method m = getClass().getMethod("publicMethod");

      m.invoke(this, 5);
    }
  }

  @Test
  public void invokeWrongArgumentTypeReference() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    if (verifyUnhandledException(IllegalArgumentException.class.getName())) {
      Method m = getClass().getMethod("boofaz", Boo.class, Faz.class);

      m.invoke(this, new Faz(), new Boo());
    }
  }

  public void throwThrowable() throws Throwable {
    throw new Throwable("purposeful exception");
  }

  @Test
  public void invokeInvocationTargetException() throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Class<?> clazz;
    Method method;

    if (verifyUnhandledException(InvocationTargetException.class.getName())) {
      clazz = getClass();
      method = clazz.getMethod("throwThrowable");

      method.invoke(this);
    }
  }

  @Test
  public void testReturnType() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();

      try {
        Class<?> cls = o.getClass();
        Method m = cls.getMethod("getBoo");
        Class<?> rt = m.getReturnType();
        String s = rt.getName();

        assert Boo.class.getName().equals(s) : "wrong return type: " + s;

      } catch (Throwable t) {
        t.printStackTrace();

        assert false : " unexpected exception in Method.getReturnType(): " + t;
      }
    }
  }

  public void boofaz(Boo b, Faz f) {
    b = null; // Get rid of IDE warning
    f = null;
  }

  @Test
  public void testParameterTypes() {
    if (verifyNoPropertyViolation()) {
      MethodTest o = new MethodTest();

      try {
        Class<?> cls = o.getClass();

        for (Method m : cls.getMethods()) {
          if (m.getName().equals("boofaz")) {
            Class<?>[] pt = m.getParameterTypes();

            assert Boo.class.getName().equals(pt[0].getName()) : "wrong parameter type 0: " + pt[0].getName();
            assert Faz.class.getName().equals(pt[1].getName()) : "wrong parameter type 1: " + pt[1].getName();
          }
        }

      } catch (Throwable t) {
        t.printStackTrace();

        assert false : " unexpected exception in Method.getParameterTypes(): " + t;
      }
    }
  }

  //--- argument value conversion tests
  
  static final Object[] testArgValues = {
    Byte.valueOf((byte) 7),
    Short.valueOf((short) 8),
    Integer.valueOf(9),
    Long.valueOf(10),
    Float.valueOf(3.1415f),
    Double.valueOf(3.14159),
    Boolean.TRUE,
    Character.valueOf('w'),
    "hello",
    null
  };
  
  static final Object ILLEGAL = new Object(); // we use this to flag an IllegalArgumentException
  
  private void invokeTest (Method m, Object argValue, Object expected){
    System.out.print(argValue);
    System.out.print("=>");
    try {
      Object ret = m.invoke(this, argValue);
      System.out.println(ret);
      if (isJPFRun()) {
        assertTrue( ((ret == null) && (expected == null)) || ret.equals(expected));
      }
      
    } catch (IllegalArgumentException ix){
      System.out.println("ILLEGAL");
      if (isJPFRun()) {
        assertTrue( expected == ILLEGAL);
      }
      
    } catch (Throwable t){
      fail("_test invocation failed for value = " + argValue + " with " + t);
    }    
  }

  //--- boolean argument
  
  boolean _test (boolean v){
    //System.out.println("-- test(boolean) got " + v);
    return v;    
  }
  
  @Test
  public void argTestBoolean() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", boolean.class);
      Object[] expected = { // all but Boolean throws
          ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, Boolean.TRUE, ILLEGAL, ILLEGAL, ILLEGAL
      };

      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }
  
  //--- byte argument
  byte _test(byte v){
    //System.out.println("-- test(long) got " + v);
    return v;
  }
  
  @Test
  public void argTestByte() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", byte.class);
      Object[] expected = { // all but byte throws
          Byte.valueOf((byte)7), ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }    
  }
  
  //--- short argument
  short _test(short v){
    //System.out.println("-- test(short) got " + v);
    return v;
  }
  
  @Test
  public void argTestShort() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", short.class);
      Object[] expected = { // all but byte and short throws
          Short.valueOf((short)7), Short.valueOf((short)8), ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }    
  }

  //--- char argument
  char _test(char v){
    //System.out.println("-- test(char) got " + v);
    return v;
  }
  
  @Test
  public void argTestChar() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", char.class);
      Object[] expected = { // all but char throws
          ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, Character.valueOf('w'), ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }    
  }


  //--- int argument
  int _test(int v){
    //System.out.println("-- test(int) got " + v);
    return v;
  }
  
  @Test
  public void argTestInt() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", int.class);
      Object[] expected = { // all but byte, short, int and char throws
          Integer.valueOf(7), Integer.valueOf(8), Integer.valueOf(9), ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL,
          Integer.valueOf('w'), ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }    
  }

  
  //--- long argument
  long _test(long v){
    //System.out.println("-- test(long) got " + v);
    return v;
  }
  
  @Test
  public void argTestLong() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", long.class);
      Object[] expected = {
          Long.valueOf(7L),Long.valueOf(8L), Long.valueOf(9L), Long.valueOf(10L),
          ILLEGAL, ILLEGAL, ILLEGAL, Long.valueOf('w'), ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }

  //--- float argument
  float _test(float v){
    //System.out.println("-- test(float) got " + v);
    return v;
  }
  
  @Test
  public void argTestFloat() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", float.class);
      Object[] expected = {
          Float.valueOf(7f), Float.valueOf(8f), Float.valueOf(9f), 
          Float.valueOf(10f), Float.valueOf(3.1415f), ILLEGAL, ILLEGAL, 
          Float.valueOf('w'), ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }

  //--- double argument
  double _test(double v){
    //System.out.println("-- test(double) got " + v);
    return v;
  }
  
  @Test
  public void argTestDouble() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", double.class);
      Object[] expected = {
          Double.valueOf(7.0), Double.valueOf(8.0), Double.valueOf(9.0), 
          Double.valueOf(10.0), Double.valueOf(3.1415f), Double.valueOf(3.14159),
          ILLEGAL, Double.valueOf('w'), ILLEGAL, ILLEGAL
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }

  //--- String argument
  String _test(String v){
    //System.out.println("-- test(String) got " + v);
    return v;
  }
  
  @Test
  public void argTestString() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", String.class);
      Object[] expected = {
          ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, ILLEGAL, "hello", null
      };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }
  
  //--- Object argument
  Object _test(Object v){
    //System.out.println("-- test(String) got " + v);
    return v;
  }
  
  @Test
  public void argTestObject() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", Object.class);
      Object[] expected = testArgValues;
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }

  //--- Number argument
  Number _test(Number v){
    //System.out.println("-- test(Number) got " + v);
    return v;
  }
  
  @Test
  public void argTestNumber() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", Number.class);
      Object[] expected = {
          Byte.valueOf((byte) 7),
          Short.valueOf((short) 8),
          Integer.valueOf(9),
          Long.valueOf(10),
          Float.valueOf(3.1415f),
          Double.valueOf(3.14159),
          ILLEGAL,
          ILLEGAL,
          ILLEGAL,
          null  // we already used the real null to flag an IllegalArgumentException
       };
      
      for (int i=0; i<testArgValues.length; i++){
        invokeTest( m, testArgValues[i], expected[i]);
      }
    }
  }

  //--- array argument
  int[] _test(int[] v){
    //System.out.println("-- test(int[]) got " + v);
    return v;
  }
  
  @Test
  public void argTestIntArray() throws NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Method m = MethodTest.class.getDeclaredMethod("_test", int[].class);
      Object[] testVals = {
        new int[0],
        new float[0],
        "blah",
        null
      };
      Object[] expected = {
          testVals[0],
          ILLEGAL,
          ILLEGAL,
          null
       };
      
      for (int i=0; i<testVals.length; i++){
        invokeTest( m, testVals[i], expected[i]);
      }
    }
  }

  
  //--- parameter annotation reflection
  
  @Retention(RetentionPolicy.RUNTIME)
  @interface A {
    String value();
  }
  
  void noFoo() {}
  void noFoo(int a) {}
  void oneFoo (@A("arg 1")int a){}
  void twoFoo (int a, @A("arg 2") int b){}
  
  @Test
  public void testParameterAnnotations(){
    if (verifyNoPropertyViolation()){
      try {
        Method mth;
        Annotation[][] pai;
        Class<MethodTest> cls = MethodTest.class;
/**
        mth = cls.getDeclaredMethod("noFoo");
        pai = mth.getParameterAnnotations();
        assertTrue("should return Annotation[0][] for noFoo()", pai != null && pai.length == 0);
        
        mth = cls.getDeclaredMethod("noFoo", int.class );
        pai = mth.getParameterAnnotations();
        assertTrue("should return Annotation[1][{}] for noFoo(int)", pai != null && pai.length == 1 
            && ((pai[0] != null) && (pai[0].length == 0)));
        System.out.println("noFoo(int) : " + pai[0]);
**/
        mth = cls.getDeclaredMethod("oneFoo", int.class);
        pai = mth.getParameterAnnotations();
        assertTrue("should return Annotation[1][{@A}] for oneFoo(int)", pai != null && pai.length == 1 
            && ((pai[0] != null) && (pai[0].length == 1) && (pai[0][0] instanceof A)));
        System.out.println("oneFoo(@A int) : " + pai[0][0]);

        mth = cls.getDeclaredMethod("twoFoo", int.class, int.class);
        pai = mth.getParameterAnnotations();
        assertTrue("should return Annotation[1][{@A}{}] for twoFoo(int,int)", pai != null && pai.length == 2 
            && ((pai[0] != null) && (pai[0].length == 0))
            && ((pai[1] != null) && (pai[1].length == 1)  && (pai[1][0] instanceof A)));
        System.out.println("twoFoo(int, @A int)  : " + pai[0] + ',' +  pai[1][0]);
        
        
      } catch (Throwable t){
        t.printStackTrace();
        fail("retrieving parameter annotation failed: " + t);
      }

    }
  }
  
}
