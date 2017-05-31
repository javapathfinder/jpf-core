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
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * a simple heuristic that tries to minimize preemptive scheduling, i.e.
 * switching from a thread that is not blocked.
 * 
 * This is supposed to be a less expensive and more robust version of the old
 * IterativContextBounding search
 */
public class MinimizePreemption extends SimplePriorityHeuristic {
  
  // an optional threshold value of preemptions that cause states to be
  // added at the end of the queue (or discarded if queue is full)
  int threshold;
  
  public MinimizePreemption (Config config, VM vm) {
    super(config,vm);
    
    threshold = config.getInt("search.mp.threshold", Integer.MAX_VALUE);
  }
  
  @Override
  protected int computeHeuristicValue () {
    int preemptions = 0;

    // this is redundant, but since it is easy enough to compute we don't store it
    // <2do> this relies on that there are no cascaded SchedulingPoints (which would not work anyways)
    for (ThreadChoiceGenerator tcg = vm.getLastChoiceGeneratorOfType(ThreadChoiceGenerator.class); tcg != null;){
      ThreadInfo ti= tcg.getNextChoice();
      ThreadChoiceGenerator tcgPrev = tcg.getPreviousChoiceGeneratorOfType(ThreadChoiceGenerator.class);

      if (tcg.isSchedulingPoint()){
        if (tcgPrev != null){
          ThreadInfo tiPrev = tcgPrev.getNextChoice();
          if (ti != tiPrev){
            if (tcg.contains(tiPrev)){
              // the previous thread is still in the runnable list, so it can't be blocked or terminated
              preemptions++;
              
              if (preemptions >= threshold){
                // we don't care, it gets the lowest priority (highest heuristic value)
                return Integer.MAX_VALUE;
              }
            }            
          }
        }
      }
      
      tcg = tcgPrev;
    }
    
    return preemptions;
  }
}
