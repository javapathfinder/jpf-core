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
import gov.nasa.jpf.jvm.bytecode.ArrayStoreInstruction;
import gov.nasa.jpf.jvm.bytecode.ASTORE;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMLocalVariableInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.jvm.bytecode.LSTORE;
import gov.nasa.jpf.jvm.bytecode.DSTORE;
import gov.nasa.jpf.jvm.bytecode.FSTORE;
import gov.nasa.jpf.jvm.bytecode.ISTORE;
import gov.nasa.jpf.util.FieldSpec;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.MethodSpec;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class BitFlipListener extends ListenerAdapter {

  static JPFLogger log = JPF.getLogger("gov.nasa.jpf.listener.BitFlipListener");

  public static class FieldBitFlip {
    public FieldSpec fieldSpec;
    public int nBit;
    FieldBitFlip (FieldSpec fieldSpec, int nBit) {
      this.fieldSpec = fieldSpec;
      this.nBit = nBit;
    }
  }

  public static class ParamBitFlip {
    public MethodSpec mthSpec;
    public String name;
    public int nBit;
    ParamBitFlip (MethodSpec mthSpec, String name, int nBit) {
      this.mthSpec = mthSpec;
      this.name = name;
      this.nBit = nBit;
    }
  }

  public static class LocalVarBitFlip {
    public MethodSpec mthSpec;
    public String name;
    public int nBit;
    LocalVarBitFlip (MethodSpec mthSpec, String name, int nBit) {
      this.mthSpec = mthSpec;
      this.name = name;
      this.nBit = nBit;
    }
  }

  static HashMap<String, Long> lastFlippedBits;
  static List<FieldBitFlip> fieldWatchList;
  static List<ParamBitFlip> paramWatchList;
  static List<LocalVarBitFlip> localVarWatchList;
  static int[][] binomial;
  static {
    initializeBinomial();
    lastFlippedBits = new HashMap<>();
    fieldWatchList = new ArrayList<>();
    paramWatchList = new ArrayList<>();
    localVarWatchList = new ArrayList<>();
  }

  public BitFlipListener (Config conf) {
    String[] fieldIds = conf.getCompactTrimmedStringArray("bitflip.fields");
    for (String id : fieldIds) {
      addToFieldWatchList(conf, id);
    }

    String[] paramsIds = conf.getCompactTrimmedStringArray("bitflip.params");
    for (String id : paramsIds) {
      addToParamWatchList(conf, id);
    }

    String[] localVarIds = conf.getCompactTrimmedStringArray("bitflip.localvars");
    for (String id : localVarIds) {
      addToLocalVarWatchList(conf, id);
    }
  }

  protected void addToFieldWatchList (Config conf, String id) {
    String keyPrefix = "bitflip." + id;
    String fs = conf.getString(keyPrefix + ".field");
    if (fs != null) {
      FieldSpec fieldSpec = FieldSpec.createFieldSpec(fs);
      if (fieldSpec != null) {
        int nBit = conf.getInt(keyPrefix + ".nbit", 1);
        fieldWatchList.add(new FieldBitFlip(fieldSpec, nBit));
      } else {
        log.warning("malformed field specification for", keyPrefix);
      }
    } else {
      log.warning("missing field specification for ", keyPrefix);
    }
  }

  protected void addToParamWatchList (Config conf, String id) {
    String keyPrefix = "bitflip." + id;
    String ms = conf.getString(keyPrefix + ".method");
    if (ms != null) {
      MethodSpec mthSpec = MethodSpec.createMethodSpec(ms);
      if (mthSpec != null) {
        String name = conf.getString(keyPrefix + ".name");
        if (name != null) {
          int nBit = conf.getInt(keyPrefix + ".nbit", 1);
          paramWatchList.add(new ParamBitFlip(mthSpec, name, nBit));
        } else {
          log.warning("missing parameter name for", keyPrefix);
        }
      } else {
        log.warning("malformed method specification for", keyPrefix);
      }
    } else {
      log.warning("missing method specification for ", keyPrefix);
    }
  }

  protected void addToLocalVarWatchList (Config conf, String id) {
    String keyPrefix = "bitflip." + id;
    String ms = conf.getString(keyPrefix + ".method");
    if (ms != null) {
      MethodSpec mthSpec = MethodSpec.createMethodSpec(ms);
      if (mthSpec != null) {
        String name = conf.getString(keyPrefix + ".name");
        if (name != null) {
          int nBit = conf.getInt(keyPrefix + ".nbit", 1);
          localVarWatchList.add(new LocalVarBitFlip(mthSpec, name, nBit));
        } else {
          log.warning("missing parameter name for", keyPrefix);
        }
      } else {
        log.warning("malformed method specification for", keyPrefix);
      }
    } else {
      log.warning("missing method specification for ", keyPrefix);
    }
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
   * inject bit flips into specified fields, parameters and local variables
   * support both annotations and command line argument
   * if both present, the command line argument supresses the annotation
   */
  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute) {
    // parameters
    if (insnToExecute instanceof JVMInvokeInstruction) {
      MethodInfo mi = ((JVMInvokeInstruction) insnToExecute).getInvokedMethod();
      LocalVarInfo[] args = mi.getArgumentLocalVars();
      if (args == null) return;
      byte[] argTypes = mi.getArgumentTypes();
      for (int i = argTypes.length-1, j = args.length-1, off = 0; i >= 0; --i, --j) {
        int nBit = 0;
        int len = Types.getTypeSizeInBits(argTypes[i]);
        AnnotationInfo[] annotations = mi.getParameterAnnotations(i);
        if (annotations != null) {
          for (AnnotationInfo ai : annotations) {
            if ("gov.nasa.jpf.annotation.BitFlip".equals(ai.getName())) {
              nBit = ai.getValueAsInt("value");
            }
          }
        }
        for (ParamBitFlip pbf : paramWatchList) {
          if (pbf.mthSpec.matches(mi) && pbf.name.equals(args[j].getName())) {
            nBit = pbf.nBit;
          }
        }
        if (nBit < 0 || nBit > 7 || nBit > len) {
          throw new JPFException("Invalid number of bits to flip: should be between 1 and 7, and not exceed type length");
        }
        int size = Types.getTypeSize(argTypes[i]);
        if (nBit > 0) {
          String key = mi.getFullName() + ":ParameterBitFlip";
          flip(vm, ti, insnToExecute, key, off, size, len, nBit);
          break;
        }
        off += size;
      }
    }
    // fields
    else if (insnToExecute instanceof WriteInstruction) {
      FieldInfo fi = ((WriteInstruction) insnToExecute).getFieldInfo();
      int nBit = 0;
      AnnotationInfo ai = fi.getAnnotation("gov.nasa.jpf.annotation.BitFlip");
      if (ai != null) {
        nBit = ai.getValueAsInt("value");
      }
      for (FieldBitFlip fbf : fieldWatchList) {
        if (fbf.fieldSpec.matches(fi)) {
          nBit = fbf.nBit;
        }
      }
      String signature = fi.getSignature();
      int len = Types.getTypeSizeInBits(signature);
      if (nBit < 0 || nBit > 7 || nBit > len) {
        throw new JPFException("Invalid number of bits to flip: should be between 1 and 7, and not exceed type length");
      }
      if (nBit > 0) {
        String key = fi.getFullName() + ":FieldBitFlip";
        int size = Types.getTypeSize(signature);
        flip(vm, ti, insnToExecute, key, 0, size, len, nBit);
      }
    }
    // local variables (not including arrays and objects)
    else if (insnToExecute instanceof StoreInstruction && !(insnToExecute instanceof ArrayStoreInstruction)) {
      JVMLocalVariableInstruction insn = (JVMLocalVariableInstruction) insnToExecute;
      LocalVarInfo lv = insn.getLocalVarInfo();
      if (lv == null) return;
      int nBit = 0;
      AnnotationInfo ai = lv.getTypeAnnotation("gov.nasa.jpf.annotation.BitFlip");
      if (ai != null) {
        nBit = ai.getValueAsInt("value");
      }
      for (LocalVarBitFlip lvbp : localVarWatchList) {
        if (lvbp.mthSpec.matches(insn.getMethodInfo()) && lvbp.name.equals(lv.getName())) {
          nBit = lvbp.nBit;
        }
      }
      if (nBit > 0) {
        String signature = lv.getSignature();
        String key = insn.getVariableId() + ":LocalVariableBitFlip";
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
