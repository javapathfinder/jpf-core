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

import gov.nasa.jpf.vm.VM;

/**
 * HeuristicState with a scalar, static priority.
 * Due to legacy reasons, lower values represent higher priorities
 */
public class PrioritizedState extends HeuristicState implements Comparable<PrioritizedState>{

  int heuristicValue; // watch out, this is inverted: 0 is max priority

  public PrioritizedState(VM vm, int heuristicValue) {
    super(vm);
    
    this.heuristicValue = heuristicValue;
  }

  public int getPriority () {
    return heuristicValue;
  }

  /*
   * NOTE - since we can't use the Java 1.6 pollFirst()/pollLast() yet,
   * we have to use remove(o), which in turn requires "compareTo(a) == 0" to
   * be identical to "equals(o) == true", so we should implement both
   */
  @Override
  public int compareTo (PrioritizedState o) {
     int diff = heuristicValue - o.heuristicValue;
     if (diff == 0) {
       return (stateId - o.stateId);
     } else {
       return diff;
     }
  }
  @Override
  public boolean equals (Object o) {
    if (o instanceof PrioritizedState) {
      PrioritizedState other = (PrioritizedState) o;
      return ((stateId == other.stateId) && (heuristicValue == other.heuristicValue));
    } else {
      return false;
    }
  }
  
  @Override
  public String toString() {
    return "{"+stateId+','+heuristicValue+'}';
  }
}
