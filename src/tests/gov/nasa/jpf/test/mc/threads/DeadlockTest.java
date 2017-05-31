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
 * various deadlock detection test
 */
public class DeadlockTest extends TestJPF {

  //--- tests
  abstract class SyncRunnable implements Runnable {

    SyncRunnable other;

    public void setOther(SyncRunnable other) {
      this.other = other;
    }

    public abstract void doSomething();

    public abstract void doSomethingElse();

    @Override
	public abstract void run();
  }

  /**
   * Required by the testMissedSignalDeadlock().
   */
  class Event {

    int count = 0;

    public synchronized void signal_event() {
      count = (count + 1) % 3;
      notifyAll();
    }

    public synchronized void wait_for_event() {
      try {
        wait();
      } catch (InterruptedException e) {
      }
    }
  }

  /**
   * Required by the testMissedSignalDeadlock().
   */
  class FirstTask extends java.lang.Thread {

    Event event1;
    Event event2;
    int count = 0;

    public FirstTask(Event e1, Event e2) {
      event1 = e1;
      event2 = e2;
    }

    @Override
	public void run() {
      count = event1.count;

      while (true) {
        if (count == event1.count) {
          event1.wait_for_event();
        }

        count = event1.count;
        event2.signal_event();
      }
    }
  }

  /**
   * Required by the testMissedSignalDeadlock().
   */
  class SecondTask extends java.lang.Thread {

    Event event1;
    Event event2;
    int count = 0;

    public SecondTask(Event e1, Event e2) {
      event1 = e1;
      event2 = e2;
    }

    @Override
	public void run() {
      count = event2.count;

      while (true) {
        event1.signal_event();

        if (count == event2.count) {
          event2.wait_for_event();
        }

        count = event2.count;
      }
    }
  }

  class SyncBlockRunnable extends SyncRunnable {

    @Override
	public void doSomething() {
      synchronized (this) {
        other.doSomethingElse();
      }
    }

    @Override
	public void doSomethingElse() {
      synchronized (this) {
      }
    }

    @Override
	public void run() {
      //while (true) {
        synchronized (this) {
          other.doSomething();
        }
      //}
    }
  }

  class SyncMthRunnable extends SyncRunnable {

    @Override
	public synchronized void doSomething() {
      other.doSomethingElse();
    }

    @Override
	public synchronized void doSomethingElse() {
    }

    @Override
	public synchronized void run() {
      //while (true) {
        other.doSomething();
      //}
    }
  }
  static Object lock1 = new Object();
  static Object lock2 = new Object();
  static int counter;

  /**
   * classical asymmetric lock order deadlock
   */
  @Test
  public void testLockOrderDeadlock() {
    if (verifyDeadlock()) {
      Thread t1 = new Thread(new Runnable() {

        @Override
		public void run() {
          synchronized (lock1) {
            synchronized (lock2) {
              counter++;
            }
          }
          System.out.println("t1 finished");
        }
      });

      Thread t2 = new Thread(new Runnable() {

        @Override
		public void run() {
          synchronized (lock2) {
            synchronized (lock1) {
              counter--;
            }
          }
          System.out.println("t2 finished");
        }
      });

      t1.start();
      t2.start();
    }
  }
  /**
   * a nested monitor lockout
   */
  static Object lock = new Object();
  static Object sig = new Object();

  @Test
  public void testNestedMonitorLockoutDeadlock() {
    if (verifyDeadlock()) {
      Thread t1 = new Thread(new Runnable() {

        @Override
		public void run() {
          for (int i = 0; i < 3; i++) {
            synchronized (lock) {
              synchronized (sig) {
                try {
                  sig.wait();
                } catch (InterruptedException ix) {
                }
              }
            }
          }
          System.out.println("t1 finished");
        }
      });

      Thread t2 = new Thread(new Runnable() {

        @Override
		public void run() {
          for (int i = 0; i < 2; i++) {
            synchronized (sig) {
              synchronized (lock) {
              }
              sig.notify();
            }
          }
          System.out.println("t2 finished");
        }
      });

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testSimpleMissedSignal() {
    if (verifyDeadlock()) {

      Thread t1 = new Thread(new Runnable() {

        @Override
		public void run() {
          synchronized (sig) {
            try {
              sig.wait();
            } catch (InterruptedException ix) {
            }
          }
          System.out.println("t1 finished");
        }
      });

      Thread t2 = new Thread(new Runnable() {

        @Override
		public void run() {
          synchronized (sig) {
            sig.notify();
          }
          System.out.println("t2 finished");
        }
      });

      t1.start();
      t2.start();
    }
  }

  /**
   * Testing for deadlocks resulting from missed signal. This is the original
   * oldclassic example which has been turned into a test case.
   * as the name implies - a missed signal deadlock
   */
  @Test
  public void testMissedSignalDeadlock() {
    if (verifyDeadlock()) {

      Event new_event1 = new Event();
      Event new_event2 = new Event();

      FirstTask task1 = new FirstTask(new_event1, new_event2);
      SecondTask task2 = new SecondTask(new_event1, new_event2);

      task1.start();
      task2.start();
    }
  }

  /**
   * the test which mixes synchronized method attr with MONITORENTER/EXIT
   * simple lock order deadlock
   */
  @Test
  public void testMixedDeadlock() {
    if (verifyDeadlock()) {

      SyncRunnable r1 = new SyncMthRunnable();
      SyncRunnable r2 = new SyncBlockRunnable();
      r1.setOther(r2);
      r2.setOther(r1);

      //Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);
      Thread t1 = new Thread(r1);


      t1.start();
      t2.start();
    }
  }

  /**
   * the test which checks MONITORENTER / MONITOREXIT
   * simple lock order deadlock
   */
  @Test
  public void testSyncBlockDeadlock() {
    if (verifyDeadlock()) {

      SyncRunnable r1 = new SyncBlockRunnable();
      SyncRunnable r2 = new SyncBlockRunnable();
      r1.setOther(r2);
      r2.setOther(r1);

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  /**
   * the test which checks the synchronized method attribute
   * simple lock order deadlock
   */
  @Test
  public void testSyncMthDeadlock() {
    if (verifyDeadlock()) {

      SyncRunnable r1 = new SyncMthRunnable();
      SyncRunnable r2 = new SyncMthRunnable();
      r1.setOther(r2);
      r2.setOther(r1);

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testTerminationDeadlock() {
    if (verifyDeadlock()){
      Thread t = new Thread(){
        @Override
		public void run(){
          System.out.println("# t running");
          synchronized(this){
            System.out.println("# t waiting (forever)..");
            try {
              wait();
            } catch (InterruptedException ix){
              fail("t got unexpectedly interrupted");
            }
          }
        }
      };
      t.start();
      System.out.println("# main thread terminating");
    }
  }
}
