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
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.InstanceInvocation;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.JVMReturnInstruction;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;

import java.io.PrintWriter;
import java.util.HashMap;


/**
 * analyzes call/execute sequences of methods
 * closely modeled after the DeadlockAnalyzer, i.e. keeps it's own
 * log and doesn't require full instruction trace
 * 
 * <2do> this needs to be refactored with DeadlockAnalyzer - the whole
 * trace mgnt (except of the printing) can be made generic
 */
public class MethodAnalyzer extends ListenerAdapter {
  
  enum OpType { CALL (">  "),                 // invokeX breaks transition (e.g. blocked sync)
                EXECUTE (" - "),              // method entered method after transition break
                CALL_EXECUTE (">- "),         // call & enter within same transition
                RETURN ("  <"),               // method returned
                EXEC_RETURN (" -<"),          // enter & return in consecutive ops
                CALL_EXEC_RETURN (">-<");     // call & enter & return in consecutive ops
    String code;
    OpType (String code){ this.code = code; }
  };

  static class MethodOp {
    OpType type;
    
    ThreadInfo ti;
    ElementInfo ei;
    Instruction insn; // the caller
    MethodInfo mi;    // the callee
    int stackDepth;
    
    // this is used to keep our own trace
    int stateId = 0;
    MethodOp prevTransition;
    MethodOp p;   // prev during execution
    
    MethodOp (OpType type, MethodInfo mi, ThreadInfo ti, ElementInfo ei, int stackDepth){
      this.type = type;
      this.ti = ti;
      this.mi = mi;
      this.ei = ei;
      this.stackDepth = stackDepth;
    }

    MethodOp clone (OpType newType){
      MethodOp op = new MethodOp(newType, mi, ti, ei, stackDepth);
      op.p = p;
      return op;
    }

    boolean isMethodEnter() {
      return (type == OpType.CALL_EXECUTE) || (type == OpType.EXECUTE);
    }

    boolean isSameMethod(MethodOp op) {
      return (mi == op.mi) && (ti == op.ti) && (ei == op.ei) && (stackDepth == op.stackDepth);
    }

    void printOn(PrintWriter pw, MethodAnalyzer analyzer) {
      pw.print(ti.getId());
      pw.print(": ");
      
      pw.print(type.code);
      pw.print(' ');

      if (analyzer.showDepth){
        for (int i = 0; i < stackDepth; i++) {
          pw.print('.');
        }
        pw.print(' ');
      }

      if (!mi.isStatic()){
        if (ei.getClassInfo() != mi.getClassInfo()){ // method is in superclass
          pw.print(mi.getClassName());
          pw.print('<');
          pw.print(ei);
          pw.print('>');
        } else { // method is in concrete object class
          pw.print(ei);
        }
      } else {
        pw.print(mi.getClassName());
      }

      pw.print('.');
      
      pw.print(Types.getDequalifiedMethodSignature(mi.getUniqueName()));
    }
    
    @Override
	public String toString() {
      return "Op {" + ti.getName() + ',' + type.code +
                   ',' + mi.getFullName() + ',' + ei + '}';
    }
  }

  // report options

  StringSetMatcher includes = null; //  means all
  StringSetMatcher excludes = null; //  means none

  int maxHistory;
  String format;
  boolean skipInit;
  boolean showDepth;
  boolean showTransition;
  boolean showCompleted;

  // execution environment

  VM vm;
  Search search;

  OpType opType;
  
  // this is used to keep our own trace
  MethodOp lastOp;
  MethodOp lastTransition;
  boolean isFirstTransition = true;

  // this is set after we call revertAndFlatten during reporting
  // (we can't call revertAndFlatten twice since it is destructive, but
  // we might have to report several times in case we have several publishers)
  MethodOp firstOp = null;
  
  // for HeuristicSearches. Ok, that's braindead but at least no need for cloning
  HashMap<Integer,MethodOp> storedTransition = new HashMap<Integer,MethodOp>();

  
  public MethodAnalyzer (Config config, JPF jpf){
    jpf.addPublisherExtension(ConsolePublisher.class, this);
    
    maxHistory = config.getInt("method.max_history", Integer.MAX_VALUE);
    format = config.getString("method.format", "raw");
    skipInit = config.getBoolean("method.skip_init", true);
    showDepth = config.getBoolean("method.show_depth", false);
    showTransition = config.getBoolean("method.show_transition", false);
    
    includes = StringSetMatcher.getNonEmpty(config.getStringArray("method.include"));
    excludes = StringSetMatcher.getNonEmpty(config.getStringArray("method.exclude"));
    
    vm = jpf.getVM();
    search = jpf.getSearch();
  }


  void addOp (VM vm, OpType opType, MethodInfo mi, ThreadInfo ti, ElementInfo ei, int stackDepth){
    if (!(skipInit && isFirstTransition)) {
      MethodOp op = new MethodOp(opType, mi, ti, ei, stackDepth);
      if (lastOp == null){
        lastOp = op;
      } else {
        op.p = lastOp;
        lastOp = op;
      }
    }
  }

  boolean isAnalyzedMethod (MethodInfo mi){
    if (mi != null){
      String mthName = mi.getFullName();
      return StringSetMatcher.isMatch(mthName, includes, excludes);
    } else {
      return false;
    }
  }

  void printOn (PrintWriter pw) {
    MethodOp start = firstOp;
    int lastStateId  = Integer.MIN_VALUE;
    int transition = skipInit ? 1 : 0;
    int lastTid = start.ti.getId();
    
    for (MethodOp op = start; op != null; op = op.p) {

      if (showTransition) {
        if (op.stateId != lastStateId) {
          lastStateId = op.stateId;
          pw.print("------------------------------------------ #");
          pw.println(transition++);
        }
      } else {
        int tid = op.ti.getId();
        if (tid != lastTid) {
          lastTid = tid;
          pw.println("------------------------------------------");
        }
      }
      
      op.printOn(pw, this);
      pw.println();
    }
  }

  // warning - this rotates pointers in situ, i.e. destroys the original structure
  MethodOp revertAndFlatten (MethodOp start) {

    MethodOp last = null;
    MethodOp prevTransition = start.prevTransition;

    for (MethodOp op = start; op != null;) {
      MethodOp opp = op.p;
      op.p = last;
      
      if (opp == null) {
        if (prevTransition == null) {
          return op;
        } else {
          last = op;
          op = prevTransition;
          prevTransition = op.prevTransition;
        }
      } else {
        last = op;
        op = opp;
      }
    }

    return null;
  }
  
  //--- SearchListener interface
  // <2do> this is the same as DeadlockAnalyzer, except of xxOp type -> refactor
  @Override
  public void stateAdvanced (Search search){
    
    if (search.isNewState() && (lastOp != null)) {
      int stateId = search.getStateId();
      
      for (MethodOp op=lastOp; op != null; op=op.p) {
        op.stateId = stateId;
      }
      
      lastOp.prevTransition = lastTransition;
      lastTransition = lastOp;
    }
    
    lastOp = null;
    isFirstTransition = false;
  }
  
  @Override
  public void stateBacktracked (Search search){
    int stateId = search.getStateId();
    while ((lastTransition != null) && (lastTransition.stateId > stateId)){
      lastTransition = lastTransition.prevTransition;
    }
    lastOp = null;
  }
  
  @Override
  public void stateStored (Search search) {
    // always called after stateAdvanced
    storedTransition.put(search.getStateId(), lastTransition);
  }
  
  @Override
  public void stateRestored (Search search) {
    int stateId = search.getStateId();
    MethodOp op = storedTransition.get(stateId);
    if (op != null) {
      lastTransition = op;
      storedTransition.remove(stateId);  // not strictly required, but we don't come back
    }
  }


  //--- VMlistener interface
  @Override
  public void instructionExecuted (VM vm, ThreadInfo thread, Instruction nextInsn, Instruction executedInsn) {
    ThreadInfo ti;
    MethodInfo mi;
    ElementInfo ei = null;
    
    if (executedInsn instanceof JVMInvokeInstruction) {
      JVMInvokeInstruction call = (JVMInvokeInstruction)executedInsn;
      ti = thread;
      mi = call.getInvokedMethod(ti);
            
      if (isAnalyzedMethod(mi)) {
        OpType type;

        // check if this was actually executed, or is a blocked sync call
        if (ti.getNextPC() == call) { // re-executed -> blocked or overlayed
          type = OpType.CALL;

        } else { // executed
          if (ti.isFirstStepInsn()) {
            type = OpType.EXECUTE;
          } else {
            type = OpType.CALL_EXECUTE;
          }
        }

        if (call instanceof InstanceInvocation) {
          ei = ((InstanceInvocation)call).getThisElementInfo(ti);
        }
        
        addOp(vm,type,mi,ti,ei, ti.getStackDepth());
      }
      
    } else if (executedInsn instanceof JVMReturnInstruction) {
      JVMReturnInstruction ret = (JVMReturnInstruction)executedInsn;
      ti = thread;
      StackFrame frame = ret.getReturnFrame();
      mi = frame.getMethodInfo();

      if (isAnalyzedMethod(mi)) {
        if (!mi.isStatic()) {
          int ref = frame.getThis();
          if (ref != MJIEnv.NULL) {
            ei = ti.getElementInfo(ref);
          }
        }
        
        addOp(vm,OpType.RETURN,mi,ti,ei, ti.getStackDepth()+1); // postExec-> frame already popped
      }
    }
  }
  
  //--- the PubisherExtension part
  @Override
  public void publishPropertyViolation (Publisher publisher) {

    if (firstOp == null && lastTransition != null){ // do this just once
      firstOp = revertAndFlatten(lastTransition);
    }

    if (firstOp == null){
      return;
    }

    PrintWriter pw = publisher.getOut();
    publisher.publishTopicStart("method ops " + publisher.getLastErrorId());


    printOn(pw);
  }
}
