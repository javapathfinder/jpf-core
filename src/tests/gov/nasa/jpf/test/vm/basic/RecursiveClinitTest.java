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
import java.lang.reflect.Constructor;
import java.lang.reflect.Method;
import org.junit.Test;

/**
 * test automatic and recursive clinit invocation
 */
public class RecursiveClinitTest extends TestJPF {

  static class Base {
    static int d = 1;
    static {
      System.out.println("Base clinit");
    }
  }

  static class Derived extends Base {
    static int d = Base.d * 42;
    static {
      System.out.println("Derived clinit");
    }
    
    public Derived (int i){
      System.out.println("Derived(" + i + ')');
    }
    
    public static void foo(){
      System.out.println("Derived.foo()");
    }
  }

  @Test 
  public void testStaticField (){
    if (verifyNoPropertyViolation()) {
      System.out.println("main now referencing Derived.d");
      int d = Derived.d;
      System.out.println("back in main");
      
      assertTrue(d == 42);
    }
  }
  
  @Test
  public void testNewInstance (){
    if (verifyNoPropertyViolation()) {
      System.out.println("main now calling Derived.class.newInstance()");
      try {
        Derived.class.newInstance();
      } catch (Throwable t) {
        fail("instantiation failed with " + t);
      }
      System.out.println("back in main");
      
      assertTrue(Derived.d == 42);
    }
  }
  
  @Test
  public void testMethodReflection (){
    if (verifyNoPropertyViolation()) {
      try {
        Class<?> clazz = Class.forName("gov.nasa.jpf.test.vm.basic.RecursiveClinitTest$Derived");
        System.out.println("main now calling Derived.foo()");
        Method m = clazz.getDeclaredMethod("foo", new Class[0]);
        m.invoke(null);
        
        System.out.println("back in main");
        assertTrue(Derived.d == 42);
        
      } catch (Throwable t){
        fail("test failed with: " + t);
      }
    }    
  }
  
  @Test
  public void testCtorReflection (){
    if (verifyNoPropertyViolation()) {
      try {
        Class<?> clazz = Class.forName("gov.nasa.jpf.test.vm.basic.RecursiveClinitTest$Derived");
        System.out.println("main now creating Derived(-42)");
        Constructor ctor = clazz.getConstructor(new Class[] {int.class});
        Object o = ctor.newInstance( new Object[] {Integer.valueOf(-42)});
        
        System.out.println("back in main");
        assertTrue( o instanceof Derived);
        assertTrue(Derived.d == 42);
        
      } catch (Throwable t){
        fail("test failed with: " + t);
      }
    }    
  }
  
  // <2do> we also need the SerializatinConstructor
}
