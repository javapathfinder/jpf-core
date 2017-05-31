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
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.util.Arrays;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class SplitOutputStreamTest extends TestJPF {

  private PipedInputStream m_sinks[];
  private SplitOutputStream m_fixture;

  @Before
  public void before() throws IOException {
    PipedOutputStream output[];
    int i;

    m_sinks = new PipedInputStream[2];
    output = new PipedOutputStream[2];

    for (i = m_sinks.length; --i >= 0;) {
      m_sinks[i] = new PipedInputStream();
      output[i] = new PipedOutputStream(m_sinks[i]);
    }

    m_fixture = new SplitOutputStream(output);

    Arrays.fill(output, null);   // Force SplitOutputStream to make a copy of output
  }

  @After
  public void after() throws IOException {
    int i, length, offset, delta;
    byte expect[], actual[];

    m_fixture.flush();
    m_fixture.close();

    expect = new byte[128];
    actual = new byte[128];

    while (true) {
      length = m_sinks[0].read(expect);

      if (length < 0) {
        break;
      }

      for (i = m_sinks.length; --i > 0;) {
        assertTrue(m_sinks[i].available() >= length);

        for (offset = 0; offset < length; offset += delta) {
          delta = m_sinks[i].read(actual, offset, length - offset);

          assertTrue(delta >= 0);
        }

        assertArrayEquals(actual, expect);
      }
    }

    for (i = m_sinks.length; --i > 0;) {
      assertEquals(-1, m_sinks[i].read());
    }
  }

  @Test(expected = NullPointerException.class)
  public void passNullPointerToConstructor() throws IOException {
    OutputStream sinks[];

    sinks = null;

    new SplitOutputStream(sinks);
  }

  @Test(expected = IllegalArgumentException.class)
  public void passNoArgsToConstructor() throws IOException {
    new SplitOutputStream();
  }

  @Test(expected = NullPointerException.class)
  public void passNullArgsToConstructor() throws IOException {
    OutputStream sink;

    sink = null;

    new SplitOutputStream(sink);
  }

  @Test
  public void writeByte() throws IOException {
    m_fixture.write(123);
  }

  @Test
  public void modifyAfterConstructor() {

  }

  @Test(expected = NullPointerException.class)
  public void writeNullBuffer() throws IOException {
    m_fixture.write(null, 0, 1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void writeIndexNegOne() throws IOException {
    m_fixture.write(new byte[0], -1, 0);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void writeLengthNegOne() throws IOException {
    m_fixture.write(new byte[0], 0, -1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void writeBeyondEnd() throws IOException {
    m_fixture.write(new byte[16], 8, 9);
  }

  @Test
  public void writeLengthZero() throws IOException {
    m_fixture.write(new byte[1], 0, 0);
  }

  @Test
  public void writeArray() throws IOException {
    byte buffer[];

    buffer = new byte[]{2, 3, 5, 7, 11, 13};

    m_fixture.write(buffer);
  }
}
