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

package gov.nasa.jpf.vm.bytecode;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.Scheduler;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * abstract base for InvokeInstructions
 */
public abstract class InvokeInstruction extends Instruction {

  public abstract MethodInfo getInvokedMethod();
  
  public abstract String getInvokedMethodName();
  public abstract String getInvokedMethodSignature();
  public abstract String getInvokedMethodClassName();
  
  /**
   * this does the lock registration/acquisition and respective transition break 
   * return true if the caller has to re-execute
   */
  protected boolean reschedulesLockAcquisition (ThreadInfo ti, ElementInfo ei){
    Scheduler scheduler = ti.getScheduler();
    ei = ei.getModifiableInstance();
    
    if (!ti.isLockOwner(ei)){ // we only need to register, block and/or reschedule if this is not a recursive lock
      if (ei.canLock(ti)) {
        // record that this thread would lock the object upon next execution if we break the transition
        // (note this doesn't re-add if already registered)
        ei.registerLockContender(ti);
        if (scheduler.setsLockAcquisitionCG(ti, ei)) { // optional scheduling point
          return true;
        }
        
      } else { // we need to block
        ei.block(ti); // this means we only re-execute once we can acquire the lock
        if (scheduler.setsBlockedThreadCG(ti, ei)){ // mandatory scheduling point
          return true;
        }
        throw new JPFException("blocking synchronized call without transition break");            
      }
    }
    
    // locking will be done by ti.enter()
    return false;
  }

}
