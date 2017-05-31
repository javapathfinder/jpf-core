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

package gov.nasa.jpf.vm.bytecode;

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.Types;

/**
 * abstract base for all field access instructions
 */
public abstract class FieldInstruction extends Instruction implements ReadOrWriteInstruction {

  protected String fname;
  protected String ftype;
  protected String className;
  protected String varId;

  protected FieldInfo fi; // lazy eval, hence not public

  protected int    size;  // is it a word or a double word field
  protected boolean isReferenceField;

  protected long lastValue;

  protected FieldInstruction (String name, String clsName, String fieldDescriptor){
    fname = name;
    ftype = fieldDescriptor;
    className = Types.getClassNameFromTypeName(clsName);
    isReferenceField = Types.isReferenceSignature(fieldDescriptor);
    size = Types.getTypeSize(fieldDescriptor);
  }

  /**
   * for explicit construction
   */
  public void setField (String fname, String fclsName) {
    this.fname = fname;
    this.className = fclsName;
    if (fclsName.equals("long") || fclsName.equals("double")) {
      this.size = 2;
      this.isReferenceField = false;
    } else {
      this.size = 1;
      if (fclsName.equals("boolean") || fclsName.equals("byte") || fclsName.equals("char") || fclsName.equals("short") || fclsName.equals("int")) {
        this.isReferenceField = false;
      } else {
        this.isReferenceField = true;
      }
    }
  }
  
  public abstract FieldInfo getFieldInfo();
  @Override
  public abstract boolean isRead();
  
  // for use in instructionExecuted() implementations
  public abstract ElementInfo getLastElementInfo();
  
  // for use in executeInstruction implementations
  public abstract ElementInfo peekElementInfo (ThreadInfo ti);
  
  public String getClassName(){
     return className;
  }

  public String getFieldName(){
	  return fname;
  }

  public int getFieldSize() {
    return size;
  }
 
  public boolean isReferenceField () {
    return isReferenceField;
  }
  
  /**
   * only defined in instructionExecuted() notification context
   */
  public long getLastValue() {
    return lastValue;
  }

  public String getVariableId () {
    if (varId == null) {
      varId = className + '.' + fname;
    }
    return varId;
  }

  public String getId (ElementInfo ei) {
    // <2do> - OUTCH, should be optimized (so far, it's only called during reporting)
    if (ei != null){
      return (ei.toString() + '.' + fname);
    } else {
      return ("?." + fname);
    }
  }
  
  @Override
  public String toString() {
    return getMnemonic() + " " + className + '.' + fname;
  }
  
  
  @Override
  public boolean isMonitorEnterPrologue(){
    // override if this insn can be part of a monitorenter code pattern
    return false;
  }
  
}
