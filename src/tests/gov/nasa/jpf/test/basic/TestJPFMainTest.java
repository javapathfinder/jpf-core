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
package gov.nasa.jpf.test.basic;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

public class TestJPFMainTest extends TestJPF {
  
  public static void main(String testMethods[]) throws Throwable {
    runTestsOfThisClass(testMethods);
  }

  @Test
  public void ensureCompatibility() {
    if (!Verify.isRunningInJPF()) {
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation()) {
      System.out.println("incrementing test counter");
      Verify.incrementCounter(0);
      
    } else { // Runs after JPF finishes
      assertEquals(1, Verify.getCounter(0));
    }
  }
}
