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
 * interface to encapsulate an ADT (conceptually a set) used to answer if
 * a state has been seen already
 */
public interface StateSet {
  
  static final int UNKNOWN_ID = -1;

  void attach(VM vm);
  
  /**
   * check if a state is already in the set, and add it if not. Answer
   * it's numeric id
   */
  int addCurrent ();
  
  /**
   * how many states already in the set.  also, index of next newly-added state.
   */
  int size ();
}

