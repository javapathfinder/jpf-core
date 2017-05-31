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

/**
 * utility wrapper for exception handlers that /would/ handle
 * a given exception type
 * 
 * <2do> This should be a class hierarchy to properly distinguish between
 * ordinary catch handlers and UncaughtHandler objects, but so far
 * this isn't worth it
 */
public class HandlerContext {
  public enum UncaughtHandlerType { INSTANCE, GROUP, GLOBAL } 
  
  ThreadInfo ti;
  ClassInfo ciException;
  
  StackFrame frame;
  ExceptionHandler handler;
  // - or -
  int uncaughtHandlerRef;
  UncaughtHandlerType uncaughtHandlerType;

  HandlerContext (ThreadInfo ti, ClassInfo ciException, StackFrame frame, ExceptionHandler handler) {
    this.ti = ti;
    this.ciException = ciException;
    this.frame = frame;
    this.handler = handler;
  }
  
  HandlerContext (ThreadInfo ti, ClassInfo ciException, UncaughtHandlerType uncaughtHandlerType, int uncaughtHandlerRef){
    this.ti = ti;
    this.ciException = ciException;
    this.uncaughtHandlerType = uncaughtHandlerType;
    this.uncaughtHandlerRef = uncaughtHandlerRef;
  }

  public ThreadInfo getThreadInfo(){
    return ti;
  }
  
  public StackFrame getFrame () {
    return frame;
  }

  public ExceptionHandler getHandler () {
    return handler;
  }

  public boolean isUncaughtHandler(){
    return uncaughtHandlerType != null;
  }
  
  public UncaughtHandlerType getUncaughtHandlerType(){
    return uncaughtHandlerType;
  }
  
  public int getUncaughtHandlerRef(){
    return uncaughtHandlerRef;
  }
}
