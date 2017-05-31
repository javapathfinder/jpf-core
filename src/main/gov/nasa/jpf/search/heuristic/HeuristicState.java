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
import gov.nasa.jpf.vm.RestorableVMState;


/**
 * wrapper for states that are processed in an order that is
 * defined by a heuristic (i.e. not just depends on the shape
 * of the state graph)
 */
public abstract class HeuristicState {
  
  protected RestorableVMState vmState;
  protected int     stateId;
    
  public HeuristicState (VM vm) {
    stateId = vm.getStateId();
    vmState = vm.getRestorableState();
  }
  
  public RestorableVMState getVMState () {
    return vmState;
  }
  
  public int getStateId() {
    return stateId;
  }
}
