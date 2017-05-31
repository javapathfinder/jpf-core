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
package gov.nasa.jpf;

import java.io.OutputStream;
import java.io.PrintStream;

/**
 * this is what we use for System.out and System.err, hence we go native
 * as quickly as possible, hence we don't need an underlying stream.
 * It's already slow enough as it is
 * 
 * NOTE - we have to intercept *everything* that might go to our base class, since
 * it is not initialized properly (we want to avoid costly PrinStream init unless
 * we really need it 
 */
public class ConsoleOutputStream extends PrintStream {
  
  
  public ConsoleOutputStream() {
    // that's a hack - it only works because we intercept the ctor in the native peer
    // otherwise it would throw an exception
    super((OutputStream)null);
  }
  
  @Override
  public void flush() {
    // we are not buffered anyways
  }
  
  @Override
  public void close() {
    // nothing to close
  }
  
  @Override
  public native void print (boolean b);
  public native void print (byte b);
  @Override
  public native void print (char c);
  public native void print (short s);
  @Override
  public native void print (int i);
  @Override
  public native void print (long l);
  @Override
  public native void print (float f);
  @Override
  public native void print (double d);
  @Override
  public native void print (String s);
  
  @Override
  public void print (Object o){
    if (o == null) {
      print("null");
    } else {
      print(o.toString());
    }
  }
    
  @Override
  public native void println (boolean b);
  public native void println (byte b);
  @Override
  public native void println (char c);
  public native void println (short s);
  @Override
  public native void println (int i);
  @Override
  public native void println (long l);
  @Override
  public native void println (float f);
  @Override
  public native void println (double d);
  @Override
  public native void println (String s);
  
  @Override
  public void println (Object o){
    if (o == null) {
      println("null");
    } else {
      println(o.toString());
    }
  }
  
  @Override
  public native void println();
  
  @Override
  public native PrintStream printf (String fmt, Object... args);
  @Override
  public PrintStream format (String fmt, Object... args){
    return printf(fmt,args);
  }
  
  @Override
  public native void write (int b);
  @Override
  public native void write (byte[] buf, int off, int len);
}
