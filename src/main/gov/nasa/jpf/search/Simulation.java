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
 * this is a straight execution pseudo-search - it doesn't search at
 * all (i.e. it doesn't backtrack), but just behaves like a 'normal' VM,
 * going forward() until there is no next state
 *
 * <2do> of course it doesn't quite behave like a normal VM, since it
 * doesn't honor thread priorities yet (needs a special scheduler)
 *
 * <2do> it's not really clear to me how this differs from a 'PathSearch'
 * other than using a different scheduler. Looks like there should be just one
 *
 * <2do> this needs to be updated & tested
 *
 */
public class Simulation extends Search {
  
  public Simulation (Config config, VM vm) {
    super(config, vm);
  }

  @Override
  public void search () {
    int    depth = 0;

    depth++;

    if (hasPropertyTermination()) {
      return;
    }

    notifySearchStarted();
    
    while (!done) {
      if (forward()) {

        if (currentError != null){
          notifyPropertyViolated();

          if (hasPropertyTermination()) {
            return;
          }
        }

        depth++;

      } else { // no next state

        // <2do> we could check for more things here. If the last insn wasn't
        // the main return, or a System.exit() call, we could flag a JPFException
        checkPropertyViolation();
        done = true;
      }
    }
    notifySearchFinished();
  }
}
