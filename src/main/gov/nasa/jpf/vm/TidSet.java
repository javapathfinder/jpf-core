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

import gov.nasa.jpf.util.UnsortedArrayIntSet;

/**
 * set that stores threads via (search global) thread ids. Used to detect shared objects/classes,
 * created by configured SharedObjectPolicy factory
 * 
 * Note - this class modified contents of instances, i.e. it does destructive updates
 * and hence has state carry-over between paths
 */
public class TidSet extends UnsortedArrayIntSet implements ThreadInfoSet, Memento<ThreadInfoSet> {
  
  protected VM vm;
  
  public TidSet (ThreadInfo ti){
    vm = ti.getVM();
    
    add( ti.getId());
  }  
  
  //--- set update
  
  @Override
  public ThreadInfoSet add (ThreadInfo ti) {
    add( ti.getId());
    return this;
  }
  
  @Override
  public ThreadInfoSet remove (ThreadInfo ti) {
    remove( ti.getId());
    return this;
  }
  
  
  //--- set query
  
  @Override
  public boolean contains (ThreadInfo ti) {
    return contains( ti.getId());
  }
  
  @Override
  public boolean isShared (ThreadInfo ti, ElementInfo ei){
    return hasMultipleLiveThreads();
  }
  
  @Override
  public boolean hasMultipleLiveThreads(){
    if (size == 0){
      return false;
      
    } else {
      boolean alreadyHadOne = false;
      ThreadList tl = vm.getThreadList();
      
      for (int i=0; i<size; i++){
        ThreadInfo ti = tl.getThreadInfoForId(elements[i]);
        if (ti != null && !ti.isTerminated()){
          if (alreadyHadOne){
            return true;
          }
          alreadyHadOne = true;
        }
      }
      
      return false;
    }
  }

  @Override
  public boolean hasMultipleRunnableThreads(){
    if (size == 0){
      return false;
      
    } else {
      boolean alreadyHadOne = false;
      ThreadList tl = vm.getThreadList();
      
      for (int i=0; i<size; i++){
        ThreadInfo ti = tl.getThreadInfoForId(elements[i]);
        if (ti != null && ti.isRunnable()){
          if (alreadyHadOne){
            return true;
          }
          alreadyHadOne = true;
        }
      }
      
      return false;
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append(getClass().getName());
    sb.append('{');
    for (int i = 0; i<size; i++) {
      if (i>0) {
        sb.append(',');
      }
      sb.append(elements[i]);
    }
    sb.append('}');
    
    return sb.toString();
  }

  
  //--- state management (TidSet instance per default are their own mementos)
  
  @Override
  public Memento<ThreadInfoSet> getMemento(){
    return this;
  }

  @Override
  public ThreadInfoSet restore(ThreadInfoSet inSitu) {
    return this;
  }
}
