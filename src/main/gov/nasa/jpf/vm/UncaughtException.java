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

import gov.nasa.jpf.util.Printable;

import java.io.PrintWriter;


/**
 * represents the case of an unhandled exception detected by JPF
 *
 * This is a "controlflow exception", but I finally made my peace with it since
 * UncaughtExceptions can be thrown from various places, including the VM (<clinit>, finalizer)
 * and we can't rely on that all these locations can check for pc == null. Even if they would,
 * at this point there is nothing to do anymore, get to the NoUncaughtProperty reporting
 * as quickly as possible, since chances are we would be even obfuscating the problem
 */
@SuppressWarnings("serial")
public class UncaughtException extends RuntimeException implements Printable {

  ThreadInfo thread;
  int xObjRef;          // the exception object reference (that went uncaught)

  String     xClsName;
  String     details;

  //ArrayList  stackTrace; // unused -pcd

  public UncaughtException (ThreadInfo ti, int objRef) {
    thread = ti;
    xObjRef = objRef;
    
    ElementInfo ei = ti.getElementInfo(xObjRef);
    xClsName = ei.getClassInfo().getName();
    details = ei.getStringField("detailMessage");
  }
  
  public String getRawMessage () {
    return xClsName;
  }
  
  @Override
  public String getMessage () {
    String s = "uncaught exception in thread " + thread.getName() +
              " #" + thread.getId() + " : "
              + xClsName;
    
    if (details != null) {
      s += " : \"" + details + "\"";
    }
    
    return s;
  }

  @Override
  public void printOn (PrintWriter pw) {
    pw.print("uncaught exception in thread ");
    pw.print( thread.getName());
    pw.print(" #");
    pw.print(thread.getId());
    pw.print(" : ");

    thread.printStackTrace(pw, xObjRef);
    pw.flush();
  }
}
