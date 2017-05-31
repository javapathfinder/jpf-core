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

import gov.nasa.jpf.vm.ClosedMemento;

/**
 * on-demand restorer for MutableInteger instances
 */
public class MutableIntegerRestorer implements ClosedMemento {

  MutableInteger restoree;
  int value;
  
  public MutableIntegerRestorer (MutableInteger restoree){
    assert restoree != null : "restored object can't be null";
    this.restoree = restoree;
    this.value = restoree.intValue();
  }
  
  @Override
  public void restore (){
    restoree.set(value);
  }
}
