/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * * http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gov.nasa.jpf.test.util;

import gov.nasa.jpf.util.BitSet64;
import gov.nasa.jpf.util.FixedBitSet;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for gov.nasa.jpf.util.FixedBitSet (using BitSet64 implementation)
 */
public class FixedBitSetTest extends TestJPF {

  @Test
  public void testBasicSetGet() {
    FixedBitSet set = new BitSet64();
    for (int i = 0; i < 64; i++) {
      assertFalse("New set should be empty at " + i, set.get(i));
    }

    set.set(0);
    set.set(10);
    set.set(63);

    assertTrue(set.get(0));
    assertTrue(set.get(10));
    assertTrue(set.get(63));

    assertFalse(set.get(1));
    assertFalse(set.get(62));
  }

  @Test
  public void testClear() {
    FixedBitSet set = new BitSet64();
    
    set.set(42);
    assertTrue(set.get(42));
    
    set.clear(42);
    assertFalse("Bit should be false after clear", set.get(42));
  }

  @Test
  public void testCardinality() {
    FixedBitSet set = new BitSet64();
    
    assertEquals("Empty set should have 0 cardinality", 0, set.cardinality());
    
    set.set(1);
    set.set(5);
    set.set(60);
    
    assertEquals("Should count set bits correctly", 3, set.cardinality());
    
    set.clear(5);
    assertEquals("Should decrease count after clear", 2, set.cardinality());
  }

  @Test
  public void testToString() {
    FixedBitSet set = new BitSet64();
    set.set(1);
    set.set(3);
    
    String s = set.toString();
    assertTrue("String representation should contain index 1", s.contains("1"));
    assertTrue("String representation should contain index 3", s.contains("3"));
  }
}
