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

package java.text;

import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.TimeZone;

public abstract class DateFormat extends Format {

  public static final int FULL = 0;
  public static final int LONG = 1;
  public static final int MEDIUM = 2;
  public static final int SHORT = 3;
  public static final int DEFAULT = SHORT;
  protected Calendar calendar;

  public DateFormat() {}

  public static final DateFormat getDateTimeInstance(int dateStyle, int timeStyle, Locale locale) {
    DateFormat dateFormat = getDT(dateStyle, timeStyle, 3, locale);
    return dateFormat;
  }

  private static DateFormat getDT(int dateStyle, int timeStyle, int style, Locale locale) {
    return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
  }

  public DateFormat getDateTimeInstance(int dateStyle, int timeStyle) {
    return getDateTimeInstance(dateStyle, timeStyle, Locale.getDefault(Locale.Category.FORMAT));
  }

  public void setLenient(boolean lenient) {
    if(calendar == null) {
      calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
    }
    calendar.setLenient(lenient);
  }

  public void setTimeZone(TimeZone zone) {
    if(calendar == null) {
      calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
    }
    calendar.setTimeZone(zone);
  }

  public TimeZone getTimeZone() {
    return calendar.getTimeZone();
  }

  public static Locale[] getAvailableLocales() {
    return Locale.getAvailableLocales();
  }

  public final String format(Date date) {
    return format(date, new StringBuffer(), DontCareFieldPosition.INSTANCE).toString();
  }

  public Date parse(String source) throws ParseException {
    ParsePosition pos = new ParsePosition(0);
    Date result = parse(source, pos);
    if (pos.index == 0)
      throw new ParseException("Unable to parse date: \"" + source + "\"" , pos.errorIndex);
    return result;
  }

  public abstract Date parse(String source, ParsePosition pos);

  @Override
  public StringBuffer format(Object obj, StringBuffer toAppendTo, FieldPosition pos) {
    return null;
  }

  @Override
  public Object parseObject(String source, ParsePosition pos) {
    return null;
  }
}
