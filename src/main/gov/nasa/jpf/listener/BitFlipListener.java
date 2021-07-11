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
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.ARETURN;
import gov.nasa.jpf.jvm.bytecode.ASTORE;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.PUTFIELD;
import gov.nasa.jpf.jvm.bytecode.PUTSTATIC;
import gov.nasa.jpf.jvm.bytecode.RETURN;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.IntChoiceGenerator;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceFieldInstruction;
import gov.nasa.jpf.vm.bytecode.InstanceInvokeInstruction;
import gov.nasa.jpf.vm.bytecode.InstructionInterface;
import gov.nasa.jpf.vm.bytecode.InvokeInstruction;
import gov.nasa.jpf.vm.bytecode.LocalVariableInstruction;
import gov.nasa.jpf.vm.bytecode.ReturnInstruction;
import gov.nasa.jpf.vm.bytecode.ReturnValueInstruction;
import gov.nasa.jpf.vm.bytecode.WriteInstruction;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

public class BitFlipListener extends ListenerAdapter {

  static HashMap<String, Long> lastFlippedBits;
  static int[][] binomial;
  static {
    initializeBinomial();
    lastFlippedBits = new HashMap<>();
  }
  /**
   * initialize the binomial coefficients by Pascal's triangle
   * allow up to 7 bits to flip to avoid state explosion that JPF cannot handle
   */
  public static void initializeBinomial () {
    binomial = new int[65][8];
    binomial[0][0] = binomial[1][0] = binomial[1][1] = 1;
    for (int i = 2; i <= 64; ++i) {
      binomial[i][0] = 1;
      if (i < 8) {
        binomial[i][i] = 1;
      }
      for (int j = 1; j < i && j < 8; ++j) {
        binomial[i][j] = binomial[i-1][j] + binomial[i-1][j-1];
      }
    }
  }

  public void bitFlip (StackFrame frame, int off, int len, int nBit, int choice, String key) {
    long v = (len <= 32) ? (long)frame.peek(off) : frame.peekLong(off);
    v ^= lastFlippedBits.containsKey(key) ? lastFlippedBits.get(key) : 0;
    long flippedBits = 0;
    for (int i = len-1; i >= 0; --i) {
      if (choice >= binomial[i][nBit]) {
        v ^= (1l << i);
        flippedBits ^= (1l << i);
        choice -= binomial[i][nBit];
        nBit--;
      }
    }
    lastFlippedBits.put(key, flippedBits);
    int top = frame.getTopPos();
    if (len <= 32) {
      frame.setLocalVariable(top-off, (int)v);
    } else {
      frame.setLongLocalVariable(top-off-1, v);
    }
  }

  /**
   * inject bit flips for parameters annotated with @BitFlip
   * permit to inject bit flips to only one parameter
   */
  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute) {
    if (insnToExecute instanceof JVMInvokeInstruction) {
      MethodInfo mi = ((JVMInvokeInstruction) insnToExecute).getInvokedMethod();
      int idx = -1, offset = -1, nBit = -1;

      byte[] argTypes = mi.getArgumentTypes();
      int n = argTypes.length;
      for (int i = n-1, off = 0; i >= 0; --i) {
        int value = 0;
        AnnotationInfo[] annotations = mi.getParameterAnnotations(i);
        if (annotations != null) {
          for (AnnotationInfo a : annotations) {
            if ("gov.nasa.jpf.annotation.BitFlip".equals(a.getName())) {
              value = a.getValueAsInt("value");
            }
          }
        }
        if (value < 0 || value > 7) {
          throw new JPFException("Invalid number of bits to flip: should be between 1 and 7");
        }
        if (value > 0) {
          idx = i;
          offset = off;
          nBit = value;
          break;
        }
        off += Types.getTypeSize(argTypes[i]);
      }

      if (idx != -1) {
        String key = mi.getFullName() + ":ParameterBitFlip";
        SystemState ss = vm.getSystemState();
        int len = -1;
        switch (argTypes[idx]) {
          case Types.T_LONG:
          case Types.T_DOUBLE:
            len = 64;
            break;
          case Types.T_BOOLEAN:
            len = 1;
            break;
          // cannot handle arrays and object parameters properly now
          case Types.T_ARRAY:
          case Types.T_REFERENCE:
          case Types.T_INT:
          case Types.T_FLOAT:
            len = 32;
            break;
          case Types.T_SHORT:
            len = 16;
            break;
          case Types.T_BYTE:
          case Types.T_CHAR:
            len = 8;
            break;
        }
        if (!ti.isFirstStepInsn()) {
          IntChoiceGenerator cg = new IntIntervalGenerator(key, 0, binomial[len][nBit]-1);
          if (ss.setNextChoiceGenerator(cg)) {
            ti.skipInstruction(insnToExecute);
          }
        }
        else {
          IntChoiceGenerator cg = (IntChoiceGenerator) ss.getChoiceGenerator(key);
          if (cg != null) {
            int choice = cg.getNextChoice();
            bitFlip(ti.getTopFrame(), offset, len, nBit, choice, key);
          }
        }
      }
    }
  }
}
