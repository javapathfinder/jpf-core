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
package gov.nasa.jpf.test.mc.basic;


import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import java.io.File;

import org.junit.Test;

public class TraceTest extends TestJPF {

  static final String TEST_CLASS = TraceTest.class.getName();
  static final String TRACE = "trace";

  // the method run by JPF
  public void foo () {
    int a = Verify.getInt(0, 42);
    int b = Verify.getInt(100, 142);

    System.out.println("pre-trace choice: " + a + ',' + b);
    Verify.incrementCounter(0);

    Verify.storeTraceAndTerminateIf(!Verify.isTraceReplay(),
            TRACE, "cut off here..");

    int c = Verify.getInt(0, 3);
    Verify.incrementCounter(0);
    System.out.println("post-trace choice: " + a + ',' + b + ',' + c);
  }

  // the method that runs JPF
  @Test
  public void testPartialTrace () {
    File tf = new File(TRACE);

    try {
      if (tf.exists()) {
        tf.delete();
      }
      Verify.resetCounter(0);

      // first JPF run
      noPropertyViolation(setTestMethod(TEST_CLASS, "foo"));

      if (Verify.getCounter(0) != 1) {
        fail("wrong number of backtracks on non-replay run: " + Verify.getCounter(0));
      }

      // second JPF run
      noPropertyViolation( setTestMethod(TEST_CLASS, "foo"), "+listener=.listener.ChoiceSelector",
              "+choice.use_trace=" + TRACE);

      if (Verify.getCounter(0) != 5) {
        fail("wrong number of backtracks on replay run: " + Verify.getCounter(0));
      }

    } finally {
      tf.delete();
    }
  }


  // the method run by JPF
  public void bar () {
    int i1 = Verify.getInt(0, 5);
    int i2 = Verify.getInt(0, 5);
    int i3 = Verify.getInt(0, 5);
    boolean b1 = Verify.getBoolean();
    int i4 = Verify.getInt(0,3);

    System.out.printf("%d,%d,%d,%b,%d\n", i1, i2, i3, b1, i4);
    
    
    assert !(i1 == 0 && i2 == 1 && i3 == 2 && b1 && i4 == 3);
  }

  // the method that runs JPF
  @Test public void testErrorTrace () {
    File tf = new File(TRACE);

    try {
      if (tf.exists()) {
        tf.delete();
      }

      // first JPF run
      System.out.println("--- creating trace");
      assertionError(setTestMethod("bar"), "+listener=.listener.TraceStorer", "+trace.file=" + TRACE);

      // second JPF run
      System.out.println("--- replaying trace");
      assertionError(setTestMethod("bar"), "+listener=.listener.ChoiceSelector","+choice.use_trace=" + TRACE);
    } finally {
      tf.delete();
    }
  }

  public void baz() {
    // note there always is an automatic thread-CG after static initialization
    boolean a = Verify.getBoolean();  // depth 2
    System.out.print("a=");
    System.out.println(a);
    boolean b = Verify.getBoolean();  // depth 3
    System.out.print("b=");
    System.out.println(a);
    boolean c = Verify.getBoolean();  // depth 4
    System.out.print("c=");
    System.out.println(a);
    assert false : "should not search up to this";
  }

  @Test public void testDepth () {
    File tf = new File(TRACE);

    try {
      if (tf.exists()) {
        tf.delete();
      }

      noPropertyViolation("+listener=.listener.TraceStorer",
              "+trace.file=" + TRACE,
              "+trace.depth=3",
              "+search.depth_limit=3",
              TEST_CLASS, "baz");

      noPropertyViolation("+listener=.listener.ChoiceSelector",
              "+choice.use_trace=" + TRACE,
              "+search.depth_limit=3",
              TEST_CLASS, "baz");
    } finally {
      tf.delete();
    }
  }
}
