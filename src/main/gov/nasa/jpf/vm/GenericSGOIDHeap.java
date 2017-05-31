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
import gov.nasa.jpf.util.IntTable;

/**
 * abstract Heap trait that implements SGOIDs by means of a search global
 * Allocation map and a state managed allocCount map
 * 
 * NOTE - a reference value of 0 represents null and therefore is not a valid SGOID
 */
public abstract class GenericSGOIDHeap extends GenericHeap {

  static class GenericSGOIDHeapMemento extends GenericHeapMemento {
    IntTable.Snapshot<AllocationContext> ctxSnap;
    
    GenericSGOIDHeapMemento (GenericSGOIDHeap heap) {
      super(heap);
      
      ctxSnap = heap.allocCounts.getSnapshot();
    }

    @Override
    public Heap restore(Heap inSitu) {
      super.restore( inSitu);
      
      GenericSGOIDHeap heap = (GenericSGOIDHeap) inSitu;
      heap.allocCounts.restore(ctxSnap);
      
      return heap;
    }
  }
  
  // these are search global
  protected int nextSgoid;
  protected IntTable<Allocation> sgoids;
  
  // this is state managed 
  // NOTE - this has to be included in the mementos of concrete Heap implementations 
  protected IntTable<AllocationContext> allocCounts;
  
  protected GenericSGOIDHeap (Config config, KernelState ks){
    super(config, ks);
    
    // static inits
    initAllocationContext(config);
    sgoids = new IntTable<Allocation>();
    nextSgoid = 0;
    
    allocCounts = new IntTable<AllocationContext>();
  }
  
  
  //--- to be overridden by subclasses that use different AllocationContext implementations
  
  protected void initAllocationContext(Config config) {
    HashedAllocationContext.init(config);
    //PreciseAllocationContext.init(config);
  }
  
  // these are always called directly from the allocation primitive, i.e. the allocating site is at a fixed
  // stack offset (callers caller)
  @Override
  protected AllocationContext getSUTAllocationContext (ClassInfo ci, ThreadInfo ti) {
    return HashedAllocationContext.getSUTAllocationContext(ci, ti);
    //return PreciseAllocationContext.getSUTAllocationContext(ci, ti);
  }
  @Override
  protected AllocationContext getSystemAllocationContext (ClassInfo ci, ThreadInfo ti, int anchor) {
    return HashedAllocationContext.getSystemAllocationContext(ci, ti, anchor);
    //return PreciseAllocationContext.getSystemAllocationContext(ci, ti, anchor);
  }
  

  @Override
  protected int getNewElementInfoIndex (AllocationContext ctx) {
    int idx;
    int cnt;
    
    IntTable.Entry<AllocationContext> cntEntry = allocCounts.getInc(ctx);
    cnt = cntEntry.val;
    
    Allocation alloc = new Allocation(ctx, cnt);
    
    IntTable.Entry<Allocation> sgoidEntry = sgoids.get(alloc);
    if (sgoidEntry != null) { // we already had this one
      idx = sgoidEntry.val;
      
    } else { // new entry
      idx = ++nextSgoid;
      sgoids.put(alloc, idx);
    }
    
    // sanity check - we do this here (and not in our super class) since we know how elements are stored
//    assert get(idx) == null;
    
    return idx;
  }

}
