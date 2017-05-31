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
import java.io.OutputStream;
import java.util.Arrays;

public class SplitOutputStream extends OutputStream {

  private final OutputStream m_sinks[];

  public SplitOutputStream(OutputStream... sinks) {
    int i;

    if (sinks.length <= 0) {
      throw new IllegalArgumentException("sinks.length <= 0 : " + sinks.length);
    }

    for (i = sinks.length; --i >= 0;) {
      if (sinks[i] == null) {
        throw new NullPointerException("sinks[i] == null : " + i);
      }
    }

    m_sinks = Arrays.copyOf(sinks, sinks.length);
  }

  @Override
  public void write(int data) throws IOException {
    int i;

    for (i = m_sinks.length; --i >= 0;) {
      m_sinks[i].write(data);
    }
  }

  @Override
  public void write(byte buffer[], int offset, int length) throws IOException {
    int i;

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
      return;
    }

    for (i = m_sinks.length; --i >= 0;) {
      m_sinks[i].write(buffer, offset, length);
    }
  }

  @Override
  public void flush() throws IOException {
    int i;

    for (i = m_sinks.length; --i >= 0;) {
      m_sinks[i].flush();
    }
  }

  @Override
  public void close() throws IOException {
    int i;

    for (i = m_sinks.length; --i >= 0;) {
      m_sinks[i].close();
    }
  }
}
