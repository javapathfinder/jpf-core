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

package gov.nasa.jpf.listener;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.annotation.JPFOption;
import gov.nasa.jpf.annotation.JPFOptions;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.StringSetMatcher;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ChoicePoint;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.Random;

/**
 * this is a listener that only executes single choices until it detects
 * that it should start to search. If nothing is specified, this is pretty
 * much a simulator that randomly picks choices. Otherwise the user can
 * give it any combination of
 *  - a set of thread names
 *  - a set of method names
 *  - a start search depth
 * to turn on the search. If more than one condition is given, all have to be
 * satisfied
 */

@JPFOptions({
  @JPFOption(type = "Int", key = "choice.seed", defaultValue= "42", comment = ""),
  @JPFOption(type = "StringArray", key = "choice.threads", defaultValue = "", comment="start search, when all threads in the set are active"),
  @JPFOption(type = "StringArray", key = "choice.calls", defaultValue = "", comment = "start search, when any of the methods is called"),
  @JPFOption(type = "Int", key = "choice.depth", defaultValue = "-1", comment = "start search, when reaching this depth"),
  @JPFOption(type = "String", key = "choice.use_trace", defaultValue ="", comment = ""),
  @JPFOption(type = "Boolean", key = "choice.search_after_trace", defaultValue = "true", comment="start search, when reaching the end of the stored trace")
})
public class ChoiceSelector extends ListenerAdapter {

  Random random;
  boolean singleChoice = true;

  // those are our singleChoice end conditions (i.e. where we start the search)
  StringSetMatcher threadSet; // we start when all threads in the set are active
  boolean threadsAlive = true;;

  StringSetMatcher calls; // .. when any of the methods is called
  boolean callSeen = true;

  int startDepth; // .. when reaching this depth
  boolean depthReached = true;

  // set if we replay a trace
  ChoicePoint trace;

  // start the search when reaching the end of the stored trace. If not set,
  // the listener will just randomly select single choices once the trace
  // got processed
  boolean searchAfterTrace;
  

  public ChoiceSelector (Config config, JPF jpf) {
    random = new Random( config.getInt("choice.seed", 42));

    threadSet = StringSetMatcher.getNonEmpty(config.getStringArray("choice.threads"));
    if (threadSet != null) {
      threadsAlive = false;
    }

    calls = StringSetMatcher.getNonEmpty(config.getStringArray("choice.calls"));
    callSeen = false;

    startDepth = config.getInt("choice.depth", -1);
    if (startDepth != -1) {
      depthReached = false;
    }

    // if nothing was specified, we just do single choice (simulation)
    if ((threadSet == null) && (calls == null) && (startDepth == -1)) {
      threadsAlive = false;
      callSeen = false;
      depthReached = false;
    }

    VM vm = jpf.getVM();
    trace = ChoicePoint.readTrace(config.getString("choice.use_trace"), vm.getSUTName());
    searchAfterTrace = config.getBoolean("choice.search_after_trace", true);
    vm.setTraceReplay(trace != null);
  }

  void checkSingleChoiceCond() {
    singleChoice = !(depthReached && callSeen && threadsAlive);
  }

  @Override
  public void choiceGeneratorAdvanced (VM vm, ChoiceGenerator<?> currentCG) {
    int n = currentCG.getTotalNumberOfChoices();

    if (trace != null) { // this is a replay

      // <2do> maybe that should just be a warning, and then a single choice
      assert currentCG.getClass().getName().equals(trace.getCgClassName()) :
        "wrong choice generator class, expecting: " + trace.getCgClassName()
        + ", read: " + currentCG.getClass().getName();

      int choiceIndex = trace.getChoiceIndex();
      currentCG.select(choiceIndex);

    } else {
      if (singleChoice) {
        if (n > 1) {
          int r = random.nextInt(n);
          currentCG.select(r); // sets it done, so we never backtrack into it
        }
      }
    }
  }

  @Override
  public void threadStarted(VM vm, ThreadInfo ti) {
    if (singleChoice && (threadSet != null)) {
      String tname = ti.getName();
      if (threadSet.matchesAny( tname)){
        threadsAlive = true;
        checkSingleChoiceCond();
      }
    }
  }

  @Override
  public void executeInstruction(VM vm, ThreadInfo ti, Instruction insnToExecute) {
    if (singleChoice && !callSeen && (calls != null)) {
      if (insnToExecute instanceof JVMInvokeInstruction) {
        String mthName = ((JVMInvokeInstruction)insnToExecute).getInvokedMethod(ti).getBaseName();

        if (calls.matchesAny(mthName)){
          callSeen = true;
          checkSingleChoiceCond();
        }
      }
    }
  }

  @Override
  public void stateAdvanced(Search search) {

    if (trace != null) {
      // there is no backtracking or restoring as long as we replay
      trace = trace.getNext();

      if (trace == null){
        search.getVM().setTraceReplay(false);
        if (searchAfterTrace){
          singleChoice = false;
        }
      }

    } else {
      if (singleChoice && !depthReached && (startDepth >= 0)) {
        if (search.getDepth() == startDepth) {
          depthReached = true;
          checkSingleChoiceCond();
        }
      }
    }
  }

}
