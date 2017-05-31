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

import gov.nasa.jpf.vm.Path;
import gov.nasa.jpf.vm.ThreadList;

/**
 * class used to store property violations (property and path)
 */
public class Error {

  int            id;
  
  Property       property;
  private String errorMessage;
  
  private Path   path;
  ThreadList     threadList;
  
  public Error (int id, Property prop, Path p, ThreadList l) {
    this.id = id;
    property = prop;
    errorMessage = prop.getErrorMessage();    
    path = p; // client has to clone in case we go on
    threadList = l;
  }

  public int getId() {
    return id;
  }
  
  public String getDescription () {
    StringBuilder sb = new StringBuilder();
    sb.append(property.getClass().getName());
    
    String s = property.getExplanation();
    if (s != null) {
      sb.append(" (\"");
      sb.append(s);
      sb.append("\")");
    }
    
    return sb.toString();
  }

  public String getDetails() {
    return errorMessage;
  }
  
  public Path getPath () {
    return path;
  }

  public Property getProperty () {
    return property;
  }
}
