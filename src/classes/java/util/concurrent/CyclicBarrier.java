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
package java.util.concurrent;

/**
 * a simplistic CyclicBarrier implementation, required because the real one
 * relies heavily on Sun infrastructure (including native methods)
 */
public class CyclicBarrier {

  private Runnable action;
  private int parties;
  private int count;
  private boolean isBroken;

  // make sure nobody from the outside can interfere with our locking
  private final Object lock = new Object();

  
  public CyclicBarrier (int parties) {
    this(parties, null);
  }

  public CyclicBarrier (int parties, Runnable action) {
    this.parties = parties;
    count = parties;
    
    this.action = action;
  }
  
  public int await () throws InterruptedException, BrokenBarrierException {
    synchronized (lock) {
      int arrival = parties - count;

      if (--count == 0) {
        if (action != null) {
          action.run();
        }
        count = parties; // reset barrier
        lock.notifyAll();
      } else {
        try {
          lock.wait();
          if (isBroken) {
            throw new BrokenBarrierException();
          }
        } catch (InterruptedException ix) {
          if (count > 0) {
            isBroken = true;
            lock.notifyAll();
          }

          throw ix;
        }
      }

      return arrival;
    }
  }

  public int getParties () {
    return parties;
  }

  public void reset () {
    synchronized (lock) {
      if ((count != parties) && (count != 0)) {
        // there are waiters
        isBroken = true;
        lock.notifyAll();
      } else {
        count = parties;
        isBroken = false;
      }
    }
  }

  public boolean isBroken () {
    // true if one of the parties got out of an await by being
    // interrupted
    return isBroken;
  }

  public int getNumberWaiting () {
    synchronized (lock) {
      return (parties - count);
    }
  }
}
