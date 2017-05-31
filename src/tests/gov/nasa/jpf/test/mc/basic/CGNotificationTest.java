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
import gov.nasa.jpf.jvm.bytecode.EXECUTENATIVE;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.SyncPolicy;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.vm.choice.IntChoiceFromList;

import java.util.ArrayList;

import org.junit.Test;

/**
 * regression test for CG notifications
 */
public class CGNotificationTest extends TestJPF {

  public static class Sequencer extends ListenerAdapter {

    static ArrayList<String> sequence;

    @Override
    public void choiceGeneratorRegistered(VM vm, ChoiceGenerator<?> nextCG, ThreadInfo ti, Instruction executedInsn) {
      System.out.println("# CG registered: " + nextCG);
      sequence.add("registered " + nextCG.getId());

      assert nextCG.hasMoreChoices();
    }

    @Override
    public void choiceGeneratorSet(VM vm, ChoiceGenerator<?> newCG) {
      System.out.println("# CG set:        " + newCG);
      sequence.add("set " + newCG.getId());

      assert newCG.hasMoreChoices();
    }

    @Override
    public void choiceGeneratorAdvanced(VM vm, ChoiceGenerator<?> currentCG) {
      System.out.println("#   CG advanced: " + currentCG);
      sequence.add("advance " + currentCG.getId() + ' ' + currentCG.getNextChoice());
    }

    @Override
    public void choiceGeneratorProcessed(VM vm, ChoiceGenerator<?> processedCG) {
      System.out.println("# CG processed:  " + processedCG);
      sequence.add("processed " + processedCG.getId());

      assert !processedCG.hasMoreChoices();
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction lastInsn){
      SystemState ss = vm.getSystemState();

      if (lastInsn instanceof EXECUTENATIVE) { // break on native method exec
        EXECUTENATIVE exec = (EXECUTENATIVE) lastInsn;

        if (exec.getExecutedMethodName().equals("getInt")){// this insn did create a CG
          if (!ti.isFirstStepInsn()){

            ChoiceGenerator<Integer> cg = new IntChoiceFromList("listenerCG", 3,4);
            ss.setNextChoiceGenerator(cg);
          }
        }
      }

    }
  }

  @Test
  public void testCGNotificationSequence () {
    if (!isJPFRun()){
      Sequencer.sequence = new ArrayList<String>();
    }

    // make sure max insn preemption does not interfere 
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.CGNotificationTest$Sequencer",
                                  "+vm.max_transition_length=MAX")){
      boolean b = Verify.getBoolean(); // first CG
      int i = Verify.getInt(1,2); // this one gets a CG on top registered by the listener
/*
      System.out.print("b=");
      System.out.print(b);
      System.out.print(",i=");
      System.out.println(i);
*/
    }

    if (!isJPFRun()){
      String[] expected = {
        "registered " + SyncPolicy.ROOT,
        "set " + SyncPolicy.ROOT,
        "advance " + SyncPolicy.ROOT + " ThreadInfo [name=main,id=0,state=RUNNING]",
        "registered verifyGetBoolean",
        "set verifyGetBoolean",
        "advance verifyGetBoolean false",
        "registered verifyGetInt(II)",
        "registered listenerCG",
        "set verifyGetInt(II)",
        "set listenerCG",
        "advance verifyGetInt(II) 1",
        "advance listenerCG 3",
        "advance listenerCG 4",
        "processed listenerCG",
        "advance verifyGetInt(II) 2",
        "advance listenerCG 3",
        "advance listenerCG 4",
        "processed listenerCG",
        "processed verifyGetInt(II)",
        "advance verifyGetBoolean true",
        "registered verifyGetInt(II)",
        "registered listenerCG",
        "set verifyGetInt(II)",
        "set listenerCG",
        "advance verifyGetInt(II) 1",
        "advance listenerCG 3",
        "advance listenerCG 4",
        "processed listenerCG",
        "advance verifyGetInt(II) 2",
        "advance listenerCG 3",
        "advance listenerCG 4",
        "processed listenerCG",
        "processed verifyGetInt(II)",
        "processed verifyGetBoolean",
        "processed " + SyncPolicy.ROOT
      };

      assert Sequencer.sequence.size() == expected.length;

      int i=0;
      for (String s : Sequencer.sequence){
        assert expected[i].equals(s) : "\"" + expected[i] + "\" != \"" + s + "\"";
        //System.out.println("\"" + s + "\",");
        i++;
      }
    }
  }
}
