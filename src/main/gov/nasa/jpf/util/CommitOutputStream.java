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

public class CommitOutputStream extends OutputStream
{
   private final OutputStream m_sink;
   private       byte         m_buffer[];
   private       int          m_size;
   
   public CommitOutputStream(OutputStream sink)
   {
      if (sink == null)
         throw new NullPointerException("sink == null");
      
      m_sink   = sink;
      m_buffer = new byte[1024];
   }
   
   @Override
  public void write(int data)
   {
      if (m_size >= m_buffer.length)
         m_buffer = Arrays.copyOf(m_buffer, 2 * m_buffer.length);
      
      m_buffer[m_size++] = (byte) data;
   }

   @Override
  public void write(byte buffer[], int offset, int length)
   {
      if (offset < 0)
         throw new IndexOutOfBoundsException("offset < 0 : " + offset);
      
      if (length < 0)
         throw new IndexOutOfBoundsException("length < 0 : " + length);
         
	   if (offset + length > buffer.length)
	      throw new IndexOutOfBoundsException("offset + length > buffer.length : " + offset + " + " + length + " > " + buffer.length);
      
      if (length == 0)
         return;
      
      if (m_size + length > m_buffer.length)
         m_buffer = Arrays.copyOf(m_buffer, Math.max(m_size + length, 2 * m_buffer.length));
      
      System.arraycopy(buffer, offset, m_buffer, m_size, length);
      
      m_size += length;
   }
   
   public int getSize()
   {
      return(m_size);
   }
   
   public void commit() throws IOException
   {
      if (m_size == 0)
         return;
      
      m_sink.write(m_buffer, 0, m_size);
      
      m_size = 0;
   }
   
   public void rollback()
   {
      m_size = 0;
   }
   
   @Override
  public void flush() throws IOException
   {
      m_sink.flush();      
   }
   
   @Override
  public void close() throws IOException
   {
      m_sink.close();
   }
}
