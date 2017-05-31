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
package gov.nasa.jpf;

import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.Printable;
import gov.nasa.jpf.vm.VM;


/**
 * abstraction that is used by Search objects to determine if program
 * properties have been violated (e.g. NoUncaughtExceptions)
 */
public interface Property extends Printable {

  /**
   * return true if property is NOT violated
   */
  boolean check (Search search, VM vm);

  String getErrorMessage ();
  
  String getExplanation();
  
  void reset (); // required for search.multiple_errors
  
  Property clone() throws CloneNotSupportedException; // so that we can store multiple errors
}
