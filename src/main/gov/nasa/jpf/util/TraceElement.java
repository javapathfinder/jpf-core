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

/**
 * encapsulates a listener-managed trace operation
 */
public class TraceElement<T> {
  T op;
  
  int stateId;
  TraceElement<T> prevElement;
  TraceElement<T> prevTransition;
  
  public TraceElement (T op){
    this.op = op;
  }
  
  public TraceElement<T> getPrevElement() {
    return prevElement;
  }
  
  public T getOp() {
    return op;
  }
  
  @Override
  public TraceElement<T> clone() {
    TraceElement<T> e = new TraceElement<T>(op);
    e.stateId = stateId;
    
    // we don't clone the linkage
    e.prevElement = null;
    e.prevTransition = null;
    
    return e;
  }
}
