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
package gov.nasa.jpf.test.java.nio;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.StandardCharsets;
import java.util.Random;
import java.util.Scanner;

public class BufferTest extends TestJPF {

  /**
   * This test case checks to see if the missing
   * class java.nio.Buffer.<init>(IIII)V issue
   * is resolved and fails otherwise
   */
  @Test
  public void testByteBufferConstructor() {
    if (verifyNoPropertyViolation()) {
      byte[] bytes1 = "Buffer".getBytes(StandardCharsets.UTF_8);
      byte[] bytes2 = "testBuffer".getBytes(StandardCharsets.UTF_8);

      ByteBuffer buffer1 = ByteBuffer.wrap(bytes1);
      ByteBuffer buffer2 = ByteBuffer.wrap(bytes2);
      buffer2.position(4);

      assertTrue(buffer1.equals(buffer2));
    }
  }

  @Test
  public void testCharBufferConstructor() {
    if(verifyNoPropertyViolation()) {
      Random random = new Random();
      byte[] bytes = new byte[8];
      random.nextBytes(bytes);
      new Scanner(new String(bytes));
    }
  }

  /** Tests that reducing the limit also reduces a position beyond it. */
  @Test
  public void testLowerLimitClampsPosition() {
    if (verifyNoPropertyViolation()) {
      for (Buffer buffer : new Buffer[]{ByteBuffer.allocate(8), CharBuffer.allocate(8)}) {
        buffer.position(6);
        assertSame(buffer, buffer.limit(2));
        assertEquals(2, buffer.limit());
        assertEquals(2, buffer.position());
        assertEquals(0, buffer.remaining());
        assertFalse(buffer.hasRemaining());

        buffer.limit(0);
        assertEquals(0, buffer.position());
        assertEquals(0, buffer.remaining());
        assertEquals(8, buffer.capacity());

        buffer.limit(8);
        assertEquals(0, buffer.position());
        assertEquals(8, buffer.remaining());
      }
    }
  }

  /** Tests that changing the limit keeps a position that is still valid. */
  @Test
  public void testLimitPreservesPositionWithinBounds() {
    if (verifyNoPropertyViolation()) {
      for (Buffer buffer : new Buffer[]{ByteBuffer.allocate(8), CharBuffer.allocate(8)}) {
        buffer.position(2);
        buffer.limit(6);
        assertEquals(2, buffer.position());
        assertEquals(4, buffer.remaining());
        assertTrue(buffer.hasRemaining());

        buffer.limit(2);
        assertEquals(2, buffer.position());
        assertEquals(0, buffer.remaining());

        buffer.limit(8);
        assertEquals(2, buffer.position());
        assertEquals(6, buffer.remaining());
      }
    }
  }

  /** Tests that an invalid limit leaves the buffer state unchanged. */
  @Test
  public void testInvalidLimitPreservesState() {
    if (verifyNoPropertyViolation()) {
      for (Buffer buffer : new Buffer[]{ByteBuffer.allocate(8), CharBuffer.allocate(8)}) {
        buffer.position(4);
        buffer.limit(6);
        for (int invalidLimit : new int[]{-1, 9}) {
          try {
            buffer.limit(invalidLimit);
            fail("invalid limit should throw IllegalArgumentException");
          } catch (IllegalArgumentException expected) {
            assertEquals(6, buffer.limit());
            assertEquals(4, buffer.position());
            assertEquals(2, buffer.remaining());
          }
        }
      }
    }
  }

  /** Tests that a buffer exhausted by lowering its limit can still be sliced. */
  @Test
  public void testSliceAfterLoweringLimitBelowPosition() {
    if (verifyNoPropertyViolation()) {
      ByteBuffer buffer = ByteBuffer.allocate(8);
      buffer.position(6);
      ((Buffer) buffer).limit(2);

      ByteBuffer slice = buffer.slice();
      assertEquals(0, slice.capacity());
      assertEquals(0, slice.position());
      assertEquals(0, slice.limit());
    }
  }

}
