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

import gov.nasa.jpf.vm.NativeMethodInfo;
import gov.nasa.jpf.vm.NativeStackFrame;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

/**
 * a NativeStackFrame used for calling NativeMethods from Java bytecode
 */
public class JVMNativeStackFrame extends NativeStackFrame {

  public JVMNativeStackFrame (NativeMethodInfo callee){
    super(callee);
  }
  
  public void setArguments (ThreadInfo ti){
    StackFrame callerFrame = ti.getTopFrame(); // we are not going to modify it
    NativeMethodInfo nmi = (NativeMethodInfo) mi;
    int      nArgs = nmi.getNumberOfArguments();
    byte[]   argTypes = nmi.getArgumentTypes();

    Object[] a = new Object[nArgs+2];

    int      stackOffset;
    int      i, j, k;
    int      ival;
    long     lval;

    for (i = 0, stackOffset = 0, j = nArgs + 1, k = nArgs - 1;
         i < nArgs;
         i++, j--, k--) {
      switch (argTypes[k]) {
      case Types.T_BOOLEAN:
        ival = callerFrame.peek(stackOffset);
        a[j] = Boolean.valueOf(Types.intToBoolean(ival));

        break;

      case Types.T_BYTE:
        ival = callerFrame.peek(stackOffset);
        a[j] = Byte.valueOf((byte) ival);

        break;

      case Types.T_CHAR:
        ival = callerFrame.peek(stackOffset);
        a[j] = Character.valueOf((char) ival);

        break;

      case Types.T_SHORT:
        ival = callerFrame.peek(stackOffset);
        a[j] = new Short((short) ival);

        break;

      case Types.T_INT:
        ival = callerFrame.peek(stackOffset);
        a[j] = new Integer(ival);

        break;

      case Types.T_LONG:
        lval = callerFrame.peekLong(stackOffset);
        stackOffset++; // 2 stack words
        a[j] = new Long(lval);

        break;

      case Types.T_FLOAT:
        ival = callerFrame.peek(stackOffset);
        a[j] = new Float(Types.intToFloat(ival));

        break;

      case Types.T_DOUBLE:
        lval = callerFrame.peekLong(stackOffset);
        stackOffset++; // 2 stack words
        a[j] = new Double(Types.longToDouble(lval));

        break;

      default:
        // NOTE - we have to store T_REFERENCE as an Integer, because
        // it shows up in our native method as an 'int'
        ival = callerFrame.peek(stackOffset);
        a[j] = new Integer(ival);
      }

      stackOffset++;
    }

    //--- set  our standard MJI header arguments
    a[0] = ti.getMJIEnv();
    
    if (nmi.isStatic()) {
      a[1] = new Integer( nmi.getClassInfo().getClassObjectRef());
    } else {
      int thisRef = callerFrame.getCalleeThis(nmi);
      a[1] = new Integer( thisRef);
      
      setThis(thisRef);
    }

    setArgs(a);
  }
}
