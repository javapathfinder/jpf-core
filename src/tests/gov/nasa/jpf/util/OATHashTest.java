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

import java.util.HashSet;
import java.util.Random;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import static gov.nasa.jpf.util.OATHash.*;

/**
 * just the very basic sanity checks for a hash function.
 * <2do> should add a test for uniformity of hash vals and measure collisions in medium bin size constraints
 */
public class OATHashTest extends TestJPF {

  @Test
  public void testRandom(){
    int maxRounds = 256;
    int maxKey = 8;
    Random r = new Random(42);
    HashSet<Integer> seen = new HashSet<Integer>(); // not very smart, but the number of rounds is reasonably small
    
    for (int i=0; i<maxRounds; i++){
      int h = 0;
      for (int j=0; j<maxKey; j++){
        int x = r.nextInt();
        h = hashMixin(h, x);
        
        if (j>0){
          System.out.print(',');
        }
        System.out.print(Integer.toHexString(x));
      }
      h = hashFinalize(h);
      System.out.print(" => ");
      System.out.println(h);
      
      if (seen.contains(h)){
        fail("collision on round " + i);
      }
      seen.add(h);
    }
  }
  
  @Test
  public void testRandomSmall(){
    int maxRounds = 256;
    int maxKey = 4;
    int maxVal = 32;
    Random r = new Random(42);
    HashSet<Integer> seen = new HashSet<Integer>(); // not very smart, but the number of rounds is reasonably small
    
    for (int i=0; i<maxRounds; i++){
      int h = 0;
      for (int j=0; j<maxKey; j++){
        int x = r.nextInt(maxVal);
        h = hashMixin(h, x);
        
        if (j>0){
          System.out.print(',');
        }
        System.out.print(x);
      }
      h = hashFinalize(h);
      System.out.print(" => ");
      System.out.println(h);
      
      if (seen.contains(h)){
        fail("collision on round " + i);
      }
      seen.add(h);
    }
  }
  
}
