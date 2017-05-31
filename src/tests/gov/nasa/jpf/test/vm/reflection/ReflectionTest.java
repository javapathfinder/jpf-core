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

import org.junit.Test;

public class ReflectionTest extends TestJPF {

  static class MyClass {
    void bar(){
      foo();
    }

    // compilation will cause a warning about internal proprietary API that cannot be suppressed, but we have to test this
    // since it is still used by standard libs
    void foo (){
      Class<?> callerCls = sun.reflect.Reflection.getCallerClass(0); // that would be getCallerClass()
      System.out.println("-- getCallerClass(0) = " + callerCls);
      assertTrue(callerCls.getName().equals("sun.reflect.Reflection"));
      
      callerCls = sun.reflect.Reflection.getCallerClass(1); // foo()
      System.out.println("-- getCallerClass(1) = " + callerCls);
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.vm.reflection.ReflectionTest$MyClass"));
      
      callerCls = sun.reflect.Reflection.getCallerClass(2); // bar()
      System.out.println("-- getCallerClass(2) = " + callerCls);
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.vm.reflection.ReflectionTest$MyClass"));

      callerCls = sun.reflect.Reflection.getCallerClass(3); // callIt()
      System.out.println("-- getCallerClass(3) = " + callerCls);
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.vm.reflection.ReflectionTest"));
      
      // <2do> should also test Method.invoke skipping
    }
  }
  
  void callIt(){
    MyClass o = new MyClass();
    o.bar();
  }
  
  @Test
  public void testCallerClass() {
    if (verifyNoPropertyViolation()){
      callIt();
    }
  }
}
