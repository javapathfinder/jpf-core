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

import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.serialize.AmmendableFilterConfiguration.FieldAmmendment;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class FieldAmmendmentByName implements FieldAmmendment {
  protected final Set<String> fullFieldNames;
  protected final boolean policy;
  
  public FieldAmmendmentByName(String[] fieldNames, boolean policy) {
    this(Arrays.asList(fieldNames), policy);
  }
  
  public FieldAmmendmentByName(Collection<String> fullFieldNames, boolean policy) {
    this.fullFieldNames = new HashSet<String>(fullFieldNames);
    this.policy = policy;
  }

  public FieldAmmendmentByName(Iterable<String> fullFieldNames, boolean policy) {
    this.fullFieldNames = new HashSet<String>();
    for (String name : fullFieldNames) {
      this.fullFieldNames.add(name);
    }
    this.policy = policy;
  }
  
  @Override
  public boolean ammendFieldInclusion(FieldInfo fi, boolean sofar) {
    if (fullFieldNames.contains(fi.getFullName())) {
      return policy;
    } else {
      return sofar;
    }
  }

}
