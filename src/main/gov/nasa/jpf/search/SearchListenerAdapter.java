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

/**
 * a no-action SearchListener which we can use to override only the
 * notifications we are interested in
 */
public class SearchListenerAdapter implements SearchListener {

  @Override
  public void stateAdvanced(Search search) {}

  @Override
  public void stateProcessed(Search search) {}

  @Override
  public void stateBacktracked(Search search) {}

  @Override
  public void statePurged(Search search) {}

  @Override
  public void stateStored(Search search) {}

  @Override
  public void stateRestored(Search search) {}

  @Override
  public void searchProbed(Search search) {}
  
  @Override
  public void propertyViolated(Search search) {}

  @Override
  public void searchStarted(Search search) {}

  @Override
  public void searchConstraintHit(Search search) {}

  @Override
  public void searchFinished(Search search) {}

}
