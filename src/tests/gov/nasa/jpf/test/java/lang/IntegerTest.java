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

package gov.nasa.jpf.test.java.lang;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * parsing tests for Integer, Long and Short, whose parse methods have native peers
 */
public class IntegerTest extends TestJPF {

  interface Parse {
    void run();
  }

  static void assertMessageMentions(String text, Parse parse) {
    try {
      parse.run();
    } catch (NumberFormatException x) {
      assert x.getMessage() != null && x.getMessage().contains(text) : String.valueOf(x.getMessage());
      return;
    }
    assert false : "no NumberFormatException";
  }

  @Test
  public void testParseErrorKeepsMessage() {
    if (verifyNoPropertyViolation()) {
      assertMessageMentions("abc", () -> Integer.parseInt("abc"));
      assertMessageMentions("99", () -> Integer.parseInt("12", 99));
      assertMessageMentions("1x", () -> Long.parseLong("1x"));
      assertMessageMentions("1x", () -> Long.parseLong("1x", 16));
      assertMessageMentions("40000", () -> Short.parseShort("40000"));
      assertMessageMentions("zz", () -> Short.parseShort("zz", 10));
    }
  }
}
