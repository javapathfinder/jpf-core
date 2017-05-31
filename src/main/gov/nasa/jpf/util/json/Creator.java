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

import gov.nasa.jpf.vm.MJIEnv;

/**
 * Creates new JPF from JSON value.
 * @author Ivan Mushketik
 */
interface Creator {
  /**
   * Create new object, according to read value.
   * @param env - MJI environment
   * @param typeName - name of the new object's type
   * @param value - value read from JSON document
   * @return reference to the new object
   */
  public int create(MJIEnv env, String typeName, Value value);
}
