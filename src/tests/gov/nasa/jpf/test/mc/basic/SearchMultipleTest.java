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

import org.junit.Test;

/**
 * regression test for search.multiple_errors test
 */
public class SearchMultipleTest extends TestJPF {

  @Test
  public void testSimple() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyAssertionError("+search.multiple_errors")){
      boolean b = Verify.getBoolean();
      System.out.println("## b = " + b);

      Verify.incrementCounter(0);
      
      assert false : "blow up here";

      fail("should never get here");
    }

    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
    }
  }

  @Test
  public void testSimpleBFS() {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyAssertionError("+search.multiple_errors", "+search.class=.search.heuristic.BFSHeuristic")){
      boolean b = Verify.getBoolean();
      System.out.println("## b = " + b);

      Verify.incrementCounter(0);

      assert false : "blow up here";
    }

    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
    }
  }

  @Test
  public void testDeadlock(){
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyDeadlock("+search.multiple_errors", "+cg.boolean.false_first")){
      Object lock = new Object();
      boolean b = Verify.getBoolean();
      boolean c = Verify.getBoolean();
      System.out.println("b=" + b + ", c=" + c);

      if (!b){
        synchronized(lock){
          try {
            System.out.println("now deadlocking");
            lock.wait(); // this should always deadlock
          } catch (InterruptedException ix){
            System.out.println("got interrupted");
          }
        }
      }

      System.out.println("should get here for b=true");
      Verify.incrementCounter(0);
    }

    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
    }
  }
}
