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

package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;

/**
 * little listener that tries to detect endless while() loops by counting
 * backjumps, breaking transitions if the count exceeds a threshold, and
 * then checking if program states match. If they do, there would be no progress
 * in this thread.
 */
public class EndlessLoopDetector extends IdleFilter {

  boolean foundEndlessLoop = false;

  public EndlessLoopDetector(Config config) {
    super(config);

    action = Action.BREAK;
  }

  @Override
  public void stateAdvanced(Search search) {
    if (brokeTransition && search.isVisitedState()) {
      foundEndlessLoop = true;
    }
  }

  @Override
  public boolean check(Search search, VM vm) {
    return !foundEndlessLoop;
  }

  @Override
  public void reset () {
    foundEndlessLoop = false;
  }
}
