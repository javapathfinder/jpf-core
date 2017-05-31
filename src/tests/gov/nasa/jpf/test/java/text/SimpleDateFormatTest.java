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
import java.text.FieldPosition;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;

import org.junit.Test;

public class SimpleDateFormatTest extends TestJPF {

  @Test
  public void testFormatWithTimeZone() {
    if (verifyNoPropertyViolation()) {
      SimpleDateFormat df = (SimpleDateFormat) DateFormat.getDateTimeInstance(DateFormat.FULL, DateFormat.FULL);
      TimeZone timeZone = TimeZone.getTimeZone("GMT");
      df.setTimeZone(timeZone);
      Calendar calendar = new GregorianCalendar(timeZone);
      calendar.set(2010, 10, 10, 10, 10, 10);
      String time = "10:10"; // some locales don't print the secs
      assertTrue(df.format(calendar.getTime(), new StringBuffer(), new FieldPosition(0)).toString().contains(time));
      df.setTimeZone(TimeZone.getTimeZone("EST"));
      time = "5:10"; // see above
      assertTrue(df.format(calendar.getTime(), new StringBuffer(), new FieldPosition(0)).toString().contains(time));
    }
  }
}
