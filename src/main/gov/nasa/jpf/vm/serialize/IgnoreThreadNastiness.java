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


public class IgnoreThreadNastiness implements FieldAmmendment {
  @Override
  public boolean ammendFieldInclusion(FieldInfo fi, boolean sofar) {
    String cname = fi.getClassInfo().getName();
    String fname = fi.getName();
    if (cname.equals("java.lang.Thread")) {
      if (!fname.equals("target")) {
        return POLICY_IGNORE;  // nothing but perhaps `target' should be critical
        // (that includes static fields)
      }
    } else if (cname.equals("java.lang.ThreadGroup")) {
      return POLICY_IGNORE;  // hopefully none of it is critical
      // (that includes static fields; not that there are any)
    }
    return sofar;
  }
  
  
  public static final IgnoreThreadNastiness instance = new IgnoreThreadNastiness();
}
