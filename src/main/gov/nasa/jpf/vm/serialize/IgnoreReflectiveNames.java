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

package gov.nasa.jpf.vm.serialize;


public class IgnoreReflectiveNames extends FieldAmmendmentByName {
  /**
   * These are not critical to state because the objects that contain them
   * also have int values that can be used to look up the name within the
   * same VM execution.
   */
  static final String[] reflectiveNameFields = {
    "java.lang.Class.name",
    "java.lang.reflect.Method.name",
    "java.lang.reflect.Field.name"
  };
  
  public IgnoreReflectiveNames() {
    super(reflectiveNameFields, POLICY_IGNORE);
  }

  // must be at bottom!
  public static final IgnoreReflectiveNames instance = new IgnoreReflectiveNames();
}
