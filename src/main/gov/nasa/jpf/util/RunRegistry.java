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

import java.util.ArrayList;

/**
 * little helper to enable resetting classes and objects between JPF runs,
 * mostly to avoid memory leaks
 * 
 * reset() has to be called at the beginning of a new run, causing all
 * still registered listeners to be notified. Listeners have to implement
 * their own logic to check for re-initialization, but can use the
 * 'run' timestamp to do so
 */
public class RunRegistry {
  static RunRegistry singleton = new RunRegistry();
  
  ArrayList<RunListener> listeners = new ArrayList<RunListener>();
  long run;
  
  public static RunRegistry getDefaultRegistry() {
    return singleton;
  }
  
  public void addListener (RunListener r) {
    if (!listeners.contains(r)){
      listeners.add(r);
    }
  }
  
  public boolean isRegistered (RunListener r){
    return listeners.contains(r);
  }
  
  public void reset() {
    run = System.currentTimeMillis();
    
    for (RunListener r : listeners){
      r.reset(this);
    }
    
    listeners.clear();
  }
  
  public long getRun() {
    return run;
  }
}
