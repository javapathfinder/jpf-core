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
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;


public class AtomicTest extends TestJPF {
  
  static int data = 42;
  
  @Test public void testNoRace () {
    if (verifyNoPropertyViolation("+cg.enable_atomic")) {
      Runnable r = new Runnable() {

        @Override
		public void run() {
          System.out.println("  enter run in Thread-0");
          assert data == 42;
          data += 1;
          assert data == 43;
          data -= 1;
          assert data == 42;
          System.out.println("  exit run in Thread-0");
        }
      };

      Thread t = new Thread(r);

      Verify.beginAtomic();
      System.out.println("enter atomic section in main");
      t.start();
      assert data == 42;
      data += 2;
      assert data == 44;
      data -= 2;
      assert data == 42;
      System.out.println("exit atomic section in main");
      Verify.endAtomic();
    }
  }
  
  @Test 
  public void testDataCG () {
    if (verifyNoPropertyViolation("+cg.enable_atomic")) {
      Runnable r = new Runnable() {

        @Override
		public void run() {
          data += 10;
        }
      };

      Thread t = new Thread(r);

      Verify.beginAtomic();
      t.start();
      int i = Verify.getInt(1, 2);
      data += i;
      assert data < 45 : "data got incremented: " + data;
      Verify.incrementCounter(0);
      assert i == Verify.getCounter(0);
      Verify.endAtomic();
    }
  }

  @Test public void testBlockedInAtomic () {
    if (verifyDeadlock("+cg.enable_atomic")){
      Runnable r = new Runnable() {

        @Override
		public synchronized void run() {
          System.out.println("T notifying..");
          this.notify();
        }
      };

      Thread t = new Thread(r);

      synchronized (r){
        System.out.println("main going atomic, holding r lock");
        Verify.beginAtomic();
        t.start();

        try {
          System.out.println("main waiting on r");
          r.wait();
        } catch (InterruptedException x){
          System.out.println("main got interrupted");
        }
        System.out.println("main leaving atomic");
        Verify.endAtomic();
      }
    }
  }
}
