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

import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Comparator;

public class ThreadChoiceFromSet extends ChoiceGeneratorBase<ThreadInfo> implements ThreadChoiceGenerator {

  protected boolean isSchedulingPoint;
  protected ThreadInfo[] values;
  protected int count;
    
  protected ThreadChoiceFromSet (String id){
    super(id);
    
    // all other fields have to be computed by subclass ctor
    count = -1;
  }
  
  public ThreadChoiceFromSet (String id, ThreadInfo[] set, boolean isSchedulingPoint) {
    super(id);
        
    values = set;
    count = -1;

    this.isSchedulingPoint = isSchedulingPoint;

    /**
    if (isSchedulingPoint){
      // do a sanity check to see if the candidates are acutally runnable
      for (int i = 0; i < set.length; i++) {
        if (!set[i].isTimeoutRunnable()) {
          throw new JPFException("trying to schedule non-runnable: " + set[i]);
        }
      }
    }
    **/
  }
  
  @Override
  public ThreadInfo getChoice (int idx){
    if (idx >= 0 && idx < values.length){
      return values[idx];
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }

  
  @Override
  public void reset () {
    count = -1;

    isDone = false;
  }
  
  @Override
  public ThreadInfo getNextChoice () {
    if ((count >= 0) && (count < values.length)) {
      return values[count];
    } else {
      // we don't raise an exception here because this might be (mis)used
      // from a listener, which shouldn't produce JPFExceptions
      return null;
    }
  }

  @Override
  public boolean hasMoreChoices () {
    return (!isDone && (count < values.length-1));
  }


  /**
   * this has to handle timeouts, which we do with temporary thread status
   * changes (i.e. the TIMEOUT_WAITING threads are in our list of choices, but
   * only change their status to TIMEDOUT when they are picked as the next choice)
   *
   * <2do> this should be in SystemState.nextSuccessor - there might be
   * other ThreadChoiceGenerators, and we should handle this consistently
   */
  @Override
  public void advance () {    
    if (count < values.length-1) { // at least one choice left
      count++;
    }
  }

  @Override
  public int getTotalNumberOfChoices () {
    return values.length;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return count+1;
  }

  public Object getNextChoiceObject () {
    return getNextChoice();
  }
  
  public ThreadInfo[] getChoices(){
    return values;
  }
  
  @Override
  public boolean supportsReordering(){
    return true;
  }
  
  @Override
  public ThreadChoiceGenerator reorder (Comparator<ThreadInfo> comparator){
    ThreadInfo[] newValues = values.clone();
    Arrays.sort(newValues, comparator);
    
    return new ThreadChoiceFromSet( id, newValues, isSchedulingPoint);
  }
  
  @Override
  public void printOn (PrintWriter pw) {
    pw.print(getClass().getName());
    pw.append("[id=\"");
    pw.append(id);
    pw.append('"');

    pw.append(",isCascaded:");
    pw.append(Boolean.toString(isCascaded));

    pw.print(",{");
    for (int i=0; i<values.length; i++) {
      if (i > 0) pw.print(',');
      if (i == count) {
        pw.print(MARKER);
      }
      pw.print(values[i].getName());
    }
    pw.print("}]");
  }
  
  @Override
  public ThreadChoiceFromSet randomize () {
    for (int i = values.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      ThreadInfo tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
    }
    return this;
  }
  
  public ThreadInfo[] getAllThreadChoices() {
	  return values; 
  }
  
  @Override
  public boolean contains (ThreadInfo ti){
    for (int i=0; i<values.length; i++){
      if (values[i] == ti){
        return true;
      }
    }
    return false;
  }

  @Override
  public Class<ThreadInfo> getChoiceType() {
    return ThreadInfo.class;
  }
  
  @Override
  public boolean isSchedulingPoint() {
    return isSchedulingPoint;
  }
}
