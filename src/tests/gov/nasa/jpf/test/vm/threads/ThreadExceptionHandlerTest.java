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
package gov.nasa.jpf.test.vm.threads;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/** Unit tests for the three levels of exception handlers that threads have. */
public class ThreadExceptionHandlerTest extends TestJPF {

  static int n = 0;

  public final static void main(String[] testMethods) {
    runTestsOfThisClass(testMethods);
  }

  class NPEHandler implements Thread.UncaughtExceptionHandler {

    @Override
	public void uncaughtException(Thread t, Throwable e) {
      /**
      System.out.println("--- in NPEHandler.uncaughtException");
      System.out.print("   "); System.out.println(t);
      System.out.print("   "); System.out.println(e);
      assertTrue(e instanceof NullPointerException);
      **/
      n = 1;
    }
  }

  class NPEHandler2 extends ThreadGroup {

    public NPEHandler2(String name) {
      super(name);
    }

    public NPEHandler2(ThreadGroup parent, String name) {
      super(parent, name);
    }

    @Override
	public void uncaughtException(Thread t, Throwable e) {
      assertTrue(e instanceof NullPointerException);
      n = 2;
    }
  }

  class NPEHandler3 extends ThreadGroup {

    public NPEHandler3(String name) {
      super(name);
    }

    @Override
	public void uncaughtException(Thread t, Throwable e) {
      assertTrue(e instanceof NullPointerException);
      n = 3;
    }
  }

  class NPEHandler4 extends ThreadGroup {
    public NPEHandler4(ThreadGroup parent, String name) {
      super(parent, name);
    }

    // this one doesn't override uncaughtException()
  }

  class NPEHandlerExc implements Thread.UncaughtExceptionHandler {

    @Override
	public void uncaughtException(Thread t, Throwable e) {
      throw new NullPointerException("test");
    }
  }
  
  class TestRunnable implements Runnable {

    @Override
	public void run() {
      throw new NullPointerException();
    }
  }

  class TestRunnable2 implements Runnable {

    @Override
	public void run() {
    }
  }

  /* DefaultHandler is null, the other two are equal */
  @Test
  public void checkDefaults() {
    if (verifyNoPropertyViolation()){
      Thread ct = Thread.currentThread();
      assertEquals(ct.getUncaughtExceptionHandler(),
              ct.getThreadGroup());
      assertNull(Thread.getDefaultUncaughtExceptionHandler());
    }
  }

  /* Test if handler gets executed */
  @Test
  public void testChildHandler() {
    if (verifyNoPropertyViolation("+vm.ignore_uncaught_handler=false",
                                  "+vm.pass_uncaught_handler")){
      n = 0;
      Thread w = new Thread(new TestRunnable());
      w.setUncaughtExceptionHandler(new NPEHandler());
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
      assertEquals(n, 1);
      n = 0;
    }
  }

  /* Test if default handler gets executed */
  /* The existing handlers should forward the exception to the newly
   * set default handler. */
  @Test
  public void testChildDefaultHandler() {
    if (verifyNoPropertyViolation("+vm.ignore_uncaught_handler=false",
                                  "+vm.pass_uncaught_handler")){
      n = 0;
      Thread w = new Thread(new TestRunnable());
      Thread.setDefaultUncaughtExceptionHandler(new NPEHandler());
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
      assertEquals(n, 1);
      n = 0;
    }
  }

  /* Handler information should be set to null after thread termination,
   * except for custom default handler which does not get reset (this is
   * poorly documented in official Java 1.6. */
  @Test
  public void testChildHandlerAfterTermination() {
    if (verifyNoPropertyViolation()){
      Thread w = new Thread(new TestRunnable2());
      w.setUncaughtExceptionHandler(new NPEHandler());
      Thread.setDefaultUncaughtExceptionHandler(new NPEHandler());
      assertNotNull(w.getUncaughtExceptionHandler());
      assertNotNull(w.getThreadGroup());
      assertNotNull(Thread.getDefaultUncaughtExceptionHandler());
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
      assertNull(w.getUncaughtExceptionHandler());
      assertNull(w.getThreadGroup());
      assertNotNull(Thread.getDefaultUncaughtExceptionHandler());
    }
  }

  /* The uncaughtHandler has precedence over any other handlers,
   * including the thread group a thread belongs to. */
  @Test
  public void testPrecedence1() {
    if (verifyNoPropertyViolation("+vm.ignore_uncaught_handler=false",
                                  "+vm.pass_uncaught_handler")){
      n = 0;
      Thread w = new Thread(new NPEHandler2("test"), new TestRunnable());
      w.setUncaughtExceptionHandler(new NPEHandler());
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
      assertEquals(n, 1);
      n = 0;
    }
  }

  /* The handler of a thread's ThreadGroup has precedence over
   * the default handler. */
  @Test
  public void testPrecedence2() {
    if (verifyNoPropertyViolation("+vm.ignore_uncaught_handler=false",
                                  "+vm.pass_uncaught_handler")){
      n = 0;
      Thread w = new Thread(new NPEHandler2("test"), new TestRunnable()); // n = 2
      Thread.setDefaultUncaughtExceptionHandler(new NPEHandler()); // n = 1
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
      assertEquals(n, 2);
      n = 0;
    }
  }

  /* The handler of a ThreadGroup's parent has precedence over
   * the child ThreadGroup and the default handler. */
  @Test
  public void testPrecedence3() {
    if (verifyNoPropertyViolation("+vm.ignore_uncaught_handler=false",
                                  "+vm.pass_uncaught_handler")){
      n = 0;
      Thread w =
              new Thread(new NPEHandler4(new NPEHandler3("parent"), "child"),
              new TestRunnable());
      Thread.setDefaultUncaughtExceptionHandler(new NPEHandler());
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
      assertEquals(n, 3);
      n = 0;
    }
  }

  /* Custom exception handler throws (unhandled) exception */
  @Test
  public void testHandlerThrowsExc() {
    if (verifyUnhandledException("java.lang.NullPointerException",
				 "+vm.ignore_uncaught_handler=false",
                                 "+vm.pass_uncaught_handler")){
      Thread w = new Thread(new TestRunnable());
      Thread.setDefaultUncaughtExceptionHandler(new NPEHandlerExc());
      w.start();
      try {
        w.join();
      } catch (InterruptedException e) {
      }
    }
  }
}
