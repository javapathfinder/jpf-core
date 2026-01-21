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

import gov.nasa.jpf.util.WeakPool;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for gov.nasa.jpf.util.WeakPool
 */
public class WeakPoolTest extends TestJPF {

    @Test
    public void testBasicPooling() {
        WeakPool<Integer> pool = new WeakPool<>(4);
        Integer val = 100;
        Integer p1 = pool.pool(val);
        assertSame(val, p1);
        assertTrue(pool.isPooled(val));
        Integer val2 = new Integer(100);
        Integer p2 = pool.pool(val2);

        assertSame(val, p2);
        assertNotSame(val2, p2);
    }

    @Test
    public void testWeakReferenceBehavior() {
        WeakPool<Object> pool = new WeakPool<>(16);

        // Create an object and pool it
        Object obj = new Object();
        pool.pool(obj);
        assertTrue("Object should be in pool", pool.isPooled(obj));

        // Remove our strong reference
        obj = null;
        System.gc();
        for (int i = 0; i < 1000; i++) {
            pool.pool(new String("GarbageCollectMe" + i));
        }
    }

    @Test
    public void testGrowth() {
        WeakPool<Integer> pool = new WeakPool<>(1); // Size 2
        // Add more elements than capacity to force resize
        for (int i = 0; i < 10; i++) {
            pool.pool(i);
        }
        // Verify they are still there
        for (int i = 0; i < 10; i++) {
            assertTrue("Pool should contain " + i, pool.isPooled(i));
        }
    }
}