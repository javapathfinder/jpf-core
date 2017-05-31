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

import gov.nasa.jpf.JPFListener;

/**
 * interface to register for notification by the Search object.
 * Observer role in same-name pattern
 */
public interface SearchListener extends JPFListener {
  
  /**
   * got the next state
   * Note - this will be notified before any potential propertyViolated, in which
   * case the currentError will be already set
   */
  void stateAdvanced (Search search);
  
  /**
   * state is fully explored
   */
  void stateProcessed (Search search);
  
  /**
   * state was backtracked one step
   */
  void stateBacktracked (Search search);

  /**
   * some state is not going to appear in any path anymore
   */
  void statePurged (Search search);

  /**
   * somebody stored the state
   */
  void stateStored (Search search);
  
  /**
   * a previously generated state was restored
   * (can be on a completely different path)
   */
  void stateRestored (Search search);
  
  /**
   * there was a probe request, e.g. from a periodical timer
   * note this is called synchronously from within the JPF execution loop
   * (after instruction execution)
   */
  void searchProbed (Search search);
  
  /**
   * JPF encountered a property violation.
   * Note - this is always preceeded by a stateAdvanced
   */
  void propertyViolated (Search search);
  
  /**
   * we get this after we enter the search loop, but BEFORE the first forward
   */
  void searchStarted (Search search);
  
  /**
   * there was some contraint hit in the search, we back out
   * could have been turned into a property, but usually is an attribute of
   * the search, not the application
   */
  void searchConstraintHit (Search search);
  
  /**
   * we're done, either with or without a preceeding error
   */
  void searchFinished (Search search);
}

