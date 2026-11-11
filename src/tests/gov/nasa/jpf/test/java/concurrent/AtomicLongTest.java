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
import org.junit.Test;

import java.util.concurrent.atomic.AtomicLong;

/**
 * raw test for java.util.concurrent.atomic.AtomicLong
 */
public class AtomicLongTest extends TestJPF {

  @Test
  public void testPeerMethods() {
    if (verifyNoPropertyViolation()) {
      AtomicLong l = new AtomicLong(5);

      assertEquals(8, l.addAndGet(3));
      assertEquals(9, l.incrementAndGet());
      assertEquals(8, l.decrementAndGet());
      assertEquals(8, l.getAndIncrement());
      assertEquals(9, l.getAndDecrement());
      assertEquals(8, l.getAndAdd(2));
      assertEquals(10, l.get());

      assertTrue(l.compareAndSet(10, 20));
      assertFalse(l.compareAndSet(10, 30));
      assertEquals(20, l.get());
    }
  }

  @Test
  public void testSetAndGet() {
    if (verifyNoPropertyViolation()) {
      AtomicLong l = new AtomicLong();
      assertEquals(0, l.get());

      l.set(42);
      assertEquals(42, l.get());

      l.lazySet(43);
      assertEquals(43, l.get());

      assertEquals(43, l.getAndSet(44));
      assertTrue(l.weakCompareAndSet(44, 45));
      assertEquals(45, l.longValue());
      assertEquals(45, l.intValue());
      assertEquals("45", l.toString());
    }
  }

  static class Incrementer implements Runnable {
    final AtomicLong counter;

    Incrementer(AtomicLong counter) {
      this.counter = counter;
    }

    @Override
    public void run() {
      counter.incrementAndGet();
    }
  }

  @Test
  public void testConcurrentIncrement() throws InterruptedException {
    if (verifyNoPropertyViolation()) {
      AtomicLong counter = new AtomicLong();
      Thread t1 = new Thread(new Incrementer(counter));
      Thread t2 = new Thread(new Incrementer(counter));
      t1.start();
      t2.start();
      t1.join();
      t2.join();
      assertEquals(2, counter.get());
    }
  }
}
