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

import gov.nasa.jpf.vm.ChoiceGenerator;

/**
 * Creates Choice generator from Value array.
 * We need this interface because there are too many Choice Generators in JPF if we
 * will try to create CG trying to match CG constructors parameters and Values array
 * we will have following problems:
 * <li> Ambiguity with some constructors
 * <li> User would unable to create some kind of CG (some requires Config for example)
 * <li> User would need to specify unique ids in JSON
 * @see Value
 * @author Ivan Mushketik
 */
public interface CGCreator {

  /**
   * Create choice generator
   * @param id - unique id for this CG
   * @param params - params read from JSON file
   * @return new CG.
   */
  public ChoiceGenerator<?> createCG(String id, Value[] params);
}
