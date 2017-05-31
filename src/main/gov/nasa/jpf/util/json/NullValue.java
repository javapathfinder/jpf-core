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

/**
 * Null value from JSON document
 * @author Ivan Mushketik
 */
class NullValue implements Value {

  public NullValue() {
  }

  @Override
  public String getString() {
    return null;
  }

  @Override
  public Double getDouble() {
    return null;
  }

  @Override
  public Boolean getBoolean() {
    return null;
  }

  @Override
  public JSONObject getObject() {
    return null;
  }

  @Override
  public Value[] getArray() {
    return null;
  }

}
