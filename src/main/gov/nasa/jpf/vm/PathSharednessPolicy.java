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

import gov.nasa.jpf.Config;

/**
 * a SharednessPolicy implementation that only computes and keeps sharedness
 * along the same path, i.e. not search global.
 * 
 * This is the policy that should be used to create traces that can be replayed.
 * 
 * This policy uses PersistentTids, i.e. it does not modify existing ThreadInfoSet
 * instances but replaces upon add/remove with new ones. This ensures that ThreadInfoSets
 * are path local, but it also means that operations that add/remove ThreadInfos
 * have to be aware of the associated ElementInfo cloning and don't keep/use references
 * to old ElementInfos. The upside is that sharedness should be the same along any
 * given path, regardless of which paths were executed before. The downside is that all
 * callers of ThreadInfoSet updates have to be aware of that the ElementInfo might change.
 * 
 * Note that without additional transition breaks this can miss races due 
 * to non-overlapping thread execution along all paths. Most real world systems
 * have enough scheduling points (sync, field access within loops etc.) to avoid this,
 * but short living threads that only have single field access interaction points can
 * run into this effect: T1 creates O, starts T2, accesses O and
 * terminates before T2 runs. When T2 runs, it only sees access to O from an already
 * terminated thread and therefore treats this as a clean handover. Even if
 * T2 would break at its O access, there is no CG that would bring T1 back
 * into a state between creation and access of O, hence races caused by the O access
 * of T1 are missed (see .test.mc.threads.MissedPathTest).
 * Two additional scheduling points help to prevent this case: thread start CGs and
 * object exposure CGs (/after/ assignment in reference field PUTs where the owning
 * object is shared, but the referenced object isn't yet). Both are conservative by
 * nature, i.e. might introduce superfluous states for the sake of not missing paths to 
 * race points. Both can be controlled by configuration options ('cg.threads.break_start'
 * and 'vm.por.break_on_exposure').
 * 
 * Note also that shared-ness is not the same along all paths, because our goal
 * is just to find potential data races. As long as JPF explores /one/ path that
 * leads into a race we are fine - we don't care how many paths don't detect a race.
 */
public class PathSharednessPolicy extends GenericSharednessPolicy {
  
  public PathSharednessPolicy (Config config){
    super(config);
  }
  
  @Override
  public void initializeObjectSharedness (ThreadInfo allocThread, DynamicElementInfo ei) {
    ei.setReferencingThreads( new PersistentTidSet(allocThread));
  }

  @Override
  public void initializeClassSharedness (ThreadInfo allocThread, StaticElementInfo ei) {
    ei.setReferencingThreads( new PersistentTidSet(allocThread));
    ei.setExposed(); // static fields are per se exposed
  }
  
  @Override
  protected FieldLockInfo createFieldLockInfo (ThreadInfo ti, ElementInfo ei, FieldInfo fi){
    int[] lockRefs = ti.getLockedObjectReferences();
    switch (lockRefs.length){
      case 0:
        return FieldLockInfo.getEmptyFieldLockInfo();
      case 1:
        return new PersistentSingleLockThresholdFli(ti, lockRefs[0], lockThreshold);
      default: 
        return new PersistentLockSetThresholdFli(ti, lockRefs, lockThreshold);
    }
  }
  
  @Override
  protected boolean checkOtherRunnables (ThreadInfo ti){
    // since we only consider properties along this path, we can
    // ignore states that don't have other runnables
    return ti.hasOtherRunnables();
  }
}