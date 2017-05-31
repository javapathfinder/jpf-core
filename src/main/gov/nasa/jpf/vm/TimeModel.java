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
package gov.nasa.jpf.vm;

/**
 * interface that encapsulates the mechanism to obtain values for
 * 
 *   System.getCurrentTimeMillis()
 *   System.nanoTime()
 * 
 * calls. Implementors should guarantee the invariant that time values are
 * strictly increasing along any given path, but don't have to backtrack
 * time values in order to achieve uniform time increments along all paths.
 * 
 * Note that implementations have to avoid creating state leaks, i.e.
 * the respective time value storage should not contribute to the state space
 * hashing. If it has to be backtrackable, it either has to be stored on the 
 * native side, or marked as @FilterField
 */
public interface TimeModel {
  
  public long currentTimeMillis();
  public long nanoTime();
}
