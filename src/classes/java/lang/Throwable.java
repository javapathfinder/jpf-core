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

package java.lang;

import java.io.PrintStream;
import java.io.PrintWriter;

/**
 * barebones model class for java.lang.Throwable
 * 
 * the main reason for having one is to defer the stacktrace creation, which
 * we don't want if all an application does is intercepting a gazillion of
 * exceptions like NoSuchMethodException
 */
public class Throwable {

  int[] snapshot; // this is an internal array of { globalMethodId, pcOffset } pairs
  
  protected Throwable cause; // in case this is a wrapper exception (like InvocationTargetException)
  
  protected String detailMessage; // where we keep the context specific info
    
  protected StackTraceElement[] stackTrace; // only set on demand, if getStackTrace() is called
  
  public Throwable() {
    try {                                            // Use Class.forName() instead of new StackTraceElement() since the latter creates garbage.
      Class.forName("java.lang.StackTraceElement");  // Force this class to load here instead of in createStackTrace().
    } catch (ClassNotFoundException e) {
      throw new NoClassDefFoundError("java.lang.StackTraceElement");
    }
    cause = this;
    fillInStackTrace();
  }

  public Throwable (String msg) {
    this();
    detailMessage = msg;
    cause = this;
  }

  public Throwable (String msg, Throwable xCause) {
    this();
    detailMessage = msg;
    cause = xCause;
  }

  public Throwable (Throwable xCause) {
    this();
    
    cause = xCause;
    if (cause != null){
      detailMessage = xCause.toString();
    }
  }

  public String getMessage() {
    return detailMessage;
  }
  
  public String getLocalizedMessage() {
    return detailMessage;
  }

  public Throwable getCause() {
    if (cause == this){
      return null;  // quirky
    } else {
      return cause;
    }
  }

  // this sets 'snapshot' from the native side
  public native Throwable fillInStackTrace();
  
  // and this turns 'snapshot' into an array of StackTraceElement objects
  private native StackTraceElement[] createStackTrace();
  
  public StackTraceElement[] getStackTrace() {
    if (stackTrace == null){
      stackTrace = createStackTrace();
    }
    
    return stackTrace;
  }
  
  public void setStackTrace (StackTraceElement[] st) {
    stackTrace = st;
  }

  public synchronized Throwable initCause (Throwable xCause) {
    if (xCause == this){
      throw new IllegalArgumentException("self-causation not permitted");
    }
    
    if (cause != this){
      throw new IllegalStateException("cannot overwrite cause");
    }
    
    cause = xCause;
    
    return this;
  }

  @Override
  public native String toString();
  
  public native void printStackTrace ();
  
  // <2do> and lots more of printStackTraces..

  private native String getStackTraceAsString();
  
  public void printStackTrace (PrintStream ps){
    String s = getStackTraceAsString();
    ps.print(s);
  }
  
  public void printStackTrace (PrintWriter pw){
    String s = getStackTraceAsString();
    pw.print(s);    
  }
  
  int getStackTraceDepth(){
    return (snapshot.length / 2); // snapshot stores the methodId and pc for each StackFrame 
  }
}
