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
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;


/**
 * Push item from runtime constant pool
 * ... => ..., value
 */
public class LDC extends Instruction implements JVMInstruction {

  public enum Type {STRING, CLASS, INT, FLOAT};

  Type type;

  protected String  string;  // the string value if Type.STRING, classname if Type.CLASS
  protected int     value;

  public LDC() {}

  public LDC (String s, boolean isClass){
    if (isClass){
      string = Types.getClassNameFromTypeName(s);
      type = Type.CLASS;
    } else {
      string = s;
      type = Type.STRING;
    }
  }

  public LDC (int v){
    value = v;
    type = Type.INT;
  }

  public LDC (float f){
    value = Float.floatToIntBits(f);
    type = Type.FLOAT;
  }


  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    
    switch (type){
      case STRING:
        // too bad we can't cache it, since location might change between different paths
        ElementInfo eiValue = ti.getHeap().newInternString(string, ti); 
        value = eiValue.getObjectRef();
        frame.pushRef(value);
        break;

      case INT:
      case FLOAT:
        frame.push(value);
        break;

      case CLASS:
        ClassInfo ci;
        // resolve the referenced class
        try {
          ci = ti.resolveReferencedClass(string);
        } catch(LoadOnJPFRequired lre) {
          return frame.getPC();
        }

        // LDC doesn't cause a <clinit> - we only register all required classes
        // to make sure we have class objects. <clinit>s are called prior to
        // GET/PUT or INVOKE
        if (!ci.isRegistered()) {
          ci.registerClass(ti);
        }

        frame.pushRef( ci.getClassObjectRef());

        break;
    }
    
    return getNext(ti);
  }

  @Override
  public int getLength() {
    return 2; // opcode, index
  }

  @Override
  public int getByteCode () {
    return 0x12;
  }
  
  public int getValue() {
    return value;
  }
  
  public Type getType() {
    return type;
  }
  
  public boolean isString() {
    return (type == Type.STRING);
  }
  
  public float getFloatValue(){
	  if(type!=Type.FLOAT){
      throw new IllegalStateException();
	  }
    
	  return Float.intBitsToFloat(value);
	}

  public String getStringValue() { // if it is a String (not acquired from the class const pool)
    if (type == Type.STRING) {
      return string;
    } else {
      return null;
    }
  }
  
  public String getClassValue() { // if it is the name of a Class (acquired from the class const pool)
	    if (type == Type.CLASS) {
	      return string;
	    } else {
	      return null;
	    }
	  }

  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
