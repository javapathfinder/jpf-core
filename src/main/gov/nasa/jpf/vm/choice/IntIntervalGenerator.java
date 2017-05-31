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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.IntChoiceGenerator;

import java.util.Arrays;
import java.util.Comparator;

/**
 * Choice Generator that enumerates an interval of int values. Pretty simplistic
 * implementation for now, but at least it can count up and down
 *
 * randomizing is handled through RandomOrderIntCG
 */
public class IntIntervalGenerator extends ChoiceGeneratorBase<Integer> implements IntChoiceGenerator {

  protected int min, max;
  protected int next;
  protected int delta;

  @Override
  public void reset () {
    isDone = false;

    if (delta == 0) {
      throw new JPFException("IntIntervalGenerator delta value is 0");
    }

    if (min > max) {
      int t = max;
      max = min;
      min = t;
    }

    if (delta > 0) {
      next = min - delta;
    } else {
      next = max - delta;
    }
  }

  /**
   *  don't use this since it is not safe for cascaded ChoiceGenerators
   * (we need the 'id' to be as context specific as possible)
   */
  @Deprecated public IntIntervalGenerator(int min, int max){
    this("?", min, max);
  }

  @Deprecated public IntIntervalGenerator(int min, int max, int delta){
    this("?", min, max, delta);
  }

  public IntIntervalGenerator(String id, int min, int max, int delta) {
    super(id);

    this.min = min;
    this.max = max;
    this.delta = delta;

    reset();
  }

  public IntIntervalGenerator(String id, int min, int max) {
    this(id, min, max, 1);
  }

  public IntIntervalGenerator(Config conf, String id) {
    super(id);
    min = conf.getInt(id + ".min");
    max = conf.getInt(id + ".max");
    delta = conf.getInt(id + ".delta", 1);

    reset();
  }

  @Override
  public Integer getChoice (int idx){
    int nChoices = getTotalNumberOfChoices();
    if (idx >= 0 && idx < nChoices){
      return min + idx*delta;
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }
  
  @Override
  public Integer getNextChoice () {
    return new Integer(next);
  }

  @Override
  public boolean hasMoreChoices () {
    if (isDone) {
      return false;
    } else {
      if (delta > 0) {
        return (next < max);
      } else {
        return (next > min);
      }
    }
  }

  @Override
  public void advance () {
    next += delta;
  }
  
  @Override
  public int getTotalNumberOfChoices () {
    return Math.abs((max - min) / delta) + 1;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    if (delta > 0) {
      if (next < min){
        return 0;
      } else {
        return (Math.abs((next - min) / delta) + 1);
      }
    } else {
      if (next > max){
        return 0;
      } else {
        return (Math.abs((max - next) / delta) + 1);
      }
    }
  }
  
  public boolean isAscending(){
    return delta > 0;
  }

  /**
   *  note this should only be called before the CG is advanced since it resets
   *  the enumeration state 
   */
  public void reverse(){
    delta = -delta;
    reset();
  }
  
  
  public Integer[] getChoices(){
    int n = getTotalNumberOfChoices();
    Integer[] vals = new Integer[n];
    int v = (delta > 0) ? min : max;
    
    for (int i=0; i<n; i++){
      vals[i] = v;
      v += delta;
    }
    
    return vals;
  }

  @Override
  public boolean supportsReordering(){
    return true;
  }
  
  @Override
  public ChoiceGenerator<Integer> reorder (Comparator<Integer> comparator){
    Integer[] vals = getChoices();
    Arrays.sort(vals, comparator);
    
    return new IntChoiceFromList(id, vals);
  }
  
  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("[id=\"");
    sb.append(id);
    sb.append('"');

    sb.append(",isCascaded:");
    sb.append(isCascaded);

    sb.append(",");
    sb.append(min);
    sb.append("..");
    sb.append(max);
    sb.append(",delta=");
    if (delta > 0) {
      sb.append('+');
    }
    sb.append(delta);
    sb.append(",cur=");
    sb.append(getNextChoice());
    sb.append(']');
    return sb.toString();
  }

  @Override
  public Class<Integer> getChoiceType() {
    return Integer.class;
  }

  @Override
  public ChoiceGenerator<Integer> randomize() {
    return new RandomOrderIntCG(this);
  }
}
