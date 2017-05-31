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
import gov.nasa.jpf.jvm.bytecode.EXECUTENATIVE;
import gov.nasa.jpf.report.ConsolePublisher;
import gov.nasa.jpf.report.Publisher;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.ListIterator;
import java.util.Stack;

/**
 * example of a listener that creates property specific traces. The interesting
 * thing is that it does so without the need to store steps, i.e. it maintains
 * it's own transition stack.
 * this is still work in progress, analyzing the trace can be much more
 * elaborate (we just dump up to a max history size for now)
 * 
 * <2do> DeadlockAnalyzer output can be confusing if a reorganizing
 * ThreadList is used (which reassigns thread ids) 
 */
public class DeadlockAnalyzer extends ListenerAdapter {

  enum OpType { block, lock, unlock, wait, notify, notifyAll, started, terminated };
  static String[] opTypeMnemonic = { "B", "L", "U", "W", "N", "A", "S", "T" };
  
  static class ThreadOp {  
    ThreadInfo ti;
    ElementInfo ei;
    Instruction insn;
    
    // kind of redundant, but there might be context attributes in addition
    // to the insn itself
    OpType opType;
    
    // we could identify this with the insn, but only in case this is
    // a transition boundary, which is far less general than we can be
    int stateId;
    ThreadOp prevTransition;
    ThreadOp prevOp;

    ThreadOp (ThreadInfo ti, ElementInfo ei, OpType type) {
      this.ti = ti;
      this.ei = ei;
      insn = getReportInsn(ti); // we haven't had the executeInsn notification yet
      opType = type;
      
      prevOp = null;
    }

    Instruction getReportInsn(ThreadInfo ti){
      StackFrame frame = ti.getTopFrame();
      if (frame != null) {
        Instruction insn = frame.getPC();
        if (insn instanceof EXECUTENATIVE) {
          frame = frame.getPrevious();
          if (frame != null) {
            insn = frame.getPC();
          }
        }

        return insn;
      } else {
        return null;
      }
    }

    void printLocOn (PrintWriter pw) {
      pw.print(String.format("%6d", new Integer(stateId)));
      
      if (insn != null) {
        pw.print(String.format(" %18.18s ", insn.getMnemonic()));
        pw.print(insn.getFileLocation());
        String line = insn.getSourceLine();
        if (line != null){
          pw.print( " : ");
          pw.print(line.trim());
          //pw.print(insn);
        }
      }
    }
    
    void printOn (PrintWriter pw){
      pw.print( stateId);
      pw.print( " : ");
      pw.print( ti.getName());
      pw.print( " : ");
      pw.print( opType.name());
      pw.print( " : ");
      pw.println(ei);
    }
    
    @Override
	public String toString() {
      StringBuilder sb = new StringBuilder();
      sb.append( stateId);
      sb.append( " : ");
      sb.append( ti.getName());
      sb.append( " : ");
      sb.append( opType.name());
      sb.append( " : ");
      sb.append(ei);
      return sb.toString();
    }
    
    void printColumnOn(PrintWriter pw, Collection<ThreadInfo> tlist){
      for (ThreadInfo t : tlist) {
        if (ti == t) {
          if (opType == OpType.started || opType == OpType.terminated) {
            pw.print(String.format("   %1$s    ", opTypeMnemonic[opType.ordinal()]));
          } else {
            pw.print(String.format("%1$s:%2$-5x ", opTypeMnemonic[opType.ordinal()], ei.getObjectRef()));
          }
          //break;
        } else {
          pw.print("   |    ");
        }
      }      
    }
  }
  
  ThreadOp lastOp;
  ThreadOp lastTransition;
  
  int maxHistory;
  String format;
  
  VM vm;
  Search search;
  
  public DeadlockAnalyzer (Config config, JPF jpf){
    jpf.addPublisherExtension(ConsolePublisher.class, this);
    
    maxHistory = config.getInt("deadlock.max_history", Integer.MAX_VALUE);
    format = config.getString("deadlock.format", "essential");
    
    vm = jpf.getVM();
    search = jpf.getSearch();
  }
  
  boolean requireAllOps() {
    return (format.equalsIgnoreCase("essential"));
  }
  
  void addOp (ThreadInfo ti, ElementInfo ei, OpType opType){
    ThreadOp op = new ThreadOp(ti, ei, opType);
    if (lastOp == null){
      lastOp = op;
    } else {
      assert lastOp.stateId == 0;
      
      op.prevOp = lastOp;
      lastOp = op;
    }
  }
  
  void printRawOps (PrintWriter pw) {
    int i=0;
    
    for (ThreadOp tOp = lastTransition; tOp != null; tOp = tOp.prevTransition){
      for (ThreadOp op = tOp; op != null; op=op.prevOp) {
        if (i++ >= maxHistory){
          pw.println("...");
          return;
        }
        op.printOn(pw);
      }
    }
  }
      
  /**
   * include all threads that are currently blocked or waiting, and
   * all the threads that had the last interaction with them. Note that
   * we do this completely on the basis of the recorded ThreadOps, i.e.
   * don't rely on when this is called
   */
  void printEssentialOps(PrintWriter pw) {
    LinkedHashSet<ThreadInfo> threads = new LinkedHashSet<ThreadInfo>();
    ArrayList<ThreadOp> ops = new ArrayList<ThreadOp>();
    HashMap<ElementInfo,ThreadInfo> waits = new HashMap<ElementInfo,ThreadInfo>();
    HashMap<ElementInfo,ThreadInfo> blocks = new HashMap<ElementInfo,ThreadInfo>();
    HashSet<ThreadInfo> runnables = new HashSet<ThreadInfo>();
    
    // collect all relevant threads and ops
    for (ThreadOp trans = lastTransition; trans != null; trans = trans.prevTransition){
      for (ThreadOp tOp = trans; tOp != null; tOp = tOp.prevOp) {
        OpType ot = tOp.opType;
        ThreadInfo oti = tOp.ti;
        
        if (ot == OpType.wait || ot == OpType.block) {
          if (!runnables.contains(oti) && !threads.contains(oti)){
            HashMap<ElementInfo, ThreadInfo> map = (ot == OpType.block) ? blocks : waits;
            threads.add(oti);
            map.put(tOp.ei, oti);
            ops.add(tOp);
          }
          
        } else if (ot == OpType.notify || ot == OpType.notifyAll || ot == OpType.lock) {
          HashMap<ElementInfo, ThreadInfo> map = (ot == OpType.lock) ? blocks : waits;
          ThreadInfo ti = map.get(tOp.ei);
          
          if (ti != null && ti != oti){
            if (!threads.contains(oti)){
              threads.add(oti);
            }
            map.remove(tOp.ei);
            ops.add(tOp);
          }
          
          runnables.add(oti);

        } else if (ot == OpType.unlock) {
          // not relevant
          runnables.add(oti);
          
        } else if (ot == OpType.terminated || ot == OpType.started) {
          ops.add(tOp); // might be removed later-on
        }
      }
    }

    // remove all starts/terminates of irrelevant threads
    for (ListIterator<ThreadOp> it = ops.listIterator(); it.hasNext(); ){
      ThreadOp tOp = it.next();
      if (tOp.opType == OpType.terminated || tOp.opType == OpType.started) {
        if (!threads.contains(tOp.ti)){
          it.remove();
        }
      }
    }
    
    // now we are ready to print
    printHeader(pw,threads);

    for (ThreadOp tOp : ops) {
      tOp.printColumnOn(pw,threads);
      tOp.printLocOn(pw);
      pw.println();          
    }
  }
    
  
  Collection<ThreadInfo> getThreadList() {
    ArrayList<ThreadInfo> tcol = new ArrayList<ThreadInfo>();
    boolean allOps = requireAllOps();
    int i=0;
    
    prevTrans:
    for (ThreadOp tOp = lastTransition; tOp != null; tOp = tOp.prevTransition){
      i++;
      if (!allOps && (i >= maxHistory)){
        break;
      }
      
      for (ThreadInfo ti : tcol) {
        if (ti == tOp.ti) continue prevTrans;
      }
      tcol.add(tOp.ti);
    }
    
    return tcol;
  }
  
  void printHeader (PrintWriter pw, Collection<ThreadInfo> tlist){
    for (ThreadInfo ti : tlist){
      pw.print(String.format("  %1$2d    ", ti.getId()));
    }
    pw.print(" trans      insn          loc                : stmt");
    pw.println();
        
    for (int i=0; i<tlist.size(); i++){
      pw.print("------- ");
    }
    pw.print("---------------------------------------------------");
    pw.println();
  }

  
  void printColumnOps (PrintWriter pw){
    int i = 0;
    Collection<ThreadInfo> tlist = getThreadList();
    printHeader(pw,tlist);
    
    // and now the data
    for (ThreadOp tOp = lastTransition; tOp != null; tOp = tOp.prevTransition){
      for (ThreadOp op = tOp; op != null; op=op.prevOp) {
        if (i++ >= maxHistory){
          pw.println("...");
          return;
        }
        
        op.printColumnOn(pw,tlist);
        op.printLocOn(pw);
        pw.println();
      }
    }
  }
    
  /**
   * this is the workhorse - filter which ops should be shown, and which
   * are irrelevant for the deadlock
   */
  boolean showOp (ThreadOp op, ThreadInfo[] tlist,
                  boolean[] waitSeen, boolean[] notifySeen,
                  boolean[] blockSeen, boolean[] lockSeen,
                  Stack<ElementInfo>[] unlocked) {
    ThreadInfo ti = op.ti;
    ElementInfo ei = op.ei;
    int idx;
    for (idx=0; idx < tlist.length; idx++){
      if (tlist[idx] == ti) break;
    }
    
    // we could delegate this to the enum type, but let's not be too fancy
    switch (op.opType) {
    case block:
      // only report the last one if thread is blocked
      if (ti.isBlocked()) {
        if (!blockSeen[idx]) {
          blockSeen[idx] = true;
          return true;
        }        
      }
      return false;
    
    case unlock:
      unlocked[idx].push(ei);
      return false;
      
    case lock:
      // if we had a corresponding unlock, ignore
      if (!unlocked[idx].isEmpty() && (unlocked[idx].peek() == ei)) {
        unlocked[idx].pop();
        return false;
      }
      
      // only report the last one if there is a thread that's currently blocked on it
      for (int i=0; i<tlist.length; i++){
        if ((i != idx) && tlist[i].isBlocked() && (tlist[i].getLockObject() == ei)) {
          if (!lockSeen[i]){
            lockSeen[i] = true;
            return true;
          }
        }
      }
      
      return false;
      
    case wait:
      if (ti.isWaiting()){ // only show the last one if this is a waiting thread
        if (!waitSeen[idx]) {
          waitSeen[idx] = true;
          return true;
        }
      }
      
      return false;
      
    case notify:
    case notifyAll:
      // only report the last one if there's a thread waiting on it
      for (int i=0; i<tlist.length; i++){
        if ((i != idx) && tlist[i].isWaiting() && (tlist[i].getLockObject() == ei)) {
          if (!notifySeen[i]) {
            notifySeen[i] = true;
            return true;
          }
        }
      }

      return false;
      
    case started:
    case terminated:
      return true;
    }
    
    return false;
  }

  void storeLastTransition(){
    if (lastOp != null) {
      int stateId = search.getStateId();
      ThreadInfo ti = lastOp.ti;

      for (ThreadOp op = lastOp; op != null; op = op.prevOp) {
        assert op.stateId == 0;

        op.stateId = stateId;
      }

      lastOp.prevTransition = lastTransition;
      lastTransition = lastOp;

      lastOp = null;
    }
  }

  //--- VM listener interface
  
  @Override
  public void objectLocked (VM vm, ThreadInfo ti, ElementInfo ei) {
    addOp(ti, ei, OpType.lock);
  }

  @Override
  public void objectUnlocked (VM vm, ThreadInfo ti, ElementInfo ei) {
    addOp(ti, ei, OpType.unlock);
  }

  @Override
  public void objectWait (VM vm, ThreadInfo ti, ElementInfo ei) {
    addOp(ti, ei, OpType.wait);
  }

  @Override
  public void objectNotify (VM vm, ThreadInfo ti, ElementInfo ei) {
    addOp(ti, ei, OpType.notify);
  }

  @Override
  public void objectNotifyAll (VM vm, ThreadInfo ti, ElementInfo ei) {
    addOp(ti, ei, OpType.notifyAll);
  }

  @Override
  public void threadBlocked (VM vm, ThreadInfo ti, ElementInfo ei){
    addOp(ti, ei, OpType.block);
  }
  
  @Override
  public void threadStarted (VM vm, ThreadInfo ti){
    addOp(ti, null, OpType.started);    
  }
  
  @Override
  public void threadTerminated (VM vm, ThreadInfo ti){
    addOp(ti, null, OpType.terminated);
  }
  
  //--- SearchListener interface

  @Override
  public void stateAdvanced (Search search){
    if (search.isNewState()) {
      storeLastTransition();
    }
  }

  @Override
  public void stateBacktracked (Search search){
    int stateId = search.getStateId();
    while ((lastTransition != null) && (lastTransition.stateId > stateId)){
      lastTransition = lastTransition.prevTransition;
    }
    lastOp = null;
  }
  
  // for HeuristicSearches. Ok, that's braindead but at least no need for cloning
  HashMap<Integer,ThreadOp> storedTransition = new HashMap<Integer,ThreadOp>();
  
  @Override
  public void stateStored (Search search) {
    // always called after stateAdvanced
    storedTransition.put(search.getStateId(), lastTransition);
  }
  
  @Override
  public void stateRestored (Search search) {
    int stateId = search.getStateId();
    ThreadOp op = storedTransition.get(stateId);
    if (op != null) {
      lastTransition = op;
      storedTransition.remove(stateId);  // not strictly required, but we don't come back
    }
  }
  
  @Override
  public void publishPropertyViolation (Publisher publisher) {
    PrintWriter pw = publisher.getOut();
    publisher.publishTopicStart("thread ops " + publisher.getLastErrorId());
    
    if ("column".equalsIgnoreCase(format)){
      printColumnOps(pw);
    } else if ("essential".equalsIgnoreCase(format)) {
      printEssentialOps(pw);
    } else {
      printRawOps(pw);
    }
  }
}
