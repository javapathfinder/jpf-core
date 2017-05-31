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
 * PathSearch is not really a Search object, just a simple 'forward'
 * driver for the VM that loops until there is no next instruction or
 * a property doesn't hold
 * 
 */
public class PathSearch extends Search {
	
  public PathSearch (Config config, VM vm) {
    super(config,vm);    
  }
  
  @Override
  public boolean requestBacktrack () {
    doBacktrack = true;

    return true;
  }

  @Override
  public void search () {
    depth++;

    if (hasPropertyTermination()) {
      return;
    }

    notifySearchStarted();

    while (true) {
      while (doBacktrack) { // might be set by StateListeners

        if (depth > 0) {
          vm.backtrack();
          depth--;

          notifyStateBacktracked();
        }

        doBacktrack = false;
      }

      forward();
      // isVisitedState is never true, because we don't really search, just replay
      notifyStateAdvanced();

      if (currentError != null){
        notifyPropertyViolated();

        if (hasPropertyTermination()) {
          break;
        }
      }

      if (isEndState()) {
        break;
      }

      depth++;
    }

    notifySearchFinished();
  }

  @Override
  public boolean supportsBacktrack () {
    return true;
  }
}
