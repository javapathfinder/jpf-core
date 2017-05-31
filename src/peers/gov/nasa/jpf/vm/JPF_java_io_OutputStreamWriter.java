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
import java.nio.charset.CharsetEncoder;

/**
 * native peer for OutputStreamWriter, to avoid that we have the
 * char-to-byte conversion in JPF
 *
 * <2do> this needs to be de-staticed (see model class)
 */
public class JPF_java_io_OutputStreamWriter extends NativePeer {

  static final int BUF_SIZE=128; // needs to be the same as in the model class!
  static CharsetEncoder encoder;
  
  static CharBuffer in = CharBuffer.allocate(BUF_SIZE);
  static ByteBuffer out = ByteBuffer.allocate(BUF_SIZE*6); // worst case UTF-8

  public JPF_java_io_OutputStreamWriter() {
    encoder = Charset.defaultCharset().newEncoder();
  }

  @MJI
  public int encode___3CII_3B__I (MJIEnv env, int objref,
                                         int cref, int off, int len,
                                         int bref){
    if (len > BUF_SIZE){ // check for buffer overflow
      len = BUF_SIZE;
    }
    int imax = off+len;

    out.clear();
    in.clear();
    
    for (int i=off; i<imax; i++){
      in.put(env.getCharArrayElement(cref, i));
    }

    in.flip();
    encoder.encode(in,out,true);
    
    int n = out.position();
    for (int i=0; i<n; i++){
      env.setByteArrayElement(bref,i,out.get(i));
    }
    
    return n;
  }
  
  @MJI
  public int encode__Ljava_lang_String_2II_3B__I (MJIEnv env, int objref,
                                         int sref, int off, int len,
                                         int bref){
    int cref = env.getReferenceField(sref, "value");
    
    return encode___3CII_3B__I(env,objref,cref,off,len,bref);
  }
  
  @MJI
  public int encode__C_3B__I (MJIEnv env, int objref, char c, int bufref) {
    out.clear();
    
    in.clear();
    in.put(c);
    in.flip();

    encoder.encode(in,out,true);
    
    int n = out.position();
    for (int i=0; i<n; i++){
      env.setByteArrayElement(bufref,i,out.get(i));
    }
    
    return n;
  }
}
