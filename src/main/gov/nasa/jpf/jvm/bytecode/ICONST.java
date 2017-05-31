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
 * Push int constant
 * ... => ..., <i>
 */
public class ICONST extends Instruction implements JVMInstruction {
  protected int value;

  public ICONST(int value){
    this.value = value;
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    
    frame.push(value);

    return getNext(ti);
  }

  public int getValue() {
    return value;
  }
  
  @Override
  public int getByteCode () {
    assert ((value >= -1) && (value < 6)) : ("illegal iconst value: " + value);

    switch (value) {
    case -1: return 0x2;
    case 0: return 0x3;
    case 1: return 0x4;
    case 2: return 0x5;
    case 3: return 0x6;
    case 4: return 0x7;
    case 5: return 0x8;
    }
    return 0;
  }
  
  @Override
  public String getMnemonic () {
    String s = "iconst_";
    
    if (value == -1) {
      return s + "m1";
    } else {
      return s + value;
    }
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
