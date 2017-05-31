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
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.IntChoiceGenerator;

import java.util.Random;

/**
 * a IntChoiceGenerator that randomly chooses a configured number
 * of values from a specified range
 * this is usually configured through app properties
 * 
 * <2do> this is too redundant to RandomOrderIntCG - replace
 */
public class RandomIntIntervalGenerator extends ChoiceGeneratorBase<Integer> implements IntChoiceGenerator {

  protected int min, max; // both inclusive
  protected int nChoices;
  protected long seed;

  protected Random random;
  protected int range;

  protected int next;
  protected int count = 0;

  public RandomIntIntervalGenerator (String id, int min, int max, int nChoices){
    this(id, min,max,nChoices,0L);
  }

  public RandomIntIntervalGenerator (String id, int min, int max, int nChoices, long seed){
    super(id);

    this.min = min;
    this.max = max;
    this.nChoices = nChoices;
    this.seed = seed;

    range = max - min;
    random = new Random(seed);
  }

  public RandomIntIntervalGenerator(Config conf, String id) {
    super(id);

    min = conf.getInt(id + ".min");
    max = conf.getInt(id + ".max");
    nChoices = conf.getInt(id + ".n", 1);
    seed = conf.getLong(id + ".seed", 1);

    range = max - min;
    random = new Random(seed);
  }

  @Override
  public Integer getChoice (int idx){
    if (idx >= 0 && idx < nChoices){
      // Ok, this is really not efficient - use only for non-performance critical operations
      Random r = new Random(seed);
      int v=0;
      for (int i=0; i<idx; i++){
        v = r.nextInt(range);
      }
      return v + min;
      
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }

  @Override
  public void reset () {
    random = new Random(seed);
    count = 0;

    isDone = false;
  }

	@Override
	public boolean hasMoreChoices() {
    return !isDone && (count < nChoices);
	}

  @Override
  public void advance (){
    if (count < nChoices){
      count++;
      next = random.nextInt(range) + min;
    }
  }

  @Override
  public Integer getNextChoice () {
    return new Integer(next);
  }

  @Override
  public int getTotalNumberOfChoices () {
    return nChoices;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return count;
  }

  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder(getClass().getName());
    if (id == null) {
      sb.append('[');
    } else {
      sb.append("[id=\"");
      sb.append(id);
      sb.append("\",");
    }
    sb.append(min);
    sb.append("..");
    sb.append(max);
    sb.append(",n=");
    sb.append(nChoices);
    sb.append(",cur=");
    sb.append(getNextChoice());
    sb.append(",count=");
    sb.append(count);
    sb.append(']');
    return sb.toString();
  }

  @Override
  public Class<Integer> getChoiceType() {
    return Integer.class;
  }

  @Override
  public ChoiceGenerator<Integer> randomize() {
    return this;
  }

}
