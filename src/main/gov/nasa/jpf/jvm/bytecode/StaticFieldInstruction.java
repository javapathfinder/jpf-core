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
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StaticElementInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.bytecode.FieldInstruction;

/**
 * class to abstract instructions accessing static fields
 */
public abstract class StaticFieldInstruction extends FieldInstruction {

  protected StaticFieldInstruction(String fieldName, String clsDescriptor, String fieldDescriptor){
    super(fieldName, clsDescriptor, fieldDescriptor);
  }

  /**
   * on-demand initialize the ClassInfo and FieldInfo fields. Note that
   * classinfo might not correspond with the static className, but can be one of
   * the super classes. Rather than checking for this on each subsequent access,
   * we get the right one that declares the field here
   */
  protected void initialize() {
    ClassInfo ciRef = mi.getClassInfo().resolveReferencedClass(className);
    
    FieldInfo f = ciRef.getStaticField(fname);
    ClassInfo ciField = f.getClassInfo();
    if (!ciField.isRegistered()){
      // classLoaded listeners might change/remove this field
      ciField.registerClass(ThreadInfo.getCurrentThread());
      f = ciField.getStaticField(fname);
    }
    
    fi = f;
  }

  /**
   * who owns the field?
   * NOTE: this should only be used from a executeInstruction()/instructionExecuted() context
   */
  @Override
  public ElementInfo getElementInfo(ThreadInfo ti){
    return getFieldInfo().getClassInfo().getStaticElementInfo();
  }
  
  @Override
  public String toPostExecString(){
    StringBuilder sb = new StringBuilder();
    sb.append(getMnemonic());
    sb.append(' ');
    sb.append( fi.getFullName());
    
    return sb.toString();
  }

  public ClassInfo getClassInfo() {
    if (fi == null) {
      initialize();
    }
    return fi.getClassInfo();
  }

  @Override
  public FieldInfo getFieldInfo() {
    if (fi == null) {
      initialize();
    }
    return fi;
  }

  /**
   *  that's invariant, as opposed to InstanceFieldInstruction, so it's
   *  not really a peek
   */
  @Override
  public ElementInfo peekElementInfo (ThreadInfo ti) {
    return getLastElementInfo();
  }

  @Override
  public StaticElementInfo getLastElementInfo() {
    return getFieldInfo().getClassInfo().getStaticElementInfo();
  }

  // this can be different than ciField - the field might be in one of its
  // superclasses
  public ClassInfo getLastClassInfo(){
    return getFieldInfo().getClassInfo();
  }

  public String getLastClassName() {
    return getLastClassInfo().getName();
  }

  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction typeSafeClone(MethodInfo mi) {
    StaticFieldInstruction clone = null;

    try {
      clone = (StaticFieldInstruction) super.clone();

      // reset the method that this insn belongs to
      clone.mi = mi;
      clone.fi = null; // ClassInfo is going to be different
      
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return clone;
  }
}

