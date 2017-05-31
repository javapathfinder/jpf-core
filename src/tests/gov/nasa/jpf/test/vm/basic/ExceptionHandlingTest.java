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

import java.lang.reflect.Method;

import org.junit.Test;

/**
 * JPF unit test for exception handling
 */
@SuppressWarnings("null")
public class ExceptionHandlingTest extends TestJPF {
  int data;

  void foo () {
  }
  
  static void bar () {
    ExceptionHandlingTest o = null;
    o.foo();
  }
  
  @Test public void testNPE () {
    if (verifyUnhandledException("java.lang.NullPointerException")){
      ExceptionHandlingTest o = null;
      o.data = -1;

      assert false : "should never get here";
    }
  }
  
  @Test public void testNPECall () {
    if (verifyUnhandledException("java.lang.NullPointerException")){
      ExceptionHandlingTest o = null;
      o.foo();

      assert false : "should never get here";
    }
  }

  @Test public void testArrayIndexOutOfBoundsLow () {
    if (verifyUnhandledException("java.lang.ArrayIndexOutOfBoundsException")){
      int[] a = new int[10];
      a[-1] = 0;

      assert false : "should never get here";
    }
  }

  @Test public void testArrayIndexOutOfBoundsHigh () {
    if (verifyUnhandledException("java.lang.ArrayIndexOutOfBoundsException")){
      int[] a = new int[10];
      a[10] = 0;

      assert false : "should never get here";
    }
  }

  @Test public void testLocalHandler () {
    if (verifyNoPropertyViolation()){
      try {
        ExceptionHandlingTest o = null;
        o.data = 0;
      } catch (IllegalArgumentException iax) {
        assert false : "should never get here";
      } catch (NullPointerException npe) {
        return;
      } catch (Exception x) {
        assert false : "should never get here";
      }

      assert false : "should never get here";
    }
  }

  @Test public void testCallerHandler () {
    if (verifyNoPropertyViolation()){
      try {
        bar();
      } catch (Throwable t) {
        return;
      }

      assert false : "should never get here";
    }
  }
  
  @Test public void testEmptyHandler () {
    if (verifyNoPropertyViolation()){
      try {
        throw new RuntimeException("should be empty-handled");
      } catch (Throwable t) {
        // nothing
      }
    }
  }
  
  @Test public void testEmptyTryBlock () {
    if (verifyNoPropertyViolation()){
      try {
        // nothing
      } catch (Throwable t) {
        assert false : "should never get here";
      }
    }
  }
  
  @Test public void testStackTrace() {
    if (verifyNoPropertyViolation()){

      Throwable x = new Throwable();
      StackTraceElement[] st = x.getStackTrace();

      //x.printStackTrace();
      for (int i=0; i<st.length; i++){
        System.out.print("\t at ");
        System.out.print(st[i].getClassName());
        System.out.print('.');
        System.out.print(st[i].getMethodName());
        System.out.print('(');
        System.out.print(st[i].getFileName());
        System.out.print(':');
        System.out.print(st[i].getLineNumber());
        System.out.println(')');
      }

      // note - direct call stackframes should not show up here, they are JPF internal
      assert st.length == 3 : "wrong stack trace depth";

      assert st[0].getClassName().equals(ExceptionHandlingTest.class.getName());
      assert st[0].getMethodName().equals("testStackTrace");

      assert st[1].getClassName().equals(Method.class.getName());
      assert st[1].getMethodName().equals("invoke");

      assert st[2].getClassName().equals(TestJPF.class.getName());
      assert st[2].getMethodName().equals("runTestMethod");
    }
  }  
}

