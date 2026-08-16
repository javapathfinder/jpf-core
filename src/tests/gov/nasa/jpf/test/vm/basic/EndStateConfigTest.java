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

package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * tests for the vm.end_state.require_all_terminated property, which
 * requires all threads (incl. daemons) to be terminated before a state
 * is considered an end state
 */
public class EndStateConfigTest extends TestJPF {

  // the daemon signals that it is about to run, so that the main thread
  // cannot return while the daemon is still in NEW state
  static volatile boolean daemonStarted = false;

  static final Object lock = new Object();

  public static class EndStateCounter extends ListenerAdapter {
    static int endStateCount;

    public EndStateCounter() {
      endStateCount = 0;
    }

    @Override
    public void stateAdvanced(Search search) {
      if (search.isEndState()) {
        endStateCount++;
      }
    }
  }

  // runs a daemon that never terminates, and makes sure it is already
  // running when this method returns
  private void runNonTerminatingDaemon() {
    Thread t = new Thread() {
      @Override
      public void run() {
        daemonStarted = true;
        while (true) { // keep the daemon alive forever
        }
      }
    };
    t.setDaemon(true);
    t.start();

    while (!daemonStarted) {
      Thread.yield();
    }
  }

  // runs a daemon that blocks forever (nobody will ever notify it), and
  // makes sure it is already blocked when this method returns
  private void runBlockedDaemon() {
    Thread t = new Thread() {
      @Override
      public void run() {
        daemonStarted = true;
        synchronized (lock) {
          while (true) {
            try {
              lock.wait(); // block forever
            } catch (InterruptedException ix) {
              // doesn't matter
            }
          }
        }
      }
    };
    t.setDaemon(true);
    t.start();

    while (!daemonStarted) {
      Thread.yield();
    }
  }

  // the daemon is still alive when the main thread returns, so with
  // vm.end_state.require_all_terminated no end state should ever be reached
  @Test
  public void testDaemonPreventsEndState() {
    if (verifyNoPropertyViolation("+vm.end_state.require_all_terminated=true",
                                  "+listener=gov.nasa.jpf.test.vm.basic.EndStateConfigTest$EndStateCounter",
                                  "+search.depth_limit=100")) {
      runNonTerminatingDaemon();
    }
    if (!isJPFRun()) {
      assertTrue("unexpected end state while daemon is still alive",
                 EndStateCounter.endStateCount == 0);
    }
  }

  // without the property, a daemon does not prevent an end state once the
  // main thread terminated (default behavior must be preserved)
  @Test
  public void testDaemonDoesNotPreventEndStateByDefault() {
    if (verifyNoPropertyViolation("+listener=gov.nasa.jpf.test.vm.basic.EndStateConfigTest$EndStateCounter")) {
      runBlockedDaemon();
    }
    if (!isJPFRun()) {
      assertTrue("expected an end state although only the daemon is still alive",
                 EndStateCounter.endStateCount > 0);
    }
  }
}