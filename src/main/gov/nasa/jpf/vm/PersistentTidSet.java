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
 * set that stores threads via (search global) thread ids. Used to detect shared objects/classes,
 * created by configured SharedObjectPolicy factory.
 * This set is persistent, i.e. does not modify contents of existing set instances to avoid
 * introducing search global state
 */
public class PersistentTidSet extends TidSet {
  
  public PersistentTidSet (ThreadInfo ti){
    super(ti);
  }  
  
  //--- non-destructive set update
  
  @Override
  public ThreadInfoSet add (ThreadInfo ti) {
    int id = ti.getId();
    
    if (!contains(id)){
      PersistentTidSet newSet = (PersistentTidSet)clone();
      newSet.add(id);
      return newSet;
      
    } else {
      return this;
    }
  }
  
  @Override
  public ThreadInfoSet remove (ThreadInfo ti) {
    int id = ti.getId();

    if (contains(id)){
      PersistentTidSet newSet = (PersistentTidSet)clone();
      newSet.remove(id);
      return newSet;
      
    } else {
      return this;
    }
  }
}
