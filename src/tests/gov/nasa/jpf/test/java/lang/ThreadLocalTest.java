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

package gov.nasa.jpf.test.java.lang;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class ThreadLocalTest extends TestJPF {

  @Test
  public void testThreadLocalWithInitial() {
    if (verifyNoPropertyViolation()) {
      ThreadLocal<Integer> threadLocal = ThreadLocal.withInitial(() -> 64);

      //verify assigned value
      int val = threadLocal.get();
      assertEquals(64, val);

      //set a new value and verify
      threadLocal.set(66);
      int anotherVal = threadLocal.get();
      assertEquals(66, anotherVal);
    }
  }
}
