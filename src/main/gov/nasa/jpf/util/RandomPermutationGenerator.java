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
import java.util.Random;

/**
 * a permutation generator that uses the Fisher-Yates shuffle
 * (Durstenfeld, R. (July 1964). "Algorithm 235: Random permutation". 
 * Communications of the ACM 7 (7): 420)
 * 
 * use this if TotalPermutations would be too large and PairPermutations
 * not enough
 */
public class RandomPermutationGenerator extends PermutationGenerator {

  protected int seed;
  protected Random rand;
  
  protected int[] orig;
    
  public RandomPermutationGenerator (int nElements, int nPermutations, int seed){
    super(nElements);
    this.nPermutations = nPermutations;
    rand = new Random(seed);
    orig = permutation.clone();
  }
  
  @Override
  protected long computeNumberOfPermutations() {
    return nPermutations; // it's input (set)
  }

  @Override
  public void reset() {
    initPermutations();
    rand = new Random(seed);
    nGenerated = 0;
  }

  @Override
  public int[] next() {
    if (nGenerated == 0){
      nGenerated = 1;
      return permutation;
      
    } else if (nGenerated < nPermutations){
      permutation = orig.clone();
      for (int i=0; i<nElements; i++){
        int r = i + rand.nextInt( nElements-i);  // i <= r < nElements-1
        swap(permutation, r, i);
      }        
      nGenerated++;
      return permutation;
        
    } else {
      throw new NoSuchElementException();
    }
  }
}
