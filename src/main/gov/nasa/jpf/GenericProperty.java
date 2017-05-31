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
import gov.nasa.jpf.vm.VM;

import java.io.PrintWriter;

/**
 * generic abstract base class implementing program properties. This is mostly
 * a convenience construct that implements error printout, so that only
 * the check itself has to be provided
 * 
 * <2do> why is this still not an interface ??
 */
public abstract class GenericProperty implements Property, Cloneable {
  @Override
  public abstract boolean check (Search search, VM vm);

  protected GenericProperty () {
    // nothing yet
  }
  
  @Override
  public Property clone() throws CloneNotSupportedException {
    return (Property) super.clone();
  }
  
  @Override
  public String getErrorMessage () {
    return null;
  }

  @Override
  public String getExplanation () {
    return null;
  }
  
  @Override
  public void reset () {
    // nothing to do here, but Property implementors that store
    // stuff have to override (it's called if search.multiple_errors is on)
  }
  
  @Override
  public void printOn (PrintWriter pw) {
    pw.println(getErrorMessage());
  }
}
