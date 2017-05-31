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
import gov.nasa.jpf.jvm.bytecode.DIRECTCALLRETURN;
import gov.nasa.jpf.jvm.bytecode.EXECUTENATIVE;
import gov.nasa.jpf.jvm.bytecode.JVMFieldInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.LockInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.GlobalSchedulingPoint;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * This is a Graphviz dot-file generator similar to SimpleDot. It is useful in
 * the case of Multiprocess applications. It distinguishes local choices from global
 * choices.
 * 
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class DistributedSimpleDot extends SimpleDot {

  static final String MP_START_NODE_ATTRS = "shape=octagon,fillcolor=green";
  static final String MP_NODE_ATTRS = "shape=octagon,fillcolor=azure2";
  
  protected String mpNodeAttrs;
  protected String mpStartNodeAttrs;
  
  protected Instruction insn;
  
  public DistributedSimpleDot (Config config, JPF jpf) {
    super(config, jpf);
    
    mpNodeAttrs = config.getString("dot.mp_node_attr", MP_NODE_ATTRS);
    startNodeAttrs = config.getString("dot.mp_start_node_attr", MP_START_NODE_ATTRS);
  }

  @Override
  public void instructionExecuted(VM vm, ThreadInfo currentThread, Instruction nextInstruction, Instruction executedInstruction) {
    insn = executedInstruction;
  }
  
  @Override
  public void stateAdvanced(Search search){
    int id = search.getStateId();
    long edgeId = ((long)lastId << 32) | id;
    
    String prcId = "P"+Integer.toString(search.getVM().getCurrentApplicationContext().getId());
    if (id <0 || seenEdges.contains(edgeId)){
      return; // skip the root state and property violations (reported separately)
    }
    
    String lastInst = getNextCG();
    String choice =  prcId+"."+getLastChoice();
    
    if (search.isErrorState()) {
      String eid = "e" + search.getNumberOfErrors();
      printTransition(getStateId(lastId), eid, choice, getError(search));
      printErrorState(eid);
      lastErrorId = eid;

    } else if (search.isNewState()) {
      
      if (search.isEndState()) {
        printTransition(getStateId(lastId), getStateId(id), choice, lastInst);
        printEndState(getStateId(id));
      } else {
        printTransition(getStateId(lastId), getStateId(id), choice, lastInst);
        printMultiProcessState(getStateId(id));
      }

    } else { // already visited state
      printTransition(getStateId(lastId), getStateId(id), choice, lastInst);
    }

    seenEdges.add(edgeId);
    lastId = id;
  }
  
  @Override
  protected String getNextCG(){
    if (insn instanceof EXECUTENATIVE) {
      return getNativeExecCG((EXECUTENATIVE)insn);

    } else if (insn instanceof JVMFieldInstruction) { // shared object field access
      return getFieldAccessCG((JVMFieldInstruction)insn);

    } else if (insn instanceof LockInstruction){ // monitor_enter
      return getLockCG((LockInstruction)insn);

    } else if (insn instanceof JVMInvokeInstruction){ // sync method invoke
      return getInvokeCG((JVMInvokeInstruction)insn);
    } else if(insn instanceof DIRECTCALLRETURN && vm.getCurrentThread().getNextPC()==null) {
      return "return";
    }

    return insn.getMnemonic(); // our generic fallback
  }
  
  protected void printMultiProcessState(String stateId){
    if(GlobalSchedulingPoint.isGlobal(vm.getNextChoiceGenerator())) {
      pw.print(stateId);

      pw.print(" [");
      pw.print(mpNodeAttrs);
      pw.print(']');

      pw.println("  // multiprc state");
    }
  }
  
  @Override
  protected String getNativeExecCG (EXECUTENATIVE insn){
    MethodInfo mi = insn.getExecutedMethod();
    String s = mi.getName();

    if (s.equals("start")) {
      s = lastTi.getName() + ".start";
    } else if (s.equals("wait")) {
      s = lastTi.getName() + ".wait";
    }

    return s;
  }
  
  @Override
  protected String getLastChoice() {
    ChoiceGenerator<?> cg = vm.getChoiceGenerator();
    Object choice = cg.getNextChoice();

    if (choice instanceof ThreadInfo){
       return ((ThreadInfo) choice).getName();
    } else {
      return choice.toString();
    }
  }
}
