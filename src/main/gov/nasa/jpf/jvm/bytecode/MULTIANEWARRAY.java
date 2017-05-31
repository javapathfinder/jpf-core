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
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.LoadOnJPFRequired;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;


/**
 * Create new multidimensional array
 * ..., count1, [count2, ...] => ..., arrayref
 */
public class MULTIANEWARRAY extends Instruction implements JVMInstruction {
  protected String type;
  
  protected int dimensions;
  protected int[] arrayLengths;

  public MULTIANEWARRAY (String typeName, int dimensions){
    this.type = Types.getClassNameFromTypeName(typeName);
    this.dimensions = dimensions;
  }

  public static int allocateArray (Heap heap, String type, int[] dim, ThreadInfo ti, int d) {
    int l = dim[d];
    ElementInfo eiArray = heap.newArray(type.substring(d + 1), l, ti);

    if (dim.length > (d + 1)) {
      for (int i = 0; i < l; i++) {
        eiArray.setReferenceElement(i, allocateArray(heap, type, dim, ti, d + 1));
      }
    }

    return eiArray.getObjectRef();
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    String compType = Types.getComponentTerminal(type);

    // resolve the component class first
    if(Types.isReferenceSignature(type)) {
      try {
        ti.resolveReferencedClass(compType);
      } catch(LoadOnJPFRequired lre) {
        return ti.getPC();
      }
    }

    arrayLengths = new int[dimensions];
    StackFrame frame = ti.getModifiableTopFrame();

    for (int i = dimensions - 1; i >= 0; i--) {
      arrayLengths[i] = frame.pop();
    }

    // there is no clinit for array classes, but we still have  to create a class object
    // since its a builtin class, we also don't have to bother with NoClassDefFoundErrors
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(type);
    if (!ci.isRegistered()) {
      ci.registerClass(ti);
      ci.setInitialized();
    }
    
    int arrayRef = allocateArray(ti.getHeap(), type, arrayLengths, ti, 0);

    // put the result (the array reference) on the stack
    frame.pushRef(arrayRef);

    return getNext(ti);
  }

  @Override
  public int getLength() {
    return 4; // opcode, index1, index2, dimensions
  }
  
  @Override
  public int getByteCode () {
    return 0xC5;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  public String getType(){
    return type;
  }
  
  public int getDimensions() {
    return dimensions;
  }
  
  public int getArrayLength (int dimension){
    if (dimension < dimensions && arrayLengths != null){
      return arrayLengths[dimension];
    } else {
      return -1;
    }
  }
  
  @Override
  public void cleanupTransients(){
    arrayLengths= null;
  }
}
