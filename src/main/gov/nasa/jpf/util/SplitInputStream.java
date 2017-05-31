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
import java.util.Arrays;
import java.util.concurrent.locks.ReentrantLock;

/* Note: This class fails after 8 petabytes of data has been read.  This should
 * never be a problem.  For example, assuming a 10 Ghz clock and 1 cycle to read
 * 4-bytes from L1 cache, it would take 7.3 years to read that much data.
 */
public class SplitInputStream {

  static final int INITIAL_BUFFER_SIZE = 1024; // Recommended to be a power of 2.  If not, it will be rounded up to the next power of 2.

  private final ReentrantLock m_sourceLock = new ReentrantLock();
  private final ReentrantLock m_dataLock = new ReentrantLock();
  private final InputStream m_source;      // Protected by m_sourceGate lock
  private final Stream m_stream[];    // Not protected by a lock
  private long m_write;       // Must hold m_dataGate lock to read.  Must hold m_dataGate and m_sourceGate to write.
  private int m_available;   // Must hold m_dataGate lock to read.  Must hold m_dataGate and m_sourceGate to write.
  private int m_openStreams; // Must hold m_dataGate lock to access.
  private byte m_buffer[];    // Must hold m_dataGate lock to read.  Must hold m_sourceGate lock to write.  The written data doesn't become available until m_write is updated.

  public SplitInputStream(InputStream source, int streamCount) {
    this(source, streamCount, INITIAL_BUFFER_SIZE);
  }

  public SplitInputStream(InputStream source, int streamCount, int initialSize) {
    int i;

    if (source == null) {
      throw new NullPointerException("source == null");
    }

    if (streamCount <= 0) {
      throw new IllegalArgumentException("streamCount <= 0 : " + streamCount);
    }

    if (initialSize <= 0) {
      throw new IllegalArgumentException("initialSize <= 0 : " + initialSize);
    }

    m_source = source;
    m_openStreams = streamCount;
    m_stream = new Stream[streamCount];

    for (i = streamCount; --i >= 0;) {
      m_stream[i] = new Stream(i);
    }

    initialSize--;                     // Rounds initialSize up to the next power of 2
    initialSize |= initialSize >> 1;
    initialSize |= initialSize >> 2;
    initialSize |= initialSize >> 4;
    initialSize |= initialSize >> 8;
    initialSize |= initialSize >> 16;
    initialSize++;

    m_buffer = new byte[initialSize];
  }

  public int getStreamCount() {
    return (m_stream.length);
  }

  public InputStream getStream(int index) {
    return (m_stream[index]);
  }

  private int read(int index) throws IOException {
    long position;
    int offset, result;

    m_dataLock.lock();

    try {
      position = m_stream[index].getPosition();

      if (position == m_write) {
        if (!fill(index)) {
          return (-1);
        }

        position = m_stream[index].getPosition();
      }

      offset = getBufferOffset(position);
      result = m_buffer[offset] & 0x0FF;

      m_stream[index].setPosition(position + 1);
    } finally {
      m_dataLock.unlock();
    }

    return (result);
  }

  private int read(int index, byte buffer[], int offset, int length) throws IOException {
    long position;
    int off;

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

    m_dataLock.lock();

    try {
      position = m_stream[index].getPosition();

      if (position == m_write) {
        if (!fill(index)) {
          return (-1);
        }

        position = m_stream[index].getPosition();
      }

      off = getBufferOffset(position);
      length = (int) Math.min(length, m_write - position);
      length = Math.min(length, m_buffer.length - off);

      m_stream[index].setPosition(position + length);
      System.arraycopy(m_buffer, off, buffer, offset, length);
    } finally {
      m_dataLock.unlock();
    }

    return (length);
  }

  private long skip(int index, long n) throws IOException {
    long position;

    if (n <= 0) {
      return (0);
    }

    m_dataLock.lock();

    try {
      position = m_stream[index].getPosition();

      if (position == m_write) {
        if (!fill(index)) {
          return (0);
        }

        position = m_stream[index].getPosition();
      }

      n = Math.min(n, m_write - position);

      m_stream[index].setPosition(position + n);
    } finally {
      m_dataLock.unlock();
    }

    return (n);
  }

  private boolean fill(int index) throws IOException {
    long minPosition, write;
    int length, offsetPosition, offsetWrite;

    try {
      if (!doLock(index)) {
        return (true);
      }

      minPosition = getMinPosition();

      if (m_write - minPosition + 1 >= m_buffer.length) {
        expand();
      }

      write = m_write;               // Capture the data in local variables so the calculations can take place outside m_dataLock.
      length = m_buffer.length;
      m_available = m_source.available();

      m_dataLock.unlock();                 // Don't hold m_dataLock while blocked reading.  That way other Streams with data left to read can do so.

      offsetWrite = getBufferOffset(write);
      offsetPosition = getBufferOffset(minPosition); // If the minPosition advances while not holding the lock, no big deal.  It simply means less data will be read from m_source.
      length = getReadLength(offsetPosition, offsetWrite, length);

      do {
        length = m_source.read(m_buffer, offsetWrite, length);
      } while (length == 0); // Guarantee that at least 1 byte is read or end of file is reached.

      if (length < 0) {
        return (false);
      }

      m_dataLock.lock();

      m_write += length;
      m_available = m_source.available();
    } finally {
      m_sourceLock.unlock();

      if (!m_dataLock.isHeldByCurrentThread()) // Restore the lock state when the method was called.
      {
        m_dataLock.lock();
      }
    }

    return (true);
  }

  private boolean doLock(int index) {
    long position;

    /* m_sourceLock must be acquired before m_dataLock.  Otherwise, there will
     * be a deadlock.  But, if we can tryLock() m_sourceLock while holding 
     * m_dataLock, this will save CPU time.
     */
    if (m_sourceLock.tryLock()) {
      return (true);
    }

    m_dataLock.unlock();
    m_sourceLock.lock();
    m_dataLock.lock();

    position = m_stream[index].getPosition();

    return (position == m_write);    // Does the thread still need to read data?
  }

  private long getMinPosition() {
    long result, position;
    int i;

    result = Long.MAX_VALUE;

    for (i = m_stream.length; --i >= 0;) {
      if (!m_stream[i].isClosed()) {
        position = m_stream[i].getPosition();
        result = Math.min(result, position);
      }
    }

    return (result);
  }

  private int getReadLength(int offsetPosition, int offsetWrite, int length) {
    if (offsetPosition > offsetWrite) {
      return (offsetPosition - offsetWrite - 1);
    }

    length -= offsetWrite;

    if (offsetPosition == 0) {
      length--;
    }

    return (length);
  }

  private void expand() {
    int length;
    byte buffer[];

    buffer = m_buffer;
    length = buffer.length;
    m_buffer = Arrays.copyOf(buffer, 2 * length);

      // Since we are doubling the length of the array, we simply have to duplicate the contents.
    // This allows us to avoid figuring out which part of m_buffer is actually holding data and dealing with wrapping.
    System.arraycopy(buffer, 0, m_buffer, length, length);
  }

  private int available(int index) throws IOException {
    long result;
    boolean sourceLock;

    m_dataLock.lock();

    sourceLock = m_sourceLock.tryLock();   // By putting this after locking m_dataLock, the only way tryLock() will fail is if a thread is reading from m_source.

    try {
      if (sourceLock) {
        m_available = m_source.available();
      }

      result = m_available;
      result += m_write - m_stream[index].getPosition();
    } finally {
      m_dataLock.unlock();

      if (sourceLock) {
        m_sourceLock.unlock();
      }
    }

    if (result > Integer.MAX_VALUE) {
      return (Integer.MAX_VALUE);
    }

    return ((int) result);
  }

  private void close() throws IOException {
    boolean close;

    m_dataLock.lock();

    try {
      m_openStreams--;

      close = m_openStreams == 0;
    } finally {
      m_dataLock.unlock();
    }

    if (!close) {
      return;
    }

    m_sourceLock.lock();

    try {
      m_source.close();
    } finally {
      m_sourceLock.unlock();
    }
  }

  private int getBufferOffset(long position) {
    return ((int) (position & (m_buffer.length - 1)));
  }

  private class Stream extends InputStream {

    private long m_position;
    private final int m_index;
    private boolean m_closed;

    private Stream(int index) {
      m_index = index;
    }

    long getPosition() {
      return (m_position);
    }

    void setPosition(long position) {
      m_position = position;
    }

    synchronized boolean isClosed() {
      return (m_closed);
    }

    @Override
	public int read() throws IOException {
      if (isClosed()) {
        return (-1);
      }

      return (SplitInputStream.this.read(m_index));
    }

    @Override
	public int read(byte buffer[], int offset, int length) throws IOException {
      if (isClosed()) {
        return (-1);
      }

      return (SplitInputStream.this.read(m_index, buffer, offset, length));
    }

    @Override
	public long skip(long n) throws IOException {
      if (isClosed()) {
        return (0);
      }

      return (SplitInputStream.this.skip(m_index, n));
    }

    @Override
	public int available() throws IOException {
      if (isClosed()) {
        return (0);
      }

      return (SplitInputStream.this.available(m_index));
    }

    @Override
	public void close() throws IOException {
      synchronized (this) {
        if (m_closed) {
          return;
        }

        m_closed = true;
      }

      SplitInputStream.this.close();
    }
  }
}
