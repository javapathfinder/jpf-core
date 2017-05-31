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
/**
 * this is a raw test class for detection of thread-shared fields, i.e.
 * it executes the garbage collection based reachability analysis
 */

package gov.nasa.jpf.test.mc.threads;

import gov.nasa.jpf.util.TypeRef;
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

class SharedObject {
  int instanceField;
  int whatEver;
}


public class RaceTest extends TestJPF {

  static final TypeRef PROPERTY = new TypeRef("gov.nasa.jpf.listener.PreciseRaceDetector");
  static final String LISTENER = "+listener=gov.nasa.jpf.listener.PreciseRaceDetector";

  static int staticField;

  @Test
  public void testStaticRace () {
    if (verifyUnhandledException("java.lang.RuntimeException")) {

      Runnable r1 = new Runnable() {

        @Override
		public void run() {
          staticField = 1;
          if (staticField != 1) {
            throw new RuntimeException("r1 detected race!");
          }
        }
      };

      Runnable r2 = new Runnable() {

        @Override
		public void run() {
          staticField = 0;
          if (staticField != 0) {
            throw new RuntimeException("r2 detected race!");
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test 
  public void testStaticRaceNoThrow () {
    if (verifyPropertyViolation(PROPERTY, LISTENER)) {
      Runnable r1 = new Runnable() {

        @Override
		public void run() {
          staticField = 1;
        }
      };

      Runnable r2 = new Runnable() {

        @Override
		public void run() {
          staticField = 0;
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }
  
  
  // this represents the case where the class loading thread is non-deterministic
  
  static class Container {
    static int data; // that's what we race for
  }
  
  static class StaticRacer extends Thread {
    @Override
	public void run(){
      Container.data++;
    }
  }
  
  @Test
  public void testSymmetricStaticRace(){
    if (verifyUnhandledExceptionDetails("java.lang.RuntimeException", "got race",
                                        "+vm.scheduler.sharedness.class=.vm.GlobalSharednessPolicy")) {
      StaticRacer t1 = new StaticRacer();
      StaticRacer t2 = new StaticRacer();
      t1.start();
      t2.start();
      try {
        t1.join();
        t2.join();
      } catch (InterruptedException ix){
        fail("got interrupted");
      }
      
      if (Container.data != 2){
        System.out.print("Container.data = ");
        System.out.print( Container.data);
        System.out.println(" => throwing RuntimeException");
        throw new RuntimeException("got race");
      }
    }
  }
  
  @Test
  public void testInstanceRace () {
    if (verifyUnhandledException("java.lang.RuntimeException")) {
      final SharedObject o = new SharedObject();

      Runnable r1 = new Runnable() {

        SharedObject d = o;

        @Override
		public void run() {
          d.instanceField = 1;
          if (d.instanceField != 1) {
            throw new RuntimeException("r1 detected race!");
          }
        }
      };

      Runnable r2 = new Runnable() {

        SharedObject d = o;

        @Override
		public void run() {
          d.instanceField = 0;
          if (d.instanceField != 0) {
            throw new RuntimeException("r2 detected race!");
          }
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testInstanceRaceNoThrow () {
    if (verifyPropertyViolation(PROPERTY, LISTENER)) {
      final SharedObject o = new SharedObject();

      Runnable r1 = new Runnable() {

        SharedObject d = o;

        @Override
		public void run() {
          d.instanceField = 1;
        }
      };

      Runnable r2 = new Runnable() {

        SharedObject d = o;

        @Override
		public void run() {
          d.instanceField = 0;
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  @Test
  public void testInstanceRaceListenerExclude () {
    if (verifyNoPropertyViolation(LISTENER, "+race.exclude="+ RaceTest.class.getName() + "*")){
      testInstanceRaceNoThrow();
    }
  }

  @Test
  public void testInstanceRaceListenerInclude () {
    if (verifyPropertyViolation(PROPERTY, LISTENER,
                                 "+race.include=" + RaceTest.class.getName() + "*")){
      testInstanceRaceNoThrow();
    }
  }

  @Test
  public void testStaticRaceListenerIncludeOther () {
    if (verifyNoPropertyViolation(LISTENER, "+race.include=sho.bi.Doo*")){
      testStaticRaceNoThrow();
    }
  }

  @Test
  public void testArrayRaceNoThrow () {
    if (verifyPropertyViolation(PROPERTY, LISTENER, "+cg.threads.break_arrays")){
      final int[] shared = new int[1];

      Runnable r1 = new Runnable(){
        int[] a = shared;
        @Override
		public void run() {
          a[0] = 0;
        }
      };

      Runnable r2 = new Runnable(){
        int[] a = shared;
        @Override
		public void run() {
          a[0] = 1;
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }

  /*
   * mostly the same as above except of that the race candidates are the same insn instance, i.e. use the same
   * cached insn fields values
   */
  static class AT extends Thread {
    int[] a;
    int idx;
    
    AT (int[] a, int idx) {
      this.a = a;
      this.idx = idx;
    }
    
    @Override
	public void run (){
      //assertTrue( a[idx] == 0);
      a[idx] = 1;
    }
  }
  
  @Test
  public void testNoArrayRaceSameInsn (){
    if (verifyNoPropertyViolation(LISTENER, "+cg.threads.break_arrays")){
      int[] a = new int[2];
      AT t1 = new AT(a, 0);
      t1.start();
      AT t2 = new AT(a, 1);
      t2.start();
    }
  }

  // the dual
  @Test
  public void testArrayRaceSameInsn (){
    if (verifyPropertyViolation(PROPERTY, LISTENER, "+cg.threads.break_arrays")){
      int[] a = new int[2];
      AT t1 = new AT(a, 1);
      t1.start();
      AT t2 = new AT(a, 1);
      t2.start();
    }
  }
  
  
  @Test
  public void testNoArrayRaceElements () {
    if (verifyNoPropertyViolation(LISTENER, "+cg.threads.break_arrays")){
      final int[] shared = new int[2];

      Runnable r1 = new Runnable(){
        int[] a = shared;
        @Override
		public void run() {
          a[0] = 0;
        }
      };

      Runnable r2 = new Runnable(){
        int[] a = shared;
        @Override
		public void run() {
          a[1] = 1;
        }
      };

      Thread t1 = new Thread(r1);
      Thread t2 = new Thread(r2);

      t1.start();
      t2.start();
    }
  }


  //--- these are tests to check false positives

  static class SameInsnRunnable implements Runnable {
    SharedObject o = new SharedObject();

    @Override
	public void run () {
      o.instanceField = 42;  // same insn, different 'o', no race
    }
  }

  @Test
  public void testSameInsnOtherObject () {
    if (verifyNoPropertyViolation(LISTENER)) {
      SameInsnRunnable r1 = new SameInsnRunnable();
      SameInsnRunnable r2 = new SameInsnRunnable();

      Thread t = new Thread(r1);
      t.start();

      r2.run();
    }
  }

  @Test
  public void testSameObjectOtherField() {
    if (verifyNoPropertyViolation(LISTENER)) {
      final SharedObject o = new SharedObject();

      Runnable r = new Runnable() {

        @Override
		public void run() {
          o.instanceField = 42;
        }
      };

      Thread t = new Thread(r);

      o.whatEver = -42;  // different field, no race
    }
  }
  
  
  //--- try variations of locks
  
  class AnotherSharedObject {
    Object lock1 = new Object();
    Object lock2 = new Object();
    
    int x = 0;
  }
  
  @Test
  public void testNoSync() {
    if (verifyUnhandledException("java.lang.RuntimeException")) {

      final AnotherSharedObject o = new AnotherSharedObject();
      Runnable r = new Runnable() {

        @Override
		public void run() {
          o.x++;
          if (o.x == 0) {
            throw new RuntimeException("testNoSync race");
          }
        }
      };
      Thread t = new Thread(r);
      t.start();

      o.x--;
    }
  }
  
  
  @Test
  public void testTSync() {
    if (verifyUnhandledException("java.lang.RuntimeException")) {

      final AnotherSharedObject o = new AnotherSharedObject();
      Runnable r = new Runnable() {

        @Override
		public void run() {
          synchronized (o.lock1) {
            o.x++;
            if (o.x == 0) {
              throw new RuntimeException("testT1Sync race");
            }
          }
        }
      };
      Thread t = new Thread(r);
      t.start();

      // no sync
      o.x--;
    }
  }
  
  @Test
  public void testMainSync () {
    if (verifyUnhandledException("java.lang.RuntimeException")) {

      final AnotherSharedObject o = new AnotherSharedObject();
      Runnable r = new Runnable() {

        @Override
		public void run() {
          // not synchronized
          o.x++;
          if (o.x == 0) {
            throw new RuntimeException("testMainSync race");
          }
        }
      };
      Thread t = new Thread(r);
      t.start();

      synchronized (o.lock1) {
        o.x--;
      }
    }
  }
  
  @Test
  public void testBothSync () {
    if (verifyNoPropertyViolation()) {
      final AnotherSharedObject o = new AnotherSharedObject();
      Runnable r = new Runnable() {

        @Override
		public void run() {
          synchronized (o.lock1) {
            o.x++;
            if (o.x == 0) {
              throw new RuntimeException("testBothSync race??");
            }
          }
        }
      };
      Thread t = new Thread(r);
      t.start();

      synchronized (o.lock1) {
        o.x = 0;
      }
    }
  }

  @Test
  public void testWrongSync () {
    if (verifyUnhandledException("java.lang.RuntimeException")) {

      final AnotherSharedObject o = new AnotherSharedObject();

      Runnable r = new Runnable() {

        @Override
		public void run() {
          synchronized (o.lock1) {
            o.x++;
            if (o.x == 0) {
              throw new RuntimeException("testWrongSync race");
            }
          }
        }
      };
      Thread t = new Thread(r);
      t.start();

      synchronized (o.lock2) {
        o.x--;
      }
    }
  }
}


