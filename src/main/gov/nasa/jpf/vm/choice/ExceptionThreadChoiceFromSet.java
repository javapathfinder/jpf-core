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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.ThreadChoiceGenerator;
import gov.nasa.jpf.vm.ThreadInfo;
import java.util.Arrays;
import java.util.Comparator;

/**
 * a ThreadChoiceFromSet that reschedules the specified thread with exceptions
 */
public class ExceptionThreadChoiceFromSet extends ThreadChoiceFromSet {

  protected ThreadInfo exceptionThread;
  protected String[] exceptions;
  
  public ExceptionThreadChoiceFromSet (String id, ThreadInfo[] runnables, ThreadInfo exceptionThread, String[] exceptionClsNames){
    super(id);
    
    this.exceptionThread = exceptionThread;
    
    values = new ThreadInfo[runnables.length + exceptionClsNames.length];
    exceptions = new String[values.length];
    
    System.arraycopy(runnables, 0, values, 0, runnables.length);
    for (int i=0, j=runnables.length; i<exceptionClsNames.length; i++, j++){
      values[j] = exceptionThread;
      exceptions[j] = exceptionClsNames[i];
    }
    
    isSchedulingPoint = true; // not much use otherwise
  }
  
  public String getExceptionForCurrentChoice(){
    if ((count >= 0) && (count < values.length)) {
      return exceptions[count];
    } else {
      return null;
    }
  }
  
  @Override
  public ThreadChoiceGenerator reorder (Comparator<ThreadInfo> comparator){
    ThreadInfo[] newValues = values.clone();
    Arrays.sort(newValues, comparator);
    
    // we don't really reorder occurrences of the exceptionThread, but since the Comparator 
    // only knows ThreadInfos that shouldn't matter
    String[] newExceptions = new String[exceptions.length];
    for (int i=0, j=-1; i<newValues.length; i++){
      if (newValues[i] == exceptionThread){
        for (j++; exceptions[j] == null; j++);
        newExceptions[i] = exceptions[j];
      }
    }

    try {
      ExceptionThreadChoiceFromSet reorderedCG = (ExceptionThreadChoiceFromSet)clone();
      reorderedCG.values = newValues;
      reorderedCG.exceptions = newExceptions;
      reorderedCG.count = -1;
      
      return reorderedCG;
      
    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("clone of ExceptionalThreadChoice failed");
    }
  }
  
  @Override
  public ThreadChoiceFromSet randomize () {
    for (int i = values.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      ThreadInfo tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
      
      String tmpX = exceptions[i];
      exceptions[i] = exceptions[j];
      exceptions[j] = tmpX;
    }
    return this;
  }
}
