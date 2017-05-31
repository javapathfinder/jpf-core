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

import java.io.OutputStream;
import java.io.PrintStream;

import javax.swing.JTextArea;

/**
 * a utility that can be used to write logs which are displayed in a JTextArea
 */
public class ConsoleStream extends PrintStream {
  OutputStream os;
  JTextArea textArea;
  
  public ConsoleStream (JTextArea textArea) {
    super(System.out, true);
    this.textArea = textArea;
  }
  
  @Override
  public void write (byte[] buf, int off, int len) {
    String s = new String(buf, off, len);
    textArea.append(s);
  }
  
  @Override
  public void print( String s) {
    //super.print(s);
    textArea.append(s);
  }
  
  @Override
  public void println (String s) {
    //super.println(s);
    textArea.append(s);
    textArea.append("\n");
  }
  
  @Override
  public void print (Object o) {
    textArea.append(o.toString());
  }
  
  @Override
  public void println (Object o) {
    println(o.toString());
  }
  
  @Override
  public void println() {
    textArea.append("\n");
  }
}