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

import java.io.IOException;
import java.io.InputStream;

public class LimitedInputStream extends InputStream {

  private final InputStream m_source;
  private int m_limit;

  public LimitedInputStream(InputStream source) {
    if (source == null) {
      throw new NullPointerException("source == null");
    }

    m_source = source;
  }

  public int getLimit() {
    return (m_limit);
  }

  public void setLimit(int length) {
    if (length < 0) {
      throw new IllegalArgumentException("length < 0 : " + length);
    }

    m_limit = length;
  }

  @Override
  public int read() throws IOException {
    int result;

    if (m_limit <= 0) {
      return (-1);
    }

    result = m_source.read();

    if (result >= 0) {
      m_limit--;
    }

    return (result);
  }

  @Override
  public int read(byte buffer[], int offset, int length) throws IOException {
    if (buffer == null) {
      throw new NullPointerException("buffer == null");
    }

    if (offset < 0) {
      throw new IndexOutOfBoundsException("offset < 0 : " + offset);
    }

    if (length < 0) {
      throw new IndexOutOfBoundsException("length < 0 : " + length);
    }

    if (offset + length > buffer.length) {
      throw new IndexOutOfBoundsException("offset + length > buffer.length : " + offset + " + " + length + " > " + buffer.length);
    }

    if (length == 0) {
      return (0);
    }

    length = Math.min(m_limit, length);

    if (length == 0) {
      return (-1);
    }

    length = m_source.read(buffer, offset, length);

    if (length > 0) {
      m_limit -= length;
    }

    return (length);
  }

  @Override
  public long skip(long n) throws IOException {
    n = Math.min(n, m_limit);

    if (n <= 0) {
      return (0);
    }

    n = m_source.skip(n);

    if (n > 0) {
      m_limit -= n;
    }

    return (n);
  }

  @Override
  public int available() throws IOException {
    int result;

    if (m_limit <= 0) {
      return (0);
    }

    result = m_source.available();
    result = Math.min(result, m_limit);

    return (result);
  }

  @Override
  public void close() throws IOException {
    m_limit = 0;

    m_source.close();
  }
}
