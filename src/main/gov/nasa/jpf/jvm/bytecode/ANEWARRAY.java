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
import gov.nasa.jpf.vm.LoadOnJPFRequired;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;


/**
 * Create new array of reference
 * ..., count => ..., arrayref
 */
public class ANEWARRAY extends NewArrayInstruction {

  public ANEWARRAY (String typeDescriptor){
    type = Types.getTypeSignature(typeDescriptor, true);
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    // resolve the component class first
    String compType = Types.getTypeName(type);
    if(Types.isReferenceSignature(type)) {
      try {
        ti.resolveReferencedClass(compType);
      } catch(LoadOnJPFRequired lre) {
        return ti.getPC();
      }
    }

    // there is no clinit for array classes, but we still have  to create a class object
    // since its a builtin class, we also don't have to bother with NoClassDefFoundErrors
    String clsName = "[" + type;
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);

    if (!ci.isRegistered()) {
      ci.registerClass(ti);
      ci.setInitialized();
    }

    StackFrame frame = ti.getModifiableTopFrame();

    arrayLength = frame.pop();
    if (arrayLength < 0){
      return ti.createAndThrowException("java.lang.NegativeArraySizeException");
    }

    Heap heap = ti.getHeap();
    if (heap.isOutOfMemory()) { // simulate OutOfMemoryError
      return ti.createAndThrowException("java.lang.OutOfMemoryError",
                                        "trying to allocate new " +
                                          Types.getTypeName(type) +
                                        "[" + arrayLength + "]");
    }

    ElementInfo eiArray = heap.newArray(type, arrayLength, ti);
    int aRef = eiArray.getObjectRef();
    
    // pushes the object reference on the top stack frame
    frame.push(aRef, true);
    
    return getNext(ti);
  }

  @Override
  public int getLength () {
    return 3; // opcode, index1, index2
  }
  
  @Override
  public int getByteCode () {
    return 0xBD;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
