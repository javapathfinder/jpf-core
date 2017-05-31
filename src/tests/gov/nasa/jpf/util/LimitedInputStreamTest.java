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
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.SecureRandom;

import org.junit.Before;
import org.junit.Test;

public class LimitedInputStreamTest extends TestJPF {

  private static final SecureRandom s_random = new SecureRandom();

  private byte m_expected[];
  private ByteArrayInputStream m_source;
  private LimitedInputStream m_fixture;

  @Before
  public void before() {
    m_expected = new byte[10];

    s_random.nextBytes(m_expected);

    m_source = new ByteArrayInputStream(m_expected);
    m_fixture = new LimitedInputStream(m_source);
  }

  @Test(expected = NullPointerException.class)
  public void constructorNullArg() {
    new LimitedInputStream(null);
  }

  @Test(expected = IllegalArgumentException.class)
  public void setLimitNegArg() {
    m_fixture.setLimit(-1);
  }

  @Test(expected = NullPointerException.class)
  public void readNullBuffer() throws IOException {
    m_fixture.read(null, 0, 0);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readNegOffset() throws IOException {
    m_fixture.read(m_expected, -1, 1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readNegLength() throws IOException {
    m_fixture.read(m_expected, 0, -1);
  }

  @Test(expected = IndexOutOfBoundsException.class)
  public void readOffsetLengthTooLong() throws IOException {
    m_fixture.read(m_expected, 5, 6);
  }

  @Test
  public void setLimitZero() {
    m_fixture.setLimit(0);

    assertEquals(0, m_fixture.getLimit());
  }

  @Test
  public void setLimitMax() {
    m_fixture.setLimit(Integer.MAX_VALUE);

    assertEquals(Integer.MAX_VALUE, m_fixture.getLimit());
  }

  @Test
  public void readMaxLimit() throws IOException {
    read(Integer.MAX_VALUE);
  }

  @Test
  public void readExactLimit() throws IOException {
    read(m_expected.length);
  }

  @Test
  public void readShortLimit() throws IOException {
    read(m_expected.length / 2);
  }

  @Test
  public void readZeroLimit() throws IOException {
    read(0);
  }

  private void read(int limit) throws IOException {
    int i;

    m_fixture.setLimit(limit);

    limit = Math.min(limit, m_expected.length);

    for (i = 0; i < limit; i++) {
      assertEquals(limit - i, m_fixture.available());
      assertEquals(m_expected[i] & 0x00FF, m_fixture.read());
    }

    assertEquals(-1, m_fixture.read());
    assertEquals(0, m_fixture.available());
  }

  @Test
  public void readBufferZeroLength() throws IOException {
    assertEquals(0, m_fixture.read(m_expected, 0, 0));
  }

  @Test
  public void readBufferZeroLimit() throws IOException {
    assertEquals(-1, m_fixture.read(m_expected, 0, 1));
  }

  @Test
  public void readBufferShortLimit() throws IOException {
    int i, length;
    byte actual[];

    length = m_expected.length / 2;
    actual = new byte[m_expected.length];

    m_fixture.setLimit(length);

    assertEquals(length, m_fixture.read(actual, 0, actual.length));
    assertEquals(-1, m_fixture.read());
    assertEquals(0, m_fixture.available());

    for (i = 0; i < length; i++) {
      assertEquals(m_expected[i], actual[i]);
    }

    for (i = length; i < actual.length; i++) {
      assertEquals(0, actual[i]);
    }
  }

  @Test
  public void readBufferEOF() throws IOException {
    int i;
    byte actual[];

    actual = new byte[m_expected.length];

    m_fixture.setLimit(Integer.MAX_VALUE);

    assertEquals(actual.length, m_fixture.read(actual));

    for (i = actual.length; --i >= 0;) {
      assertEquals(m_expected[i], actual[i]);
    }

    assertEquals(-1, m_fixture.read(actual));
  }

  @Test
  public void skipZeroLimit() throws IOException {
    assertEquals(0, m_fixture.skip(1));
  }

  @Test
  public void skipShortLimit() throws IOException {
    int length;

    length = m_expected.length / 2;

    m_fixture.setLimit(m_expected.length);

    assertEquals(length, m_fixture.skip(length));
  }

  @Test
  public void skipEOF() throws IOException {
    m_fixture.setLimit(Integer.MAX_VALUE);

    assertEquals(m_expected.length, m_fixture.skip(Integer.MAX_VALUE));
    assertEquals(0, m_fixture.skip(1));
  }

  @Test
  public void close() throws IOException {
    CountClose m_counter;

    m_counter = new CountClose();
    m_fixture = new LimitedInputStream(m_counter);

    m_fixture.setLimit(5);
    m_fixture.close();

    assertEquals(1, m_counter.getCloseCount());
    assertEquals(0, m_fixture.getLimit());
  }

  private static class CountClose extends InputStream {

    private int m_close;

    @Override
	public int read() {
      return (0);
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
