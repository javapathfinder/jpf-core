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

import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;

/**
 * just a little helper for java.lang.reflect peers
 * 
 * <2do> - it's overly simplistic for now
 */
public class MethodInfoRegistry {
  
  final int NREG = 10;
  MethodInfo[] registered;
  int nRegistered;
  
  public MethodInfoRegistry () {
    registered = new MethodInfo[NREG];
    nRegistered = 0;
  }
    
  public int registerMethodInfo (MethodInfo mi) {
    int idx;
    
    for (idx=0; idx < nRegistered; idx++) {
      if (registered[idx] == mi) {
        return idx;
      }
    }
    
    if (idx == registered.length) {
      MethodInfo[] newReg = new MethodInfo[registered.length+NREG];
      System.arraycopy(registered, 0, newReg, 0, registered.length);
      registered = newReg;
    }
    
    registered[idx] = mi;
    nRegistered++;
    return idx;
  }
  
  public MethodInfo getRegisteredFieldInfo (int idx) {
    return registered[idx];
  }

  public MethodInfo getMethodInfo (MJIEnv env, int objRef, String fieldName) {
    int idx = env.getIntField( objRef, fieldName);
    
    assert ((idx >= 0) || (idx < nRegistered)) : "illegal MethodInfo request: " + idx + ", " + nRegistered;
    
    return registered[idx];
  }

}
