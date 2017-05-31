/*
 * Copyright (C) 2015, United States Government, as represented by the
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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * artificial bytecode to defer class status changes of classes that don't have
 * a <clinit>(), simulating native VM specific behavior that involves nested locking
 * during class init
 */
public class FINISHCLINIT extends Instruction {

  protected ClassInfo ciInit;

  public FINISHCLINIT (ClassInfo ci){
    ciInit = ci;
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    ciInit.setInitialized();
    return getNext(ti);
  }

  @Override
  public boolean isExtendedInstruction() {
    return true;
  }

  public static final int OPCODE = 262;

  @Override
  public int getByteCode () {
    return OPCODE;
  }
}
