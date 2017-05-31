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
 * This example shows a deadlock that occurs as a result of a missed signal,
 * i.e. a wait() that happens after the corresponding notify().
 * 
 * The defect is caused by a violated monitor encapsulation, i.e. directly
 * accessing monitor internal data ('Event.count') from concurrent clients
 * ('FirstTask', 'SecondTask'), without synchronization with the
 * corresponding monitor operations ('wait_for-Event()' and 'signalEvent()').
 * 
 * The resulting race is typical for unsafe optimizations that try to 
 * avoid expensive blocking calls by means of local caches
 * 
 * This example was inspired by a defect found in the "Remote Agent" 
 * spacecraft controller that flew on board of "Deep Space 1", as described
 * in: 
 * 
 *   Model Checking Programs
 *   W. Visser, K. Havelund, G. Brat, S. Park and F. Lerda
 *   Automated Software Engineering Journal
 *   Volume 10, Number 2, April 2003
 *  
 * @author wvisser
 */

//------- the test driver
public class oldclassic {
  public static void main (String[] args) {
    Event      new_event1 = new Event();
    Event      new_event2 = new Event();

    FirstTask  task1 = new FirstTask(new_event1, new_event2);
    SecondTask task2 = new SecondTask(new_event1, new_event2);

    task1.start();
    task2.start();
  }
}

//------- shared objects implemented as monitors
class Event {
  int count = 0;

  public synchronized void signal_event () {
    
    // NOTE: this abstraction is not strictly required - even if the state space would
    // be unbound, JPF could still find the error at a reasonable search depth,
    // unless it's left-most branch in the search tree is unbound. If it is,
    // there are two ways to work around: (1) use a different search strategy
    // (e.g. HeuristicSearch with BFSHeuristic), or (2) set a random choice
    // enumeration order ("+cg.randomize_choices=true"). In this example, (2)
    // works just fine
    count = (count + 1) % 3;
    //count++;  // requires "+cg.randomize_choices=true" for DFSearch policy
    
    notifyAll();
  }

  public synchronized void wait_for_event () {
    try {
      wait();
    } catch (InterruptedException e) {
    }
  }
}

//------- the two concurrent threads using the monitors
class FirstTask extends Thread {
  Event event1;
  Event event2;
  int   count = 0;  // bad optimization - local cache of event1 internals

  public FirstTask (Event e1, Event e2) {
    this.event1 = e1;
    this.event2 = e2;
  }

  @Override
  public void run () {
    count = event1.count;          // <race> violates event1 monitor encapsulation

    while (true) {
      System.out.println("1");

      if (count == event1.count) { // <race> ditto
        event1.wait_for_event();
      }

      count = event1.count;        // <race> ditto
      event2.signal_event();       // updates event2.count
    }
  }
}

class SecondTask extends Thread {
  Event event1;
  Event event2;
  int   count = 0;  // bad optimization - local cache of event2 internals

  public SecondTask (Event e1, Event e2) {
    this.event1 = e1;
    this.event2 = e2;
  }

  @Override
  public void run () {
    count = event2.count;          // <race> violates event2 monitor encapsulation

    while (true) {
      System.out.println("  2");
      event1.signal_event();       // updates event1.count

      if (count == event2.count) { // <race> ditto
        event2.wait_for_event();
      }

      count = event2.count;        // <race> ditto
    }
  }
}
