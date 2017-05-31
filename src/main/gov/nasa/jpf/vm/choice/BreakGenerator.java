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
package gov.nasa.jpf.vm.choice;

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;

/**
 * a pseudo CG that is used to break transitions. It can be used to break and
 * just reschedule the current thread, or to indicate an end state
 * (e.g. for System.exit())
 */
public class BreakGenerator extends ChoiceGeneratorBase<ThreadInfo> implements ThreadChoiceGenerator {

  protected ThreadInfo ti;
  protected int state = -1;
  protected boolean isTerminator;

  public BreakGenerator (String id, ThreadInfo ti, boolean isTerminator) {
    super(id);
    
    this.ti = ti;
    this.isTerminator = isTerminator;
  }
  
  @Override
  public ThreadInfo getNextChoice () {
    assert !isTerminator : "illegal operation on terminal BreakGenerator";
    return (state == 0) ? ti : null;
  }

  @Override
  public ThreadInfo getChoice (int idx){
    if (idx == 0){
      return ti;
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }
  
  @Override
  public void printOn (PrintWriter pw) {
    pw.println("BreakGenerator {" + ti.getName() + "}");
  }

  @Override
  public void advance () {
    assert !isTerminator : "illegal operation on terminal BreakGenerator";
    state++;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return (state >= 0) ? 1 : 0;
  }

  @Override
  public int getTotalNumberOfChoices () {
    return 1;
  }

  @Override
  public boolean hasMoreChoices () {
    if (isTerminator){
      return false;
    }
    
    return (state < 0);
  }

  @Override
  public void reset () {
    state = -1;
    isDone = false;
  }

  @Override
  public boolean contains (ThreadInfo ti){
    return this.ti == ti;
  }

  @Override
  public Class<ThreadInfo> getChoiceType() {
    return ThreadInfo.class;
  }

  @Override
  public ChoiceGenerator<ThreadInfo> randomize() {
    return this;
  }
  
  @Override
  public boolean isSchedulingPoint(){
    return true; // that's the whole point of having a BreakGenerator
  }

}
