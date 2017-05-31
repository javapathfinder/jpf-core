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
 * Push long constant
 * ... => ..., <l>
 */
public class LCONST extends Instruction implements JVMInstruction {
  protected long value;


  public LCONST(long value){
    this.value = value;
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    
    frame.pushLong(value);
    
    return getNext(ti);
  }

  @Override
  public int getByteCode () {
    if (value == 0) {
      return 0x09;
    } else {
      return 0x0a;
    }
  }
  
  @Override
  public String getMnemonic () {
    if (value == 0) {
      return "lconst_0";
    } else {
      return "lconst_1";
    }    
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  public long getValue() {
	  return value;
  }
  
}
