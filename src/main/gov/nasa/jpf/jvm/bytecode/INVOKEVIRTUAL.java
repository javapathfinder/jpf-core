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
 * Invoke instance method; dispatch based on class
 * ..., objectref, [arg1, [arg2 ...]] => ...
 */
public class INVOKEVIRTUAL extends VirtualInvocation {
  public INVOKEVIRTUAL () {}

  protected INVOKEVIRTUAL (String clsDescriptor, String methodName, String signature){
    super(clsDescriptor, methodName, signature);
  }


  @Override
  public int getByteCode () {
    return 0xB6;
  }
  
  @Override
  public String toString() {
    // methodInfo not set outside real call context (requires target object)
    return "invokevirtual " + cname + '.' + mname;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
