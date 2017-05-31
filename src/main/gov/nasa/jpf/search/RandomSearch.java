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
import gov.nasa.jpf.vm.RestorableVMState;


/**
 * this is a straight execution pseudo-search - it doesn't search at
 * all (i.e. it doesn't backtrack), but just behaves like a 'normal' VM,
 * going forward() until there is no next state then it restarts the search 
 * until it hits a certain number of paths executed
 *
 * <2do> this needs to be updated & tested
 */
public class RandomSearch extends Search {
  int path_limit = 0;
  
  public RandomSearch (Config config, VM vm) {
    super(config, vm);
    
    path_limit = config.getInt("search.RandomSearch.path_limit", 0);
  }
  
  @Override
  public void search () {
    int    depth = 0;
    int paths = 0;
    depth++;
    
    if (hasPropertyTermination()) {
      return;
    }
    
    //vm.forward();
    RestorableVMState init_state = vm.getRestorableState();
    
    notifySearchStarted();
    while (!done) {
      if ((depth < depthLimit) && forward()) {
        notifyStateAdvanced();

        if (currentError != null){
          notifyPropertyViolated();

          if (hasPropertyTermination()) {
            return;
          }
        }

        if (isEndState()){
          return;
        }

        depth++;

      } else { // no next state or reached depth limit
        // <2do> we could check for more things here. If the last insn wasn't
        // the main return, or a System.exit() call, we could flag a JPFException
        if (depth >= depthLimit) {
          notifySearchConstraintHit("depth limit reached: " + depthLimit);
        }
        checkPropertyViolation();
        done = (paths >= path_limit);
        paths++;
        System.out.println("paths = " + paths);
        depth = 1;
        vm.restoreState(init_state);
        vm.resetNextCG();
      }
    }
    notifySearchFinished();
  }
}
