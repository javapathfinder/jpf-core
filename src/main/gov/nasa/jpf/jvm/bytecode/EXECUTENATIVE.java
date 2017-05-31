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

package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.lang.reflect.Method;

/**
 * this is a synthetic instruction to (re-)execute native methods
 *
 * Note that StackFrame and lock handling has to occur from within
 * the corresponding NativeMethodInfo
 */
public class EXECUTENATIVE extends Instruction implements JVMInstruction {

  // unfortunately we can't null this in cleanupTransients(), but it is
  // a potential leak for stored traces
  protected NativeMethodInfo executedMethod;

  @Override
  public boolean isExtendedInstruction() {
    return true;
  }

  public static final int OPCODE = 259;

  @Override
  public int getByteCode () {
    return OPCODE;
  }

  public EXECUTENATIVE (){}

  public EXECUTENATIVE (NativeMethodInfo mi){
    executedMethod = mi;
  }

  public void setExecutedMethod (NativeMethodInfo mi){
    executedMethod = mi;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction execute (ThreadInfo ti) {

    // we don't have to enter/leave or push/pop a frame, that's all done
    // in NativeMethodInfo.execute()
    // !! don't re-enter if this is reexecuted !!
    return executedMethod.executeNative(ti);
  }

  public MethodInfo getExecutedMethod() {
    return executedMethod;
  }

  public String getExecutedMethodName(){
    return executedMethod.getName();
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("executenative");

    if (executedMethod != null){
      Method m = executedMethod.getMethod();
      sb.append(' ');
      sb.append( m.getDeclaringClass().getSimpleName());
      sb.append('.');
      sb.append( m.getName());
    }

    return sb.toString();
  }
}
