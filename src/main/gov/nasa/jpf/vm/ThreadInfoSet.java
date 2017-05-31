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
 * interface to abstract the referencing set of threadinfos per object/class
 * Used to detect shared objects/classes 
 * Instances are created through a configured factory (SharedObjectPolicy)
 * 
 * We abstract the container so that the way we identify threads is not exposed
 * to the client, and implementations can use either ThreadInfo references or
 * global ids.
 */
public interface ThreadInfoSet extends Cloneable {

  /**
   * @return true if the thread wasn't in the set yet and was added
   */
  ThreadInfoSet add (ThreadInfo ti);
  
  ThreadInfoSet remove (ThreadInfo ti);
  
  boolean contains (ThreadInfo ti);
  
  boolean isShared (ThreadInfo ti, ElementInfo ei);
  
  
  boolean hasMultipleLiveThreads ();
  boolean hasMultipleRunnableThreads ();
  
  Memento<ThreadInfoSet> getMemento();
  
  int size();
}
