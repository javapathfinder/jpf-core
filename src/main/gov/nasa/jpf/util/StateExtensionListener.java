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
package gov.nasa.jpf.util;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.SystemState;

/**
 * generic listener that keeps track of state extensions, using
 * state ids as index values into a dynamic array of T objects
 * 
 * the purpose of this utility class is to make state extensions
 * backtrackable, so that clients don't have to care about this
 */
public class StateExtensionListener <T> extends ListenerAdapter {
  StateExtensionClient<T> client;
  DynamicObjectArray<T> states;

  public StateExtensionListener (StateExtensionClient<T> cli) {
    client = cli;
    states = new DynamicObjectArray<T>();

    // set initial state
    T se = client.getStateExtension();
    states.set(0, se);
  }

  @Override
  public void stateAdvanced (Search search) {
    int idx = search.getStateId()+1;
 
    T se = client.getStateExtension();
    states.set(idx, se);
  }

  @Override
  public void stateBacktracked (Search search) {
    int idx = search.getStateId()+1;

    T se = states.get(idx);
    client.restore(se);
  }

  @Override
  public void stateRestored (Search search) {
    int idx = search.getStateId()+1;
 
    T se = states.get(idx);
    client.restore(se);

    SystemState ss = search.getVM().getSystemState();
    ChoiceGenerator<?> cgNext = ss.getNextChoiceGenerator();
    cgNext.reset();
  }
}
