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
import java.security.SecureRandom;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CommitOutputStreamTest extends TestJPF {

  private static final SecureRandom s_random = new SecureRandom();

  private PipedInputStream m_result;
  private CommitOutputStream m_fixture;

  @Before
  public void before() throws IOException {
    PipedOutputStream pipeOut;

    pipeOut = new PipedOutputStream();
    m_fixture = new CommitOutputStream(pipeOut);
    m_result = new PipedInputStream(pipeOut, 8 * 1024);
  }

  @After
  public void after() throws IOException {
    m_fixture.flush();
    assertEquals(0, m_result.available());
  }

  @Test(expected = NullPointerException.class)
  public void constructorNullArg() {
    new CommitOutputStream(null);
  }

  @Test
  public void flush() throws IOException {
    CountedOutputStream counted;
    CommitOutputStream fixture;

    counted = new CountedOutputStream();
    fixture = new CommitOutputStream(counted);

    fixture.write(10);
    fixture.flush();

    assertEquals(0, counted.getWriteCount());
    assertEquals(1, counted.getFlushCount());
    assertEquals(1, fixture.getSize());
  }

  @Test
  public void close() throws IOException {
    CountedOutputStream counted;
    CommitOutputStream fixture;

    counted = new CountedOutputStream();
    fixture = new CommitOutputStream(counted);

    fixture.write(10);
    fixture.close();

    assertEquals(0, counted.getWriteCount());
    assertEquals(1, counted.getCloseCount());
    assertEquals(1, fixture.getSize());
  }

  @Test
  public void rollback() throws IOException {
    m_fixture.write(10);
    assertEquals(1, m_fixture.getSize());
    m_fixture.rollback();
    assertEquals(0, m_fixture.getSize());
    m_fixture.commit();
    m_fixture.flush();
    assertEquals(0, m_result.available());
  }

  @Test
  public void expand() throws IOException {
    int i;

    for (i = 0; i < 2 * 1024; i++) {
      m_fixture.write(i);
    }

    assertEquals(2 * 1024, m_fixture.getSize());

    m_fixture.commit();

    assertEquals(0, m_fixture.getSize());
    assertEquals(2 * 1024, m_result.available());

    for (i = 0; i < 2 * 1024; i++) {
      assertEquals(i & 0x00FF, m_result.read());
    }
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

    assertEquals(0, m_fixture.getSize());
  }

  @Test
  public void writeArray() throws IOException {
    byte expected[], actual[];

    expected = new byte[10];

    s_random.nextBytes(expected);

    m_fixture.write(expected);
    assertEquals(expected.length, m_fixture.getSize());

    m_fixture.commit();
    assertEquals(0, m_fixture.getSize());

    m_fixture.flush();
    assertEquals(expected.length, m_result.available());

    actual = new byte[expected.length];

    assertEquals(actual.length, m_result.read(actual));
    assertArrayEquals(expected, actual);
  }

  @Test
  public void expandDouble() throws IOException {
    byte expected[], actual[];

    expected = new byte[3 * 1024 / 2];

    s_random.nextBytes(expected);

    m_fixture.write(expected);
    assertEquals(expected.length, m_fixture.getSize());

    m_fixture.commit();
    assertEquals(0, m_fixture.getSize());

    m_fixture.flush();
    assertEquals(expected.length, m_result.available());

    actual = new byte[expected.length];

    assertEquals(actual.length, m_result.read(actual));
    assertArrayEquals(expected, actual);
  }

  @Test
  public void expandTriple() throws IOException {
    byte expected[], actual[];

    expected = new byte[3 * 1024];

    s_random.nextBytes(expected);

    m_fixture.write(expected);
    assertEquals(expected.length, m_fixture.getSize());

    m_fixture.commit();
    assertEquals(0, m_fixture.getSize());

    m_fixture.flush();
    assertEquals(expected.length, m_result.available());

    actual = new byte[expected.length];

    assertEquals(actual.length, m_result.read(actual));
    assertArrayEquals(expected, actual);
  }

  private static class CountedOutputStream extends OutputStream {

    private int m_write;
    private int m_flush;
    private int m_close;

    @Override
	public void write(int data) {
      m_write++;
    }

    public int getWriteCount() {
      return (m_write);
    }

    @Override
	public void flush() {
      m_flush++;
    }

    public int getFlushCount() {
      return (m_flush);
    }

    @Override
	public void close() {
      m_close++;
    }

    public int getCloseCount() {
      return (m_close);
    }
  }
}
