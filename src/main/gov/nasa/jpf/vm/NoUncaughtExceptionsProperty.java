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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.GenericProperty;
import gov.nasa.jpf.search.Search;

import java.io.PrintWriter;
import java.io.StringWriter;


/**
 * property class to check for uncaught exceptions
 */
public class NoUncaughtExceptionsProperty extends GenericProperty {
  
  ExceptionInfo uncaughtXi;
  
  public NoUncaughtExceptionsProperty (Config config) {
    uncaughtXi = null;
  }
  
  void setExceptionInfo (ExceptionInfo xi){
    uncaughtXi = xi;
  }
  
  public ExceptionInfo getUncaughtExceptionInfo() {
	  return uncaughtXi;
  }
  
  @Override
  public String getExplanation () {
    // that's pretty self explaining, isn't it?
    return null;
    //return "no uncaught exception";
  }

  @Override
  public String getErrorMessage () {
    if (uncaughtXi != null) {
      StringWriter sw = new StringWriter();
      uncaughtXi.printOn(new PrintWriter(sw));
      return sw.toString();
    }
    
    return null;
  }
  
  @Override
  public void reset() {
    uncaughtXi = null;
  }
  
  @Override
  public boolean check (Search search, VM vm) {
    uncaughtXi = vm.getPendingException();
    return (uncaughtXi == null);
  }

}
