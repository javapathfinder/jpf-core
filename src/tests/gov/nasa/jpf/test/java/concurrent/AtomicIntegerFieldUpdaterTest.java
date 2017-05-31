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

import java.util.concurrent.atomic.AtomicIntegerFieldUpdater;

import org.junit.Test;

/**
 * raw test for java.util.concurrent.atomic.AtomicIntegerFieldUpdater
 */
public class AtomicIntegerFieldUpdaterTest extends TestJPF {

  int value;

  @Test
  public void testField() {

    if (verifyNoPropertyViolation()) {
      AtomicIntegerFieldUpdater<AtomicIntegerFieldUpdaterTest> upd =
              AtomicIntegerFieldUpdater.newUpdater(AtomicIntegerFieldUpdaterTest.class, "value");

      final int v1 = 98734534;
      final int v2 = 79304843;
      final int nogo = 46907854;
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
