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
import gov.nasa.jpf.vm.Transition;


/**
 * a heuristic state prioritizer that favors certain threads (specified
 * by thread names during initialization)
 * 
 * <2do> for both efficiency and encapsulation reasons, this should be just
 * a Scheduler policy (so that we don't have to expand all children)
 */
public class PreferThreads extends SimplePriorityHeuristic {
  String[] preferredThreads;

  public PreferThreads (Config config, VM vm) {
    super(config,vm);
    
    preferredThreads = config.getStringArray("search.heuristic.preferredThreads");
  }

  @Override
  protected int computeHeuristicValue () {
    Transition t = vm.getLastTransition();

    if (t == null) {
      return 1;
    }

    String tn = vm.getThreadName();

    for (int i = 0; i < preferredThreads.length; i++) {
      if (tn.equals(preferredThreads[i])) {
        return 0;
      }
    }

    return 1;
  }
}
