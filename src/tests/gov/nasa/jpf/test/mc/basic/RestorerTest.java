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
package gov.nasa.jpf.test.mc.basic;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

/**
 * regression test for on-demand state restoration by means of
 * ClosedMementos
 */
public class RestorerTest extends TestJPF {

  static class X {
    int id;
    public void whoami() {
      System.out.print("I am X #");
      System.out.println(id);
    }
  }
  
  @Test
  public void testRestoredInsnCount (){
    if (verifyNoPropertyViolation()){
      
      boolean b = Verify.getBoolean();
      System.out.println( "--- 1. CG: " + b);
      
      for (int i=1; i<=5; i++){
        X x = new X();
        x.whoami();
        assert x.id == i;
      }

      b = Verify.getBoolean();
      System.out.println( "--- 2. CG: " + b);
      
      X x = new X();
      x.whoami();
      assert x.id == 1; // different location, so we restart with 1
    }
  }
  
  @Test
  public void testRestoredInsnCountBFS (){
    if (verifyNoPropertyViolation("+search.class=.search.heuristic.BFSHeuristic")){
      
      boolean b = Verify.getBoolean();
      System.out.println( "--- 1. CG: " + b);
      
      for (int i=1; i<=5; i++){
        X x = new X();
        x.whoami();
        assert x.id == i;
      }

      b = Verify.getBoolean();
      System.out.println( "--- 2. CG: " + b);
      
      X x = new X();
      x.whoami();
      assert x.id == 1; // different location, so we restart with 1
    }
  }
}
