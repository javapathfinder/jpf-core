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
 * generic class for modeling automatons
 * 
 * Since this is used in so many extensions from both model and native code,
 * it seems appropriate to add a basis implementation to util.
 * 
 * To make it more amenable to modeling (e.g. for native peer implementation),
 * we avoid using standard Java containers at the expense of efficiency for
 * large numbers of states and transitions 
 */
public class Automaton <S extends State> {
    
  static final int STATE_INC = 16;
  static final int INPUT_INC = 16;
  
  protected String label;
  
  protected int nStates;
  protected State[] states;
  
  protected int nInputs;
  protected String[] alphabet;
  
  protected int current;
  
  
  public Automaton (String label, int numberOfStates, int numberOfInputs){
    this.label = label;
    states = new State[numberOfStates];
    alphabet = new String[numberOfInputs];    
  }
  
  public Automaton (String label, int numberOfStates){
    this( label, numberOfStates, INPUT_INC);
  }

  public Automaton(String label){
    this(label, STATE_INC, INPUT_INC);
  }
  
  public void addState (State newState){
    if (nStates == states.length){
      State[] a = new State[nStates + STATE_INC];
      System.arraycopy(states, 0, a, 0, nStates);
      states = a;
    }
    
    states[nStates] = newState;
    newState.setId(nStates);
    nStates++;
  }

  public void addStates (State ... newStates){
    int n = nStates + newStates.length;
    if (n >= states.length){
      State[] a = new State[n];
      System.arraycopy(states, 0, a, 0, nStates);
      states = a;      
    }
    
    for (int i=0; i<newStates.length; i++){
      State s = newStates[i];
      states[nStates] = s;
      s.setId(nStates);
      nStates++;
    }
  }
  
  public String getLabel(){
    return label;
  }
  
  public int getNumberOfStates(){
    return nStates;
  }
  
  public S getCurrentState(){
    return (S)states[current];
  }
  
  public String[] computeAlphabet(){
    for (int i = 0; i < nStates; i++) {
      State s = states[i];
      int nTrans = s.getNumberOfTransitions();

      nextTransition:
      for (int j = 0; j < nTrans; j++) {
        Transition t = s.getTransition(j);
        String label = t.getLabel();

        for (int k = 0; k < nInputs; k++) {
          if (alphabet[k].equals(label)) {
            break nextTransition;
          }
        }

        if (nInputs == alphabet.length) {
          String[] a = new String[nInputs + INPUT_INC];
          System.arraycopy(alphabet, 0, a, 0, nInputs);
          alphabet = a;
        }

        alphabet[nInputs] = label;
        nInputs++;
      }
    }
    
    return alphabet;
  }
  
  public String[] getAlphabet(){
    if (nInputs == 0){
      return computeAlphabet();
    } else {
      return alphabet;
    }
  }
  
  public void printOn (PrintStream ps){
    ps.printf("Automaton '%s'\n", label);
    
    for (int i=0; i<nStates; i++){
      states[i].printOn(ps);
    }
  }
}
