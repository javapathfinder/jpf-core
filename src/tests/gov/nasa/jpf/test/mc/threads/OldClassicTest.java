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

public class OldClassicTest extends TestJPF {

  /**************************** tests **********************************/
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

  class FirstTask extends java.lang.Thread {

    Event event1;
    Event event2;
    int count = 0;

    public FirstTask(Event e1, Event e2) {
      this.event1 = e1;
      this.event2 = e2;
    }

    @Override
	public void run() {
      count = event1.count;

      while (true) {
        if (count == event1.count) {
          //assert (count == event1.count);
          event1.wait_for_event();
        }
        count = event1.count;
        event2.signal_event();
      }
    }
  }

  class SecondTask extends java.lang.Thread {

    Event event1;
    Event event2;
    int count = 0;

    public SecondTask(Event e1, Event e2) {
      this.event1 = e1;
      this.event2 = e2;
    }

    @Override
	public void run() {
      count = event2.count;

      while (true) {
        event1.signal_event();
        if (count == event2.count) {
          //assert (count == event2.count);
          event2.wait_for_event();
        }
        count = event2.count;
      }
    }
  }

  public void run() {
    Event new_event1 = new Event();
    Event new_event2 = new Event();

    FirstTask task1 = new FirstTask(new_event1, new_event2);
    SecondTask task2 = new SecondTask(new_event1, new_event2);

    task1.start();
    task2.start();

  }

  /**
   * Tests running the Crossing example with no heuristics, i.e., with the
   * default DFS.
   */
  @Test
  public void testDFSearch() {
    if (verifyDeadlock()) {
      run();
    }
  }

  /**
   * Tests running the Crossing example with BFS heuristic.
   */
  @Test
  public void testBFSHeuristic() {
    if (verifyDeadlock("+search.class=gov.nasa.jpf.search.heuristic.BFSHeuristic")) {
      run();
    }
  }
}
