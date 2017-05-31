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

import gov.nasa.jpf.JPF;

/**
 * generic interface for state extensions that have to be backtracked/restored
 */
public interface StateExtensionClient <T> {
  
  // these are the automatic SearchListener callbacks from the corresponding StateExtensionListener
  T getStateExtension();
  void restore (T stateExtension);

  // this needs to be called somewhere from the app
  void registerListener (JPF jpf);
}
