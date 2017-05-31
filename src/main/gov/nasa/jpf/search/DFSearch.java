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
package gov.nasa.jpf.search;


import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.VM;


/**
 * standard depth first model checking (but can be bounded by search depth
 * and/or explicit Verify.ignoreIf)
 */
public class DFSearch extends Search {

  public DFSearch (Config config, VM vm) {
  	super(config,vm);
  }

  @Override
  public boolean requestBacktrack () {
    doBacktrack = true;

    return true;
  }

  /**
   * state model of the search
   *    next new  -> action
   *     T    T      forward
   *     T    F      backtrack, forward
   *     F    T      backtrack, forward
   *     F    F      backtrack, forward
   *
   * end condition
   *    backtrack failed (no saved states)
   *  | property violation (currently only checked in new states)
   *  | search constraint (depth or memory or time)
   *
   * <2do> we could split the properties into forward and backtrack properties,
   * the latter ones being usable for liveness properties that are basically
   * condition accumulators for sub-paths of the state space, to be checked when
   * we backtrack to the state where they were introduced.
   */
  @Override
  public void search () {
    boolean depthLimitReached = false;

    depth = 0;

    notifySearchStarted();

    while (!done) {
      if (checkAndResetBacktrackRequest() || !isNewState() || isEndState() || isIgnoredState() || depthLimitReached ) {
        if (!backtrack()) { // backtrack not possible, done
          break;
        }

        depthLimitReached = false;
        depth--;
        notifyStateBacktracked();
      }

      if (forward()) {
        depth++;
        notifyStateAdvanced();

        if (currentError != null){
          notifyPropertyViolated();

          if (hasPropertyTermination()) {
            break;
          }
          // for search.multiple_errors we go on and treat this as a new state
          // but hasPropertyTermination() will issue a backtrack request
        }

        if (depth >= depthLimit) {
          depthLimitReached = true;
          notifySearchConstraintHit("depth limit reached: " + depthLimit);
          continue;
        }

        if (!checkStateSpaceLimit()) {
          notifySearchConstraintHit("memory limit reached: " + minFreeMemory);
          // can't go on, we exhausted our memory
          break;
        }

      } else { // forward did not execute any instructions
        notifyStateProcessed();
      }
    }

    notifySearchFinished();
  }


  @Override
  public boolean supportsBacktrack () {
    return true;
  }
}
