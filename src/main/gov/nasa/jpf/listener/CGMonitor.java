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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.VM;
import java.io.PrintStream;

/**
 * listener to report out what CGs and choices are processed during the search.
 * This is a simple tool to find out about the SUT state space
 */
public class CGMonitor extends ListenerAdapter {
  
  protected PrintStream out;
  
  protected int depth;
  
  // display options
  protected boolean showInsn = false;   // show the insn that caused the CG
  protected boolean showChoice = false; // show the choice value (-> show each CG.advance())
  protected boolean showDepth = true;   // show search depth at point of CG set/advance
  
  public CGMonitor (Config conf) {
    showInsn = conf.getBoolean("cgm.show_insn", showInsn);
    showChoice = conf.getBoolean("cgm.show_choice", showChoice);
    showDepth = conf.getBoolean("cgm.show_depth", showDepth);
    
    out = System.out;
  }
  
  @Override
  public void stateAdvanced (Search search) {
    depth++;
  }
  
  @Override
  public void stateBacktracked (Search search) {
    depth--;
  }
  
  @Override
  public void stateRestored (Search search) {
    depth = search.getDepth();    
  }
  
  void printPrefix(char c) {
    for (int i=0; i<depth; i++) {
      System.out.print(c);
    }
  }
  
  void printCG (ChoiceGenerator<?> cg, boolean printChoice){
    if (showDepth){
      printPrefix('.');
    }
    
    out.print(cg);

    if (printChoice){
      out.print(", ");
      out.print(cg.getNextChoice());
    }

    if (showInsn){
      out.print(", \"");
      out.print(cg.getInsn());
      out.print('\"');
    }

    out.println();    
  }
  
  @Override
  public void choiceGeneratorSet (VM vm, ChoiceGenerator<?> currentCG) {
    if (!showChoice){
      printCG( vm.getChoiceGenerator(), false);
    }
  }
  
  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    if (showChoice){
      printCG( vm.getChoiceGenerator(), true);      
    }
  }

}
