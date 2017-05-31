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
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;


/**
 * regression test for various Thread.join scenarios
 */
public class JoinTest extends TestJPF {

  static final String[] JPF_ARGS = { "+cg.threads.break_start=true",
                                     "+cg.threads.break_yield=true",
                                     "+vm.tree_output=false",
                                     "+vm.path_output=true"};

  @Test public void testSimpleJoin(){
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 run");
        }
      };

      Thread t = new Thread(r);
      t.start();

      try {
        t.join();
        assert !t.isAlive();
        System.out.println("main returned from join");
      } catch (InterruptedException x) {
        fail("join() did throw InterruptedException");
      }
    }
  }

  @Test public void testNoRunnableSimpleJoin() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Thread t = new Thread() {
        @Override
		public synchronized void run() {
          System.out.println("thread-0 run");
        }
      };

      t.start();

      try {
        t.join();
        assert !t.isAlive();
        System.out.println("main returned from join");
      } catch (InterruptedException x) {
        fail("join() did throw InterruptedException");
      }
    }
  }

  static class SomeThread extends Thread {
    Object o;

    @Override
	public void run() {
      synchronized (this){
        // this causes a transition break - write on a shared object while
        // we still hold the lock
        o = new Object();
      }
      System.out.println("thread-0 done");
    }
  }

  @Test public void testBlockedJoin() {
    if (verifyNoPropertyViolation("+cg.threads.break_start=true",
                                  "+vm.storage.class=null")) {
      Thread t = new SomeThread();

      t.start();
      System.out.println("main started thread-0");

      try {
        t.join();
        assert !t.isAlive();
        System.out.println("main returned from join");
      } catch (InterruptedException x) {
        fail("join() did throw InterruptedException");
      }
    }
  }

  @Test public void testJoinHoldingLock(){
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 run");
        }
      };

      Thread t = new Thread(r);
      t.start();

      try {
        synchronized(t){
          t.join();
        }
        System.out.println("main returned from join");
      } catch (InterruptedException x) {
        fail("join() did throw InterruptedException");
      }
    }
  }


  @Test public void testNotAliveJoin(){
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 run");
        }
      };

      Thread t = new Thread(r);
      t.start();

      // poor man's join
      while (t.isAlive()){
        Thread.yield();
      }

      try {
        t.join();
        System.out.println("main returned from join");
      } catch (InterruptedException x) {
        fail("join() did throw InterruptedException");
      }
    }
  }

  @Test public void testPreJoinInterrupt() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 run");
        }
      };

      Thread.currentThread().interrupt();

      Thread t = new Thread(r);
      t.start();

      try {
        t.join();
        fail("join() didn't throw InterruptedException");
      } catch (InterruptedException x) {
        System.out.println("caught InterruptedException");
      }
    }
  }

  @Test public void testInterruptedJoin() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      final Thread mainThread = Thread.currentThread();

      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 interrupting main");
          mainThread.interrupt();
        }
      };

      Thread t = new Thread(r);
      t.start();

      try {
        t.join();
        fail("join() didn't throw InterruptedException");
      } catch (InterruptedException x) {
        System.out.println("caught InterruptedException");
      }
    }
  }

  @Test public void testJoinLoop() {
    if (verifyDeadlock(JPF_ARGS)) {
      try {
        Thread.currentThread().join();
        fail("main can't get here if waiting for itself");
      } catch (InterruptedException ex) {
        fail("thread cannot be interrupted");
      }
    }
  }

  @Test public void testMultipleJoins() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      try {
        final Thread t1 = new Thread() {
          @Override
		public void run() {
            Thread.yield();
          }
        };
        Thread t2 = new Thread() {
          @Override
		public void run() {
            try {
              t1.join();
            } catch (InterruptedException e) {
              fail("unexpected interrupt");
            }
          }
        };

        t1.start();
        t2.start();
        t1.join();
        t2.join();

        assert (!t1.isAlive() && !t2.isAlive());
      } catch (Exception ex) {
        fail("unexpected exception: " + ex);
      }
    }
  }

  @Test public void testJoinBeforeStart() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      try {
        Thread t = new Thread();
        t.join();
        System.out.println("join on not-yet-started thread has no effect");
      } catch (Exception ex) {
        fail(ex.getMessage());
      }
    }
  }


  @Test public void testJoinAfterNotify() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      try {
        final Thread t = new Thread() {
          @Override
		public void run() {
            synchronized(this){
              System.out.println("thread-0 notifying");
              notifyAll(); // this should not get us out of the join()
            }
            System.out.println("thread-0 terminating");
          }
        };

        t.start();
        System.out.println("main joining..");
        t.join();
        System.out.println("main joined");

        Verify.printPathOutput("main termination");
        assert !t.isAlive();

      } catch (Exception ex) {
        fail("unexpected exception: " + ex);
      }
    }
  }

  @Test public void testJoinNotifyDeadlock() {
    if (verifyDeadlock(JPF_ARGS)) {
      try {
        final Thread t = new Thread() {
          @Override
		public void run() {
            synchronized(this){
              System.out.println("thread-0 notifying");
              notifyAll();

              try {
                System.out.println("thread-0 waiting");
                wait();
              } catch (InterruptedException ix){
                System.out.println("unexpected interrupt");
              }
            }
            System.out.println("thread-0 terminating");
          }
        };

        t.start();
        System.out.println("main joining..");
        t.join();
        System.out.println("main joined");

        synchronized (t){
          System.out.println("main notifying");
          t.notify();
        }

        Verify.printPathOutput("main termination");
        assert !t.isAlive();

      } catch (Exception ex) {
        fail("unexpected exception: " + ex);
      }
    }
  }


  @Test public void testRedundantJoin() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      try {
        Thread t = new Thread();

        t.start();
        t.join();
        System.out.println("main returned from first join()");
        t.join();
        System.out.println("main returned from second join()");

        assert (!t.isAlive());
      } catch (Exception ex) {
        fail(ex.getMessage());
      }
    }
  }

  @Test public void testJoinThreadSet(){
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      final Thread[] worker = new Thread[3];
      worker[0] = new Thread(new Runnable() {
        @Override
		public void run() {
          System.out.println("worker[0] finished");
        }
      });
      worker[1] = new Thread(new Runnable() {
        @Override
		public void run() {
          System.out.println("worker[1] finished");
        }
      });
      worker[2] = new Thread(new Runnable() {
        @Override
		public void run() {
          System.out.println("worker[2] finished");
        }
      });

      int nJoin = 0;
      for (int i = 0; i < worker.length; i++) {
        worker[i].start();
        nJoin++;
      }

      while (nJoin > 0) {
        for (int i = 0; i < worker.length; i++) {
          if (worker[i] != null) {
            try {
              worker[i].join();
            } catch (InterruptedException x) {
              fail("unexpected interrupt");
            }
            nJoin--;
            worker[i] = null;
            System.out.println("main joined worker[" + i + "]");
          }
        }
      }
    }
  }


  @Test public void testRecursiveJoinThreadGroup() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
      Verify.resetCounter(2);
      Verify.resetCounter(3);
    }

    if (verifyNoPropertyViolation(JPF_ARGS)) {

      ThreadGroup workers = new ThreadGroup("workers");

      Thread t = new Thread( workers, new Runnable(){
        @Override
		public void run() {
          Thread t1 = new Thread( new Runnable(){
            @Override
			public void run() {
              Thread t11 = new Thread(new Runnable() {
                @Override
				public void run() {
                  System.out.println("t11 run");
                  Verify.incrementCounter(0);
                }
              }, "t11");
              t11.start();
              System.out.println("t1 run");
              Verify.incrementCounter(1);
            }
          }, "t1");
          t1.start();

          Thread t2 = new Thread( new Runnable(){
            @Override
			public void run() {
              System.out.println("t2 run");
              Verify.incrementCounter(2);
            }
          }, "t2");
          t2.start();
          System.out.println("t run");
          Verify.incrementCounter(3);
        }
      }, "t");
      t.start();

      try {
        Thread[] actives = new Thread[10]; // the length is just a guess here
        int nActives = workers.enumerate(actives, true);
        System.out.println("main joining " + nActives + " active threads");

        while (nActives > 0){
          assert nActives < actives.length; // it has to be strictly less to know we've got all
          for (int i=0; i<nActives; i++){
            System.out.println("main joining: " + actives[i].getName());
            actives[i].join();
            System.out.println("main joined: " + actives[i].getName());
          }
          nActives = workers.enumerate(actives, true);
          System.out.println("..main now joining " + nActives + " active threads");
        }
      } catch (Throwable x){
        fail("unexpected exception: " + x);
      }

      System.out.println("main done");
      Verify.printPathOutput("end");
    }

    if (!isJPFRun()){
      // not an ideal test since we don't know if the threads are still alive
      assert Verify.getCounter(0) > 0;
      assert Verify.getCounter(1) > 0;
      assert Verify.getCounter(2) > 0;
      assert Verify.getCounter(3) > 0;
    }
  }


  @Test
  public void testInterruptThreadWaitingToJoin() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation(JPF_ARGS)) {
      try {
        class ChildThread extends Thread {
          Thread toInterrupt;
          public void setToInterrupt(Thread toInterrupt) {
            this.toInterrupt = toInterrupt;
          }
          @Override
		public void run() {
            toInterrupt.interrupt();
          }
        }
        final ChildThread child = new ChildThread();

        class WaitingToJoinThread extends Thread {
          @Override
		public void run() {
            try {
              child.setToInterrupt(this);
              child.start();
              child.join();
            } catch (InterruptedException ix) {
              System.out.println("-- parent interrupted while child continues to run");
              Verify.incrementCounter(0);
            }
          }
        }

        WaitingToJoinThread threadWaitingToJoin = new WaitingToJoinThread();

        threadWaitingToJoin.start();
        try {
          threadWaitingToJoin.join();
        } catch (InterruptedException ix) {
          throw new RuntimeException("main thread was interrupted");
        }
        try {
          child.join();
        } catch (InterruptedException ix) {
          throw new RuntimeException("main thread was interrupted");
        }
      } catch (Exception ex) {
        throw new RuntimeException(ex.getMessage());
      }
    }

    if (!isJPFRun()){
      // at least one execution interrupts parent while child continues to run
      assert Verify.getCounter(0) > 0;
    }
  }

  @Test public void testTimeoutJoin () {
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
    }

    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 run");
          Thread.yield();
        }
      };

      Thread t = new Thread(r);
      t.start();
      //Thread.yield();

      try {
        System.out.println("main joining..");
        t.join(42);
        System.out.println("main joined, t state: " + t.getState());

        // we should get here for both terminated and non-terminated thread
        switch (t.getState()) {
          case TERMINATED:
            System.out.println("got terminated case");
            Verify.incrementCounter(0);
            break;
          case RUNNABLE:
            System.out.println("got timedout case");
            Verify.incrementCounter(1);
            break;
          default:
            fail("infeasible thread state: " + t.getState());
        }

      } catch (InterruptedException ix) {
        fail("main thread was interrupted");
      }
    }

    if (!isJPFRun()) {
      assert Verify.getCounter(0) > 0;
      assert Verify.getCounter(1) > 0;
    }
  }

  @Test public void testZeroTimeoutJoin () {
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
    }

    if (verifyNoPropertyViolation(JPF_ARGS)) {
      Runnable r = new Runnable() {
        @Override
		public void run() {
          System.out.println("thread-0 run");
          Thread.yield();
        }
      };

      Thread t = new Thread(r);
      t.start();
      //Thread.yield();

      try {
        System.out.println("main joining..");
        t.join(0);
        System.out.println("main joined, t state: " + t.getState());

        // we should get here for both terminated and non-terminated thread
        switch (t.getState()) {
          case TERMINATED:
            Verify.incrementCounter(0);
            break;
          case RUNNABLE:
            Verify.incrementCounter(1);
            break;
          default:
            fail("infeasible thread state: " + t.getState());
        }

      } catch (InterruptedException ix) {
        fail("main thread was interrupted");
      }
    }

    if (!isJPFRun()) {
      assert Verify.getCounter(0) > 0;
      assert Verify.getCounter(1) == 0;
    }
  }

  @Test public void testNegativeTimeoutJoin() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      try {
        Thread t = new Thread();
        t.join(-1);
        fail("should never get here");
      } catch (InterruptedException ix) {
        fail("unexpected InterruptedException");
      } catch (IllegalArgumentException ax){
        System.out.println("caught " + ax);
      }
    }
  }

  @Test public void testNestedLocksJoin() {
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Thread t1 = new Thread() {
        @Override
		public synchronized void run() {
          System.out.println("t1 notifying");
          notifyAll();

          try{
            System.out.println("t1 waiting");
            wait();
          } catch (InterruptedException ix){
            System.out.println("t1 unexpectedly interrupted");
          }

          System.out.println("t1 terminating");
        }
      };

      Thread t2 = new Thread() {
        @Override
		public synchronized void run() {
          System.out.println("t2 notifying");
          notifyAll();

          try{
            System.out.println("t2 waiting");
            wait();
          } catch (InterruptedException ix){
            System.out.println("t2 unexpectedly interrupted");
          }

          System.out.println("t2 terminating");
        }
      };

      synchronized (t2){
        try {
          t2.start();
          
          System.out.println("main waiting on t2");
          t2.wait();

          synchronized(t1){
            t1.start();

            System.out.println("main waiting on t1");
            t1.wait();

            System.out.println("main notifying t1");
            t1.notify();
          }

          System.out.println("main joining t1");
          t1.join();

          System.out.println("main notifying t2");
          t2.notify();

          System.out.println("main joining t2");
          t2.join();

        } catch (InterruptedException ix){
          System.out.println("main unexpectedly interrupted");
        }
      }

      System.out.println("main terminating");
      Verify.printPathOutput("main termination");
    }
  }
}
