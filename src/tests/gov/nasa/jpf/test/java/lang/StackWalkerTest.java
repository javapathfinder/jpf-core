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
package gov.nasa.jpf.test.java.lang;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

import java.util.List;
import java.util.stream.Collectors;

public class StackWalkerTest extends TestJPF {

  static class MyClass {
    void bar(){
      foo();
    }

    /**
     * Traverse from the top frame of the stack which is the {@code foo} method
     * and assert that the declaring class of each stack frame is valid.
     * Reflection frames are ignored, see: {@link java.lang.StackWalker.Option#SHOW_REFLECT_FRAMES}
     */
    void foo (){
      final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
      List<Class<?>> callerClasses = walker.walk(s -> s
              .limit(4)
              .map(StackWalker.StackFrame::getDeclaringClass)
              .collect(Collectors.toList()));

      Class<?> callerCls = callerClasses.get(0); // foo()
      System.out.println("-- declaring class of the top frame of the stack = " + callerCls);
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.java.lang.StackWalkerTest$MyClass"));

      callerCls = callerClasses.get(1); // bar()
      System.out.println("-- StackFrame[1].getDeclaringClass = " + callerCls);
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.java.lang.StackWalkerTest$MyClass"));

      callerCls = callerClasses.get(2); // callIt()
      System.out.println("-- StackFrame[2].getDeclaringClass = " + callerCls);
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.java.lang.StackWalkerTest"));
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
