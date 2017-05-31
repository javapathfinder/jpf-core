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
package gov.nasa.jpf.test.mc.threads;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

/**
 * regression test for concurrent clinit execution
 */
public class ClinitTest extends TestJPF {

  static class X {
    static int x;

    static {
      Verify.threadPrintln("initializing X");
      assertTrue(x == 0);
      x++;
    }
  }

  @Test
  public void testNoConcurrentClinit() {
    if (verifyNoPropertyViolation()) {

      Runnable r = new Runnable() {
        @Override
        public void run() {
          int x = X.x;
        }
      };
      Thread t = new Thread(r);
      t.start();

      int x = X.x;
      assertTrue("x = " + x, x == 1);
    }
  }


  static class Y {
    static long y;

    static {
      Thread t = Thread.currentThread();
      Verify.threadPrintln("initializing Y");
      y = t.getId();
    }
  }

  @Test
  public void testClinitChoices() {
    if (verifyAssertionErrorDetails("gotcha")) {
      Runnable r = new Runnable() {
        @Override
        public void run() {
          long y = Y.y;
        }
      };
      Thread t = new Thread(r);
      t.start();

      long y = Y.y;
      Thread tCur = Thread.currentThread();
      Verify.threadPrintln("testing Y.y");
      assertTrue("gotcha", y == tCur.getId());
    }
  }
}
