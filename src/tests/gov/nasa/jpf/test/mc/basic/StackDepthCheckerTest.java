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

import org.junit.Test;

/**
 * regression test for StackDepthChecker listener
 */
public class StackDepthCheckerTest extends TestJPF {
  
  int n;
  
  void foo(){
    n++;
    System.out.print("entered foo() at level ");
    System.out.println(n);
    
    foo();
    
    n--; // not that we ever get here
    System.out.print("exited foo() at level ");
    System.out.println(n);
  }
  
  @Test 
  @SuppressWarnings("deprecation")
  public void testInfiniteRecursion (){
    if (verifyUnhandledException("java.lang.StackOverflowError", 
        "+listener=.listener.StackDepthChecker", "+sdc.max_stack_depth=42")){
      Thread t = Thread.currentThread();
      n = t.countStackFrames(); // it's deprecated, but we just want to make the printout more readable
      foo();
    }
  }

}
