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
package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.PropertyListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.ATHROW;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Heap;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * this is a property listener that turns thrown AssertionErrors into
 * property violations before they are caught (i.e. potentially
 * change the stack).
 * Besides serving the purpose of eliminating the "catch(Throwable)" case,
 * it can be used in conjunction with "search.multiple_errors=true" to
 * report assertions but otherwise ignore them and go on searching the
 * same path (otherwise, multiple_errors would cause a backtrack)
 */
public class AssertionProperty extends PropertyListenerAdapter {

  static JPFLogger log = JPF.getLogger("gov.nasa.jpf.listener.AssertionProperty");
  
  boolean goOn;
  String msg;
  
  public AssertionProperty (Config config) {
    goOn = config.getBoolean("ap.go_on",false);
  }
  
  @Override
  public boolean check(Search search, VM vm) {
    return (msg == null);
  }

  @Override
  public String getErrorMessage() {
    return msg;
  }

  protected String getMessage (String details, Instruction insn){
    String s = "failed assertion";
    
    if (details != null){
      s += ": \"";
      s += details;
      s += '"';
    }

    s += " at ";
    s += insn.getSourceLocation();
    
    return s;
  }

  @Override
  public void executeInstruction (VM vm, ThreadInfo ti, Instruction insn){
    
    if (insn instanceof ATHROW) {
      
      Heap heap = vm.getHeap();
      StackFrame frame = ti.getTopFrame();
      int xobjref = frame.peek();
      ElementInfo ei = heap.get(xobjref);
      ClassInfo ci = ei.getClassInfo();
      
      if (ci.getName().equals("java.lang.AssertionError")) {
        int msgref = ei.getReferenceField("detailMessage");
        ElementInfo eiMsg = heap.get(msgref);
        String details = eiMsg != null ? eiMsg.asString() : null;

        // Ok, arm ourselves
        msg = getMessage( details, insn.getNext());
        
        if (goOn) {
          log.warning(msg);

          frame = ti.getModifiableTopFrame();
          frame.pop(); // ensure operand stack integrity (ATHROW pops)
          
          ti.skipInstruction(insn.getNext());

        } else {
          ti.skipInstruction(insn);
          ti.breakTransition("assertion");
        }
      }
    }
  }
  
  @Override
  public void reset() {
    msg = null;
  }
}
