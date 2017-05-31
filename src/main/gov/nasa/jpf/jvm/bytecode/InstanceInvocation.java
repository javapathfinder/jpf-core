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
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.bytecode.InstanceInvokeInstruction;

/**
 * base class for INVOKEVIRTUAL, INVOKESPECIAL and INVOLEINTERFACE
 */
public abstract class InstanceInvocation extends JVMInvokeInstruction implements InstanceInvokeInstruction {

  protected InstanceInvocation() {}

  protected InstanceInvocation (String clsDescriptor, String methodName, String signature){
    super(clsDescriptor, methodName, signature);
  }

  @Override
  public int getArgSize () {
    if (argSize < 0) {
      argSize = Types.getArgumentsSize(signature) + 1; // 'this'
    }

    return argSize;
  }
  
  @Override
  public int getCalleeThis (ThreadInfo ti) {
    if (!ti.isPostExec()){
      // we have to dig out the 'this' reference from the callers stack
      return ti.getCalleeThis( getArgSize());
    } else {
      // enter() cached it
      return lastObj;
    }
  }

  @Override
  public int getObjectSlot (StackFrame frame){
    int top = frame.getTopPos();
    int argSize = getArgSize();
    
    if (argSize == 1){ // object ref is on top
      return top;
      
    } else {
      return top - argSize -1;
    }
  }
  
  public ElementInfo getThisElementInfo (ThreadInfo ti) {
    int thisRef = getCalleeThis(ti);
    if (thisRef != MJIEnv.NULL) {
      return ti.getElementInfo(thisRef);
    } else {
      return null;
    }
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

}
