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

package gov.nasa.jpf.jvm;

import gov.nasa.jpf.annotation.BitFlip;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.FixedBitSet;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import static gov.nasa.jpf.vm.JPF_gov_nasa_jpf_vm_Verify.*;

/**
 * a stackframe that is used for executing Java bytecode, supporting both
 * locals and an operand stack. This is essentially the JVm stack machine
 * implementation
 */
public class JVMStackFrame extends StackFrame {

  public JVMStackFrame (MethodInfo callee){
    super( callee);
  }
  
  /**
   * creates callerSlots dummy Stackframe for testing of operand/local operations
   * NOTE - TESTING ONLY! this does not have callerSlots MethodInfo
   */
  protected JVMStackFrame (int nLocals, int nOperands){
    super( nLocals, nOperands);
  }
  
  /**
   * this sets up arguments from a bytecode caller 
   */
  protected void setCallArguments (ThreadInfo ti){
    StackFrame caller = ti.getTopFrame();
    MethodInfo miCallee = mi;
    int nArgSlots = miCallee.getArgumentsSize();
    
    if (nArgSlots > 0){
      int[] calleeSlots = slots;
      FixedBitSet calleeRefs = isRef;
      int[] callerSlots = caller.getSlots();
      FixedBitSet callerRefs = caller.getReferenceMap();

      for (int i = 0, j = caller.getTopPos() - nArgSlots + 1; i < nArgSlots; i++, j++) {
        calleeSlots[i] = callerSlots[j];
        if (callerRefs.get(j)) {
          calleeRefs.set(i);
        }
        Object a = caller.getSlotAttr(j);
        if (a != null) {
          setSlotAttr(i, a);
        }
      }

      if (!miCallee.isStatic()) {
        thisRef = calleeSlots[0];
      }

      // inject bit flips for parameters annotated @BitFlip
      byte[] argTypes = mi.getArgumentTypes();
      int n = argTypes.length;
      for (int i = n-1, off = 0; i >= 0; --i) {
        int nBit = 0;
        AnnotationInfo[] annotations = mi.getParameterAnnotations(i);
        if (annotations != null) {
          for (AnnotationInfo a : annotations) {
            if ("gov.nasa.jpf.annotation.BitFlip".equals(a.getName())) {
              System.out.println(a.getName());
              nBit = a.getValueAsInt("value");
            }
          }
        }
        if (nBit < 0 || nBit > 7) {
          throw new JPFException("Invalid number of bits to flip: should be between 1 and 7");
        }
        switch (argTypes[i]) {
          case Types.T_ARRAY:
          case Types.T_REFERENCE:
            // cannot handle arrays and object arguments now
            off++;
            break;
          case Types.T_LONG:
          case Types.T_DOUBLE:
            if (nBit > 0) {
              setLongLocalVariable(top-off-1, getBitFlip__JI__J(ti.getMJIEnv(), 0, peekLong(off), nBit));
            }
            off += 2;
            break;
          case Types.T_BOOLEAN:
            if (nBit > 0) {
              setLocalVariable(top-off, peek(off)^1);
            }
            off++;
            break;
          case Types.T_INT:
          case Types.T_FLOAT:
            if (nBit > 0) {
              setLocalVariable(top-off, getBitFlip__II__I(ti.getMJIEnv(), 0, peek(off), nBit));
            }
            off++;
            break;
          case Types.T_SHORT:
            if (nBit > 0) {
              setLocalVariable(top-off, (int)getBitFlip__SI__S(ti.getMJIEnv(), 0, (short)peek(off), nBit));
            }
            off++;
            break;
          case Types.T_BYTE:
          case Types.T_CHAR:
            if (nBit > 0) {
              setLocalVariable(top-off, (int)getBitFlip__BI__B(ti.getMJIEnv(), 0, (byte)peek(off), nBit));
            }
            off++;
            break;
        }
      }
    }
  }

  @Override
  public void setExceptionReference (int exRef){
    clearOperandStack();
    pushRef( exRef);
  }
  
  //--- these are for setting up arguments from a VM / listener caller

  /*
   * to be used to initialize locals of a stackframe (only required for explicit construction without a caller,
   * otherwise the Stackframe ctor/invoke insn will take care of copying the values from its caller)
   */
  @Override
  public void setArgumentLocal (int idx, int v, Object attr){
    setLocalVariable( idx, v);
    if (attr != null){
      setLocalAttr( idx, attr);
    }
  }
  @Override
  public void setReferenceArgumentLocal (int idx, int ref, Object attr){
    setLocalReferenceVariable( idx, ref);
    if (attr != null){
      setLocalAttr( idx, attr);
    }
  }
  @Override
  public void setLongArgumentLocal (int idx, long v, Object attr){
    setLongLocalVariable( idx, v);
    if (attr != null){
      setLocalAttr( idx, attr);
    }
  }

}
