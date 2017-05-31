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

import gov.nasa.jpf.util.OATHash;

/**
 * helper class for search global object id (SGOID) computation. This
 * captures both allocation context and count.
 * 
 * NOTE: this is used as a key for associative arrays, but we do
 * allow destructive updates via init() in order to enable key
 * caching for lookups that don't lead to new entries. 
 * THE CALLER HAS TO MAKE SURE init() IS NEVER CALLED ON A STORED KEY !!
 */
public class Allocation {
  
  final AllocationContext context;
  final int count;
  final int hash;
  
  public Allocation (AllocationContext context, int count){
    this.context = context;
    this.count = count;
    this.hash = OATHash.hash(context.hashCode(), count);
  }
  
  @Override
  public boolean equals (Object o) {
    if (o instanceof Allocation) {
      Allocation other = (Allocation)o;
      
      if (other.hash == hash) {
        if (other.context.equals(context)) {
          return true;
        }
      }
    }
    
    return false;
  }
  
  @Override
  public int hashCode() {
    return hash;
  }
}
