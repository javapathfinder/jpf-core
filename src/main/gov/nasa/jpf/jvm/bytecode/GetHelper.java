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
import gov.nasa.jpf.vm.MethodInfo;

/**
 * helper class to factor out common GET code
 * 
 * <2do> This is going to be moved into a Java 8 interface with default methods
 */
public class GetHelper {

  /**
   * do a little bytecode pattern analysis on the fly, to find out if a
   * GETFIELD or GETSTATIC is just part of a "..synchronized (obj) {..} .."
   * pattern, which usually translates into the following pattern:
   *   ...
   *   getfield / getstatic
   *   dup
   *   [astore]
   *   monitorenter
   *   ...
   *
   * If it does, there is no need to break the transition since the object
   * reference is not used for anything that can cause violations between
   * the get and the monitorenter.
   *
   * <2do> We might want to extend this in the future to also cover sync on
   * local vars, like "Object o = myField; synchronized(o){..}..", but then
   * the check becomes more expensive since we get interspersed aload/astore
   * insns, and some of the locals could be used outside the sync block. Not
   * sure if it buys much on the bottom line
   *   
   * <2do> this relies on javac code patterns. The dup/astore could
   * lead to subsequent use of the object reference w/o corresponding get/putfield
   * insns (if it's not a volatile), but this access would be either a call
   * or a get/putfield on a share object, i.e. would be checked separately 
   */
  protected static boolean isMonitorEnterPrologue(MethodInfo mi, int insnIndex){
    Instruction[] code = mi.getInstructions();
    int off = insnIndex+1;

    if (off < code.length-3) {
      // we don't reach out further than 3 instructions
      if (code[off] instanceof DUP) {
        off++;

        if (code[off] instanceof ASTORE) {
          off++;
        }

        if (code[off] instanceof MONITORENTER) {
          return true;
        }
      }
    }
    
    return false; // if in doubt, we assume it is not part of a monitorenter code pattern
  }
}
