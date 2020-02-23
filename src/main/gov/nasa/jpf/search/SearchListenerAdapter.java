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
 * The {@code SearchListenerAdapter} abstract class instantiates the methods from {@code SearchListener} in order to create an adapter design pattern.
 * 
 * <p>Any desired methods will require implementing logic in child classes, however unwanted methods can be left uninstantiated in order to aid in
 * code readability.
 * 
 *  <p>This class is to be used alongside classes that extend {@code Search} class functionality. {@code SearchListenerAdapter} is capable of gauging 
 *  Search attributes through the implemented methods and can receive information such as depth, configured properties, and other important {@code Search}
 *  attributes.
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
