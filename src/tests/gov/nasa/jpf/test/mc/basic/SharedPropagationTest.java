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
import org.junit.Test;

/**
 * various shared object propagations that could lead to missed paths
 */
public class SharedPropagationTest extends TestJPF {

  static class Gotcha extends RuntimeException {
    // nothing in here
  }
  
  //--- simple local ref 
  
  static class T1 extends Thread {

    static class X {

      boolean pass;
    }
    X myX; // initially not set

    public static void main(String[] args) {
      T1 t = new T1();
      t.start();

      X x = new X();
      t.myX = x;        // (0) x not shared until this GOT executed

      //Thread.yield();  // this would expose the error
      x.pass = true;     // (1) need to break BEFORE assignment or no error
    }

    @Override
	public void run() {
      if (myX != null) {
        if (!myX.pass) {  // (2) won't fail unless main is between (0) and (1)
          throw new Gotcha();
        }
      }
    }
  }
  
  @Test
  public void testLocalRef(){
    if (verifyUnhandledException( Gotcha.class.getName(), "+vm.scheduler.sharedness.class=.vm.GlobalSharednessPolicy")){
      T1.main(new String[0]);
    }
  }
  
  
  //--- one reference level down
  
  static class T2 extends Thread {

    static class X {
      boolean pass;
    }

    static class Y {
      X x;
    }
    
    Y y;

    public static void main(String[] args) {
      T2 t = new T2();
      Y y = new Y();
      X x = new X();

      y.x = x;
      // neither x nor y  shared at this point

      t.start();
      t.y = y; // y becomes shared, and with it x

      x.pass = true;
    }

    @Override
	public void run() {
      if (y != null) {
        if (!y.x.pass) {
          throw new Gotcha();
        }
      }
    }
  }
  
  @Test
  public void testLevel1Ref(){
    if (verifyUnhandledException(Gotcha.class.getName())){
      T2.main(new String[0]);
    }
  }

  //--- propagation via static field
  
  static class T3 extends Thread {

    static class X {
      boolean pass;
    }

    static class Y {
      X x;
    }
    static Y globalY; // initially not set

    
    public static void main(String[] args) {
      T3 t = new T3();
      t.start();

      X x = new X();
      Y y = new Y();
      y.x = x;

      globalY = y;           // (0) x not shared until this GOT executed

      //Thread.yield();  // this would expose the error
      x.pass = true;     // (1) need to break BEFORE assignment or no error
    }

    @Override
	public void run() {
      if (globalY != null) {
        if (!globalY.x.pass) {  // (2) won't fail unless main is between (0) and (1)
          throw new Gotcha();
        }
      }
    }
  }
  
  @Test
  public void testStaticFieldPropagation(){
    if (verifyUnhandledException(Gotcha.class.getName(), "+vm.scheduler.sharedness.class=.vm.GlobalSharednessPolicy")){
      T3.main(new String[0]);
    }
  }
  
  
  //--- the infamous Hyber example
  
  static class Hyber {
    private static Timeout thread = new Timeout();

    public static void main(String[] args) {
      thread.start();
      Timeout.Entry timer = thread.setTimeout(); // (0)
      //Thread.yield();    // this forces the error
      timer.hyber = true;  // (1) we need to break here to catch the error
    }
  }

  static class Timeout extends Thread {

    static class Entry {
      boolean hyber = false;
      Entry next = null;
      Entry prev = null;
    }
    Entry e = new Entry();

    Timeout() {
      e.next = e.prev = e;
    }

    public Entry setTimeout() {
      Entry entry = new Entry();
      synchronized (e) {
        entry.next = e;
        entry.prev = e.prev;
        entry.prev.next = entry;
        entry.next.prev = entry;
      }

      return entry;
    }

    @Override
	public void run() {
      synchronized (e) {
        for (Entry entry = e.next; entry != e; entry = entry.next) {
          if (!entry.hyber) { // (2) only fails if main thread between (0) and (1)
            throw new Gotcha();
          }
        }
      }
    }
  }
  
  @Test
  public void testHyber() {
    if (verifyUnhandledException(Gotcha.class.getName(), "+vm.scheduler.sharedness.class=.vm.GlobalSharednessPolicy")){
      Hyber.main(new String[0]);
    }    
  }

}
