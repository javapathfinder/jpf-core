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

public class OOMEInjectorTest extends TestJPF {

  @Test
  public void testDirectLoc () {
    if (verifyUnhandledException("java.lang.OutOfMemoryError", "+listener=.listener.OOMEInjector",
                                  "+oome.locations=OOMEInjectorTest.java:32")){
      Object o = new Integer(42);
    }
  }
  
  
  static int bar(int y){
    Integer res = new Integer(y);  // this should fail
    return res;
  }
  
  static int foo (int x){
    int res = x + bar(42);
    return res;
  }
  
  @Test
  public void testScope(){
    if (verifyUnhandledException("java.lang.OutOfMemoryError", "+listener=.listener.OOMEInjector",
        "+oome.locations=OOMEInjectorTest.java:52-53")){
      bar(4200); // this should be Ok
      int res = foo(42);  // this should cause an OOME
      System.out.println("should never get here!");
    }    
  }
  
  static class DontAllocateMe {}
  static class X extends DontAllocateMe {}
  
  @Test
  public void testType(){
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation("+listener=.listener.OOMEInjector",
        "+oome.types=*DontAllocateMe+")){
      try {
        X x = new X(); // that should trip an OOME
      } catch (OutOfMemoryError oome){
        oome.printStackTrace();
        Verify.incrementCounter(0);
      }
    }
    
    if (!isJPFRun()){
      assertEquals(1, Verify.getCounter(0));
    }
  }
}
