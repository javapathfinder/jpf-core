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

package gov.nasa.jpf.test.java.lang.reflect;

import gov.nasa.jpf.util.test.TestJPF;
import java.lang.reflect.Method;
import org.junit.Test;

public class MethodTest extends TestJPF {

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  @Test
  public void equalsTest () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      Method m1 = MethodTest.class.getMethod("equalsTest", new Class[0]);
      Method m2 = MethodTest.class.getMethod("equalsTest", new Class[0]);
      assertTrue(m1.equals(m2));
      assertFalse(m1 == m2);
    }
  }

  public void testIsVarArg1s (Class<?>... argTypes){
  }

  public void testIsVarArgs2 (Class<?>[] argTypes){
  }

  @Test
  public void isVarArgsTest () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      for (Method m : MethodTest.class.getDeclaredMethods()){
        if (m.getName().equals("testIsVarArg1s"))
          assertTrue(m.isVarArgs());
        else if (m.getName().equals("testIsVarArg1s")) {
          assertFalse(m.isVarArgs());
        }
      }
    }
  }

  @Test
  public void hashCodeTest () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      Method m1 = MethodTest.class.getMethod("hashCodeTest", new Class[0]);
      Method m2 = MethodTest.class.getMethod("hashCodeTest", new Class[0]);
      Method m3 = MethodTest.class.getMethod("equalsTest", new Class[0]);
      assertTrue(m1.equals(m2));
      assertTrue(m1.hashCode() == m2.hashCode());
      assertFalse(m1.hashCode() == m3.hashCode());
    }
  }

  public static class A {
    public A foo (int a){
      return new A();
    }
  }

  public static class B extends A {
    @Override
	public B foo (int x){
      return new B();
    }
  }

  @Test
  public void isBridgeTest (){
    if (verifyNoPropertyViolation()){
      assertFalse(B.class.getDeclaredMethods()[0].isBridge());
      assertTrue(B.class.getDeclaredMethods()[1].isBridge());
    }
  }
  
  //--- aux
  
  void recordSeen (boolean[] seen, String[] expected, Method m){
    String mname = m.toString();
    for (int i=0; i<expected.length; i++){
      if (expected[i].equals(mname)){
        seen[i] = true;
      }
    }
  }
  
  boolean checkSeen(boolean[] seen, String[] expected){
    for (int i=0; i<expected.length; i++){
      if (!seen[i]){
        System.out.println("NOT seen: " + expected[i]);
        return false;
      }
    }
    return true;
  }
  
  //------------ getMethods() on interfaces
  interface I1 {
    void i1();
  }
  
  interface I2 extends I1 {
    void i2();
  }
  
  @Test
  public void testGetMethodsOnIfc(){
    if (verifyNoPropertyViolation()){
      String[] expected = {
        "public abstract void gov.nasa.jpf.test.java.lang.reflect.MethodTest$I2.i2()",
        "public abstract void gov.nasa.jpf.test.java.lang.reflect.MethodTest$I1.i1()"
      };
      boolean[] seen = new boolean[expected.length];
      
      Method[] methods = I2.class.getMethods();
      
      for (Method m : methods){
        System.out.println(m);
        recordSeen(seen, expected, m);
      }
      assertTrue(methods.length == expected.length);
      //assertTrue(checkSeen(seen, expected));
    }
  }
  
  
  //------------ getMethods() on classes
  public static class C extends B {
    static {
      System.out.println("C.<clinit>");
    }
    
    // non-public method
    void nope(){
    }
    
    // ctor
    C (){
      System.out.println("C.<init>");      
    }
  } 
  
  @Test
  public void testGetMethodsOnClass(){
    if (verifyNoPropertyViolation()){
      String[] expected = {
        "public native int java.lang.Object.hashCode()",
        "public final native void java.lang.Object.notify()",
        "public final native void java.lang.Object.notifyAll()",
        "public java.lang.String java.lang.Object.toString()",
        "public final native java.lang.Class java.lang.Object.getClass()",
        "public final native void java.lang.Object.wait(long)",
        "public final void java.lang.Object.wait(long,int)",
        "public gov.nasa.jpf.test.java.lang.reflect.MethodTest$B gov.nasa.jpf.test.java.lang.reflect.MethodTest$B.foo(int)",
        "public boolean java.lang.Object.equals(java.lang.Object)",
        "public volatile gov.nasa.jpf.test.java.lang.reflect.MethodTest$A gov.nasa.jpf.test.java.lang.reflect.MethodTest$B.foo(int)",
        "public final void java.lang.Object.wait()"
      };
      boolean[] seen = new boolean[expected.length];
      
      Method[] methods = C.class.getMethods();
      for (Method m : methods){
        System.out.println(m);
      }
    }
  }
}
