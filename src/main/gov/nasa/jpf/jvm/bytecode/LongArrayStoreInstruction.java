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

import gov.nasa.jpf.vm.ArrayIndexOutOfBoundsExecutiveException;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * absraction for long array stores
 *
 * ... array, index, long-value => ...
 */
public abstract class LongArrayStoreInstruction extends ArrayStoreInstruction {
  protected void setField (ElementInfo e, int index, long value)
                    throws ArrayIndexOutOfBoundsExecutiveException {
    e.checkArrayBounds(index);
    e.setLongElement(index, value);
  }

  @Override
  protected int getElementSize () {
    return 2;
  }

  protected long getValue (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();    
    return frame.popLong();
  }
  
  @Override
  public int peekArrayRef(ThreadInfo ti) {
    return ti.getTopFrame().peek(3);  // ..,ref,idx,long(value)
  }

  @Override
  public int peekIndex(ThreadInfo ti){
    return ti.getTopFrame().peek(2);
  }

  @Override
  public Object  peekArrayAttr (ThreadInfo ti){
    return ti.getTopFrame().getOperandAttr(3);
  }

  @Override
  public Object peekIndexAttr (ThreadInfo ti){
    return ti.getTopFrame().getOperandAttr(2);
  }


  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
