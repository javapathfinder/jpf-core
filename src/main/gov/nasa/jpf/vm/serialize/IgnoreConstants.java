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
import gov.nasa.jpf.vm.serialize.AmmendableFilterConfiguration.StaticAmmendment;

import java.util.Arrays;
import java.util.HashSet;

/**
 * Marks static final field of primitive or known immutable type to be
 * filtered.  In theory, these could be critical to state, but that would
 * be highly irregular.
 * 
 * NOTE - final does not really mean constant, so we only ignore fields
 * here that are initialized from lexical constants, i.e. a constpool entry.
 * Others might involve data choice generators
 * 
 * <br><br>
 * Ignoring constants probably isn't beneficial with the FilteringSerializer
 * but could be a big win with AbstractingSerializer, which garbage-collects
 * no-longer-reachable objects--that is, garbage collection in its
 * representation, not in VM.
 *
 * @author peterd
 */
public class IgnoreConstants implements StaticAmmendment {
  static final HashSet<String> knownImmutables =
    new HashSet<String>(Arrays.asList(new String[] {
        "boolean", "byte", "char", "double", "float", "int", "long", "short",
        "java.lang.String",
        "java.lang.Boolean",
        "java.lang.Byte",
        "java.lang.Character",
        "java.lang.Double",
        "java.lang.Float",
        "java.lang.Integer",
        "java.lang.Long",
        "java.lang.Short",
    }));
  
  @Override
  public boolean ammendFieldInclusion(FieldInfo fi, boolean sofar) {
    assert fi.isStatic();
    if (fi.isFinal() && fi.getConstantValue() != null) {
      if (knownImmutables.contains(fi.getType())) {
        return POLICY_IGNORE; 
      }
    }
    // otherwise, delegate
    return sofar; 
  }

  // must be at bottom
  public static final IgnoreConstants instance = new IgnoreConstants();
}
