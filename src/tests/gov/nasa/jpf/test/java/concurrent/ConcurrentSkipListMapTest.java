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

import java.util.concurrent.ConcurrentSkipListMap;

import org.junit.Test;

/**
 * test for java.util.concurrent.ConcurrentSkipListMap
 */
public class ConcurrentSkipListMapTest extends TestJPF {

  @Test
  public void testFunctionality() throws Exception {
    if (verifyNoPropertyViolation()) {
      ConcurrentSkipListMap<Integer, Integer> map =
          new ConcurrentSkipListMap<>();
      map.put(1, 3);

      Thread t1 = new Thread(() -> {
        map.put(2, 2);
      });
      Thread t2 = new Thread(() -> {
        map.put(3, 1);
      });
      t1.start();
      t2.start();
      t1.join();
      t2.join();

      assertTrue(map.get(1) == 3);
      assertTrue(map.firstKey() == 1);
      assertTrue(map.lastKey() == 3);
      assertTrue(map.pollFirstEntry().getValue() == 3);
      assertTrue(map.pollLastEntry().getValue() == 1);
    }
  }
}
