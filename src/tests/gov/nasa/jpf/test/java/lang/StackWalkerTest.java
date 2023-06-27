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

import java.lang.invoke.MethodType;
import java.lang.reflect.Method;
import java.util.Collections;
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
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.java.lang.StackWalkerTest$MyClass"));

      callerCls = callerClasses.get(1); // bar()
      assertTrue(callerCls.getName().equals("gov.nasa.jpf.test.java.lang.StackWalkerTest$MyClass"));

      callerCls = callerClasses.get(2); // callIt()
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

  void reflectionCallee() {
    // Case 1, don't show reflect frames
    // By default, reflect frames are filtered out
    final StackWalker walker1 = StackWalker.getInstance();
    List<String> methodNames1 = walker1.walk(s -> s
        .map(StackWalker.StackFrame::getMethodName)
        .collect(Collectors.toList()));
    assertTrue(methodNames1.get(0).equals("reflectionCallee"));
    assertTrue(methodNames1.get(1).equals("testShowReflectFrame"));

    // Case 2, show reflect frames
    final StackWalker walker2 = StackWalker.getInstance(StackWalker.Option.SHOW_REFLECT_FRAMES);
    List<String> methodNames2 = walker2.walk(s -> s
        .map(StackWalker.StackFrame::getMethodName)
        .collect(Collectors.toList()));
    // Since implementation of reflect invocation is JVM's internal detail and
    // could be different across different JVMs. We need to loose the constraints.
    // But there should be at least two `Method::invoke`s on stack (One for
    // reflectionCallee() reflection call and another for unit test reflection call).
    System.out.println(methodNames2);
    assertTrue(Collections.frequency(methodNames2, "invoke") >= 2);
  }

  @Test
  public void testShowReflectFrame() throws Exception {
    if (verifyNoPropertyViolation()){
      Method reflectCallee = StackWalkerTest.class.getDeclaredMethod("reflectionCallee");
      reflectCallee.invoke(new StackWalkerTest());
    }
  }

  @Test
  public void testStackWalkerInCaseOfChoiceGenerator() {
    if (verifyNoPropertyViolation()){
      Thread t1 = new Thread() {
        @Override
        public void run() {
          callIt();
        }
      };
      Thread t2 = new Thread() {
        @Override
        public void run() {
          callIt();
        }
      };
      t1.start();
      t2.start();
    }
  }

  void deepCallRecur(int n, int depth) {
    if (n == 1) {
      final StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
      List<String> methods = walker.walk(s -> s
          .map(StackWalker.StackFrame::getMethodName)
          .filter(m -> m.equals("deepCallRecur"))
          .collect(Collectors.toList()));
      assertEquals(methods.size(), depth);
    } else {
      deepCallRecur(n - 1, depth);
    }
  }

  void deepCall(int depth) {
    deepCallRecur(depth, depth);
  }

  @Test
  public void testDeepStack() {
    if (verifyNoPropertyViolation()) {
      deepCall(100);
    }
  }

  @Test
  public void testToStackTraceElement() {
    if (verifyNoPropertyViolation()) {
      StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
      String thisMethodName = walker.walk(s -> s
          .map(StackWalker.StackFrame::toStackTraceElement)
          .map(StackTraceElement::getMethodName)
          .collect(Collectors.toList()))
          .get(0);
      assertTrue(thisMethodName.equals("testToStackTrace"));
    }
  }

  @Test
  public void testGetMethodType() {
    if (verifyNoPropertyViolation()) {
      StackWalker walker = StackWalker.getInstance(StackWalker.Option.RETAIN_CLASS_REFERENCE);
      MethodType thisMethodType = walker.walk(s -> s
          .limit(1)
          .map(StackWalker.StackFrame::getMethodType)
          .collect(Collectors.toList()))
          .get(0);
      assertTrue(thisMethodType.equals(MethodType.fromMethodDescriptorString("()V", null)));
    }
  }
}
