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
 * Heuristic state prioriizer that maximizes number of blocked states. This
 * is a classic heuristic for finding deadlocks, since a deadlock requires 
 * all threads to be blocked.
 */
public class MostBlocked extends SimplePriorityHeuristic {

  public MostBlocked (Config config, VM vm) {
    super(config,vm);
  }

  @Override
  protected int computeHeuristicValue () {
    int alive = vm.getThreadList().getMatchingCount(aliveThread);
    int runnable = vm.getThreadList().getMatchingCount(vm.getTimedoutRunnablePredicate());

    // pcm - the (iSystemState based) condition was "!runnable && alive"
    // the '10000' is just a potential max thread count
    int h_value = (10000 - (alive - runnable));

    return h_value;
  }
}
