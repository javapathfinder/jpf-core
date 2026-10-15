/*
 * Copyright (C) 2026, United States Government, as represented by the
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

package gov.nasa.jpf.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * Tests that queues grow from small initial capacities.
 */
public class ArrayQueueInitialCapacityTest extends TestJPF {

  /** Checks that an object queue grows from an initial capacity of zero. */
  @Test
  public void testObjectQueueGrowsFromCapacityZero() {
    ArrayObjectQueue<String> queue = new ArrayObjectQueue<String>(0);
    queue.add("first");
    queue.add("second");

    assertEquals(2, queue.size());
    assertEquals("first", queue.remove());
    assertEquals("second", queue.remove());
  }

  /** Checks that an object queue grows from an initial capacity of one. */
  @Test
  public void testObjectQueueGrowsFromCapacityOne() {
    ArrayObjectQueue<String> queue = new ArrayObjectQueue<String>(1);
    queue.add("first");
    queue.add("second");

    assertEquals(2, queue.size());
    assertEquals("first", queue.remove());
    assertEquals("second", queue.remove());
  }

  /** Checks that a byte queue grows from an initial capacity of zero. */
  @Test
  public void testByteQueueGrowsFromCapacityZero() {
    ArrayByteQueue queue = new ArrayByteQueue(0);
    queue.add((byte) 1);
    queue.add((byte) 2);

    assertEquals(Byte.valueOf((byte) 1), queue.remove());
    assertEquals(Byte.valueOf((byte) 2), queue.remove());
  }

  /** Checks that a byte queue grows from an initial capacity of one. */
  @Test
  public void testByteQueueGrowsFromCapacityOne() {
    ArrayByteQueue queue = new ArrayByteQueue(1);
    queue.add((byte) 1);
    queue.add((byte) 2);

    assertEquals(Byte.valueOf((byte) 1), queue.remove());
    assertEquals(Byte.valueOf((byte) 2), queue.remove());
  }

  /** Checks that byte queue growth preserves FIFO order after wraparound. */
  @Test
  public void testByteQueuePreservesWrappedOrderWhenGrowing() {
    ArrayByteQueue queue = new ArrayByteQueue(4);
    queue.add((byte) 1);
    queue.add((byte) 2);
    queue.add((byte) 3);
    queue.add((byte) 4);
    queue.remove();
    queue.remove();
    queue.add((byte) 5);
    queue.add((byte) 6);
    queue.add((byte) 7);

    assertEquals(Byte.valueOf((byte) 3), queue.remove());
    assertEquals(Byte.valueOf((byte) 4), queue.remove());
    assertEquals(Byte.valueOf((byte) 5), queue.remove());
    assertEquals(Byte.valueOf((byte) 6), queue.remove());
    assertEquals(Byte.valueOf((byte) 7), queue.remove());
  }

  /** Checks that object queue growth preserves FIFO order after wraparound. */
  @Test
  public void testObjectQueuePreservesWrappedOrderWhenGrowing() {
    ArrayObjectQueue<Integer> queue = new ArrayObjectQueue<Integer>(4);
    queue.add(1);
    queue.add(2);
    queue.add(3);
    queue.add(4);
    queue.remove();
    queue.remove();
    queue.add(5);
    queue.add(6);
    queue.add(7);

    assertEquals(Integer.valueOf(3), queue.remove());
    assertEquals(Integer.valueOf(4), queue.remove());
    assertEquals(Integer.valueOf(5), queue.remove());
    assertEquals(Integer.valueOf(6), queue.remove());
    assertEquals(Integer.valueOf(7), queue.remove());
  }
}
