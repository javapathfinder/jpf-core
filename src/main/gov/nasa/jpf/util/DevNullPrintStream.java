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

import java.io.PrintStream;
import java.io.ByteArrayOutputStream;
import java.util.Locale;

/**
 * a PrintStream that doesn't print anything
 */
public class DevNullPrintStream extends PrintStream {
  
  public DevNullPrintStream(){
    super( new ByteArrayOutputStream());
  }
  
  @Override
  public void flush(){}
  @Override
  public void close(){}
  @Override
  public boolean checkError(){
    return false;
  }
  @Override
  protected void setError(){}
  @Override
  protected void clearError(){}
  
  @Override
  public void write(int a){}
  @Override
  public void write(byte[] a, int b, int c){}
  @Override
  public void print(boolean a){}
  @Override
  public void print(char a){}
  @Override
  public void print(int a){}
  @Override
  public void print(long a){}
  @Override
  public void print(float a){}
  @Override
  public void print(double a){}
  @Override
  public void print(char[] a){}
  @Override
  public void print(String a){}
  @Override
  public void print(Object a){}
  @Override
  public void println(){}
  @Override
  public void println(boolean a){}
  @Override
  public void println(char a){}
  @Override
  public void println(int a){}
  @Override
  public void println(long a){}
  @Override
  public void println(float a){}
  @Override
  public void println(double a){}
  @Override
  public void println(char[] a){}
  @Override
  public void println(String a){}
  @Override
  public void println(Object a){}
  
  @Override
  public PrintStream printf(String a, Object... b){ return this; }
  @Override
  public PrintStream printf(Locale a, String b, Object... c){ return this; }
  @Override
  public PrintStream format(String a, Object... b){ return this; }
  @Override
  public PrintStream format(Locale a, String b, Object... c){ return this; }
  @Override
  public PrintStream append(CharSequence a){ return this; }
  @Override
  public PrintStream append(CharSequence a, int b, int c){ return this; }
  @Override
  public PrintStream append(char a){ return this; }

}
