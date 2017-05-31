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
package java8;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class LambdaTest extends TestJPF{
  
  static class EnforcedException extends RuntimeException {
    // nothing in here
  }
  
  @Test
  public void testFuncObjAssignment() {
    if(verifyUnhandledException(EnforcedException.class.getName())) {
      
      Runnable r = () -> {
        throw new EnforcedException(); // make sure it gets here
        };
      
      assertTrue(r != null);
      
      (new Thread(r)).start();
    }
  }
  
  public interface FI1 {
    void sam();
  }
  
  public interface FI2 extends FI1 {
    @Override
	public String toString();
  }
  
  @Test
  public void testSyntheticFuncObjClass() {
    if (verifyNoPropertyViolation()) {
      
      FI2 fi = () -> {
        return;
        };
      
      assertTrue(fi != null);
      
      Class cls = fi.getClass();
      
      assertEquals(cls.getInterfaces().length, 1);
      
      assertEquals(cls.getDeclaredMethods().length, 1);
            
      assertSame(cls.getInterfaces()[0], FI2.class);
      
      assertSame(cls.getSuperclass(), Object.class);
    }
  }
  
  public interface FI3 {
    public String ret();
  }
  
  @Test
  public void testSAMReturn() {
    if (verifyNoPropertyViolation()) {
      FI3 rt = () -> {
        return "something"; 
        };
      
      assertEquals(rt.ret(),"something"); 
    }
  }
  
  public class C {
    int x = 1;
  }
  
  public interface IncX {
    public int incX(C o);
  }
  
  @Test
  public void testLambdaArgument() {
    if (verifyNoPropertyViolation()) {
      IncX fo = (arg) -> {
        return ++arg.x;
        };
      
      C o = new C();
      
      assertEquals(fo.incX(o),2);
      assertEquals(fo.incX(o),3);
    }
  }
  
  static Integer io = new Integer(20);
  
  @Test
  public void testClosure() {
    if (verifyNoPropertyViolation()) {
      int i = 10;
      
      FI1 fi = () -> {
        assertSame(i,10);
        assertSame(io.intValue(), 20);
      };
      
      fi.sam();
    }
  }
  
  static void method(FI1 fi) {
    fi.sam();
  }
  
  @Test
  public void testPassingToMethod() {
    if (verifyUnhandledException(EnforcedException.class.getName())) {
      int i = 10;
      
      method(() -> {
        assertSame(i,10);
        assertSame(io.intValue(), 20);
        throw new EnforcedException();
      });
    }
  }
  
  // When invokedynamic executes for the first time, it creates a new function object.
  // Re-executing the same bytecode returns the existing function object.
  @Test
  public void testRepeatInvokedynamic() {
    if (verifyNoPropertyViolation()) {
      int i = 10;
      FI1 f1, f2 = null; 
      
      for(int j=0; j<2; j++) {
        f1 = () -> {
          System.out.println("hello world!");
        };
        
        if(j==1) {
          assertTrue(f1!=null);
          assertSame(f1,f2);
        }
        
        f2 = f1;
      }
    }
  }
  
  public static class C2 {
    public static void throwException() {
      throw new EnforcedException();
    }
  }
  
  @Test
  public void testDoubleCloneOperator() {
    if (verifyUnhandledException(EnforcedException.class.getName())) {
      FI1 fi = C2::throwException;
      fi.sam();
    }
  }
  
  static class A {
    static {
      if(true) {
        throw new EnforcedException();
      }
    }
  }

  @Test
  public void testInitDoubleCloneOperator() {
    if (verifyUnhandledException(EnforcedException.class.getName())) {
      new Thread(A::new).start();
    }
  }
  
  static class D {
    static final B b = new B();
  }
  
  static class B {
    static final D a = new D();
  }
  
  @Test
  public void testClinitDeadlock() {
    if(verifyDeadlock()) {
      new Thread(D::new).start();
      new B();
    }
  }
  
  @Test
  public void testLambdaTypeName() {
    if(verifyNoPropertyViolation()) {
      Runnable r1 = (A::new);
      Runnable r2 = (B::new);
      
      assertFalse(r1.getClass().getName().equals(r2.getClass().getName()));
    }
  }
  
  public interface FI {
    default boolean returnTrue() {
      return true;
    }
    @Override
    public String toString();
    public String toString(int i);
  }
  
  @Test
  public void testLambdaWithOverridenDefaultMethods() {
    if(verifyNoPropertyViolation()) {
      FI fi = (int i) -> {return "output:"+ i;};
      assertEquals(fi.toString(10),"output:10");
    }
  }
  
  public interface FI4 {
  }
  
  public interface FI5 extends FI {
    @Override
    public boolean equals(Object obj);
  }
  
  @Test
  public void testLambdaWithMultipleSuperInterfaces() {
    if(verifyNoPropertyViolation()) {
      FI5 fi = (int i) -> {return "output:"+ i;};
      assertEquals(fi.toString(10),"output:10");
    }
  }
  
  public static class Foo {
    
    Integer i = 0;
    static Integer j = 1;
    
    
    public FI1 invokSam(FI1 fi) {
      fi.sam();
      return fi;
    }
    
    
    public FI1 withFreeVar() {
      return invokSam(()->{Foo foo = this;});
    }
    
    public static FI1 withStatic(Foo foo) {
      return foo.invokSam(()->{Foo.j = 10;});
    }
  }
  
  @Test
  public void testFreeVariables() {
    if(verifyNoPropertyViolation()) {
      
      Foo foo = new Foo();
      
      FI1 fi1 = foo.withFreeVar();  
      FI1 fi2 = foo.withFreeVar();
      
      assertFalse(fi1==fi2);
     
      fi1 = Foo.withStatic(foo);
      fi2 = Foo.withStatic(foo);
      
      assertSame(fi1,fi2);
    }
  }
}
