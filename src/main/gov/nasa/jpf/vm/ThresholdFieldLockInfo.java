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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.JPFException;

/**
 * a FieldLockInfo that assumes lock protection after n accesses with
 * non-empty lock sets
 */
public abstract class ThresholdFieldLockInfo extends FieldLockInfo implements Cloneable {
  protected int remainingChecks;

  protected ThresholdFieldLockInfo(int remainingChecks) {
    this.remainingChecks = remainingChecks;
  }

  @Override
  public boolean isProtected() {
    // otherwise this would have turned into a EmptyFieldLockInfo
    return (remainingChecks == 0);
  }

  protected void checkFailedLockAssumption(ThreadInfo ti, ElementInfo ei, FieldInfo fi) {
    if (remainingChecks == 0) {
      // with no locks remaining this would have been demoted to an
      // EmptyFieldLockInfo by now
      lockAssumptionFailed(ti, ei, fi);
    }
  }
  
  /**
   * this implements a path-local FieldLockInfo that are never mutated
   * this has to be overridden for search global FieldLockInfos
   */
  protected FieldLockInfo getInstance (int nRemaining){
    try {
      ThresholdFieldLockInfo fli = (ThresholdFieldLockInfo)clone();
      fli.remainingChecks = nRemaining;
      return fli;
              
    } catch (CloneNotSupportedException cnsx){
      throw new JPFException("clone of ThresholdFieldLockInfo failed: " + this);
    }
  }
}
