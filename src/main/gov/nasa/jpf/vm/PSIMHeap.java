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
import gov.nasa.jpf.util.PSIntMap;
import gov.nasa.jpf.util.Predicate;

import java.util.Iterator;

/**
 * heap implementation that uses a PersistentStagingMsbIntMap as the underlying container
 * 
 * This is intended for large state spaces, to minimize store/restore costs. While those
 * are negligible for PersistentMaps, the per-change overhead is not since the container
 * has to duplicate the access path to the changed node on every modification. Efficiency
 * of PSIMHeap therefore relies on accessing objects in a consecutive pattern, i.e.
 * depends on clustered access.
 * 
 * <2do> this should not be a GenericSGOIDHeap derived class since this includes
 * a number of non-persistent data structures (allocCounts, intern strings, pinDowns) that
 * are not persistent, i.e. still requires a memento that creates/restores snapshots of these
 * and hence looses a lot of the benefit we use a persistent map for
 * 
 * NOTE - a reference value of 0 represents null and therefore is not a valid SGOID
 */
public class PSIMHeap extends GenericSGOIDHeap {
  
  /**
   * this sucks - we need a memento in order to store/restore allocCounts, internStrings and pinDownList
   */
  static class PSIMMemento extends GenericSGOIDHeapMemento {
    PSIntMap<ElementInfo> eiSnap;
    
    PSIMMemento (PSIMHeap heap) {
      super(heap);
      
      heap.elementInfos.process(ElementInfo.storer);
      eiSnap = heap.elementInfos; // no need to transform anything, it's persistent
    }

    @Override
    public Heap restore(Heap inSitu) {
      super.restore( inSitu);
      
      PSIMHeap heap = (PSIMHeap) inSitu;
      heap.elementInfos = eiSnap;
      heap.elementInfos.process(ElementInfo.restorer);
      
      return heap;
    }
  }
  
  class SweepPredicate implements Predicate<ElementInfo>{
    ThreadInfo ti;
    int tid;
    boolean isThreadTermination;
    
    protected void setContext() {
      ti = vm.getCurrentThread();
      tid = ti.getId();
      isThreadTermination = ti.isTerminated();      
    }
    
    @Override
    public boolean isTrue (ElementInfo ei) {
      
      if (ei.isMarked()){ // live object, prepare for next transition & gc cycle
        ei.setUnmarked();
        ei.setAlive( liveBitValue);          
        ei.cleanUp( PSIMHeap.this, isThreadTermination, tid);
        return false;
        
      } else { // object is no longer reachable  
        // no finalizer support yet
        ei.processReleaseActions();
        // <2do> still have to process finalizers here, which might make the object live again
        vm.notifyObjectReleased( ti, ei);
        return true;
      } 
    }
  }
  
  SweepPredicate sweepPredicate;
  PSIntMap<ElementInfo> elementInfos;
  
  
  public PSIMHeap (Config config, KernelState ks) {
    super(config,ks);
    
    elementInfos = new PSIntMap<ElementInfo>();    
    sweepPredicate = new SweepPredicate();
  }
  
  @Override
  public int size() {
    return elementInfos.size();
  }

  @Override
  protected void set(int index, ElementInfo ei) {
    elementInfos = elementInfos.set(index, ei);
  }

  @Override
  public ElementInfo get(int ref) {
    if (ref <= 0) {
      return null;
    } else {      
      return elementInfos.get(ref);
    }
  }

  @Override
  public ElementInfo getModifiable(int ref) {
    // <2do> this could probably use a specialized replaceValue() method
    
    if (ref <= 0) {
      return null;
    } else {
      ElementInfo ei = elementInfos.get(ref);

      if (ei != null && ei.isFrozen()) {
        ei = ei.deepClone(); 
        // freshly created ElementInfos are not frozen, so we don't have to defreeze
        elementInfos = elementInfos.set(ref, ei);
      }

      return ei;
    }
  }

  @Override
  protected void remove(int ref) {
    elementInfos = elementInfos.remove(ref);
  }
  
  @Override
  protected void sweep () {
    sweepPredicate.setContext();
    elementInfos = elementInfos.removeAllSatisfying( sweepPredicate);
  }
  
  @Override
  public Iterator<ElementInfo> iterator() {
    return elementInfos.iterator();
  }

  @Override
  public Iterable<ElementInfo> liveObjects() {
    return elementInfos;
  }

  @Override
  public void resetVolatiles() {
    // we don't have any
  }

  @Override
  public void restoreVolatiles() {
    // we don't have any
  }

  @Override
  public Memento<Heap> getMemento(MementoFactory factory) {
    return factory.getMemento(this);
  }

  @Override
  public Memento<Heap> getMemento() {
    return new PSIMMemento(this);
  }

}
