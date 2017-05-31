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
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;

/**
 * simple tool to log method invocations
 *
 * at this point, it doesn't do fancy things yet, but gives a more high
 * level idea of what got executed by JPF than the ExecTracker
 */
public class MethodTracker extends ListenerAdapter {

  static final String INDENT = "  ";

  MethodInfo lastMi;
  PrintWriter out;

  public MethodTracker (Config conf, JPF jpf) {
    out = new PrintWriter(System.out, true);
  }

  void logMethodCall(ThreadInfo ti, MethodInfo mi, int stackDepth) {
    out.print(ti.getId());
    out.print(":");

    for (int i=0; i<stackDepth%80; i++) {
      out.print(INDENT);
    }

    if (mi.isMJI()) {
      out.print("native ");
    }

    out.print(mi.getFullName());

    if (ti.isFirstStepInsn()) {
      out.print("...");
    }

    out.println();
  }

  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute) {
    MethodInfo mi = insnToExecute.getMethodInfo();

    if (mi != lastMi) {
      logMethodCall(ti, mi, ti.getStackDepth());
      lastMi = mi;

    } else if (insnToExecute instanceof JVMInvokeInstruction) {
      MethodInfo callee;

      // that's the only little gist of it - if this is a VirtualInvocation,
      // we have to dig the callee out by ourselves (it's not known
      // before execution)

      if (insnToExecute instanceof VirtualInvocation) {
        VirtualInvocation callInsn = (VirtualInvocation)insnToExecute;
        int objref = callInsn.getCalleeThis(ti);
        if (objref != MJIEnv.NULL){
          callee = callInsn.getInvokedMethod(ti, objref);
        } else {
          return; // this is causing a NPE, so don't report it as a unknown callee
        }

      } else if (insnToExecute instanceof INVOKESPECIAL) {
        INVOKESPECIAL callInsn = (INVOKESPECIAL)insnToExecute;
        callee = callInsn.getInvokedMethod(ti);

      } else {
        JVMInvokeInstruction callInsn = (JVMInvokeInstruction)insnToExecute;
        callee = callInsn.getInvokedMethod(ti);
      }

      if (callee != null) {
        if (callee.isMJI()) {
          logMethodCall(ti, callee, ti.getStackDepth()+1);
        }
      } else {
        out.println("ERROR: unknown callee of: " + insnToExecute);
      }
    }
  }

  /*
   * those are not really required, but mark the transition boundaries
   */
  @Override
  public void stateRestored(Search search) {
    int id = search.getStateId();
    out.println("----------------------------------- [" +
                       search.getDepth() + "] restored: " + id);
  }

  //--- the ones we are interested in
  @Override
  public void searchStarted(Search search) {
    out.println("----------------------------------- search started");
  }

  @Override
  public void stateAdvanced(Search search) {
    int id = search.getStateId();

    out.print("----------------------------------- [" +
                     search.getDepth() + "] forward: " + id);
    if (search.isNewState()) {
      out.print(" new");
    } else {
      out.print(" visited");
    }

    if (search.isEndState()) {
      out.print(" end");
    }

    out.println();

    lastMi = null;
  }

  @Override
  public void stateBacktracked(Search search) {
    int id = search.getStateId();

    lastMi = null;

    out.println("----------------------------------- [" +
                       search.getDepth() + "] backtrack: " + id);
  }

  @Override
  public void searchFinished(Search search) {
    out.println("----------------------------------- search finished");
  }

}
