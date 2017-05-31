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

package gov.nasa.jpf.util.event;

import java.util.HashMap;

/**
 * a forest of named event trees
 *
 * This class mostly exists for the purpose of tree construction, which happens from respective ctors like
 * 
 *  EventForest myForest = new EventForest(){
 *    @Override
 *    protected Event createRoot(){
 *        addRoot("someState",
 *                sequence(
 *                    event(..),
 *                 ...
 *        );
 *
 *        addRoot("someOtherState",
 *                ...
 *
 *        return sequence( ... ); // default tree
 *   }
 * 
 */
public abstract class EventForest extends EventTree {

  protected HashMap<String,Event> rootMap;

  //--- construction

  /**
   *  usually called from createRootEvent()
   */
  public void addRoot (String name, Event nextRoot){
    if (rootMap == null){
      rootMap = new HashMap<>();
    }
    rootMap.put(name, nextRoot);
  }


  //--- accessors

  public Event getRoot (String name) {
    if (rootMap != null) {
      return rootMap.get(name);
    } else {
      return null;
    }
  }
}
