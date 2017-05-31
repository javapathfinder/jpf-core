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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Return void from method
 *   ...  [empty]
 */
public class RETURN extends JVMReturnInstruction {

  @Override
  public Instruction execute (ThreadInfo ti) {

    if (mi.isInit()) {  // Check to see if this method is a constructor.
      int objref = ti.getThis();
      ElementInfo ei = ti.getElementInfo(objref); // Get the object.

      if (!ei.isConstructed()) {  // Don't bother doing the following work if the object is already constructed.

        ClassInfo ei_ci = ei.getClassInfo();  // Get the object's class.
        ClassInfo mi_ci = mi.getClassInfo();  // Get the method's class.

        if (ei_ci == mi_ci) { // If the object's class and the method's class are equal, then the thread is returning from the object's constructor.
          ei = ei.getModifiableInstance();
          ei.setConstructed();
        }
      }

    } else if (mi.isClinit()) {
      // this also needs to happen in NATIVERETURN for native clinits. See comment
      // there why we can't refactor this into DIRECTCALLRETURN
      mi.getClassInfo().setInitialized();
    }

    return super.execute(ti);
  }

  @Override
  public int getReturnTypeSize() {
    return 0;
  }
  
  @Override
  protected Object getReturnedOperandAttr (StackFrame frame) {
    return null;
  }

  
  @Override
  public Object getReturnAttr (ThreadInfo ti){
    return null; // no return value
  }

  @Override
  protected void getAndSaveReturnValue (StackFrame frame) {
    // we don't have any
  }

  @Override
  protected void pushReturnValue (StackFrame frame) {
    // nothing to do
  }

  @Override
  public Object getReturnValue(ThreadInfo ti) {
    //return Void.class; // Hmm, not sure if this is right, but we have to distinguish from ARETURN <null>
    return null;
  }

  @Override
  public String toString() {
    return "return  " + mi.getFullName();
  }

  @Override
  public int getByteCode () {
    return 0xB1;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
