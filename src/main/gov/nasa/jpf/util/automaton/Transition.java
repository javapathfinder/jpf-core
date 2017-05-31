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
package gov.nasa.jpf.util.automaton;

import java.io.PrintStream;

/**
 *
 */
public class Transition {
  
  protected int id;
  protected String label;
  
  protected State fromState;
  protected State toState;
  
  public Transition (String label, State toState){
    // the fromState is set when we add this transition to the state
    this.toState = toState;

    this.label = label;
    // id will be set when this transition is added to its fromState
  }
  
  protected void setFromState (State fromState){
    this.fromState = fromState;
  }
  
  protected void setId (int id){
    this.id = id;
  }
  
  public int getId(){
    return id;
  }
  
  public String getLabel(){
    return label;
  }
  
  public boolean checkGuard(){
    // override if you have guards
    return true;
  }
  
  public void fire (){
    // just here to be overridden, for Mealy machines
  }
  
  public void printOn (PrintStream ps){
    ps.printf("\t\t[%d] '%s' => state '%s'\n", id, label, toState.getLabel());
  }
}
