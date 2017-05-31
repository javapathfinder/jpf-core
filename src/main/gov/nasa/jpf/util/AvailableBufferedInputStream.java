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

public class AvailableBufferedInputStream extends InputStream
{
   private static final boolean CLEAN               = false;
           static final int     DEFAULT_BUFFER_SIZE = 16 * 1024;        // Not private for testing purposes
   
   private final InputStream m_input;
   private final byte        m_buffer[];
   private       int         m_read;
   private       int         m_end;
   
   public AvailableBufferedInputStream(InputStream input)
   {
      this(input, DEFAULT_BUFFER_SIZE);
   }
   
   public AvailableBufferedInputStream(InputStream input, int bufferSize)
   {
      m_input  = input;
      m_buffer = new byte[bufferSize];
      
      if (input == null)
         throw new NullPointerException("input == null");
   }
   
   public int getBufferSize()
   {
      return(m_buffer.length);
   }
   
   @Override
  public int read() throws IOException
   {
      if (m_read >= m_end)
      {
         fill();
      
         if (m_read >= m_end)
            return(m_input.read());
      }
      
      return(m_buffer[m_read++] & 0x00FF);
   }

   @Override
  public int read(byte buffer[], int offset, int length) throws IOException
   {
      if (m_read >= m_end)
      {
         fill();
         
         if (m_read >= m_end)
            return(m_input.read(buffer, offset, length));
      }
      
      length  = Math.min(length, m_end - m_read);
      System.arraycopy(m_buffer, m_read, buffer, offset, length);
      m_read += length;
         
      return(length);
   }

   public int peek() throws IOException   // Returns -1 if there is nothing to read.
   {
      if (m_read >= m_end)
      {
         fill();
         
         if (m_read >= m_end)
            return(-1);
      }
      
      return(m_buffer[m_read] & 0x00FF);
   }

   @Override
  public int available() throws IOException
   {
      if (m_read >= m_end)
         fill();
      
      return(m_end - m_read);
   }
   
   public void unread(int data) throws IOException
   {
      if (m_read <= 0)
      {
         if (m_end >= m_buffer.length)
            throw new IOException("Internal buffer overflow");
         
         System.arraycopy(m_buffer, m_read, m_buffer, m_buffer.length - m_end, m_end);
         m_read = m_buffer.length - m_end;
         m_end  = m_buffer.length;
      }
      
      m_buffer[--m_read] = (byte) data;
   }
   
   private void fill() throws IOException
   {
      if (CLEAN)
         Arrays.fill(m_buffer, 0, m_buffer.length, (byte) 0);

      m_read = 0;
      m_end  = m_input.available();
      m_end  = Math.max(m_end, 0);

      if (m_end <= 0)                              // m_input.read(m_buffer, 0, 0) can be expensive.  Don't waste time.
         return;

      m_end  = Math.min(m_end, m_buffer.length);
      m_end  = m_input.read(m_buffer, 0, m_end);
      m_end  = Math.max(m_end, 0);                 // Defend against a bug where m_input.available() returning > 0 and m_input.read() returning -1
   }

   @Override
  public String toString()  // For debugging purposes
   {
      return(new String(m_buffer, m_read, m_end - m_read));
   }
}
