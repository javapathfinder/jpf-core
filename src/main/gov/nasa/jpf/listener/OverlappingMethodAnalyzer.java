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
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * this is a specialized MethodAnalyzer that looks for overlapping method
 * calls on the same object from different threads.
 * 
 * <2do> transition reporting does not work yet
 */
public class OverlappingMethodAnalyzer extends MethodAnalyzer {

  public OverlappingMethodAnalyzer (Config config, JPF jpf){
    super(config,jpf);
  }

  MethodOp getReturnOp (MethodOp op, boolean withinSameThread){
    MethodInfo mi = op.mi;
    int stateId = op.stateId;
    int stackDepth = op.stackDepth;
    ElementInfo ei = op.ei;
    ThreadInfo ti = op.ti;

    for (MethodOp o = op.p; o != null; o = o.p){
      if (withinSameThread && o.ti != ti){
        break;
      }

      if ((o.mi == mi) && (o.ti == ti) && (o.stackDepth == stackDepth) && (o.ei == ei)){
        return o;
      }
    }

    return null;
  }

  // check if there is an open exec from another thread for the same ElementInfo
  boolean isOpenExec (HashMap<ThreadInfo,Deque<MethodOp>> openExecs, MethodOp op){
    ThreadInfo ti = op.ti;
    ElementInfo ei = op.ei;

    for (Map.Entry<ThreadInfo, Deque<MethodOp>> e : openExecs.entrySet()) {
      if (e.getKey() != ti) {
        Deque<MethodOp> s = e.getValue();
        for (Iterator<MethodOp> it = s.descendingIterator(); it.hasNext();) {
          MethodOp o = it.next();
          if (o.ei == ei) {
            return true;
          }
        }
      }
    }

    return false;
  }

  // clean up (if necessary) - both RETURNS and exceptions
  void cleanUpOpenExec (HashMap<ThreadInfo,Deque<MethodOp>> openExecs, MethodOp op){
    ThreadInfo ti = op.ti;
    int stackDepth = op.stackDepth;

    Deque<MethodOp> stack = openExecs.get(ti);
    if (stack != null && !stack.isEmpty()) {
      for (MethodOp o = stack.peek(); o != null && o.stackDepth >= stackDepth; o = stack.peek()) {
        stack.pop();
      }
    }
  }

  void addOpenExec (HashMap<ThreadInfo,Deque<MethodOp>> openExecs, MethodOp op){
    ThreadInfo ti = op.ti;
    Deque<MethodOp> stack = openExecs.get(ti);

    if (stack == null){
      stack = new ArrayDeque<MethodOp>();
      stack.push(op);
      openExecs.put(ti, stack);

    } else {
      stack.push(op);
    }
  }

  @Override
  void printOn (PrintWriter pw) {
    MethodOp start = firstOp;

    HashMap<ThreadInfo,Deque<MethodOp>> openExecs = new HashMap<ThreadInfo,Deque<MethodOp>>();

    int lastStateId  = -1;
    int lastTid = start.ti.getId();

    for (MethodOp op = start; op != null; op = op.p) {

      cleanUpOpenExec(openExecs, op);

      if (op.isMethodEnter()) {  // EXEC or CALL_EXEC
        MethodOp retOp = getReturnOp(op, true);
        if (retOp != null) { // completed, skip
          if (!isOpenExec(openExecs, op)) {
            op = retOp;
            lastStateId = op.stateId;
            continue;
          }
        } else { // this is an open method exec, record it
          addOpenExec(openExecs, op);
        }
      }

      op = consolidateOp(op);
      
      if (showTransition) {
        if (op.stateId != lastStateId) {
          if (lastStateId >= 0){
            pw.print("------------------------------------------ #");
            pw.println(lastStateId);
          }
        }
        lastStateId = op.stateId;
        
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

  MethodOp consolidateOp (MethodOp op){
    for (MethodOp o = op.p; o != null; o = o.p){
      if (showTransition && (o.stateId != op.stateId)){
        break;
      }
      if (o.isSameMethod(op)){
        switch (o.type) {
          case RETURN:
            switch (op.type){
              case CALL_EXECUTE:
                op = o.clone(OpType.CALL_EXEC_RETURN); break;
              case EXECUTE:
                op = o.clone(OpType.EXEC_RETURN); break;
            }
            break;
          case EXEC_RETURN:
            switch (op.type){
              case CALL:
                op = o.clone(OpType.CALL_EXEC_RETURN); break;
            }
            break;
          case CALL_EXECUTE:  // simple loop
            switch (op.type){
              case CALL_EXEC_RETURN:
                op = o;
            }
            break;
        }
      } else {
        break;
      }
    }

    return op;
  }
}
