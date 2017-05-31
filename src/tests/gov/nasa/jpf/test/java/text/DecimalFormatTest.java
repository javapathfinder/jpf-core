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


package gov.nasa.jpf.test.java.text;

import gov.nasa.jpf.util.test.TestJPF;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.FieldPosition;
import java.text.NumberFormat;
import java.text.ParsePosition;

import org.junit.Test;

/**
 * simple regression test for java.text.DecimalFormat
 */
public class DecimalFormatTest extends TestJPF {

  @Test
  public void testDoubleConversion() {

    if (verifyNoPropertyViolation()) {
      StringBuffer sb = new StringBuffer();
      DecimalFormat dFormat = new DecimalFormat();
      sb = dFormat.format(new Double(42), sb, new FieldPosition(0));
      String output = sb.toString();
      try {
        double d = Double.parseDouble(output);
        assert (d == 42.0) : "parsed value differs: " + output;
      } catch (NumberFormatException e) {
        assert false : "output did not parse " + e;
      }
    }
  }

  @Test
  public void testIsParseIntegerOnly () {
    if (verifyNoPropertyViolation()) {
      DecimalFormat dFormat = new DecimalFormat();
      assertFalse(dFormat.isParseIntegerOnly());
      dFormat.setParseIntegerOnly(true);
      assertTrue(dFormat.isParseIntegerOnly());
      dFormat.setParseIntegerOnly(false);
      assertFalse(dFormat.isParseIntegerOnly());
      NumberFormat format = NumberFormat.getIntegerInstance();
      assertTrue(format.isParseIntegerOnly());
      format = NumberFormat.getNumberInstance();
      assertFalse(format.isParseIntegerOnly());
    }
  }

  @Test
  public void testIsGroupingUsed() {
    if (verifyNoPropertyViolation()) {
      DecimalFormat dFormat = new DecimalFormat();
      assertTrue(dFormat.isGroupingUsed());
      dFormat.setGroupingUsed(false);
      assertFalse(dFormat.isGroupingUsed());
      dFormat.setGroupingUsed(true);
      assertTrue(dFormat.isGroupingUsed());
    }
  }

  @Test
  public void testSetGroupingUsed() {

    if (verifyNoPropertyViolation()) {
      DecimalFormat dFormat = new DecimalFormat();
      String s = dFormat.format(4200000L);
      assertTrue(s.length() == 9);
      dFormat.setGroupingUsed(false);
      s = dFormat.format(4200000L);
      assertTrue(s.equals("4200000"));
      dFormat.setGroupingUsed(true);
      s = dFormat.format(4200000L);
      assertTrue(s.length() == 9);
    }
  }

  @Test
  public void testParseDouble() {

    if (verifyNoPropertyViolation()) {
      DecimalFormatSymbols dfs = new DecimalFormatSymbols();
      DecimalFormat dFormat = new DecimalFormat();
      ParsePosition ps = new ParsePosition(0);
      Number nb = dFormat.parse("10" + dfs.getDecimalSeparator() + "10",ps);
      assertTrue(nb instanceof Double);
      assertTrue(nb.doubleValue() == 10.10d);
      assertTrue(ps.getErrorIndex() == -1);
      assertTrue(ps.getIndex() == 5);
    }
  }

  @Test
  public void testParseInt() {

    if (verifyNoPropertyViolation()) {
      DecimalFormat dFormat = new DecimalFormat();
      ParsePosition ps = new ParsePosition(0);
      Number nb = dFormat.parse("10",ps);
      assertTrue(nb instanceof Long);
      assertTrue(nb.doubleValue() == 10l);
      assertTrue(ps.getErrorIndex() == -1);
      assertTrue(ps.getIndex() == 2);
    }
  }

  @Test
  public void testParseError() {

    if (verifyNoPropertyViolation()) {
      DecimalFormat dFormat = new DecimalFormat();
      int parsePos = 1;
      ParsePosition ps = new ParsePosition(parsePos);
      Number nb = dFormat.parse("^^10",ps);
      assertEquals(nb,null);
      assertEquals(ps.getIndex(), parsePos);
      assertEquals(ps.getErrorIndex(), parsePos);
    }
  }
}

