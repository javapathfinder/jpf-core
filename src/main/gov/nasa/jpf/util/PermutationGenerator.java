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

import java.io.PrintStream;

/**
 * base type for permutation generators
 */
public abstract class PermutationGenerator {
  
  protected final int nElements;
  
  protected int[] permutation;   // array containing permutation

  protected long nPermutations;
  protected long nGenerated;

  protected PermutationGenerator (int nElements){
    this.nElements = nElements;
    nPermutations = computeNumberOfPermutations();
    
    initPermutations();
  }
  
  protected void initPermutations (){
    permutation = new int[nElements];
    
    // initialize element array in order, starting with firstIdx
    for (int i=0; i<nElements; i++){
      permutation[i] = i;
    }
    
    nGenerated = 0;
  }
  
  protected abstract long computeNumberOfPermutations();
  public abstract void reset();
  
  public long getNumberOfPermutations(){
    return nPermutations;
  }
  
  public long getNumberOfGeneratedPermutations(){
    return nGenerated;
  }
 
  static void swap(int[] a, int i, int j){
    int tmp = a[j];
    a[j] = a[i];
    a[i] = tmp;
  }

  /**
   * for debugging purposes 
   */
  public void printOn (PrintStream ps){
    printOn( ps, nGenerated, permutation);
  }

  public static void printOn (PrintStream ps, long nGenerated, int[] perm){
    ps.printf("%2d: [", nGenerated);
    for (int k=0; k<perm.length; k++){
      if (k > 0) ps.print(',');
      ps.print(perm[k]);
    }
    ps.println(']');    
  }
  
  
  //--- the public iteration interface, following Iterator
  public boolean hasNext(){
    return (nGenerated < nPermutations);
  }
  
  /**
   * return the next permutation or throw a NoSuchElementException if there is none.
   * 
   * NOTE - this does not guarantee to return a different object on each call,
   * i.e. the caller has to clone if the result is stored directly
   */
  public abstract int[] next(); // the work horse, throws NoSuchElementException
}
