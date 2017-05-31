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
import gov.nasa.jpf.vm.Path;


/**
 * Heuristic to maximize thread interleavings. It is particularly good at
 * flushing out concurrency errors, since it schedules different threads 
 * as much as possible.
 * 
 */
public class Interleaving extends SimplePriorityHeuristic {
    
  int historyLimit;

  public Interleaving (Config config, VM vm) {
    super(config,vm);
    
    historyLimit = config.getInt("search.heuristic.thread_history_limit", -1);
  }

  /*
   * heuristic based on how often, how long ago, and within how many
   * live threads a certain thread did run
   */
  @Override
  protected int computeHeuristicValue () {
    int aliveThreads = vm.getThreadList().getMatchingCount(aliveThread);

    Path path = vm.getPath();
    int  pathSize = path.size();
    
    int tid = vm.getCurrentThread().getId();
    int h_value = 0;

    if (aliveThreads > 1) { // otherwise there's nothing to interleave
      
      for (int i= Math.max(0, pathSize - historyLimit); i<pathSize; i++) {
        if (path.get(i).getThreadIndex() == tid) {
          h_value += (pathSize - i) * aliveThreads;
        }
      }
    }

    return h_value;
  }
}
