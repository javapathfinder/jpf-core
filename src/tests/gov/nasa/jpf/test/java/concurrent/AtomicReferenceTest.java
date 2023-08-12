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
package gov.nasa.jpf.test.java.concurrent;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import org.junit.Test;

import java.util.concurrent.atomic.AtomicReference;

/**
 * raw test for java.util.concurrent.atomic.AtomicReference
 */
public class AtomicReferenceTest extends TestJPF {

  static {
    Verify.setProperties("cg.enumerate_cas=true");
  }

  @Test
  public void testNonWeakUpdates() {
    if (verifyNoPropertyViolation("+cg.enumerate_cas=true")) {
      final AtomicReference<String> ref = new AtomicReference<String>("initial value");

      assertTrue(ref.compareAndSet("initial value", "second value"));

      assertEquals(ref.get(), "second value");

      assertFalse(ref.compareAndSet("initial value", "don't set"));

      assertEquals(ref.get(), "second value");

      assertEquals(ref.getAndSet("final value"), "second value");

      assertEquals(ref.get(), "final value");
    }
  }
}
