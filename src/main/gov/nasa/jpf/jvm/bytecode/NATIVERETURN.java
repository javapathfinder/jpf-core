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
import gov.nasa.jpf.vm.NativeStackFrame;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

/**
 * synthetic return instruction for native method invocations, so that
 * we don't have to do special provisions to copy the caller args in case
 * a post exec listener wants them.
 */
public class NATIVERETURN extends JVMReturnInstruction {

  Object ret;
  Object retAttr;
  Byte retType;

  // this is more simple than a normal JVMReturnInstruction because NativeMethodInfos
  // are not synchronized, and NativeStackFrames are never the first frame in a thread
  @Override
  public Instruction execute (ThreadInfo ti) {
    if (!ti.isFirstStepInsn()) {
      ti.leave();  // takes care of unlocking before potentially creating a CG
      // NativeMethodInfo is never synchronized, so no thread CG here
    }

    StackFrame frame = ti.getModifiableTopFrame();    
    getAndSaveReturnValue(frame);

    // NativeStackFrame can never can be the first stack frame, so no thread CG

    frame = ti.popAndGetModifiableTopFrame();

    // remove args, push return value and continue with next insn
    frame.removeArguments(mi);
    pushReturnValue(frame);

    if (retAttr != null) {
      setReturnAttr(ti, retAttr);
    }

    if (mi.isClinit()) {
      // this is in the clinit RETURN insn for non-MJIs so we have to duplicate here
      // Duplication could be avoided in DIRECTCALLRETURN, but there is no reliable
      // way to check if the direct call did return from a clinit since the corresponding
      // synthetic method could do anything
      mi.getClassInfo().setInitialized();
    }

    return frame.getPC().getNext();
  }

  @Override
  public void cleanupTransients(){
    ret = null;
    retAttr = null;
    returnFrame = null;
  }
  
  @Override
  public boolean isExtendedInstruction() {
    return true;
  }

  public static final int OPCODE = 260;

  @Override
  public int getByteCode () {
    return OPCODE;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  protected void getAndSaveReturnValue (StackFrame frame) {
    // it's got to be a NativeStackFrame since this insn is created by JPF
    NativeStackFrame nativeFrame = (NativeStackFrame)frame;

    returnFrame = nativeFrame;

    ret = nativeFrame.getReturnValue();
    retAttr = nativeFrame.getReturnAttr();
    retType = nativeFrame.getMethodInfo().getReturnTypeCode();
  }

  @Override
  public int getReturnTypeSize() {
    switch (retType) {
    case Types.T_BOOLEAN:
    case Types.T_BYTE:
    case Types.T_CHAR:
    case Types.T_SHORT:
    case Types.T_INT:
    case Types.T_FLOAT:
      return 1;
      
    case Types.T_LONG:
    case Types.T_DOUBLE:
      return 2;

    default:
      return 1;
    }
  }

  // this is only called internally right before we return
  @Override
  protected Object getReturnedOperandAttr (StackFrame frame) {
    return retAttr;
  }

  // <2do> this should use the getResult..() methods of NativeStackFrame
  
  @Override
  protected void pushReturnValue (StackFrame fr) {
    int  ival;
    long lval;
    int  retSize = 1;

    // in case of a return type mismatch, we get a ClassCastException, which
    // is handled in executeMethod() and reported as a InvocationTargetException
    // (not completely accurate, but we rather go with safety)
    if (ret != null) {
      switch (retType) {
      case Types.T_BOOLEAN:
        ival = Types.booleanToInt(((Boolean) ret).booleanValue());
        fr.push(ival);
        break;

      case Types.T_BYTE:
        fr.push(((Byte) ret).byteValue());
        break;

      case Types.T_CHAR:
        fr.push(((Character) ret).charValue());
        break;

      case Types.T_SHORT:
        fr.push(((Short) ret).shortValue());
        break;

      case Types.T_INT:
        fr.push(((Integer) ret).intValue());
        break;

      case Types.T_LONG:
        fr.pushLong(((Long)ret).longValue());
        retSize=2;
        break;

      case Types.T_FLOAT:
        ival = Types.floatToInt(((Float) ret).floatValue());
        fr.push(ival);
        break;

      case Types.T_DOUBLE:
        lval = Types.doubleToLong(((Double) ret).doubleValue());
        fr.pushLong(lval);
        retSize=2;
        break;

      default:
        // everything else is supposed to be a reference
        fr.push(((Integer) ret).intValue(), true);
      }

      if (retAttr != null) {
        if (retSize == 1) {
          fr.setOperandAttr(retAttr);
        } else {
          fr.setLongOperandAttr(retAttr);
        }
      }
    }
  }

  @Override
  public Object getReturnAttr (ThreadInfo ti) {
    if (isCompleted(ti)){
      return retAttr;
    } else {
      NativeStackFrame nativeFrame = (NativeStackFrame) ti.getTopFrame();
      return nativeFrame.getReturnAttr();
    }
  }


  @Override
  public Object getReturnValue(ThreadInfo ti) {
    if (isCompleted(ti)){
      return ret;
    } else {
      NativeStackFrame nativeFrame = (NativeStackFrame) ti.getTopFrame();
      return nativeFrame.getReturnValue();
    }
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append("nativereturn ");
    sb.append(mi.getFullName());

    return sb.toString();
  }

}
