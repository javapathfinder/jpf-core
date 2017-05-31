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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;

public class JPF_java_io_InputStreamReader extends NativePeer {

  static final int BUF_SIZE = 128;
  
  // <2do> decoder should be stored on a per-reader basis, since Charsets
  // might differ
  CharsetDecoder decoder;
  
  // ..same here - that's a shared resource with state! Only works for now
  // since all InputStreamReader decoding is protected by the same lock
  ByteBuffer in = ByteBuffer.allocate(BUF_SIZE);
  CharBuffer out = CharBuffer.allocate(BUF_SIZE);
 
  public JPF_java_io_InputStreamReader() {
    decoder = Charset.defaultCharset().newDecoder();
  }
  
  @MJI
  public int decode___3BI_3CIZ__I (MJIEnv env, int objref,
                                         int bref, int len, int cref, int off,
                                         boolean endOfInput){
    int c = -1;
    int lim = in.limit();
    
    if (lim < in.capacity()){ // left-over bytes
      in.clear();
      in.position(lim);
    } else {
      decoder.reset();
    }
    for (int i=0; i<len; i++){
      in.put(env.getByteArrayElement(bref,i));
    }
    in.flip();
    
    decoder.decode(in,out,endOfInput);
    
    int n = out.position();
    for (int i=0, j=off; i<n; i++,j++){
      env.setCharArrayElement(cref,j, out.get(i));
    }
    out.clear();
    if (n == len){
      in.clear();
    }
    
    return n;
  }
  
  
  // <2do> - that fails if we have a multi byte char and there is a backtrack
  // between decode() calls. Granted, that seems strange, but there is an
  // InputStream.read() in the loop which might just branch into user code
  @MJI
  public int decode__IZ__I (MJIEnv env, int objref, int b, boolean endOfInput){
    int c = -1;
    int lim = in.limit();
    
    // this is terrible overhead to get a single char, I must be doing something wrong..
    
    if (lim < in.capacity()){ // left-over bytes
      in.clear();
      in.position(lim);
    } else {
      decoder.reset();
    }
    
    in.put((byte)b);
    in.flip();
    
    decoder.decode(in,out,endOfInput);

    if (out.position() == 1){
      c = out.get(0);
      out.clear();
      in.clear();
    }
    
    return c;
  }
}
