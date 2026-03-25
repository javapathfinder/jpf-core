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
package gov.nasa.jpf.test.java.util;

import gov.nasa.jpf.util.SimplePool;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for gov.nasa.jpf.util.SimplePool
 */
public class SimplePoolTest extends TestJPF {

    @Test
    public void testDeduplication() {
        SimplePool<String> pool = new SimplePool<>(4);

        // Create two distinct string objects with the same value
        String s1 = new String("testString");
        String s2 = new String("testString");

        // Ensure they are different objects in memory
        assertFalse("Strings should be distinct objects", s1 == s2);

        // Pool the first one
        String p1 = pool.pool(s1);
        assertSame("First add should return the argument", s1, p1);
        assertTrue("Element should now be pooled", pool.isPooled(s1));

        // Pool the second one
        String p2 = pool.pool(s2);
        assertSame("Second add should return the cached object", s1, p2);
        assertNotSame("Should not return the new duplicate argument", s2, p2);
    }

    @Test
    public void testQuery() {
        SimplePool<Integer> pool = new SimplePool<>(4);
        Integer val = 42;

        assertNull("Query on empty pool should return null", pool.query(val));
        assertFalse("IsMember should be false", pool.isMember(val));

        pool.add(val);

        assertEquals("Query should return the element", val, pool.query(val));
        assertTrue("IsMember should be true", pool.isMember(val));
    }

    @Test
    public void testGrowthAndRehash() {
        // Start with a small capacity (pow 1 -> size 2)
        SimplePool<Integer> pool = new SimplePool<>(1);
        int count = 100;
        // Add many elements to force the pool to resize/rehash multiple times
        for (int i = 0; i < count; i++) {
            pool.add(i);
        }
        // Verify all elements are still there after resizing
        for (int i = 0; i < count; i++) {
            assertTrue("Pool should contain " + i + " after resize", pool.isMember(i));
            assertEquals(Integer.valueOf(i), pool.query(i));
        }
        assertFalse("Pool should not contain elements not added", pool.isMember(count + 1));
    }
}