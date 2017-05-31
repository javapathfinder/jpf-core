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
package gov.nasa.jpf.test.java.concurrent;

import gov.nasa.jpf.util.test.TestJPF;

import java.util.concurrent.Semaphore;

import org.junit.Test;

/**
 * simple test for Java 1.5 java.util.concurrent support
 */
public class SemaphoreTest extends TestJPF {

  //--- test methods

  static final int MAX = 1;
  static final Semaphore avail = new Semaphore(MAX, true);
  static Resource[] items = new Resource[MAX];
  static boolean[] isUsed = new boolean[MAX];
  static final Object lock = new Object();


  static {
    for (int i = 0; i < items.length; i++) {
      items[i] = new Resource(i);
    }
  }

  static class Resource {

    String id;
    String user;

    Resource(int id) {
      this.id = "Resource-" + id;
    }

    public void use(String newUser) {
      assert user == null : "resource " + id + " in use by " + user +
              ", but attempted to be acquired by: " + newUser;
      user = newUser;
    }

    public void release() {
      user = null;
    }

    @Override
	public String toString() {
      return id;
    }
  }

  public static Resource getItem() throws InterruptedException {
    avail.acquire();

    synchronized (lock) {
      for (int i = 0; i < MAX; i++) {
        if (!isUsed[i]) {
          isUsed[i] = true;
          return items[i];
        }
      }
    }
    assert false : "couldn't find unused resource";
    return null;
  }

  public static void putItem(Resource o) {
    synchronized (lock) {
      for (int i = 0; i < MAX; i++) {
        if (items[i] == o) {
          if (isUsed[i]) {
            isUsed[i] = false;
            avail.release();
          }
          break;
        }
      }
    }
  }

  static class Client implements Runnable {

    @Override
	public void run() {
      String id = Thread.currentThread().getName();

      try {
        System.out.println(id + " acquiring resource..");
        Resource r = SemaphoreTest.getItem();
        System.out.println(id + " got resource: " + r);

        r.use(id);
        //.. more stuff here
        r.release();

        System.out.println(id + " releasing resource: " + r);
        SemaphoreTest.putItem(r);
        System.out.println(id + " released");

      } catch (InterruptedException ix) {
        System.out.println("!! INTERRUPTED");
      }
    }
  }

  //--------------- the test cases
  @Test
  public void testResourceAcquisition() {
    if (verifyNoPropertyViolation()) {
      for (int i = 0; i <= MAX; i++) {
        Thread t = new Thread(new Client());
        t.start();
      }
    }
  }
}
