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

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * this isn't yet a useful tool, but it shows how to track method calls with
 * their corresponding argument values
 */
public class CallMonitor extends ListenerAdapter {

  @Override
  public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
    
    if (executedInsn instanceof JVMInvokeInstruction) {
      if (executedInsn.isCompleted(ti) && !ti.isInstructionSkipped()) {
        JVMInvokeInstruction call = (JVMInvokeInstruction)executedInsn;
        MethodInfo mi = call.getInvokedMethod();
        Object[] args = call.getArgumentValues(ti);
        ClassInfo ci = mi.getClassInfo();

        StringBuilder sb = new StringBuilder();

        sb.append(ti.getId());
        sb.append(": ");

        int d = ti.getStackDepth();
        for (int i=0; i<d; i++){
          sb.append(" ");
        }

        if (ci != null){
          sb.append(ci.getName());
          sb.append('.');
        }
        sb.append(mi.getName());
        sb.append('(');

        int n = args.length-1;
        for (int i=0; i<=n; i++) {
          if (args[i] != null) {
            sb.append(args[i].toString());
          } else {
            sb.append("null");
          }
          if (i<n) {
            sb.append(',');
          }
        }
        sb.append(')');

        System.out.println(sb);
      }
    }
  }
}
