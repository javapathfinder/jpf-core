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

/**
 * base class for atomic field updaters
 */
public class AtomicFieldUpdater extends NativePeer {
  
  
  protected FieldInfo getFieldInfo (ElementInfo eiUpdater, ElementInfo eiTarget){
    int fidx = eiUpdater.getIntField("fieldId");
    return eiTarget.getClassInfo().getInstanceField(fidx);
  }
  
  /**
   * note - we are not interested in sharedness/interleaving of the AtomicUpdater object 
   * but in the object that is accessed by the updater
   */
  protected boolean reschedulesAccess (ThreadInfo ti, ElementInfo ei, FieldInfo fi){
    Scheduler scheduler = ti.getScheduler();
    Instruction insn = ti.getPC();
    
    if (scheduler.canHaveSharedObjectCG( ti, insn, ei, fi)){
      ei = scheduler.updateObjectSharedness( ti, ei, fi);
      return scheduler.setsSharedObjectCG( ti, insn, ei, fi);
    }
    
    return false;
  }

}
