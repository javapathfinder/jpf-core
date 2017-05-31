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
package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.INVOKESPECIAL;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.VirtualInvocation;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;

/**
 * simple tool to log stack invocations
 *
 * at this point, it doesn't do fancy things yet, but gives a more high
 * level idea of what got executed by JPF than the ExecTracker
 */
public class StackTracker extends ListenerAdapter {

  static final String INDENT = "  ";

  MethodInfo lastMi;
  PrintWriter out;
  long nextLog;
  int logPeriod;

  public StackTracker (Config conf, JPF jpf) {
    out = new PrintWriter(System.out, true);
    logPeriod = conf.getInt("jpf.stack_tracker.log_period", 5000);
  }

  void logStack(ThreadInfo ti) {
    long time = System.currentTimeMillis();

    if (time < nextLog) {
      return;
    }

    nextLog = time + logPeriod;

    out.println();
    out.print("Thread: ");
    out.print(ti.getId());
    out.println(":");

    out.println(ti.getStackTrace());
    out.println();
  }

  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute) {
    MethodInfo mi = insnToExecute.getMethodInfo();

    if (mi != lastMi) {
      logStack(ti);
      lastMi = mi;

    } else if (insnToExecute instanceof JVMInvokeInstruction) {
      MethodInfo callee;

      // that's the only little gist of it - if this is a VirtualInvocation,
      // we have to dig the callee out by ourselves (it's not known
      // before execution)

      if (insnToExecute instanceof VirtualInvocation) {
        VirtualInvocation callInsn = (VirtualInvocation)insnToExecute;
        int objref = callInsn.getCalleeThis(ti);
        callee = callInsn.getInvokedMethod(ti, objref);

      } else if (insnToExecute instanceof INVOKESPECIAL) {
        INVOKESPECIAL callInsn = (INVOKESPECIAL)insnToExecute;
        callee = callInsn.getInvokedMethod(ti);

      } else {
        JVMInvokeInstruction callInsn = (JVMInvokeInstruction)insnToExecute;
        callee = callInsn.getInvokedMethod(ti);
      }

      if (callee != null) {
        if (callee.isMJI()) {
          logStack(ti);
        }
      } else {
        out.println("ERROR: unknown callee of: " + insnToExecute);
      }
    }
  }

  @Override
  public void stateAdvanced(Search search) {
    lastMi = null;
  }

  @Override
  public void stateBacktracked(Search search) {
    lastMi = null;
  }
}
