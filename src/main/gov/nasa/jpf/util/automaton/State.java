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
public class State {
  
  static final int TRANSITION_INC = 4;
  
  protected int id;
  protected String label;
  
  protected int nTransitions;
  protected Transition[] transitions;
  
  public State (String label, int numberOfTransitions){
    this.label = label;
    transitions = new Transition[numberOfTransitions];
  }
  
  public State (String label){
    this( label, TRANSITION_INC);
  }

  public State (){
    this( null, TRANSITION_INC);
  }
  
  // to be set by Automaton.addState()
  protected void setId(int id){
    this.id = id;
    if (label == null){
      label = Integer.toString(id);
    }
  }
  
  public int getId(){
    return id;
  }
  
  public String getLabel(){
    return label;
  }
  
  public int getNumberOfTransitions(){
    return nTransitions;
  }
  
  public Transition getTransition (int idx){
    return transitions[idx];
  }
  
  public void addTransition(Transition newTransition){
    if (nTransitions == transitions.length){
      Transition[] a = new Transition[nTransitions + TRANSITION_INC];
      System.arraycopy(transitions, 0, a, 0, nTransitions);
      transitions = a;
    }
    
    transitions[nTransitions] = newTransition;
    newTransition.setId(nTransitions);
    nTransitions++;
  }
  
  public void addTransitions(Transition ... newTransitions){
    int n = nTransitions + newTransitions.length;
    if (n >= transitions.length){
      Transition[] a = new Transition[n];
      System.arraycopy(transitions, 0, a, 0, nTransitions);
      transitions = a;      
    }
    
    for (int i=0; i<newTransitions.length; i++){
      transitions[nTransitions] = newTransitions[i];
      nTransitions++;
    }
  }
  
  public void enter(){
    // just here to be overridden, for Moore machines
  }
  
  public void exit(){
    // just here to be overridden, for Moore machines
  }
  
  public void printOn (PrintStream ps){
    ps.printf("\t[%d] State '%s'\n", id, label);
    for (int i=0; i<nTransitions; i++){
      transitions[i].printOn( ps);
    }
  }
}
