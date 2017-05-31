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
package gov.nasa.jpf.util;

import gov.nasa.jpf.util.test.TestJPF;
import java.io.IOException;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AvailableBufferedInputStreamTest extends TestJPF {

  private static final int PIPE_SIZE = 1024 * 1024;

  private PipedOutputStream m_output;
  private AvailableBufferedInputStream m_input;

  @Before
  public void before() throws IOException {
    m_output = new PipedOutputStream();
    m_input = new AvailableBufferedInputStream(new PipedInputStream(m_output, PIPE_SIZE));
  }

  @After
  public void after() throws IOException {
    m_output.flush();
    m_output.close();
    m_output = null;

    assertEquals(0, m_input.available());
    assertEquals(-1, m_input.peek());
    assertEquals(-1, m_input.read());

    m_input.close();
    m_input = null;
  }

  @Test(expected = NullPointerException.class)
  public void passNullPointerToConstructor() throws IOException {
    new AvailableBufferedInputStream(null).close();
  }

  @Test
  public void availableStuck() throws IOException {
    int i;

    for (i = 10; --i >= 0;) {
      m_output.write(i);
      m_output.flush();
      assertEquals(1, m_input.available());
    }

    for (i = 10; --i >= 0;) {
      assertEquals(i, m_input.peek());
      assertEquals(i, m_input.read());
    }
  }

  @Test
  public void unreadExtra() throws IOException {
    int i;

    m_output.write(20);
    m_output.write(21);
    m_output.flush();

    assertEquals(20, m_input.peek());
    assertEquals(20, m_input.read());  // Load the buffer

    for (i = 0; i < 10; i++) {
      m_input.unread(i);
    }

    for (i = 10; --i >= 0;) {
      assertEquals(i, m_input.peek());
      assertEquals(i, m_input.read());
      assertEquals(i + 1, m_input.available());
    }

    assertEquals(21, m_input.peek());
    assertEquals(21, m_input.read());
  }

  @Test
  public void unreadMinus1() throws IOException {
    m_input.unread(-1);
    assertEquals(0x00FF, m_input.peek());
    assertEquals(0x00FF, m_input.read());
  }

  @Test
  public void readBufferSplit() throws IOException {
    byte buffer[];

    buffer = new byte[2];

    m_output.write(30);
    m_output.flush();

    m_input.available();

    m_output.write(40);
    m_output.flush();

    assertEquals(30, m_input.peek());
    assertEquals(1, m_input.read(buffer));
    assertEquals(30, buffer[0]);

    assertEquals(40, m_input.peek());
    assertEquals(1, m_input.read(buffer));
    assertEquals(40, buffer[0]);
  }

  @Test
  public void readBufferPartialNoBlock() throws IOException {
    byte buffer[];

    buffer = new byte[2];

    m_output.write(30);
    m_output.flush();

    assertEquals(30, m_input.peek());
    assertEquals(1, m_input.read(buffer));
    assertEquals(30, buffer[0]);
  }

  @Test
  public void readBufferLeftOver() throws IOException {
    byte buffer[];

    buffer = new byte[2];

    m_output.write(30);
    m_output.write(40);
    m_output.write(50);
    m_output.flush();

    assertEquals(30, m_input.peek());
    assertEquals(2, m_input.read(buffer));
    assertEquals(30, buffer[0]);
    assertEquals(40, buffer[1]);

    assertEquals(50, m_input.peek());
    assertEquals(1, m_input.read(buffer));
    assertEquals(50, buffer[0]);
    assertEquals(40, buffer[1]);
  }

  @Test
  public void unreadOverflow() throws IOException {
    int i;

    try {
      for (i = 0; i < m_input.getBufferSize(); i++) {
        m_input.unread(i);
        assertEquals(i & 0x00FF, m_input.peek());
      }
    } catch (IOException e) {
      fail();
    }

    try {
      m_input.unread(0);
      fail();
    } catch (IOException e) {
      e = null;  // Get rid of IDE warning
    }

    for (i = m_input.getBufferSize(); --i >= 0;) {
      assertEquals(i & 0x00FF, m_input.peek());
      assertEquals(i & 0x00FF, m_input.read());
    }
  }

  @Test
  public void fillWithNoMoreData() throws IOException {
    assertEquals(0, m_input.available());
    assertEquals(0, m_input.available());
  }

  @Test
  public void fillWithTooMuchData() throws IOException {
    int i;

    for (i = 0; i < m_input.getBufferSize() + 1; i++) {
      m_output.write(i);
    }

    m_output.flush();

    assertEquals(m_input.getBufferSize(), m_input.available());

    for (i = 0; i < m_input.getBufferSize() + 1; i++) {
      assertEquals(i & 0x00FF, m_input.peek());
      assertEquals(i & 0x00FF, m_input.read());
    }
  }

  @Test
  public void readAfterClose() throws IOException {
    m_output.write(10);
    m_output.flush();

    m_input.available();

    m_output.close();

    assertEquals(10, m_input.peek());
    assertEquals(10, m_input.read());
    assertEquals(-1, m_input.peek());
    assertEquals(-1, m_input.read());
  }

  @Test
  public void readBufferAfterClose() throws IOException {
    byte buffer[];

    m_output.write(10);
    m_output.flush();

    m_input.available();

    m_output.close();

    buffer = new byte[10];

    assertEquals(1, m_input.read(buffer));
    assertEquals(-1, m_input.read(buffer));
  }

  @Test
  public void testToString() throws IOException {
    int i;

    assertEquals("", m_input.toString());

    m_output.write(new byte[]{'h', 'e', 'l', 'l', 'o'});
    m_output.flush();

    m_input.available();

    assertEquals("hello", m_input.toString());

    for (i = 5; --i >= 0;) {
      m_input.read();
    }
  }

  @Test
  public void readBufferEmptyBuffer() throws IOException {
    byte buffer[];

    m_output.write(10);
    m_output.flush();

    buffer = new byte[1];

    assertEquals(1, m_input.read(buffer));
    assertEquals(10, buffer[0]);
  }
}
