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
package gov.nasa.jpf.test.vm.reflection;

import gov.nasa.jpf.util.test.TestJPF;

import java.lang.reflect.Array;

import org.junit.Test;

/**
 * Test class for primitive getters and setters in the <code>java.lang.reflect.Array</code> class.
 *  
 * @author Mirko Stojmenovic (mirko.stojmenovic@gmail.com)
 * @author Igor Andjelkovic (igor.andjelkovic@gmail.com)
 */
public class ArrayTest extends TestJPF {

  int[] arrayInt = new int[]{42};
  double[] arrayDouble = new double[]{42.0};

  @Test public void testArrayInt () {
    if (verifyNoPropertyViolation()){
      try {
        int i = Array.getInt(arrayInt, 0);
        assert i == 42;

        Array.setInt(arrayInt, 0, 43);
        assert arrayInt[0] == 43;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }

  @Test public void testArrayDouble () {
    if (verifyNoPropertyViolation()){
      try {
        double d = Array.getDouble(arrayDouble, 0);
        assert d == 42.0;

        Array.setDouble(arrayDouble, 0, 43.0);
        assert arrayDouble[0] == 43.0;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }

}
