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

package gov.nasa.jpf.util;

import gov.nasa.jpf.JPF;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;

/**
 * utility class to specify local variables in JPF options
 * example:
 *
 *  x.y.MyClass.foo(int,double):x
 * 
 * Note: this is not derived from FeatureSpec, it only used a MethodSpec for delegation
 *
 * <2do> we don't deal with scopes or types yet
 */
public class VarSpec  {

  static JPFLogger logger = JPF.getLogger("gov.nasa.jpf.util");

  String varName;
  MethodSpec mthSpec;

  public static VarSpec createVarSpec(String spec) {
    int idx = spec.indexOf(':');

    if (idx > 0) {
      String ms = spec.substring(0, idx).trim();
      String vs = spec.substring(idx+1).trim();

      MethodSpec mspec = MethodSpec.createMethodSpec(ms);
      if (mspec != null){
        return new VarSpec(vs, mspec);
      }
    }

    logger.warning("illegal variable spec ", spec);
    return null;
  }

  public VarSpec (String varName, MethodSpec mthSpec){
    this.varName = varName;
    this.mthSpec = mthSpec;
  }

  public LocalVarInfo getMatchingLocalVarInfo (MethodInfo mi, int pc, int slotIdx){

    if (mthSpec.matches(mi)){
      LocalVarInfo lvar = mi.getLocalVar(slotIdx, pc);
      if (lvar != null && lvar.getName().equals(varName)){
        return lvar;
      }
    }

    return null;
  }


}
