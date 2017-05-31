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
 * Tests the functionality of gov.nasa.jpf.listener.LockedStackDepth
 *
 * It would be very difficult to put asserts in the test.  Hence, asserts are
 * added to LockedStackDepth.
 *
 * Run all of the JPF tests with 
 * listener+=,gov.nasa.jpf.listener.LockedStackDepth to take advantage of the 
 * various tests.
 */
public class LockedStackDepthTest extends TestJPF {

  private static final String LISTENER = "+listener+=,.listener.LockedStackDepth";

  @Test
  public void recursiveLock() {
    if (verifyNoPropertyViolation(LISTENER)) {
      synchronized (this) {
        synchronized (this) {
        }
      }
    }
  }

  @Test
  public void waitRetainsDepth() throws InterruptedException {
    if (verifyNoPropertyViolation(LISTENER)) {
      synchronized (this) {
        synchronized (this) {
          wait(1);
        }
      }
    }
  }

  @Test
  public void breadthFirstSearch() throws InterruptedException {
    if (verifyNoPropertyViolation(LISTENER, "+search.class=gov.nasa.jpf.search.heuristic.BFSHeuristic")) {
      synchronized (this) {
        synchronized (this) {
          wait(1);
        }
      }
    }
  }

  @Test
  public void randomHeuristicSearch() throws InterruptedException {
    if (verifyNoPropertyViolation(LISTENER, "+search.class=gov.nasa.jpf.search.heuristic.RandomHeuristic")) {
      synchronized (this) {
        synchronized (this) {
          wait(1);
        }
      }
    }
  }

  @Test
  public void hitSameStateThroughDifferentSearchPaths() {
    if (verifyNoPropertyViolation(LISTENER)) {
      Verify.getBoolean();

      synchronized (this) {
      }

      Verify.getBoolean();
    }
  }
}
