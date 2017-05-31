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
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;


/**
 * Create new array
 * ..., count => ..., arrayref
 */
public class NEWARRAY extends NewArrayInstruction {

  public NEWARRAY(int typeCode) {
    type = Types.getElementDescriptorOfType(typeCode);
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();

    arrayLength = frame.pop();
    Heap heap = ti.getHeap();

    if (arrayLength < 0){
      return ti.createAndThrowException("java.lang.NegativeArraySizeException");
    }

    // there is no clinit for array classes, but we still have  to create a class object
    // since its a builtin class, we also don't have to bother with NoClassDefFoundErrors
    String clsName = "[" + type;
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);

    if (!ci.isRegistered()) {
      ci.registerClass(ti);
      ci.setInitialized();
    }
   
    if (heap.isOutOfMemory()) { // simulate OutOfMemoryError
      return ti.createAndThrowException("java.lang.OutOfMemoryError",
                                        "trying to allocate new " +
                                          getTypeName() +
                                        "[" + arrayLength + "]");
    }
    
    ElementInfo eiArray = heap.newArray(type, arrayLength, ti);
    int arrayRef = eiArray.getObjectRef();
    
    frame.pushRef(arrayRef);

    return getNext(ti);
  }

  @Override
  public int getLength() {
    return 2; // opcode, atype
  }
  
  @Override
  public int getByteCode () {
    return 0xBC;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append("newarray ");
    sb.append(getTypeName());
    sb.append('[');
    if (arrayLength >=0){
      sb.append(arrayLength);
    }
    sb.append(']');

    return sb.toString();
  }
}
