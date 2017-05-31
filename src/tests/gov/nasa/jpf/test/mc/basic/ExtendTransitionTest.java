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

package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Verify;
import org.junit.Test;

/**
 * regression test for extended transitions
 */
public class ExtendTransitionTest extends TestJPF {
  
  public static class CGListener extends ListenerAdapter {
    @Override
    public void choiceGeneratorRegistered (VM vm, ChoiceGenerator<?> nextCG, ThreadInfo currentThread, Instruction executedInstruction) {
      System.out.println("CG registered: " + nextCG);
    }

    @Override
    public void choiceGeneratorSet (VM vm, ChoiceGenerator<?> newCG) {
      System.out.println("\nCG set: " + newCG + " by: " + newCG.getInsn());
    }

    @Override
    public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
      System.out.println("CG advanced: " + currentCG);
    }

    @Override
    public void choiceGeneratorProcessed (VM vm, ChoiceGenerator<?> processedCG) {
      System.out.println("CG processed: " + processedCG);
    }  
    
    @Override
    public void stateAdvanced (Search search){
      System.out.println("!!! state advanced - this should not happen");
    }
  }
  
  @Test
  public void testExtendedStateTransitions(){
    if (verifyNoPropertyViolation("+vm.extend_transitions=*", "+cg.break_single_choice=false", 
            "+listener=" + getClass().getName() + "$CGListener")){
      Verify.print("-- start\n");
      for (int i=0; i<5; i++){
        int n = Verify.breakTransition( "loop cycle", i, i);
        Verify.print("i=", i);
        Verify.print(", n=", n);
        Verify.println();
      }
    }
    
    if (!isJPFRun()){
      int nStates = VM.getVM().getStateCount();
      System.out.println("nStates=" + nStates);
      assertTrue(nStates == 0);
    }
  }
}
