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
/**
 * unfortunately this has to be in the unnamed package, since we need
 * B to be in the unnamed package (so that it has a builtin type name), and as
 * of Java 1.4 you can't import unnamed package classes into named packages
 */
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/*
 * just a helper class for the JavaLangObject raw test that happens to be
 * named like a builtin typecode (byte)
 */
class B {

  int data;

  public B(int d) {
    data = d;
  }

  @Override
  public boolean equals(Object other) {
    if (other == null || !(other instanceof B)) {
      return false;
    }

    return ((B) other).data == data;
  }

  @Override
  public String toString() {
    return "B {data=" + data + "}";
  }
}

public class TypeNameTest extends TestJPF {

  @Test
  public void testArrayCloning() {
    if (verifyNoPropertyViolation()) {
      // test for collisions between typecodes of builtin types
      // and user defined classes (e.g. "B" for byte)
      B[] b = new B[10];
      b[3] = new B(42);

      Object o = b.clone();
      B[] bb = (B[]) o;
      assert b[3].equals(bb[3]);

      byte[] a = new byte[10];
      a[3] = 42;
      o = a.clone();
      byte[] aa = (byte[]) o;
      assert a[3] == aa[3];
    }
  }
}
