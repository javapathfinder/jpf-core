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
package java.io;

import gov.nasa.jpf.vm.Verify;

import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;

/**
 * natively convert char output into byte output
 *
 * <2do> - this needs to be reworked. It trades locks for saving heap objects,
 * and that is a choice between a rock and a hard place. We shouldn't call
 * write() from within the native encode() (which we would have to since this
 * could be any OutputStream). We shouldn't create a very short
 * living JPF byte[] object per write() call, since that kills the heap. We
 * shouldn't use an explicit lock since that blows up the state space. That
 * leaves us with atomic sections, but it's not really safe since clients
 * might use their own OutputStream classes with synchronized write() methods.
 * At least this would not go unnoticed
 */
public class OutputStreamWriter extends Writer {

  static final int BUF_SIZE=128;
  //private static Object lock = new Object();
  
  OutputStream out;
  
  byte[] buf = new byte[BUF_SIZE*6]; // worst case UTF-8 
  
  public OutputStreamWriter(OutputStream os) {
    out = os;
  }
  
  public OutputStreamWriter(OutputStream os, Charset cs) {
    out = os;
    throw new UnsupportedOperationException("OutputStreamWriter model does not fully implement this constructor");
  }
  
  public OutputStreamWriter(OutputStream os, CharsetEncoder end) {
    out = os;
    throw new UnsupportedOperationException("OutputStreamWriter model does not fully implement this constructor");
  }
  
  public OutputStreamWriter(OutputStream os, String charsetName) {
    out = os;
    throw new UnsupportedOperationException("OutputStreamWriter model does not fully implement this constructor");
  }
  
  @Override
  public void close() throws IOException {
    out.close();
  }
  
  @Override
  public void flush() {
    // nothing
  }

  void flushBuffer() {
    // nothing
  }
  
  public native String getEncoding();
  
  native int encode (char[] cbuf, int off, int len, byte[] buf);
  
  @Override
  public void write(char[] cbuf, int off, int len) throws IOException {
    int w=0;
    
    //synchronized(lock){
    Verify.beginAtomic();
      while (w < len){
        int n = encode(cbuf, off+w, len-w, buf);
        out.write(buf, 0, n);
        w += n;
      }
    Verify.endAtomic();
    //}
  }
  
  private native int encode (char c, byte[] buf);

  @Override
  public void write (int c) throws IOException {
    //synchronized(lock){
    Verify.beginAtomic();
      int n = encode((char)c, buf);
      out.write(buf,0,n);
    Verify.endAtomic();
    //}
  }
  
  private native int encode (String s, int off, int len, byte[] buf);
  
  @Override
  public void write(String s, int off, int len) throws IOException {
    int w=0;
    
    //synchronized(lock){
    Verify.beginAtomic();
      while (w < len){
        int n = encode(s, off+w, len-w, buf);
        out.write(buf, 0, n);
        w += n;
      }
    Verify.endAtomic();
    //}
  }
}
