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
import gov.nasa.jpf.vm.LongChoiceGenerator;

/**
 *
 */
public class RandomOrderLongCG extends ChoiceGeneratorBase<Long> implements LongChoiceGenerator {
  protected long[] choices;

  protected int nextIdx;

  public RandomOrderLongCG (LongChoiceGenerator sub) {
    super(sub.getId());
    setPreviousChoiceGenerator(sub.getPreviousChoiceGenerator());
    choices = new long[sub.getTotalNumberOfChoices()];
    for (int i = 0; i < choices.length; i++) {
      sub.advance();
      choices[i] = sub.getNextChoice();
    }
    for (int i = choices.length - 1; i > 0; i--) { // all but first
      int j = random.nextInt(i + 1);
      long tmp = choices[i];
      choices[i] = choices[j];
      choices[j] = tmp;
    }
    nextIdx = -1;
  }
  
  @Override
  public Long getChoice (int idx){
    if (idx >= 0 && idx < choices.length){
      return choices[idx];
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }


  @Override
  public Long getNextChoice() {
    return new Long(choices[nextIdx]);
  }

  @Override
  public void advance() {
    if (nextIdx + 1 < choices.length) nextIdx++;
  }

  @Override
  public int getProcessedNumberOfChoices() {
    return nextIdx+1;
  }

  @Override
  public int getTotalNumberOfChoices() {
    return choices.length;
  }

  @Override
  public boolean hasMoreChoices() {
    return !isDone && (nextIdx + 1 < choices.length);
  }

  @Override
  public void reset() {
    nextIdx = -1;

    isDone = false;
  }

  @Override
  public Class<Long> getChoiceType() {
    return Long.class;
  }
}
