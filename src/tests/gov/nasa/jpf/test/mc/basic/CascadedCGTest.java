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
import gov.nasa.jpf.jvm.bytecode.GETFIELD;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.vm.choice.IntChoiceFromSet;
import gov.nasa.jpf.vm.choice.IntIntervalGenerator;

import org.junit.Test;

/**
 * regression test for cascaded ChoiceGenerators
 */
public class CascadedCGTest extends TestJPF {

  public static class IntChoiceCascader extends ListenerAdapter {
    static int result;

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
      SystemState ss = vm.getSystemState();

      if (executedInsn instanceof EXECUTENATIVE) { // break on native method exec
        EXECUTENATIVE exec = (EXECUTENATIVE) executedInsn;

        if (exec.getExecutedMethodName().equals("getInt")){// this insn did create a CG
          if (!ti.isFirstStepInsn()){
            result = 0;

            IntIntervalGenerator cg = new IntIntervalGenerator("listenerCG", 3,4);
            ss.setNextChoiceGenerator(cg);
            System.out.println("# listener registered " + cg);

          } else { // reexecution

            ChoiceGenerator<?>[] curCGs = ss.getCurrentChoiceGenerators();
            assert curCGs.length == 2;

            IntIntervalGenerator cg = ss.getCurrentChoiceGenerator("listenerCG", IntIntervalGenerator.class);
            assert cg != null : "no 'listenerCG' IntIntervalGenerator found";
            int i = cg.getNextChoice();
            System.out.println("# current listener CG choice: " + i);

            cg = ss.getCurrentChoiceGenerator("verifyGetInt(II)", IntIntervalGenerator.class);
            assert cg != null : "no 'verifyGetInt(II)' IntIntervalGenerator found";
            int j = cg.getNextChoice();
            System.out.println("# current insn CG choice: " + j);

            result += i * j;
          }
        }
      }
    }
  }

  @Test
  public void testCascadedIntIntervals () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.CascadedCGTest$IntChoiceCascader")){
      int i = Verify.getInt( 1, 2);
      System.out.print("i=");
      System.out.println(i);
    } else {
      assert IntChoiceCascader.result == 21;
    }
  }


  //--- mixed data and thread CG

  // this listener replaces all GETFIELD "mySharedField" results with configured
  // choice values (i.e. it is a simplified field Perturbator).
  // The demo point is that it is not aware of that such GETFIELDs might also be
  // scheduling points because of shared object field access, and it should work
  // the same no matter if there also was a ThreadChoice/context switch or not

  // NOTE: while the cascaded CG interface is easy to use (almost the same as the
  // single CG interface), the context can be quite tricky because the cascaded
  // CG (the scheduling point in this case) means the corresponding instruction
  // is already rescheduled and might have been cut short in insn specific ways
  // (in this case before pushing the field value on the operand stack). For this
  // reason a simple ti.isFirstStepInsn() check is not sufficient. There might not
  // have been a reschedule if there was only one thread, or even if this is the
  // first step insn, the corresponding CG might have been not related to the
  // getfield but some action in the preceeding thread (e.g. a terminate).
  // In this case, the simple solution is based on that we want the data CG
  // unconditionally, so we check if there is a corresponding current CG
  // (which means this is not the first step insn)

  public static class FieldAccessCascader extends ListenerAdapter {

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn) {
      SystemState ss = vm.getSystemState();

      if (executedInsn instanceof GETFIELD){
        GETFIELD getInsn = (GETFIELD) executedInsn;
        FieldInfo fi = getInsn.getFieldInfo();
        if (fi.getName().equals("mySharedField")){

          IntChoiceFromSet cg = ss.getCurrentChoiceGenerator("fieldReplace", IntChoiceFromSet.class);
          if (cg == null){

            // we might get here after a preceding rescheduling exec, i.e.
            // partial execution (with successive re-execution), or after
            // non-rescheduling exec has been completed (only one runnable thread).
            // In the first case we have to restore the operand stack so that
            // we can reexecute
            if (!ti.willReExecuteInstruction()){
              // restore old operand stack contents
              StackFrame frame = ti.getModifiableTopFrame();

              frame.pop();
              frame.pushRef( getInsn.getLastThis());
            }

            cg = new IntChoiceFromSet("fieldReplace", 42, 43);
            ss.setNextChoiceGenerator(cg);
            ti.reExecuteInstruction();

            System.out.println("# listener registered CG: " + cg);

          } else {
            StackFrame frame = ti.getModifiableTopFrame();

            int v = cg.getNextChoice();
            int n = frame.pop();
            frame.push(v);

            System.out.println("# listener replacing " + n + " with " + v);
          }
        }
      }
    }

    //--- those are just for debugging purposes
    @Override
    public void stateBacktracked(Search search) {
      System.out.println("#------ [" + search.getDepth() + "] backtrack: " + search.getStateId());
    }
    
    @Override
    public void stateAdvanced(Search search){
      System.out.println("#------ " + search.getStateId() + " isNew: " + search.isNewState() + ", isEnd: " + search.isEndState());
    }
    
    @Override
    public void threadScheduled(VM vm, ThreadInfo ti){
      System.out.println("# running thread: " + ti);
    }
    
    @Override
    public void threadTerminated(VM vm, ThreadInfo ti){
      System.out.println("# terminated thread: " + ti);
    }
    
    @Override
    public void threadStarted(VM vm, ThreadInfo ti){
      System.out.println("# started thread: " + ti);
    }
    
    @Override
    public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
      System.out.println("# choice: " + currentCG);
    }
  }

  int mySharedField = -1;

  @Test
  public void testMixedThreadDataCGs () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.CascadedCGTest$FieldAccessCascader")){
      Thread t = new Thread(){
        @Override
		public void run() {
          int n = mySharedField;
          System.out.print("<thread> mySharedField read: ");
          System.out.println( n);
          assert n == 42 || n == 43; // regardless of main thread exec state
        }
      };
      t.start();

      mySharedField = 7;
      System.out.println("<main> mySharedField write: 7");
    }
  }
}
