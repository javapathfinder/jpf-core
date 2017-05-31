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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import org.junit.Test;

/**
 * test non-deterministic init of final fields
 */
public class FinalFieldChoiceTest extends TestJPF {

  
  //--- instance fields
  
  static class X {
    final boolean a;
    final boolean b;
    
    X(){
      a = Verify.getBoolean();
      b = Verify.getBoolean();
    }
  }
  
  @Test
  public void testFinalInstanceFields(){
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
    }
    
    if (verifyNoPropertyViolation()){
      X x = new X();
      System.out.print("a=");
      System.out.print(x.a);
      System.out.print(", b=");
      System.out.println(x.b);
      
      Verify.incrementCounter(0);
      
      int n = Verify.getCounter(1);
      if (x.a && x.b) Verify.setCounter(1, n+3);
      else if (x.a) Verify.setCounter(1, n+2);
      else if (x.b) Verify.setCounter(1, n+1);
    }
    
    if (!isJPFRun()){
      assertTrue( "wrong number of choices", Verify.getCounter(0) == 4);
      assertTrue( "wrong choice values", Verify.getCounter(1) == 6);
    }    
  }
  
  //--- static fields
  static class Y {
    static final boolean a = Verify.getBoolean();
    static final boolean b = Verify.getBoolean();
  }
  
  @Test 
  public void testFinalStaticFields(){
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
    }
    
    if (verifyNoPropertyViolation()){
      boolean a = Y.a;
      boolean b = Y.b;
      
      System.out.print("Y.a=");
      System.out.print(a);
      System.out.print(", Y.b=");
      System.out.println(b);
      
      Verify.incrementCounter(0);
      
      int n = Verify.getCounter(1);
      if (Y.a && Y.b) Verify.setCounter(1, n+3);
      else if (Y.a) Verify.setCounter(1, n+2);
      else if (Y.b) Verify.setCounter(1, n+1);
    }
    
    if (!isJPFRun()){
      assertTrue( "wrong number of choices", Verify.getCounter(0) == 4);
      assertTrue( "wrong choice values", Verify.getCounter(1) == 6);
    }  
  }
}
