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


/**
 * Invoke interface method
 * ..., objectref, [arg1, [arg2 ...]] => ...
 */
public class INVOKEINTERFACE extends VirtualInvocation {
  public INVOKEINTERFACE () {}

  protected INVOKEINTERFACE (String clsDescriptor, String methodName, String signature){
    super(clsDescriptor, methodName, signature);
  }


  @Override
  public int getLength() {
    return 5; // opcode, index1, index2, nargs, 0
  }
  
  @Override
  public int getByteCode () {
    return 0xB9;
  }

  @Override
  public String toString() {
    // methodInfo not set outside real call context (requires target object)
    return "invokeinterface " + cname + '.' + mname;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
