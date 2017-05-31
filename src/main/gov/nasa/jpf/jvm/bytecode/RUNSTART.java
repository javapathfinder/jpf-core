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
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * this is an artificial instruction that is automatically prepended to
 * a run()/main() method call.
 * 
 * The main purpose is to have a special instruction marking the beginning
 * of a new thread execution which does not cause CGs or is otherwise subject
 * to execution semantics that change the program state.
 * 
 * For instance, without it we would have to add a new ThreadInfo state to
 * determine if the first instruction within this thread was re-executed or
 * just happens to be the first transition we execute within this thread
 * 
 */
public class RUNSTART extends Instruction implements JVMInstruction {

  public RUNSTART () {
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    // nothing here, we could have used a NOP
    return getNext(ti);
  }

  public static final int OPCODE = 257;

  @Override
  public int getByteCode () {
    return OPCODE;
  }

  @Override
  public boolean isExtendedInstruction() {
    return true;
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
