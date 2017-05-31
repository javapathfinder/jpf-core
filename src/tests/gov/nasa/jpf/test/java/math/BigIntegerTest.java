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
package gov.nasa.jpf.test.java.math;

import gov.nasa.jpf.util.test.TestJPF;

import java.math.BigInteger;

import org.junit.Test;

public class BigIntegerTest extends TestJPF {

  /************************** test methods ************************/
  @Test
  public void testArithmeticOps() {
    if (verifyNoPropertyViolation()) {
      System.out.println("testing arithmetic operations of BigInteger objects");

      BigInteger big = new BigInteger("4200000000000000000");
      BigInteger o = new BigInteger("100000000000000");
      BigInteger notSoBig = new BigInteger("1");

      BigInteger x = big.add(notSoBig);
      String s = x.toString();
      System.out.println("x = " + s);
      assert s.equals("4200000000000000001");

      x = big.divide(o);
      int i = x.intValue();
      assert i == 42000;
    }
  }
}
