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
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * Load reference from local variable
 * ... => ..., objectref
 */
public class ALOAD extends JVMLocalVariableInstruction {

  public ALOAD(int index){
    super(index);
  }

  /**
   * for explicit construction
   */
  public void setIndex (int index){
    this.index = index;    
  }
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    
    frame.pushLocal(index);

    return getNext(ti);
  }

  @Override
  public int getLength() {
    if (index > 3){
      return 2; // opcode, index
    } else {
      return 1; // immediate; opcode
    }
  }
  
  @Override
  public int getByteCode () {
    switch (index) {
      case 0: return 0x2a;
      case 1: return 0x2b;
      case 2: return 0x2c;
      case 3: return 0x2d;
    }

    return 0x19;  // ? wide versions
  }
  
  @Override
  public String getBaseMnemonic() {
    return "aload";
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
