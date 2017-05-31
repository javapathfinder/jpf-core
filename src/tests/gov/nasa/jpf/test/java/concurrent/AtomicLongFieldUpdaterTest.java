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

import java.util.concurrent.atomic.AtomicLongFieldUpdater;

import org.junit.Test;

/**
 * raw test for java.util.concurrent.atomic.AtomicLongFieldUpdater
 */
public class AtomicLongFieldUpdaterTest extends TestJPF {

  static {
    Verify.setProperties("cg.enumerate_cas=true");
  }
  long value;

  @Test
  public void testField() {
    if (verifyNoPropertyViolation("+cg.enumerate_cas=true")) {
      AtomicLongFieldUpdater<AtomicLongFieldUpdaterTest> upd =
              AtomicLongFieldUpdater.newUpdater(AtomicLongFieldUpdaterTest.class, "value");

      final long v1 = 723489234098734534L;
      final long v2 = 256092348679304843L;
      final long nogo = 823468902346907854L;
      value = v1;

      assert upd.compareAndSet(this, v1, v2);
      assert value == v2;

      assert !upd.compareAndSet(this, v1, nogo);
      assert value == v2;

      assert value == upd.get(this);

      assert v2 == upd.getAndSet(this, v1);
      assert value == v1;

      upd.set(this, v2);
      assert value == v2;

      upd.lazySet(this, v1);
      assert value == v1;

      assert upd.weakCompareAndSet(this, v1, v2);
      assert value == v2;

      assert !upd.weakCompareAndSet(this, v1, nogo);
      assert value == v2;

      assert v2 == upd.getAndAdd(this, 5);
      assert v2 + 5 == value;
    }
  }
}
