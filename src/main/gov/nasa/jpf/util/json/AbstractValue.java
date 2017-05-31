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

/**
 * Implementation of all Value methods. Classes that inherits this class should
 * only override methods they can handle. Other methods will throw exceptions with
 * error description.
 *
 * @author Ivan Mushketik
 */
public class AbstractValue implements Value {

  String read;

  protected AbstractValue(String read) {
    this.read = read;
  }

  @Override
  public String getString() {
    throw new JPFException("Can't convert '" + read + " to String");
  }

  @Override
  public Double getDouble() {
    throw new JPFException("Can't convert '" + read + "' to Double");
  }

  @Override
  public JSONObject getObject() {
    throw new JPFException("Can't convert '" + read + "' to JSON object");
  }

  @Override
  public Value[] getArray() {
    throw new JPFException("Can't convert '" + read + "' to Array");
  }

  @Override
  public Boolean getBoolean() {
    throw new JPFException("Can't convert '" + read + "' to Boolean");
  }

}
