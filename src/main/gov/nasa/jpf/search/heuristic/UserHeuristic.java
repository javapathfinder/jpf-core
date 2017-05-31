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
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.JPF_gov_nasa_jpf_vm_Verify;
import gov.nasa.jpf.vm.VM;


/**
 * heuristic state prioritizer that is controlled by the system under test, which can
 * use Verify.get/set/resetHeuristicSearchValue() to compute priorities
 */
public class UserHeuristic extends SimplePriorityHeuristic {
  public UserHeuristic (Config config, VM vm) {
    super(config, vm);
  }

  @Override
  protected int computeHeuristicValue () {
    return JPF_gov_nasa_jpf_vm_Verify.heuristicSearchValue;
  }
}
