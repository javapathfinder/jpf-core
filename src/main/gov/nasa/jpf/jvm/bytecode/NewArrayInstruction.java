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

import gov.nasa.jpf.vm.bytecode.NewInstruction;
import gov.nasa.jpf.vm.Types;

public abstract class NewArrayInstruction extends NewInstruction implements JVMInstruction {

  protected String type;
  protected String typeName; // deferred initialization
  
  protected int arrayLength = -1;

  /**
   * this only makes sense post-execution since the array dimension
   * is obtained from the operand stack
   * 
   * @return length of allocated array
   */
  public int getArrayLength(){
    return arrayLength;
  }
  
  public String getType(){
    return type;
  }
  
  public String getTypeName() {
    if (typeName == null){
      typeName = Types.getTypeName(type);
    }
    return typeName;
  }
  
  @Override
  public void cleanupTransients(){
    arrayLength = -1;
  }
}
