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

package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.json.CGCreator;
import gov.nasa.jpf.util.json.Value;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.BooleanChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 *
 * @author Ivan Mushketik
 */
public class CGCreatorFactoryTest extends TestJPF {
static class TestBoolCGCreator implements CGCreator {

    @Override
	public ChoiceGenerator createCG(String id, Value[] values) {
      return new BooleanChoiceGenerator(id);
    }

  }

  class B {

    boolean b;

    public B(boolean b) {
      this.b = b;
    }

    @Override
    public boolean equals(Object o) {
      B other = (B) o;

      return this.b == other.b;
    }

  }

  @Test
  public void testAddUserDefinedCGCreator() {
    if (verifyNoPropertyViolation("+jpf-core.native_classpath+=;${jpf-core}/build/tests",
            "+jpf-core.test_classpath+=;${jpf-core.native_classpath}",
            "+cg-creators=TF:" + TestBoolCGCreator.class.getName())) {

      String json = "{"
              + "'b' : TF()"
              + "}";

      Object[] expected = {
        new B(true), new B(false)
      };

      B b = Verify.createFromJSON(B.class, json);
      JSONTest.checkValue(expected, b);
    }
  }
}
