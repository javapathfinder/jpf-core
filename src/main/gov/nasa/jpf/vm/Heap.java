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
 * this is our implementation independent model of the heap
 */
public interface Heap extends Iterable<ElementInfo> {

  //--- this is the common heap client API

  ElementInfo get (int objref);
  ElementInfo getModifiable (int objref);

  void gc();

  boolean isOutOfMemory();

  void setOutOfMemory(boolean isOutOfMemory);

  //--- the allocator primitives
  ElementInfo newArray (String elementType, int nElements, ThreadInfo ti);
  ElementInfo newObject (ClassInfo ci, ThreadInfo ti);
  
  ElementInfo newSystemArray (String elementType, int nElements, ThreadInfo ti, int anchor);
  ElementInfo newSystemObject (ClassInfo ci, ThreadInfo ti, int anchor);

  //--- convenience allocators that avoid constructor calls
  // (those are mostly used for their reference values since they already have initialized fields,
  // but to keep it consistent we use ElementInfo return types)
  ElementInfo newString (String str, ThreadInfo ti);
  ElementInfo newSystemString (String str, ThreadInfo ti, int anchor);
  
  ElementInfo newInternString (String str, ThreadInfo ti);
  
  ElementInfo newSystemThrowable (ClassInfo ci, String details, int[] stackSnapshot, int causeRef,
                          ThreadInfo ti, int anchor);
  
  Iterable<ElementInfo> liveObjects();

  int size();

  //--- system internal interface


  //void updateReachability( boolean isSharedOwner, int oldRef, int newRef);

  void markThreadRoot (int objref, int tid);

  void markStaticRoot (int objRef);

  // these update per-object counters - object will be gc'ed if it goes to zero
  void registerPinDown (int objRef);
  void releasePinDown (int objRef);

  void unmarkAll();

  void cleanUpDanglingReferences();

  boolean isAlive (ElementInfo ei);

  void registerWeakReference (ElementInfo ei);

  // to be called from ElementInfo.markRecursive(), to avoid exposure of
  // mark implementation
  void queueMark (int objref);

  boolean hasChanged();


  // <2do> this will go away
  void markChanged(int objref);

  void resetVolatiles();

  void restoreVolatiles();

  void checkConsistency (boolean isStateStore);


  Memento<Heap> getMemento(MementoFactory factory);
  Memento<Heap> getMemento();
}
