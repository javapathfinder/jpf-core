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
import gov.nasa.jpf.util.Predicate;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;

/**
 * a heuristic that is based on static priorities that are determined
 * at state storage time
 */
public abstract class SimplePriorityHeuristic extends HeuristicSearch {

  StaticPriorityQueue queue;
  
  protected Predicate<ThreadInfo> aliveThread;
  
  public SimplePriorityHeuristic (Config config, VM vm) {
    super(config,vm);

    queue = new StaticPriorityQueue(config);
    
    aliveThread = new Predicate<ThreadInfo>() {
      @Override
	public boolean isTrue (ThreadInfo ti) {
        return (ti.isAlive());
      }
    };
    
  }

  protected abstract int computeHeuristicValue ();

  protected int computeAstarPathCost (VM vm) {
    return vm.getPathLength();
  }
  
  @Override
  protected HeuristicState queueCurrentState () {
    int heuristicValue;
    
    if (vm.isInterestingState()) {
      heuristicValue = 0;
    } else if (vm.isBoringState()) {
      heuristicValue = Integer.MAX_VALUE;
      
    } else {
      heuristicValue = computeHeuristicValue();
      
      if (useAstar) {
        // <2do> we probably don't want this for isInteresting/isBoring?
        heuristicValue += computeAstarPathCost(vm);
      }
    }
    
    PrioritizedState hState = new PrioritizedState(vm,heuristicValue);
    
    queue.add(hState);
    
    return hState;
  }
  
  @Override
  protected HeuristicState getNextQueuedState () {
    
    //HeuristicState hState = queue.pollFirst();  // only Java 1.6
    //if (isBeanSearch) { queue.clear(); }
    //return hState;

    if (queue.size() == 0) {                      // the dreaded Java 1.5 version
      return null;
    }
    HeuristicState hState = queue.first();
    
    if (isBeamSearch) {
      queue.clear();
    } else {
      queue.remove(hState);      
    }
    
    return hState;
  }

  @Override
  public int getQueueSize() {
    return queue.size();
  }
  
  @Override
  public boolean isQueueLimitReached() {
    return queue.isQueueLimitReached();
  }
}
