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


/**
 * how hard can it be to transform byte(s) into a char? I hate Unicode
 */
public class InputStreamReader extends Reader {

  static final int BUF_SIZE=128;
  private static Object lock = new Object(); // our peer has state
  
  InputStream in;
  byte[] bbuf = new byte[BUF_SIZE];
  String charSetName=null;
  
  public InputStreamReader (InputStream in){
    this.in = in;
  }  
  
  public InputStreamReader (InputStream in,String charSetName){
    this.in = in;
    this.charSetName = charSetName;
  }  
  
  @Override
  public void close () throws IOException {
    in.close();
  }

  private native int decode (int b, boolean endOfInput);

  @Override
  public boolean ready() {
    try {
      return (in.available() > 0);
    } catch (IOException iox){
      return false;
    }
  }
  
  @Override
  public int read () throws IOException {    
    synchronized (lock){
      while (true){
        
        int b = in.read();
        if (b < 0){
          return -1;
        }

        int c = decode(b, (in.available() == 0));
        if (c >= 0 ) {
          return c;
        }
      }
    }
  }
    
  
  
  native int decode (byte[] inBuf,int len,
                            char[] outBuf, int off,
                            boolean endOfInput);

  
  @Override
  public int read (char[] cbuf, int off, int len) throws IOException {
    int r = 0;
    boolean available = true;
    
    synchronized (lock){
      while (available && r < len){
        // <2do> - so what if that backtracks? the peer might have 
        // iteration-specific state that gets lost. see native peer comments        
        int b = in.read(bbuf, 0, Math.min(len-r,bbuf.length));
        if (b < 0){
          return (r == 0) ? -1 : r;
        }

        // true if we have not reach the end of the input stream "in"
        // and there are still more bytes available to be read
        available = (in.available() > 0);
        
        r += decode(bbuf,b, cbuf,off+r, !available);
      }
    }
      
    return r;
  }
  
}
