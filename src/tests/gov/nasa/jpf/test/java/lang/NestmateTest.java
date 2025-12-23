/*
 * Copyright (C) 2018, United States Government, as represented by the
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
import static org.junit.Assert.*;
public class NestmateTest extends TestJPF{

    public class Inner {}

    @Test
    public void testNestHost() {
        if (verifyNoPropertyViolation()) {
            Class<?> host = NestmateTest.class.getNestHost();
            assertEquals(NestmateTest.class, host);
            Class<?> innerHost = Inner.class.getNestHost();
            assertEquals(NestmateTest.class, innerHost);

            System.out.println("testNestHost passed!");
        }
    }

    @Test
    public void testIsNestmateOf() {
        if (verifyNoPropertyViolation()) {
            assertTrue(NestmateTest.class.isNestmateOf(Inner.class));
            assertTrue(Inner.class.isNestmateOf(NestmateTest.class));

            System.out.println("testIsNestmateOf passed!");
        }
    }
}
