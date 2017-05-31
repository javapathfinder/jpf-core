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

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.bytecode.ReadInstruction;


/**
 * Fetch field from object
 * ..., objectref => ..., value
 */
public class GETFIELD extends JVMInstanceFieldInstruction implements ReadInstruction {

  public GETFIELD (String fieldName, String classType, String fieldDescriptor){
    super(fieldName, classType, fieldDescriptor);
  }
  
  
  @Override
  public int getObjectSlot (StackFrame frame){
    return frame.getTopPos();
  }
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    int objRef = frame.peek(); // don't pop yet, we might re-enter
    lastThis = objRef;

    //--- check for obvious exceptions
    if (objRef == MJIEnv.NULL) {
      return ti.createAndThrowException("java.lang.NullPointerException",
              "referencing field '" + fname + "' on null object");
    }

    ElementInfo eiFieldOwner = ti.getElementInfo(objRef);
    FieldInfo fieldInfo = getFieldInfo();
    if (fieldInfo == null) {
      return ti.createAndThrowException("java.lang.NoSuchFieldError",
              "referencing field '" + fname + "' in " + eiFieldOwner);
    }

    //--- check for potential transition breaks (be aware everything above gets re-executed)
    Scheduler scheduler = ti.getScheduler();
    if (scheduler.canHaveSharedObjectCG( ti, this, eiFieldOwner, fieldInfo)){
      eiFieldOwner = scheduler.updateObjectSharedness( ti, eiFieldOwner, fieldInfo);
      if (scheduler.setsSharedObjectCG( ti, this, eiFieldOwner, fieldInfo)){
        return this; // re-execute
      }
    }
    
    frame.pop(); // Ok, now we can remove the object ref from the stack
    Object fieldAttr = eiFieldOwner.getFieldAttr(fieldInfo);

    // We could encapsulate the push in ElementInfo, but not the GET, so we keep it at the same level
    if (fieldInfo.getStorageSize() == 1) { // 1 slotter
      int ival = eiFieldOwner.get1SlotField(fieldInfo);
      lastValue = ival;
      
      if (fieldInfo.isReference()){
        frame.pushRef(ival);
        
      } else {
        frame.push(ival);
      }
      
      if (fieldAttr != null) {
        frame.setOperandAttr(fieldAttr);
      }

    } else {  // 2 slotter
      long lval = eiFieldOwner.get2SlotField(fieldInfo);
      lastValue = lval;

      frame.pushLong( lval);
      if (fieldAttr != null) {
        frame.setLongOperandAttr(fieldAttr);
      }
    }

    return getNext(ti);
  }

  @Override
  public ElementInfo peekElementInfo (ThreadInfo ti) {
    StackFrame frame = ti.getTopFrame();
    int objRef = frame.peek();
    ElementInfo ei = ti.getElementInfo(objRef);
    return ei;
  }

  @Override
  public boolean isMonitorEnterPrologue(){
    return GetHelper.isMonitorEnterPrologue(mi, insnIndex);
  }
  
  @Override
  public int getLength() {
    return 3; // opcode, index1, index2
  }

  @Override
  public int getByteCode () {
    return 0xB4;
  }

  @Override
  public boolean isRead() {
    return true;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
