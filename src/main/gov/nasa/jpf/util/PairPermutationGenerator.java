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
 * a generator for pair-wise permutations, which only considers permutations
 * for each pair of elements, regardless of position. This reduces the
 * number of generated permutations from N! to sum(i=1..N){N-i} + 1.
 * This can be used to test order dependencies between two concurrent
 * entities (threads, state machines etc), based on the same assumptions
 * that are used in pair-wise testing
 */
public class PairPermutationGenerator extends PermutationGenerator {

  protected int i, j;

  public PairPermutationGenerator (int nElements){
    super(nElements);
  }

  @Override
  public void reset(){
    initPermutations();
    i = 0;
    j = 0;
  }
  
  public static long computeNumberOfPermutations (int n){
    long v = 1;
    for (int l=1; l<n; l++){
      v += (n - l);
    }
    return v;
  }
  
  @Override
  protected long computeNumberOfPermutations(){
    return computeNumberOfPermutations(nElements);
  }
          
  @Override
  public int[] next (){
    int n = permutation.length;

    if (nGenerated == 0){ // the initial order
      nGenerated = 1;
      return permutation;
      
    } else if (nGenerated > 1){
      if (nGenerated == nPermutations){
        throw new NoSuchElementException();
      }
      swap(permutation, i, j); // revert last permutation
    }


    if (++j == n){
      if (++i == n){
        throw new NoSuchElementException();
      } else {
        j = i+1;
      }
    }

    swap(permutation, i, j);
    nGenerated++;
    return permutation;
  }

}
