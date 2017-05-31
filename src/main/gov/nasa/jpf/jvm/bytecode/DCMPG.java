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
 * Compare double
 * ..., value1, value2 => ..., result
 */
public class DCMPG extends DoubleCompareInstruction {

  @Override
  protected int conditionValue(double v1, double v2) {
    if (Double.isNaN(v1) || Double.isNaN(v2)) {
      return 1;
    } else if (v1 == v2) {
      return 0;
    } else if (v2 > v1) {
      return 1;
    } else {
      return -1;
    }
  }

  @Override
  public int getByteCode () {
    return 0x98;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
