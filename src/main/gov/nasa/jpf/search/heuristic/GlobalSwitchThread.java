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
package gov.nasa.jpf.search.heuristic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.VM;


/**
 * heuristic state prioritizer that tries to minimize re-scheduling
 */
public class GlobalSwitchThread extends SimplePriorityHeuristic {
  private int[] threads;

  public GlobalSwitchThread (Config config, VM vm) {
    super(config, vm);
    
    int threadHistorySize = config.getInt("search.heuristic.thread_history_size", 10);
    
    threads = new int[threadHistorySize];

    for (int i = 0; i < threads.length; i++) {
      threads[i] = -1;
    }
  }

  @Override
  protected int computeHeuristicValue () {
    int aliveThreads = vm.getThreadList().getMatchingCount(aliveThread);

    int lastRun = vm.getLastTransition().getThreadIndex();
    int h_value = 0;

    if (aliveThreads > 1) {
      for (int i = 0; i < threads.length; i++) {
        if (lastRun == threads[i]) {
          h_value += ((threads.length - i) * aliveThreads);
        }
      }
    }

    int temp0 = threads[0];
    int temp1;
    threads[0] = lastRun;

    for (int i = 1; i < threads.length; i++) {
      temp1 = threads[i];
      threads[i] = temp0;
      temp0 = temp1;
    }

    return h_value;
  }
}
