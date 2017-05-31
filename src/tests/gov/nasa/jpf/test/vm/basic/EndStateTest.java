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

package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * test SUT end states
 */
public class EndStateTest extends TestJPF {

  @Test 
  public void testSingleThread () {
    if (verifyNoPropertyViolation("+listener=.test.vm.basic.EndStateListener")){
      System.out.println("** this is testSingleThread - it should succeed");      
    }
  }

  @Test 
  public void testMultipleThreads () {
    if (verifyNoPropertyViolation("+listener=.test.vm.basic.EndStateListener")){
      System.out.println("** this is testMultipleThreads - it should succeed");

      Thread t = new Thread() {
        @Override
		public synchronized void run() {
          System.out.println("** this is " + Thread.currentThread().getName() + " terminating");
        }
      };
      t.start();

      synchronized(this){
        System.out.println("** this is thread main terminating");
      }
    }
  }
}
