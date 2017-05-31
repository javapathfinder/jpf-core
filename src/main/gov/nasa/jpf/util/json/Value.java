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
 * Value that was read from JSON document. ("key" : value).
 * Value class has methods to return string, double or boolean value.
 * Derived class should throw exceptions if they can't convert value read from
 * JSON document to the requested one.
 * @author Ivan Mushketik
 */
public interface  Value {
  /**
   * Get string value.
   * @return string value read from JSON document
   */
  public String getString();

  /**
   * Get double value.
   * @return double value read from JSON document
   */
  public Double getDouble();

  /**
   * Get JSON object.
   * @return JSON object value read from JSON document
   */
  public JSONObject getObject();

  /**
   * Get array value.
   * @return array value read from JSON document
   */
  public Value[] getArray();

  /**
   * Get boolean value.
   * @return boolean value read from JSON document.
   */
  public Boolean getBoolean();

}
