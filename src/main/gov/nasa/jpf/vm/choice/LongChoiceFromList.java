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
import gov.nasa.jpf.vm.LongChoiceGenerator;

public class LongChoiceFromList extends NumberChoiceFromList<Long> implements LongChoiceGenerator {

  @Override
  protected Long[] createValueArray(int len) {
    return new Long[len];
  }

  @Override
  protected Long getDefaultValue() {
    return 0L;
  }

  @Override
  public Class<Long> getChoiceType() {
    return Long.class;
  }

  @Override
  protected Long parseLiteral(String literal, int sign) {
    long val = Long.parseLong(literal);
    return new Long(val * sign);
  }

  @Override
  protected Long newValue(Number num, int sign) {
    return new Long(num.longValue() * sign);
  }

  /**
   * super constructor for subclasses that want to configure themselves
   * 
   * @param id
   *          name used in choice config
   */
  protected LongChoiceFromList(String id) {
    super(id);
  }

  protected LongChoiceFromList(String id, Long[] vals) {
    super(id, vals);
  }

  public LongChoiceFromList(Config conf, String id) {
    super(conf, id);
  }

  public LongChoiceFromList(String id, long... val) {
    super(id);

    if (val != null) {
      values = new Long[val.length];
      for (int i = 0; i < val.length; i++) {
        values[i] = val[i]; // enable use of cached Integer values
      }
    } else {
      throw new JPFException("empty set for LongChoiceFromList");
    }

    count = -1;
  }
}
