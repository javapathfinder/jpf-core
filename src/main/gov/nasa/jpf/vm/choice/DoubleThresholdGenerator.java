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
import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.DoubleChoiceGenerator;

/**
 * ChoiceGenerator instance that produces a simple 3 value enumeration
 * 
 */
public class DoubleThresholdGenerator extends ChoiceGeneratorBase<Double> implements DoubleChoiceGenerator {

  protected double[] values = new double[3];
  protected int count;

  public DoubleThresholdGenerator(Config conf, String id) {
    super(id);

    values[0] = conf.getDouble(id + ".low");
    values[1] = conf.getDouble(id + ".threshold");
    values[2] = conf.getDouble(id + ".high");
    count = -1;
  }

  @Override
  public Double getChoice (int idx){
    if (idx >= 0 && idx < 3){
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
  public boolean hasMoreChoices () {
    return !isDone && (count < 2);
  }

  @Override
  public Double getNextChoice () {
    if (count >=0) {
      return new Double(values[count]);
    } else {
      return new Double(values[0]);
    }
  }

  @Override
  public void advance () {
    if (count < 2)
      count++;
  }

  @Override
  public int getTotalNumberOfChoices () {
    return 3;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return count + 1;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("[id=\"");
    sb.append(id);
    sb.append("\",");
    
    for (int i=0; i<3; i++) {
      if (count == i) {
        sb.append(MARKER);
      }
      sb.append(values[i]);
      if (count < 2) {
        sb.append(',');
      }
    }
    sb.append(']');
    return sb.toString();
  }
  
  @Override
  public DoubleThresholdGenerator randomize () {
    for (int i = values.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      double tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
    }    
    return this;
  }

  @Override
  public Class<Double> getChoiceType() {
    return Double.class;
  }
}
