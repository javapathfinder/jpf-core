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
package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 * regression test for StopWatchFuzzer
 */
public class StopWatchFuzzerTest extends TestJPF {
  
  @Test
  public void testPaths() {
    
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
    }

    if (verifyNoPropertyViolation("+listener=.listener.StopWatchFuzzer")){
      long tStart = System.currentTimeMillis();
      System.out.println("some lengthy computation..");
      long tEnd = System.currentTimeMillis();
      
      if (tEnd - tStart <= 5000){
        System.out.println("all fine, finished in time");
        Verify.incrementCounter(0); // should get here two times, for < and ==
      } else {
        System.out.println("panic, we didn't make it in time");
        Verify.incrementCounter(1);
      }
    }
    
    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 2);
      assertTrue( Verify.getCounter(1) == 1);
    }
  }
}
