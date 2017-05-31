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

import gov.nasa.jpf.util.Attributable;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * we use that to access Instruction methods from xInstruction interfaces
 * 
 * NOTE - this has to be kept in sync with Instruction
 */
public interface InstructionInterface extends Attributable {

  /**
   * this is for cases where we need the Instruction type. Try to use InstructionInterface in clients
   */
  Instruction asInstruction();
  
  int getByteCode();
  boolean isFirstInstruction();
  boolean isBackJump();
  boolean isExtendedInstruction();
  Instruction getNext();
  int getInstructionIndex();
  int getPosition();
  MethodInfo getMethodInfo();
  int getLength();
  Instruction getPrev();
  boolean isCompleted(ThreadInfo ti);
  String getSourceLine();
  String getSourceLocation();
  Instruction execute(ThreadInfo ti);
  String toPostExecString();
  String getMnemonic();
  int getLineNumber();
  String getFileLocation();
  String getFilePos();
  Instruction getNext (ThreadInfo ti);

  
  
  //.. and probably a lot still missing
}
