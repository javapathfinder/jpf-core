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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.JPFException;


/**
 * @author flerda
 *
 * <2do> Check usage! If this is used to intercept AIOBX in the target app,
 * this is a BAD example of using exceptions for general control flow.
 * If this is used for internal AIOBX then it should be just a JPFException
 */
@SuppressWarnings("serial")
public class ArrayIndexOutOfBoundsExecutiveException extends JPFException {
  private Instruction i;

  public ArrayIndexOutOfBoundsExecutiveException (Instruction i) {
    super("array index out of bounds");
    this.i = i;
  }

  public ArrayIndexOutOfBoundsExecutiveException (Instruction i, String msg) {
    super(msg);
    this.i = i;
  }

  public Instruction getInstruction () {
    return i;
  }
}
