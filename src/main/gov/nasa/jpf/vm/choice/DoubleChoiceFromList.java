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
import gov.nasa.jpf.vm.DoubleChoiceGenerator;

/**
 * simple DoubleChoiceGenerator that takes it's values from a single
 * property "values" (comma or blank separated list)
 * 
 */
public class DoubleChoiceFromList extends NumberChoiceFromList<Double> implements DoubleChoiceGenerator {

  @Override
  protected Double[] createValueArray(int len) {
    return new Double[len];
  }

  @Override
  protected Double getDefaultValue() {
    return 0.0;
  }

  @Override
  public Class<Double> getChoiceType() {
    return Double.class;
  }

  @Override
  protected Double parseLiteral(String literal, int sign) {
    double val = Double.parseDouble(literal);
    return new Double(val * sign);
  }

  @Override
  protected Double newValue(Number num, int sign) {
    return new Double(num.intValue() * sign);
  }

  /**
   * super constructor for subclasses that want to configure themselves
   * 
   * @param id
   *          name used in choice config
   */
  protected DoubleChoiceFromList(String id) {
    super(id);
  }

  protected DoubleChoiceFromList(String id, Double[] vals) {
    super(id, vals);
  }

  public DoubleChoiceFromList(Config conf, String id) {
    super(conf, id);
  }

  public DoubleChoiceFromList(String id, double... val) {
    super(id);

    if (val != null) {
      values = new Double[val.length];
      for (int i = 0; i < val.length; i++) {
        values[i] = val[i]; // enable use of cached Double values
      }
    } else {
      throw new JPFException("empty set for DoubleChoiceFromList");
    }

    count = -1;
  }
}
