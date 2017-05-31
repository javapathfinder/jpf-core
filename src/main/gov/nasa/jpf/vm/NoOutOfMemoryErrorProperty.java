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

import gov.nasa.jpf.GenericProperty;
import gov.nasa.jpf.search.Search;

/**
 * A property class so that gov.nasa.jpf.JPF can add an error to the 
 * gov.nasa.jpf.search.Search object when JPF catches an OutOfMemoryError.
 */
public class NoOutOfMemoryErrorProperty extends GenericProperty {

  private boolean m_triggered = true;
  
  public NoOutOfMemoryErrorProperty() {
  }
  
  @Override
  public void reset() {
    m_triggered = false;
  }
  
  @Override
  public boolean check(Search search, VM vm) {
    return(m_triggered);
  }
}
