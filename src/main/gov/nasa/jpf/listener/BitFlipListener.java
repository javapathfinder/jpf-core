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
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.jvm.bytecode.ArrayStoreInstruction;
import gov.nasa.jpf.jvm.bytecode.ASTORE;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMLocalVariableInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.jvm.bytecode.LSTORE;
import gov.nasa.jpf.jvm.bytecode.DSTORE;
import gov.nasa.jpf.jvm.bytecode.FSTORE;
import gov.nasa.jpf.jvm.bytecode.ISTORE;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.bytecode.StoreInstruction;
import gov.nasa.jpf.vm.bytecode.WriteInstruction;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.IntChoiceGenerator;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
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

  /**
   * return a long variable representing the bits to flip
   * first flip the last flipped bit back
   */
  public long getBitsToFlip (int len, int nBit, int choice, String key) {
    long bitsToFlip = 0;
    for (int i = len-1; i >= 0; --i) {
      if (choice >= binomial[i][nBit]) {
        bitsToFlip ^= (1l << i);
        choice -= binomial[i][nBit];
        nBit--;
      }
    }
    long tmp = lastFlippedBits.get(key);
    lastFlippedBits.put(key, bitsToFlip);
    bitsToFlip ^= tmp;
    return bitsToFlip;
  }

  public void flip (VM vm, ThreadInfo ti, Instruction insnToExecute, String key, int offset, int size, int len, int nBit) {
    SystemState ss = vm.getSystemState();
    if (!ti.isFirstStepInsn()) {
      IntChoiceGenerator cg = new IntIntervalGenerator(key, 0, binomial[len][nBit]-1);
      lastFlippedBits.put(key, 0l);
      if (ss.setNextChoiceGenerator(cg)) {
        ti.skipInstruction(insnToExecute);
      }
    }
    else {
      IntChoiceGenerator cg = (IntChoiceGenerator) ss.getChoiceGenerator(key);
      if (cg != null) {
        int choice = cg.getNextChoice();
        long bitsToFlip = getBitsToFlip(len, nBit, choice, key);
        ti.getTopFrame().bitFlip(offset, size, bitsToFlip);
        if (!cg.hasMoreChoices()) {
          lastFlippedBits.put(key, 0l);
        }
      }
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
      byte[] argTypes = mi.getArgumentTypes();
      int n = argTypes.length;
      for (int i = n-1, off = 0; i >= 0; --i) {
        int nBit = 0;
        int size = Types.getTypeSize(argTypes[i]);
        int len = Types.getTypeSizeInBits(argTypes[i]);
        AnnotationInfo[] annotations = mi.getParameterAnnotations(i);
        if (annotations != null) {
          for (AnnotationInfo ai : annotations) {
            if ("gov.nasa.jpf.annotation.BitFlip".equals(ai.getName())) {
              nBit = ai.getValueAsInt("value");
            }
          }
        }
        if (nBit < 0 || nBit > 7 || nBit > len) {
          throw new JPFException("Invalid number of bits to flip: should be between 1 and 7, and not exceed type length");
        }
        if (nBit > 0) {
          String key = mi.getFullName() + ":ParameterBitFlip";
          flip(vm, ti, insnToExecute, key, off, size, len, nBit);
          break;
        }
        off += size;
      }
    }
    // local variable annotations not working in JPF!
    else if (insnToExecute instanceof WriteInstruction || insnToExecute instanceof StoreInstruction && !(insnToExecute instanceof ArrayStoreInstruction)) {
      String signature = "", key = "";
      AnnotationInfo ai = null;
      if (insnToExecute instanceof WriteInstruction) {
        FieldInfo fi = ((WriteInstruction) insnToExecute).getFieldInfo();
        signature = fi.getSignature();
        ai = fi.getAnnotation("gov.nasa.jpf.annotation.BitFlip");
        key = fi.getFullName() + ":FieldBitFlip";
      }
      else {
        JVMLocalVariableInstruction insn = (JVMLocalVariableInstruction) insnToExecute;
        LocalVarInfo lv = insn.getLocalVarInfo();
        if (lv == null) return;
        signature = lv.getSignature();
        ai = lv.getTypeAnnotation("gov.nasa.jpf.annotation.BitFlip");
        key = insn.getVariableId() + ":LocalVariableBitFlip";
      }
      if (ai != null) {
        int nBit = ai.getValueAsInt("value");
        int size = Types.getTypeSize(signature);
        int len = Types.getTypeSizeInBits(signature);
        if (nBit < 0 || nBit > 7 || nBit > len) {
          throw new JPFException("Invalid number of bits to flip: should be between 1 and 7, and not exceed type length");
        }
        flip(vm, ti, insnToExecute, key, 0, size, len, nBit);
      }
    }
  }
}
