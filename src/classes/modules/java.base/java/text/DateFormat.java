/*
 * Copyright (c) 1996, 2017, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

/*
 * (C) Copyright Taligent, Inc. 1996 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
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
