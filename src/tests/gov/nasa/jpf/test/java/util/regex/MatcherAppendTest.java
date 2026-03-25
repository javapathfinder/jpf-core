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

package gov.nasa.jpf.test.java.util.regex;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Tests for Matcher.appendTail() and Matcher.appendReplacement() methods.
 */
public class MatcherAppendTest extends TestJPF {

  @Test
  public void testAppendReplacementAndTail() {
    if (verifyNoPropertyViolation()) {
      Pattern p = Pattern.compile("cat");
      Matcher m = p.matcher("one cat two cats in the cat house");
      StringBuffer sb = new StringBuffer();
      while (m.find()) {
        m.appendReplacement(sb, "dog");
      }
      m.appendTail(sb);
      String result = sb.toString();
      assertEquals("one dog two dogs in the dog house", result);
    }
  }

  @Test
  public void testAppendReplacementSingleMatch() {
    if (verifyNoPropertyViolation()) {
      Pattern p = Pattern.compile("hello");
      Matcher m = p.matcher("say hello world");
      StringBuffer sb = new StringBuffer();
      if (m.find()) {
        m.appendReplacement(sb, "hi");
      }
      m.appendTail(sb);
      assertEquals("say hi world", sb.toString());
    }
  }

  @Test
  public void testAppendTailNoMatch() {
    if (verifyNoPropertyViolation()) {
      Pattern p = Pattern.compile("xyz");
      Matcher m = p.matcher("no match here");
      StringBuffer sb = new StringBuffer();
      // No find() called - just appendTail
      m.appendTail(sb);
      assertEquals("no match here", sb.toString());
    }
  }

  @Test
  public void testAppendReplacementWithGroupReference() {
    if (verifyNoPropertyViolation()) {
      Pattern p = Pattern.compile("(\\d+)");
      Matcher m = p.matcher("item1 and item2");
      StringBuffer sb = new StringBuffer();
      while (m.find()) {
        m.appendReplacement(sb, "[$1]");
      }
      m.appendTail(sb);
      assertEquals("item[1] and item[2]", sb.toString());
    }
  }

  @Test
  public void testAppendReplacementEmptyString() {
    if (verifyNoPropertyViolation()) {
      Pattern p = Pattern.compile("remove");
      Matcher m = p.matcher("please remove this");
      StringBuffer sb = new StringBuffer();
      while (m.find()) {
        m.appendReplacement(sb, "");
      }
      m.appendTail(sb);
      assertEquals("please  this", sb.toString());
    }
  }
}
