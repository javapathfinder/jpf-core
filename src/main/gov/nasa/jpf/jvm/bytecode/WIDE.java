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
 * modifies following insn, no stack manipulation
 * NOTE: transparently handled by BCEL, we should never receive this
 *
 * (1): <iload,fload,aload,lload,dload,istore,fstore,astore,lstore,dstore,ret> indexbyte1 indexbyte2
 * (2): <iinc> indexbyte1 indexbyte2 constbyte1 constbyte2
 *
 */
public class WIDE extends Instruction implements JVMInstruction {

  // would have to be checked and reset by following insn
  public static boolean isWide = false;

  @Override
  public int getByteCode() {
    return 0xc4;
  }

  @Override
  public Instruction execute(ThreadInfo ti) {
    // nothing, BCEL doesn't even pass this on;
    return getNext(ti);
  }

  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }
}
