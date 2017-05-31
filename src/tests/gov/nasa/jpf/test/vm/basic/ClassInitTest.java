/*
 * Copyright (C) 2015, United States Government, as represented by the
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
package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * basic regression test for class initialization
 */
public class ClassInitTest extends TestJPF {

    static class Root {
        static int data;
        static {
            System.out.println("in Root.<clinit>()");
            data = 42;
        }
    }

    static class Base extends Root {
        static int data;
        static {
            System.out.println("in Base.<clinit>()");
            data = Root.data + 1;
        }
    }

    static class Derived extends Base {
        static int data;
        static {
            System.out.println("in Derived.<clinit>()");
            data = Base.data + 1;
        }
    }

    @Test
    public void testClinits (){
        if (verifyNoPropertyViolation()){
            int n = Derived.data;
            assertTrue(n == 44);
        }
    }
}
