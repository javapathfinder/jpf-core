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

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * regression test for PermutationGenerator
 */
public class PermutationGeneratorTest extends TestJPF {
  
  @Test
  public void testTotalPermutation(){
    PermutationGenerator pg = new TotalPermutationGenerator(4);
    long nPerm = pg.getNumberOfPermutations();
    assertTrue( nPerm == 24);
    
    while (pg.hasNext()){
      int[] perms = pg.next();
      assertTrue(perms != null);
      pg.printOn(System.out);
    }
  }
  
  @Test
  public void testPairPermutation(){
    PermutationGenerator pg = new PairPermutationGenerator(4);
    long nPerm = pg.getNumberOfPermutations();
    assertTrue( nPerm == 7);
    
    while (pg.hasNext()){
      int[] perms = pg.next();
      assertTrue(perms != null);
      pg.printOn(System.out);
    }
  }

  @Test
  public void testRandomPermutation(){
    int nPermutations = 14;
    PermutationGenerator pg = new RandomPermutationGenerator(4, nPermutations, 42);
    long nPerm = pg.getNumberOfPermutations();
    assertTrue( nPerm == nPermutations);
    
    System.out.println("this CAN have duplicates");
    while (pg.hasNext()){
      int[] perms = pg.next();
      assertTrue(perms != null);
      pg.printOn(System.out);
    }    
  }
  
  boolean isEqual (int[] a, int[] b){
    if (a.length == b.length){
      for (int i=0; i<a.length; i++){
        if (a[i] != b[i]){
          return false;
        }
      }
      return true;
    }
    return false;
  }
  
  @Test
  public void testUniqueRandomPermutation(){
    int nPermutations = 14;
    PermutationGenerator pg = new UniqueRandomPermGenerator(4, nPermutations, 42);
    long nPerm = pg.getNumberOfPermutations();
    assertTrue( nPerm == nPermutations);
    
    int[][] seen = new int[nPermutations][];
    int n = 0;
    
    System.out.println("this should NOT have duplicates");
    
    while (pg.hasNext()){
      int[] perms = pg.next();
      assertTrue(perms != null);
      pg.printOn(System.out);
      
      for (int i=0; i<n; i++){
        assertFalse(isEqual(seen[i], perms));
      }
      seen[n++] = perms.clone();
    }    
  }

  @Test
  public void testMaxUniqueRandomPermutation(){
    int nPermutations = 14; // too high, this only has 3! different permutations
    PermutationGenerator pg = new UniqueRandomPermGenerator(3, nPermutations, 42);
    long nPerm = pg.getNumberOfPermutations();
    assertTrue( nPerm == 6);

    while (pg.hasNext()){
      int[] perms = pg.next();
      assertTrue(perms != null);
      pg.printOn(System.out);
    }
  }
}
