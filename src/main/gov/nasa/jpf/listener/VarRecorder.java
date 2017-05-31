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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.JVMArrayElementInstruction;
import gov.nasa.jpf.jvm.bytecode.ArrayLoadInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMFieldInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMLocalVariableInstruction;
import gov.nasa.jpf.vm.bytecode.StoreInstruction;
import gov.nasa.jpf.vm.bytecode.LocalVariableInstruction;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.Step;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

import java.util.HashMap;

/**
 * Simple listener tool to record the values of variables as they are accessed.
 * Records the information in Step.setComment() so that when the trace is
 * written the values can be written too.
 */
public class VarRecorder extends ListenerAdapter {

  private final HashMap<ThreadInfo, String> pendingComment = new HashMap<ThreadInfo, String>();

  private final StringSetMatcher includes;
  private final StringSetMatcher excludes;
  private final boolean recordFields;
  private final boolean recordLocals;
  private final boolean recordArrays;

  private ClassInfo lastClass;
  private boolean recordClass;

  public VarRecorder(Config config) {
    includes = StringSetMatcher.getNonEmpty(config.getStringArray("var_recorder.include"));
    excludes = StringSetMatcher.getNonEmpty(config.getStringArray("var_recorder.exclude"));
    recordFields = config.getBoolean("var_recorder.fields", true);
    recordLocals = config.getBoolean("var_recorder.locals", true);
    recordArrays = config.getBoolean("var_recorder.arrays", true);
  }

  @Override
  public void executeInstruction(VM vm, ThreadInfo ti, Instruction insnToExecute) {
    String name, value;
    byte type;

    if (!canRecord(vm, insnToExecute))
      return;

    if (!(insnToExecute instanceof StoreInstruction))
      if (!(insnToExecute instanceof ArrayLoadInstruction))
        return;

    type = getType(ti, insnToExecute);
    name = getName(ti, insnToExecute, type);

    if (insnToExecute instanceof ArrayLoadInstruction) {
      setComment(vm, ti, name, "", "", true);
      saveVariableType(ti, type);
    } else {
      value = getValue(ti, insnToExecute, type);
      setComment(vm, ti, name, " <- ", value, true);
    }
  }

  @Override
  public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
    String name, value;
    byte type;

    if (!canRecord(vm, executedInsn))
      return;

    if (executedInsn instanceof StoreInstruction) {
      name = pendingComment.remove(ti);
      setComment(vm, ti, name, "", "", false);
      return;
    }

    type  = getType(ti, executedInsn);
    value = getValue(ti, executedInsn, type);

    if (executedInsn instanceof ArrayLoadInstruction)
      name = pendingComment.remove(ti);
    else
      name = getName(ti, executedInsn, type);

    if (isArrayReference(vm, ti))
      saveVariableName(ti, name);
    else
      saveVariableType(ti, type);

    setComment(vm, ti, name, " -> ", value, false);
  }

  private final void setComment(VM vm, ThreadInfo ti, String name, String operator, String value, boolean pending) {
    Step step;
    String comment;

    if (name == null)
      return;

    if (value == null)
      return;

    comment = name + operator + value;

    if (pending) {
      pendingComment.put(ti, comment);
    } else {
      step = vm.getLastStep();
      step.setComment(name + operator + value);
    }
  }

  private final boolean canRecord(VM vm, Instruction inst) {
    ClassInfo ci;
    MethodInfo mi;

    if (vm.getLastStep() == null)
      return(false);

    if (!(inst instanceof LocalVariableInstruction))
      if (!(inst instanceof JVMArrayElementInstruction))
        return(false);

    mi   = inst.getMethodInfo();
    if (mi == null)
      return(false);

    ci = mi.getClassInfo();
    if (ci == null)
      return(false);

    if (lastClass != ci) {
      lastClass   = ci;
      recordClass = StringSetMatcher.isMatch(ci.getName(), includes, excludes);
    }

    return(recordClass);
  }

  // <2do> general purpose listeners should not use anonymous attribute types such as String
  
  private final void saveVariableName(ThreadInfo ti, String name) {
    StackFrame frame = ti.getModifiableTopFrame();
    frame.addOperandAttr(name);
  }

  private final void saveVariableType(ThreadInfo ti, byte type) {
    StackFrame frame;
    String str;

    frame = ti.getModifiableTopFrame();
    if (frame.getTopPos() < 0) {
      return;
    }

    str = encodeType(type);
    frame.addOperandAttr(str);
  }

  private final boolean isArrayReference(VM vm, ThreadInfo ti) {
    StackFrame frame = ti.getTopFrame();

    if (frame.getTopPos() < 0) {
      return(false);
    }

    if (!frame.isOperandRef()) {
      return(false);
    }

    int objRef = frame.peek();
    if (objRef == MJIEnv.NULL) {
      return(false);
    }

    ElementInfo ei = ti.getElementInfo(objRef);
    if (ei == null) {
      return(false);
    }

    return(ei.isArray());
  }

  private byte getType(ThreadInfo ti, Instruction inst) {
    StackFrame frame;
    FieldInfo fi;
    String type;

    frame = ti.getTopFrame();
    if ((frame.getTopPos() >= 0) && (frame.isOperandRef())) {
      return (Types.T_REFERENCE);
    }

    type = null;

    if (((recordLocals) && (inst instanceof JVMLocalVariableInstruction))
            || ((recordFields) && (inst instanceof JVMFieldInstruction))) {
      if (inst instanceof JVMLocalVariableInstruction) {
        type = ((JVMLocalVariableInstruction) inst).getLocalVariableType();
      } else {
        fi = ((JVMFieldInstruction) inst).getFieldInfo();
        type = fi.getType();
      }
    }

    if ((recordArrays) && (inst instanceof JVMArrayElementInstruction)) {
      return (getTypeFromInstruction(inst));
    }

    if (type == null) {
      return (Types.T_VOID);
    }

    return (decodeType(type));
  }

  private final static byte getTypeFromInstruction(Instruction inst) {
    if (inst instanceof JVMArrayElementInstruction)
      return(getTypeFromInstruction((JVMArrayElementInstruction) inst));

    return(Types.T_VOID);
  }

  private final static byte getTypeFromInstruction(JVMArrayElementInstruction inst) {
    String name;

    name = inst.getClass().getName();
    name = name.substring(name.lastIndexOf('.') + 1);

    switch (name.charAt(0)) {
      case 'A': return(Types.T_REFERENCE);
      case 'B': return(Types.T_BYTE);      // Could be a boolean but it is better to assume a byte.
      case 'C': return(Types.T_CHAR);
      case 'F': return(Types.T_FLOAT);
      case 'I': return(Types.T_INT);
      case 'S': return(Types.T_SHORT);
      case 'D': return(Types.T_DOUBLE);
      case 'L': return(Types.T_LONG);
    }

    return(Types.T_VOID);
  }

  private final static String encodeType(byte type) {
    switch (type) {
      case Types.T_BYTE:    return("B");
      case Types.T_CHAR:    return("C");
      case Types.T_DOUBLE:  return("D");
      case Types.T_FLOAT:   return("F");
      case Types.T_INT:     return("I");
      case Types.T_LONG:    return("J");
      case Types.T_REFERENCE:  return("L");
      case Types.T_SHORT:   return("S");
      case Types.T_VOID:    return("V");
      case Types.T_BOOLEAN: return("Z");
      case Types.T_ARRAY:   return("[");
    }

    return("?");
  }

  private final static byte decodeType(String type) {
    if (type.charAt(0) == '?'){
      return(Types.T_REFERENCE);
    } else {
      return Types.getBuiltinType(type);
    }
  }

  private String getName(ThreadInfo ti, Instruction inst, byte type) {
    String name;
    int index;
    boolean store;

    if (((recordLocals) && (inst instanceof JVMLocalVariableInstruction)) ||
        ((recordFields) && (inst instanceof JVMFieldInstruction))) {
      name = ((LocalVariableInstruction) inst).getVariableId();
      name = name.substring(name.lastIndexOf('.') + 1);

      return(name);
    }

    if ((recordArrays) && (inst instanceof JVMArrayElementInstruction)) {
      store  = inst instanceof StoreInstruction;
      name   = getArrayName(ti, type, store);
      index  = getArrayIndex(ti, type, store);
      return(name + '[' + index + ']');
    }

    return(null);
  }

  private String getValue(ThreadInfo ti, Instruction inst, byte type) {
    StackFrame frame;
    int lo, hi;

    frame = ti.getTopFrame();

    if (((recordLocals) && (inst instanceof JVMLocalVariableInstruction)) ||
        ((recordFields) && (inst instanceof JVMFieldInstruction)))
    {
       if (frame.getTopPos() < 0)
         return(null);

       lo = frame.peek();
       hi = frame.getTopPos() >= 1 ? frame.peek(1) : 0;

       return(decodeValue(type, lo, hi));
    }

    if ((recordArrays) && (inst instanceof JVMArrayElementInstruction))
      return(getArrayValue(ti, type));

    return(null);
  }

  private String getArrayName(ThreadInfo ti, byte type, boolean store) {
    String attr;
    int offset;

    offset = calcOffset(type, store) + 1;
    // <2do> String is really not a good attribute type to retrieve!
    StackFrame frame = ti.getTopFrame();
    attr   = frame.getOperandAttr( offset, String.class); 

    if (attr != null) {
      return(attr);
    }

    return("?");
  }

  private int getArrayIndex(ThreadInfo ti, byte type, boolean store) {
    int offset;

    offset = calcOffset(type, store);

    return(ti.getTopFrame().peek(offset));
  }

  private final static int calcOffset(byte type, boolean store) {
    if (!store)
      return(0);

    return(Types.getTypeSize(type));
  }

  private String getArrayValue(ThreadInfo ti, byte type) {
    StackFrame frame;
    int lo, hi;

    frame = ti.getTopFrame();
    lo    = frame.peek();
    hi    = frame.getTopPos() >= 1 ? frame.peek(1) : 0;

    return(decodeValue(type, lo, hi));
  }

  private final static String decodeValue(byte type, int lo, int hi) {
    switch (type) {
      case Types.T_ARRAY:   return(null);
      case Types.T_VOID:    return(null);

      case Types.T_BOOLEAN: return(String.valueOf(Types.intToBoolean(lo)));
      case Types.T_BYTE:    return(String.valueOf(lo));
      case Types.T_CHAR:    return(String.valueOf((char) lo));
      case Types.T_DOUBLE:  return(String.valueOf(Types.intsToDouble(lo, hi)));
      case Types.T_FLOAT:   return(String.valueOf(Types.intToFloat(lo)));
      case Types.T_INT:     return(String.valueOf(lo));
      case Types.T_LONG:    return(String.valueOf(Types.intsToLong(lo, hi)));
      case Types.T_SHORT:   return(String.valueOf(lo));

      case Types.T_REFERENCE:
        ElementInfo ei = VM.getVM().getHeap().get(lo);
        if (ei == null)
          return(null);

        ClassInfo ci = ei.getClassInfo();
        if (ci == null)
          return(null);

        if (ci.getName().equals("java.lang.String"))
          return('"' + ei.asString() + '"');

        return(ei.toString());

      default:
        System.err.println("Unknown type: " + type);
        return(null);
     }
  }
}
