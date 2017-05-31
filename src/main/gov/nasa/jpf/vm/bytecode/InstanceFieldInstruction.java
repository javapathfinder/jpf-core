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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * common machine independent type for all instance field access instructions
 */
public abstract class InstanceFieldInstruction extends FieldInstruction {

  protected int lastThis = MJIEnv.NULL;

  protected InstanceFieldInstruction (String fieldName, String classType, String fieldDescriptor){
    super(fieldName, classType, fieldDescriptor);
  }

  public abstract int getObjectSlot (StackFrame frame);
  
  @Override
  public ElementInfo getElementInfo (ThreadInfo ti){
    if (isCompleted(ti)){
      return ti.getElementInfo(lastThis);
    } else {
      return peekElementInfo(ti);
    }
  }
  
  @Override
  public String toPostExecString(){
    StringBuilder sb = new StringBuilder();
    sb.append(getMnemonic());
    sb.append(' ');
    sb.append( getLastElementInfo());
    sb.append('.');
    sb.append(fname);
    
    return sb.toString();
  }

  @Override
  public FieldInfo getFieldInfo () {
    if (fi == null) {
      ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(className);
      if (ci != null) {
        fi = ci.getInstanceField(fname);
      }
    }
    return fi;
  }

  /**
   * NOTE - the return value is *only* valid in a instructionExecuted() context, since
   * the same instruction can be executed from different threads
   */
  public int getLastThis() {
    return lastThis;
  }

  /**
   * since this is based on getLastThis(), the same context restrictions apply
   */
  @Override
  public ElementInfo getLastElementInfo () {
    if (lastThis != MJIEnv.NULL) {
      return VM.getVM().getHeap().get(lastThis); // <2do> remove - should be in clients
    }

    return null;
  }

  public String getFieldDescriptor () {
    ElementInfo ei = getLastElementInfo();
    FieldInfo fi = getFieldInfo();

    return ei.toString() + '.' + fi.getName();
  }

}
