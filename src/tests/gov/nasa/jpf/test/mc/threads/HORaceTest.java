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

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * high order data race
 */
public class HORaceTest extends TestJPF {

  static class D {
    int a;
    int b;

    D (int x, int y){
      a = x;
      b = y;
    }

    D (D other){
      setA( other.getA());
      Thread.yield(); // give the 2nd thread a chance to interfere
      setB( other.getB());
    }

    synchronized int getA() { return a; }
    synchronized int getB() { return b; }
    synchronized void setA(int x){ a = x; }
    synchronized void setB(int x){ b = x; }

    synchronized void change(int delta) {
      a += delta;
      b += delta;
    }

    synchronized boolean isConsistent() {
      return a == b;
    }
  }

  static D d1;
  static D d2;

  @Test
  public void testHighOrderRace() {

    if (verifyAssertionErrorDetails("inconsistent d2")) {
      d1 = new D(42, 42);

      Thread t1 = new Thread() {

        @Override
		public void run() {
          d2 = new D(d1);
        }
      };
      Thread t2 = new Thread() {

        @Override
		public void run() {
          d1.change(-1);
        }
      };

      t1.start();
      t2.start();

      try {
        t1.join();
        t2.join();
      } catch (InterruptedException ix) {
        fail("unexpected interrupt during {t1,t2}.join()");
      }

      System.out.print("d2 = ");
      System.out.print(d2.a);
      System.out.print(',');
      System.out.println(d2.b);

      assert d2.isConsistent() : "inconsistent d2";
    }
  }

}
