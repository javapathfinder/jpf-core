/*
 * Copyright (C) 2015, United States Government, as represented by the
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
package gov.nasa.jpf.util;

import java.util.NoSuchElementException;

/**
 * a RandomPermutationGenerator that keeps track of previously produced values
 * to avoid duplicates.
 * Note this only makes sense for relatively small sample sizes, but then again
 * that is what RandomPermutationGenerators are used for (to avoid N!)
 */
public class UniqueRandomPermGenerator extends RandomPermutationGenerator {
  
  protected SortedArrayIntSet visited;
  
  public UniqueRandomPermGenerator (int nElements, int nPermutations, int seed){
    super(nElements, nPermutations, seed);
    
    visited = new SortedArrayIntSet();
    this.nPermutations = Math.min( TotalPermutationGenerator.computeNumberOfPermutations(nElements), nPermutations);
  }
  
  public void reset(){
    super.reset();
    visited = new SortedArrayIntSet();
  }
    
  public int[] next(){    
    while (nGenerated < nPermutations){
      int[] p = super.next();
      int h = OATHash.hash(p);
      
      if (visited.add(h)){
        return p;
      } else {
        nGenerated--; // that one didn't count, we already have seen it
      }
    }
    throw new NoSuchElementException();
  }
}
