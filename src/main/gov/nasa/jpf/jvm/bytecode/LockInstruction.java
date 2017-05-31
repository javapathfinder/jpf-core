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
import gov.nasa.jpf.vm.MJIEnv;


/**
 * common root for MONITORENTER/EXIT
 */
public abstract class LockInstruction extends Instruction implements JVMInstruction {
  int lastLockRef = MJIEnv.NULL;

  /**
    * only useful post-execution (in an instructionExecuted() notification)
    */
  public int getLastLockRef () {
    return lastLockRef;
  }

  @Override
  public String toPostExecString(){
    StringBuilder sb = new StringBuilder();
    sb.append(getMnemonic());
    sb.append(" @");
    sb.append( Integer.toHexString(lastLockRef));
    
    return sb.toString();
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
