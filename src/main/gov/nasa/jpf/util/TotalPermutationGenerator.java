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
package gov.nasa.jpf.util;

import java.util.NoSuchElementException;

/**
 * a pull mode generator for permutations that executes in constant space,
 * using Ives' algorithm (F. M. Ives: Permutation enumeration: four new 
 * permutation algorithms, Comm. ACM, 19, 2, Feb 1976, pg. 68-72)
 * 
 * NOTE - this is mostly here for completeness, since use of full permutations
 * in most cases is prohibitive due to N!
 */
public class TotalPermutationGenerator extends PermutationGenerator {
  
  protected int[] inverse; // inverse permutations array
    
  public TotalPermutationGenerator (int nElements){
    super( nElements);
    
    initInverse();
  }
  
  void initInverse (){
    inverse = new int[nElements];
    for (int i=0; i<nElements; i++){
      inverse[i] = i;
    }    
  }
  
  @Override
  public void reset(){
    initPermutations();
    initInverse();
  }
  
  
  public static long computeNumberOfPermutations(int nElements){
    long n = 1;
    for (int i=1; i<=nElements; i++){
      n *= i;
    }
    return n;    
  }
  
  @Override
  protected long computeNumberOfPermutations(){
    return computeNumberOfPermutations(nElements);
  }
    
  @Override
  public int[] next (){
    if (nGenerated == 0){
      nGenerated =1;
      return permutation;
      
    } else {
      for (int lower=0, upper=nElements-1; upper > lower; lower++, upper--){
        int i = inverse[lower];
        int j = (i == upper) ? lower : i+1;
        int pj = permutation[j];

        permutation[i] = pj;
        permutation[j] = lower;

        inverse[lower] = j;
        inverse[pj] = i;

        if ((permutation[lower] != lower) || (permutation[upper] != upper)){
          nGenerated++;
          return permutation;
        }
      }
      throw new NoSuchElementException();
    }
  }  
}
