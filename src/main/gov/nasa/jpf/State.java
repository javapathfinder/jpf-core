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
package gov.nasa.jpf;

import gov.nasa.jpf.search.SearchState;
import gov.nasa.jpf.vm.RestorableVMState;

/**
 * abstraction of JPF execution state that can be queried and stored by
 * listeners 
 */
public class State {
  RestorableVMState     vmState;
  SearchState searchState;
  boolean     hasSuccessor; // <2do> pcm - do we really need this?
  boolean     isNew;

  public State (boolean isNew, boolean hasSuccessor, SearchState searchState, 
                RestorableVMState vmState) {
    this.isNew = isNew;
    this.hasSuccessor = hasSuccessor;
    this.searchState = searchState;
    this.vmState = vmState;
  }

  public boolean isNew () {
    return isNew;
  }

  public SearchState getSearchState () {
    return searchState;
  }

  public RestorableVMState getVMState () {
    return vmState;
  }

  public boolean hasSuccessor () {
    return hasSuccessor;
  }

  public void restore () {
  }
}
