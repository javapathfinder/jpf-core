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
 * JPF test driver for the IdleFilter listener
 */
public class IdleLoopTest extends TestJPF {

  static final String LISTENER = "+listener=.listener.IdleFilter";

  @Test public void testBreak () {
    if (verifyNoPropertyViolation(LISTENER, "+idle.action=break", 
                                  "+log.warning=gov.nasa.jpf.listener.IdleFilter",
                                  "+vm.max_transition_length=MAX")) {
      int y = 4;
      int x = 0;

      while (x != y) { // JPF should state match on the backjump
        x = x + 1;
        if (x > 3) {
          x = 0;
        }
      }

      assert false : "we should never get here";
    }
  }

  @Test public void testPrune () {
    if (verifyNoPropertyViolation(LISTENER, "+idle.action=prune",
                                  "+log.warning=gov.nasa.jpf.listener.IdleFilter",
                                  "+vm.max_transition_length=MAX")) {
      int y = 4;
      int x = 0;

      int loopCount = 0;

      while (x != y) { // JPF should prune on the backjump despite of changed 'loopCount'
        loopCount++;
        x = x + 1;
        if (x > 3) {
          x = 0;
        }
      }

      assert false : "we should never get here";
    }
  }

  @Test public void testJump () {
    if (verifyNoPropertyViolation(LISTENER, "+idle.action=jump",
                                  "+idle.max_backjumps=100",
                                  "+log.warning=gov.nasa.jpf.listener.IdleFilter",
                                  "+vm.max_transition_length=MAX")) {

      for (int i=0; i<1000; i++){
        assert i < 500 : "JPF failed to jump past idle loop";
      }

      System.out.println("Ok, jumped past loop");
    }
  }

}
