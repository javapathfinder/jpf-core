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
 * abstraction for the container of StaticElementInfos, which manages static fields.
 * Note that there is a Statics instance per ClassLoaderInfo, i.e. ids are only unique within each
 * ClassLoader namespace.
 * 
 * This container is only growing - we don't remove/recycle classes yet
 * 
 * Since Statics instances have to be obtained from their respective ClassLoaderInfo, and
 * ClassLoaderInfos are the ones that map type names to ClassInfos, Statics does not include
 * methods for name lookup. This allows implementors to use efficient lookup based on the numerical
 * ClassInfo id (which is only unique within this Statics / ClassLoader namespace)
 */
public interface Statics extends Iterable<ElementInfo> {

  //--- construction
  
  /**
   * startup classes are registered and initialized in two steps since object
   * creation has to be deferred until we have at least Object and Class ClassInfos
   */
  StaticElementInfo newStartupClass (ClassInfo ci, ThreadInfo ti);
  
  /**
   * this returns the search global id which is unique within this ClassLoader namespace.
   * This id is also stored in the respective java.lang.Class object
   */
  StaticElementInfo newClass (ClassInfo ci, ThreadInfo ti, ElementInfo eiClsObj);
  
  
  //--- accessors 
  
  /**
   * get an ElementInfo that might or might not be suitable for modification. This should only
   * be used when retrieving field values. The 'id' argument has to be the result of a previous 'newClass()' call
   */
  StaticElementInfo get (int id);
  
  /**
   * get an ElementInfo that is guaranteed to be modifiable. This should be used when modifying
   * field values.  The 'id' argument has to be the result of a previous 'newClass()' call
   */
  StaticElementInfo getModifiable (int id);

  
  //--- housekeeping
  
  Iterable<StaticElementInfo> liveStatics();
  
  void markRoots (Heap heap);
  void cleanUpDanglingReferences (Heap heap);
  
  
  //--- state management
  
  Memento<Statics> getMemento(MementoFactory factory);
  Memento<Statics> getMemento();
  
  int size();
}
