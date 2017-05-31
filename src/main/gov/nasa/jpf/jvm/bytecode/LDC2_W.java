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
 * Push long or double from runtime constant pool (wide index)
 * ... => ..., value
 */
public class LDC2_W extends Instruction implements JVMInstruction {

  public enum Type {LONG, DOUBLE};

  protected Type type;
  protected long value;

  public LDC2_W(long l){
    value = l;
    type = Type.LONG;
  }

  public LDC2_W(double d){
    value = Double.doubleToLongBits(d);
    type = Type.DOUBLE;
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    
    frame.pushLong(value);
    return getNext(ti);
  }

  @Override
  public int getLength() {
    return 3; // opcode, index1, index2
  }

  @Override
  public int getByteCode () {
    return 0x14;
  }
  
  public Type getType() {
    return type;
  }
  
  public double getDoubleValue(){
	  if(type!=Type.DOUBLE){
		  throw new IllegalStateException();
	  }
    
	  return Double.longBitsToDouble(value);
  }
  
  public long getValue() {
    return value;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
