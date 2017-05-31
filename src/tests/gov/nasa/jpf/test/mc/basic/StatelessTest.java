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
 * regression test for stateless (non-matching) execution mode
 */
public class StatelessTest extends TestJPF {

  @Test
  public void testNumberOfPaths(){
    
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation("+vm.storage.class=null")){
      int d = Verify.getInt(0, 5);
      d = 0;
      Verify.breakTransition("testNumberOfPaths"); // just to give the serializer something to chew on (if there is any)
      System.out.println("got here");
      Verify.incrementCounter(0);
    }
    
    if (!isJPFRun()){
      assert Verify.getCounter(0) == 6;
    }
  }
}
