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
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.ObjVector;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.logging.Logger;


/**
 * This is the simple version of IdleFilter. This one simply breaks all back-edges
 * encountered to make sure JPF's partial-order reduction doesn't add meaningless
 * transitions forever. This is our dual of the cycle-proviso in classic po-reduction theory.
 *
 *  One can set how many back-edges to consider before breaking, but by default it is 1
 *
 */
public class SimpleIdleFilter extends ListenerAdapter {

	  static Logger log = JPF.getLogger("gov.nasa.jpf.listener.SimpleIdleFilter");

	  static class ThreadStat {
	    String tname;

	    int backJumps;

	    int loopStartPc;

	    int loopEndPc;

	    int loopStackDepth;

	    ThreadStat(String tname) {
	      this.tname = tname;
	    }
	  }

	  ObjVector<ThreadStat> threadStats = new ObjVector<ThreadStat>();

	  ThreadStat ts;

	  int maxBackJumps;

	  // ----------------------------------------------------- SearchListener
	  // interface

	  public SimpleIdleFilter(Config config) {
	    maxBackJumps = config.getInt("idle.max_backjumps", 1);
	  }

	  @Override
	  public void stateAdvanced(Search search) {
	    ts.backJumps = 0;
	    ts.loopStackDepth = 0;
	    ts.loopStartPc = ts.loopEndPc = 0;
	  }

	  @Override
	  public void stateBacktracked(Search search) {
	    ts.backJumps = 0;
	    ts.loopStackDepth = 0;
	    ts.loopStartPc = ts.loopEndPc = 0;
	  }

	  // ----------------------------------------------------- VMListener interface
	  @Override
	  public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {

       if (!executedInsn.isBackJump()) {     // Put this test first for a performance optimization.
         return;
       }

	    int tid = ti.getId();
	    ts = threadStats.get(tid);
	    if (ts == null) {
	      ts = new ThreadStat(ti.getName());
	      threadStats.set(tid, ts);
	    }

       ts.backJumps++;

       int loopStackDepth = ti.getStackDepth();
       int loopPc = nextInsn.getPosition();

       if ((loopStackDepth != ts.loopStackDepth) || (loopPc != ts.loopStartPc)) {
         // new loop, reset
         ts.loopStackDepth = loopStackDepth;
         ts.loopStartPc = loopPc;
         ts.loopEndPc = executedInsn.getPosition();
         ts.backJumps = 0;
       } else {
         if (ts.backJumps > maxBackJumps) {
           ti.reschedule("idleFilter"); // this breaks the executePorStep loop
         }
       }
	  }
}
