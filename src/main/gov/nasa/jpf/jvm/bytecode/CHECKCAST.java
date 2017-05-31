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
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.LoadOnJPFRequired;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;


/**
 * Check whether object is of given type
 * ..., objectref => ..., objectref
 */
public class CHECKCAST extends Instruction implements JVMInstruction {
  String type;

  public CHECKCAST() {} // this is going away

  public CHECKCAST(String typeName){
    type = Types.getClassNameFromTypeName(typeName);
  }

  public String getTypeName() {
    return type;
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getTopFrame();
    int objref = frame.peek();

    if (objref == MJIEnv.NULL) {
       // we can cast 'null' to anything

    } else {
      boolean isValid = false;

      if(Types.isReferenceSignature(type)) {
        String t;
        if(Types.isArray(type)) {
          // retrieve the component terminal
          t = Types.getComponentTerminal(type);
        } else {
          t = type;
        }

        // resolve the referenced class
        try {
          ti.resolveReferencedClass(t);
        } catch(LoadOnJPFRequired lre) {
          return ti.getPC();
        }
      }

      ElementInfo e = ti.getElementInfo(objref);
      ClassInfo eci = e.getClassInfo();

      if (type.charAt(0) == '['){  // cast between array types
        if (eci.isArray()) {
          // check if the element types are compatible
          ClassInfo cci = eci.getComponentClassInfo();
          isValid = cci.isInstanceOf(type.substring(1));
        }

      } else { // non-array types
        isValid = e.getClassInfo().isInstanceOf(type);
      }

      if (!isValid) {
        return ti.createAndThrowException("java.lang.ClassCastException",
                e.getClassInfo().getName() + " cannot be cast to " + type);
      }
    }

    return getNext(ti);
  }


  @Override
  public int getLength() {
    return 3; // opcode, index1, index2
  }
  
  @Override
  public int getByteCode () {
    return 0xC0;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
