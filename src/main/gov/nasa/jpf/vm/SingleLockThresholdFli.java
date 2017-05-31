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
 * a threshold FieldLockInfo with a single lock candidate
 * This is the base version that does destructive updates. Override singleLockThresholdFli for a persistent version
 */
public class SingleLockThresholdFli extends ThresholdFieldLockInfo {
    protected int lockRef;
    
    SingleLockThresholdFli (ThreadInfo ti, int lockRef, int remainingChecks) {
      super( remainingChecks);
      
      tiLastCheck = ti;
      this.lockRef = lockRef;
    }

    @Override
	protected int[] getCandidateLockSet() {
      int[] set = { lockRef };
      return set;
    }

    /**
     * override this for path local flis
     */
    protected SingleLockThresholdFli singleLockThresholdFli (ThreadInfo ti, int lockRef, int remainingChecks){
      this.lockRef = lockRef;
      this.tiLastCheck = ti;
      this.remainingChecks = remainingChecks;

      return this;
    }
    
    @Override
	public FieldLockInfo checkProtection (ThreadInfo ti, ElementInfo ei, FieldInfo fi) {
      int[] currentLockRefs = ti.getLockedObjectReferences();
      int nLocks = currentLockRefs.length;
      int nRemaining = Math.max(0, remainingChecks--);
            
      for (int i=0; i<nLocks; i++) {
        if (currentLockRefs[i] == lockRef) {
          return singleLockThresholdFli( ti, lockRef, nRemaining);
        }
      }
      
      checkFailedLockAssumption(ti, ei, fi);
      return empty;
    }

    /**
     * only called at the end of the gc on all live objects. The recycled ones
     * are either already nulled in the heap, or are not marked as live
     */
    @Override
	public FieldLockInfo cleanUp (Heap heap) {
      ElementInfo ei = heap.get(lockRef);
      if (!heap.isAlive(ei)) {
        return FieldLockInfo.empty;
      } else {
        return this;
      }
    }

    @Override
	public String toString() {
      return ("SingleLockThresholdFli {remainingChecks="+remainingChecks+",lock="+lockRef + '}');
    }  

}
