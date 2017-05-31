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

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class DateFormatTest extends TestJPF {

  @Test
  public void testConversionCycle() {
    if (verifyNoPropertyViolation()) {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
      df.setLenient(true);

      Date d1 = new Date();
      System.out.print("current date is: ");
      System.out.println(d1);

      String s = df.format(d1);
      System.out.print("formatted date is: ");
      System.out.println(s);

      try {
        Date d2 = df.parse(s);

        System.out.print("re-parsed date is: ");
        System.out.println(d2);

        long t1 = d1.getTime(); // in ms
        long t2 = d2.getTime(); // in ms
        long delta = Math.abs(t2 - t1);

        // since we loose the ms in String conversion, d2.after(d1) does not necessarily hold
        // some locales don't format the seconds, to we might loose them in the re-conversion
        assert delta <= 60000 : "difference > 1min";

      } catch (ParseException x) {
        assert false : "output did not parse: " + x;
      }
    }
  }

  @Test
  public void testSetAndGetTimeZone() {
    if (verifyNoPropertyViolation()) {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
      TimeZone timeZone = TimeZone.getTimeZone("GMT");
      df.setTimeZone(timeZone);
      assertEquals(timeZone, df.getTimeZone());
      timeZone = TimeZone.getTimeZone("PDT");
      df.setTimeZone(timeZone);
      assertEquals(timeZone, df.getTimeZone());
    }
  }

  @Test
  public void testFormatWithTimeZone() {
    if (verifyNoPropertyViolation()) {
      DateFormat df = DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
      TimeZone timeZone = TimeZone.getTimeZone("GMT");
      df.setTimeZone(timeZone);
      Calendar calendar = new GregorianCalendar(timeZone);
      calendar.set(2010, 10, 10, 10, 10, 10);
      String time = "10:10"; // some Locales don't include the seconds
      String dft = df.format(calendar.getTime()); 
      assertTrue(dft.contains(time));
      df.setTimeZone(TimeZone.getTimeZone("EST"));
      time = "5:10";  // some Locales don't include the seconds
      dft = df.format(calendar.getTime());
      assertTrue(dft.contains(time));
    }
  }
}
