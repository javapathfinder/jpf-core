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
package gov.nasa.jpf.util.json;

import gov.nasa.jpf.JPFException;

import java.util.ArrayList;

/**
 * Array parsed from JSON document
 * @author Ivan Mushketik
 */
public class ArrayValue implements Value {

  ArrayList<Value> values = new ArrayList<Value>();



  @Override
  public String getString() {
    throw new JPFException("Can't convert array to string");
  }

  @Override
  public Double getDouble() {
    throw new JPFException("Can't convert array to double");
  }

  @Override
  public JSONObject getObject() {
    throw new JPFException("Can't convert array to object");
  }

  @Override
  public Value[] getArray() {
    Value[] result = new Value[values.size()];

    return values.toArray(result);
  }

  @Override
  public Boolean getBoolean() {
    throw new JPFException("Can't convert array to boolean");
  }

  void addValue(Value value) {
    values.add(value);
  }

}
