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
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;

/**
 * Listener tool to monitor JPF execution. This class can be used as a drop-in replacement for JPF, which is called by
 * ExecTracker. ExecTracker is mostly a VMListener of 'instructionExecuted' and a SearchListener of 'stateAdvanced' and
 * 'statehBacktracked'
 * 
 * NOTE - the ExecTracker is machine type agnostic
 */

public class ExecTracker extends ListenerAdapter {
  
  @JPFOption(type = "Boolean", key = "et.print_insn", defaultValue = "true", comment = "print executed bytecode instructions") 
  boolean printInsn = true;
  
  @JPFOption(type = "Boolean", key = "et.print_src", defaultValue = "false", comment = "print source lines")
  boolean printSrc = false;
  
  @JPFOption(type = "Boolean", key = "et.print_mth", defaultValue = "false", comment = "print executed method names")
  boolean printMth = false;
  
  @JPFOption(type = "Boolean", key = "et.skip_init", defaultValue = "true", comment = "do not log execution before entering main()")
  boolean skipInit = false;
  
  boolean showShared = false;
  
  PrintWriter out;
  String lastLine;
  MethodInfo lastMi;
  String linePrefix;
  
  boolean skip;
  MethodInfo miMain; // just to make init skipping more efficient
  
  public ExecTracker (Config config) {
    /** @jpfoption et.print_insn : boolean - print executed bytecode instructions (default=true). */
    printInsn = config.getBoolean("et.print_insn", true);

    /** @jpfoption et.print_src : boolean - print source lines (default=false). */
    printSrc = config.getBoolean("et.print_src", false);

    /** @jpfoption et.print_mth : boolean - print executed method names (default=false). */
    printMth = config.getBoolean("et.print_mth", false);

    /** @jpfoption et.skip_init : boolean - do not log execution before entering main() (default=true). */
    skipInit = config.getBoolean("et.skip_init", true);
    
    showShared = config.getBoolean("et.show_shared", true);
    
    if (skipInit) {
      skip = true;
    }
    
    out = new PrintWriter(System.out, true);
  }
  
  /******************************************* SearchListener interface *****/
  
  @Override
  public void stateRestored(Search search) {
    int id = search.getStateId();
    out.println("----------------------------------- [" +
                       search.getDepth() + "] restored: " + id);
  }
    
  //--- the ones we are interested in
  @Override
  public void searchStarted(Search search) {
    out.println("----------------------------------- search started");
    if (skipInit) {
      ThreadInfo tiCurrent = ThreadInfo.getCurrentThread();
      miMain = tiCurrent.getEntryMethod();
      
      out.println("      [skipping static init instructions]");
    }
  }

  @Override
  public void stateAdvanced(Search search) {
    int id = search.getStateId();
    
    out.print("----------------------------------- [" +
                     search.getDepth() + "] forward: " + id);
    if (search.isNewState()) {
      out.print(" new");
    } else {
      out.print(" visited");
    }
    
    if (search.isEndState()) {
      out.print(" end");
    }
    
    out.println();
    
    lastLine = null; // in case we report by source line
    lastMi = null;
    linePrefix = null;
  }

  @Override
  public void stateProcessed (Search search) {
    int id = search.getStateId();
    out.println("----------------------------------- [" +
                       search.getDepth() + "] done: " + id);
  }

  @Override
  public void stateBacktracked(Search search) {
    int id = search.getStateId();

    lastLine = null;
    lastMi = null;

    out.println("----------------------------------- [" +
                       search.getDepth() + "] backtrack: " + id);
  }
  
  @Override
  public void searchFinished(Search search) {
    out.println("----------------------------------- search finished");
  }

  /******************************************* VMListener interface *********/

  @Override
  public void gcEnd(VM vm) {
    out.println("\t\t # garbage collection");
  }

  //--- the ones we are interested in
  @Override
  public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
    
    if (skip) {
      MethodInfo mi = executedInsn.getMethodInfo();
      if (mi == miMain) {
        skip = false; // start recording
      } else {
        return;  // skip
      }
    }

    int nNoSrc = 0;
    
    if (linePrefix == null) {
      linePrefix = Integer.toString( ti.getId()) + " : ";
    }
    
    // that's pretty redundant to what is done in the ConsolePublisher, but we don't want 
    // presentation functionality in Step anymore
    if (printSrc) {
      String line = executedInsn.getSourceLine();
      if (line != null){
        if (nNoSrc > 0) {
          out.println("            [" + nNoSrc + " insn w/o sources]");
        }

        if (!line.equals(lastLine)) {
          out.print("            [");
          out.print(executedInsn.getFileLocation());
          out.print("] : ");
          out.println(line.trim());
        }
        
        nNoSrc = 0;
        
      } else { // no source
        nNoSrc++;
      }
      
      lastLine = line;
    }
    
    if (printInsn) {      
      if (printMth) {
        MethodInfo mi = executedInsn.getMethodInfo();
        if (mi != lastMi){
          ClassInfo mci = mi.getClassInfo();
          out.print("      ");
          if (mci != null) {
            out.print(mci.getName());
            out.print(".");
          }
          out.println(mi.getUniqueName());
          lastMi = mi;
        }
      }
      
      out.print( linePrefix);
      
      out.printf("[%04x]   ", executedInsn.getPosition());
      
      out.println( executedInsn.toPostExecString());
    }
  }

  @Override
  public void threadStarted(VM vm, ThreadInfo ti) {
    out.println( "\t\t # thread started: " + ti.getName() + " index: " + ti.getId());
  }

  @Override
  public void threadTerminated(VM vm, ThreadInfo ti) {
    out.println( "\t\t # thread terminated: " + ti.getName() + " index: " + ti.getId());
  }
  
  @Override
  public void exceptionThrown (VM vm, ThreadInfo ti, ElementInfo ei) {
    MethodInfo mi = ti.getTopFrameMethodInfo();
    out.println("\t\t\t\t # exception: " + ei + " in " + mi);
  }
  
  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    out.println("\t\t # choice: " + currentCG);
    
    //vm.dumpThreadStates();
  }
  
  @Override
  public void objectExposed (VM vm, ThreadInfo currentThread, ElementInfo fieldOwnerObject, ElementInfo exposedObject) {
    if (showShared){
      String msg = "\t\t # exposed " + exposedObject;
      if (fieldOwnerObject != null){
        String ownerStatus = "";
        if (fieldOwnerObject.isShared()){
          ownerStatus = "shared ";
        } else if (fieldOwnerObject.isExposed()){
          ownerStatus = "exposed ";
        }
        
        msg += " through " + ownerStatus + fieldOwnerObject;
      }
      out.println(msg);
    }
  }
  
  @Override
  public void objectShared (VM vm, ThreadInfo currentThread, ElementInfo sharedObject) {
    if (showShared){
      out.println("\t\t # shared " + sharedObject);
    }
  }
  
  
  /****************************************** private stuff ******/

  void filterArgs (String[] args) {
    for (int i=0; i<args.length; i++) {
      if (args[i] != null) {
        if (args[i].equals("-print-lines")) {
          printSrc = true;
          args[i] = null;
        }
      }
    }
  }
}

