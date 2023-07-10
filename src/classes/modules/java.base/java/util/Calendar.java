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
 * (C) Copyright Taligent, Inc. 1996-1998 - All Rights Reserved
 * (C) Copyright IBM Corp. 1996-1998 - All Rights Reserved
 *
 *   The original version of this source code and documentation is copyrighted
 * and owned by Taligent, Inc., a wholly-owned subsidiary of IBM. These
 * materials are provided under terms of a License Agreement between Taligent
 * and Sun. This technology is protected by multiple US and International
 * patents. This notice and attribution to Taligent may not be removed.
 *   Taligent is a registered trademark of Taligent, Inc.
 *
 */

package java.util;

import sun.util.calendar.ZoneInfo;
import sun.util.locale.provider.CalendarDataUtility;
import sun.util.locale.provider.TimeZoneNameUtility;

import java.io.*;
import java.security.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

public abstract class Calendar implements Serializable, Cloneable, Comparable<Calendar> {

  public static final int ERA = 0;
  public static final int YEAR = 1;
  public static final int MONTH = 2;
  public static final int WEEK_OF_YEAR = 3;
  public static final int WEEK_OF_MONTH = 4;
  public static final int DATE = 5;
  public static final int DAY_OF_MONTH = 5;
  public static final int DAY_OF_YEAR = 6;
  public static final int DAY_OF_WEEK = 7;
  public static final int DAY_OF_WEEK_IN_MONTH = 8;
  public static final int AM_PM = 9;
  public static final int HOUR = 10;
  public static final int HOUR_OF_DAY = 11;
  public static final int MINUTE = 12;
  public static final int SECOND = 13;
  public static final int MILLISECOND = 14;
  public static final int ZONE_OFFSET = 15;
  public static final int DST_OFFSET = 16;

  /**
   * The number of distinct fields recognized by <code>get</code> and <code>set</code>.
   * Field numbers range from <code>0..FIELD_COUNT-1</code>.
   */
  public static final int FIELD_COUNT = 17;

  /**
   * The calendar field values for the currently set time for this calendar.
   * This is an array of <code>FIELD_COUNT</code> integers, with index values
   * <code>ERA</code> through <code>DST_OFFSET</code>.
   * @serial
   */
  @SuppressWarnings("ProtectedField")
  protected int[] fields;

  /**
   * The flags which tell if a specified calendar field for the calendar is set.
   * A new object has no fields set.  After the first call to a method
   * which generates the fields, they all remain set after that.
   * This is an array of <code>FIELD_COUNT</code> booleans, with index values
   * <code>ERA</code> through <code>DST_OFFSET</code>.
   * @serial
   */
  @SuppressWarnings("ProtectedField")
  protected boolean[] isSet;

  /**
   * Pseudo-time-stamps which specify when each field was set. There
   * are two special values, UNSET and COMPUTED. Values from
   * MINIMUM_USER_SET to Integer.MAX_VALUE are legal user set values.
   */
  private transient int[] stamp;

  /**
   * The currently set time for this calendar, expressed in milliseconds after
   * January 1, 1970, 0:00:00 GMT.
   * @see #isTimeSet
   * @serial
   */
  @SuppressWarnings("ProtectedField")
  protected long time;

  /**
   * True if then the value of <code>time</code> is valid.
   * The time is made invalid by a change to an item of <code>field[]</code>.
   * @see #time
   * @serial
   */
  @SuppressWarnings("ProtectedField")
  protected boolean isTimeSet;

  /**
   * True if <code>fields[]</code> are in sync with the currently set time.
   * If false, then the next attempt to get the value of a field will
   * force a recomputation of all fields from the current value of
   * <code>time</code>.
   * @serial
   */
  @SuppressWarnings("ProtectedField")
  protected boolean areFieldsSet;

  transient boolean areAllFieldsSet;

  /**
   * <code>True</code> if this calendar allows out-of-range field values during computation
   * of <code>time</code> from <code>fields[]</code>.
   * @see #setLenient
   * @serial
   */
  private boolean lenient = true;

  /**
   * The <code>TimeZone</code> used by this calendar. <code>Calendar</code>
   * uses the time zone data to translate between locale and GMT time.
   * @serial
   */
  private TimeZone zone;

  /**
   * <code>True</code> if zone references to a shared TimeZone object.
   */
  private transient boolean sharedZone = false;

  /**
   * The first day of the week, with possible values <code>SUNDAY</code>,
   * <code>MONDAY</code>, etc.  This is a locale-dependent value.
   * @serial
   */
  private int firstDayOfWeek;

  /**
   * The number of days required for the first week in a month or year,
   * with possible values from 1 to 7.  This is a locale-dependent value.
   * @serial
   */
  private int minimalDaysInFirstWeek;

  /**
   * Cache to hold the firstDayOfWeek and minimalDaysInFirstWeek
   * of a Locale.
   */
  private static final ConcurrentMap<Locale, int[]> cachedLocaleData = new ConcurrentHashMap<>(3);

  // Special values of stamp[]
  /**
   * The corresponding fields[] has no value.
   */
  private static final int UNSET = 0;

  /**
   * The value of the corresponding fields[] has been calculated internally.
   */
  private static final int COMPUTED = 1;

  /**
   * The value of the corresponding fields[] has been set externally. Stamp
   * values which are greater than 1 represents the (pseudo) time when the
   * corresponding fields[] value was set.
   */
  private static final int MINIMUM_USER_STAMP = 2;

  /**
   * The mask value that represents all of the fields.
   */
  static final int ALL_FIELDS = (1 << FIELD_COUNT) - 1;

  /**
   * The next available value for <code>stamp[]</code>, an internal array.
   * This actually should not be written out to the stream, and will probably
   * be removed from the stream in the near future.  In the meantime,
   * a value of <code>MINIMUM_USER_STAMP</code> should be used.
   * @serial
   */
  private int nextStamp = MINIMUM_USER_STAMP;

  // the internal serial version which says which version was written
  // - 0 (default) for version up to JDK 1.1.5
  // - 1 for version from JDK 1.1.6, which writes a correct 'time' value
  //     as well as compatible values for other fields.  This is a
  //     transitional format.
  // - 2 (not implemented yet) a future version, in which fields[],
  //     areFieldsSet, and isTimeSet become transient, and isSet[] is
  //     removed. In JDK 1.1.6 we write a format compatible with version 2.
  static final int currentSerialVersion = 1;

  /**
   * The version of the serialized data on the stream.  Possible values:
   * <dl>
   * <dt><b>0</b> or not present on stream</dt>
   * <dd>
   * JDK 1.1.5 or earlier.
   * </dd>
   * <dt><b>1</b></dt>
   * <dd>
   * JDK 1.1.6 or later.  Writes a correct 'time' value
   * as well as compatible values for other fields.  This is a
   * transitional format.
   * </dd>
   * </dl>
   * When streaming out this class, the most recent format
   * and the highest allowable <code>serialVersionOnStream</code>
   * is written.
   * @serial
   * @since 1.1.6
   */
  private int serialVersionOnStream = currentSerialVersion;

  // Proclaim serialization compatibility with JDK 1.1
  static final long serialVersionUID = -1807547505821590642L;

  static final int ERA_MASK = (1 << ERA);
  static final int YEAR_MASK = (1 << YEAR);
  static final int MONTH_MASK = (1 << MONTH);
  static final int WEEK_OF_YEAR_MASK  = (1 << WEEK_OF_YEAR);
  static final int WEEK_OF_MONTH_MASK = (1 << WEEK_OF_MONTH);
  static final int DAY_OF_MONTH_MASK  = (1 << DAY_OF_MONTH);
  static final int DATE_MASK = DAY_OF_MONTH_MASK;
  static final int DAY_OF_YEAR_MASK = (1 << DAY_OF_YEAR);
  static final int DAY_OF_WEEK_MASK = (1 << DAY_OF_WEEK);
  static final int DAY_OF_WEEK_IN_MONTH_MASK = (1 << DAY_OF_WEEK_IN_MONTH);
  static final int AM_PM_MASK = (1 << AM_PM);
  static final int HOUR_MASK = (1 << HOUR);
  static final int HOUR_OF_DAY_MASK = (1 << HOUR_OF_DAY);
  static final int MINUTE_MASK = (1 << MINUTE);
  static final int SECOND_MASK = (1 << SECOND);
  static final int MILLISECOND_MASK = (1 << MILLISECOND);
  static final int ZONE_OFFSET_MASK = (1 << ZONE_OFFSET);
  static final int DST_OFFSET_MASK = (1 << DST_OFFSET);

  /**
   * Constructs a calendar with the specified time zone and locale.
   *
   * @param zone the time zone to use
   * @param aLocale the locale for the week data
   */
  protected Calendar(TimeZone zone, Locale aLocale) {
    fields = new int[FIELD_COUNT];
    isSet = new boolean[FIELD_COUNT];
    stamp = new int[FIELD_COUNT];

    this.zone = zone;
    setWeekCountData(aLocale);
  }

  /**
   * Gets a calendar using the default time zone and locale. The
   * <code>Calendar</code> returned is based on the current time
   * in the default time zone with the default
   * {@link Locale.Category#FORMAT FORMAT} locale.
   * <p>
   * If the locale contains the time zone with "tz"
   * <a href="Locale.html#def_locale_extension">Unicode extension</a>,
   * that time zone is used instead.
   *
   * @return a Calendar.
   */
  public static Calendar getInstance() {
    Locale aLocale = Locale.getDefault(Locale.Category.FORMAT);
    return createCalendar(defaultTimeZone(aLocale), aLocale);
  }

  /**
   * Gets a calendar with the specified time zone and locale.
   * The <code>Calendar</code> returned is based on the current time
   * in the given time zone with the given locale.
   *
   * @param zone the time zone to use
   * @param aLocale the locale for the week data
   * @return a Calendar.
   */
  public static Calendar getInstance(TimeZone zone, Locale aLocale) {
    return createCalendar(zone, aLocale);
  }

  private static TimeZone defaultTimeZone(Locale l) {
    TimeZone defaultTZ = TimeZone.getDefault();
    String shortTZID = l.getUnicodeLocaleType("tz");
    return shortTZID != null ?
        TimeZoneNameUtility.convertLDMLShortID(shortTZID)
            .map(TimeZone::getTimeZone)
            .orElse(defaultTZ) :
        defaultTZ;
  }

  private static Calendar createCalendar(TimeZone zone, Locale aLocale) {
    Calendar cal = null;

    if (aLocale.hasExtensions()) {
      String caltype = aLocale.getUnicodeLocaleType("ca");
      if (caltype != null) {
        switch (caltype) {
          case "japanese":
            cal = new JapaneseImperialCalendar(zone, aLocale);
            break;
          case "gregory":
            cal = new GregorianCalendar(zone, aLocale);
            break;
        }
      }
    }
    if (cal == null) {
      if (Objects.equals(aLocale.getVariant(), "JP") && Objects.equals(aLocale.getLanguage(), "ja")
          && Objects.equals(aLocale.getCountry(), "JP")) {
        cal = new JapaneseImperialCalendar(zone, aLocale);
      } else {
        cal = new GregorianCalendar(zone, aLocale);
      }
    }
    return cal;
  }

  /**
   * Converts the current calendar field values in {@link #fields fields[]}
   * to the millisecond time value
   * {@link #time}.
   *
   * @see #complete()
   * @see #computeFields()
   */
  protected abstract void computeTime();

  /**
   * Converts the current millisecond time value {@link #time}
   * to calendar field values in {@link #fields fields[]}.
   * This allows you to sync up the calendar field values with
   * a new time that is set for the calendar.  The time is <em>not</em>
   * recomputed first; to recompute the time, then the fields, call the
   * {@link #complete()} method.
   *
   * @see #computeTime()
   */
  protected abstract void computeFields();

  /**
   * Returns whether the calendar fields are partially in sync with the time
   * value or fully in sync but not stamp values are not normalized yet.
   */
  final boolean isPartiallyNormalized() {
    return areFieldsSet && !areAllFieldsSet;
  }

  /**
   * Returns a <code>Date</code> object representing this
   * <code>Calendar</code>'s time value (millisecond offset from the <a
   * href="#Epoch">Epoch</a>").
   *
   * @return a <code>Date</code> representing the time value.
   * @see #setTime(Date)
   * @see #getTimeInMillis()
   */
  public final Date getTime() {
    return new Date(getTimeInMillis());
  }

  /**
   * Sets this Calendar's time with the given <code>Date</code>.
   * <p>
   * Note: Calling <code>setTime()</code> with
   * <code>Date(Long.MAX_VALUE)</code> or <code>Date(Long.MIN_VALUE)</code>
   * may yield incorrect field values from <code>get()</code>.
   *
   * @param date the given Date.
   * @see #getTime()
   * @see #setTimeInMillis(long)
   */
  public final void setTime(Date date) {
    setTimeInMillis(date.getTime());
  }

  /**
   * Returns this Calendar's time value in milliseconds.
   *
   * @return the current time as UTC milliseconds from the epoch.
   * @see #getTime()
   * @see #setTimeInMillis(long)
   */
  public long getTimeInMillis() {
    if (!isTimeSet) {
      updateTime();
    }
    return time;
  }

  /**
   * Sets this Calendar's current time from the given long value.
   *
   * @param millis the new time in UTC milliseconds from the epoch.
   * @see #setTime(Date)
   * @see #getTimeInMillis()
   */
  public void setTimeInMillis(long millis) {
    // If we don't need to recalculate the calendar field values,
    // do nothing.
    if (time == millis && isTimeSet && areFieldsSet && areAllFieldsSet
        && (zone instanceof TimeZone) && !((ZoneInfo)zone).isDirty()) {
      return;
    }
    time = millis;
    isTimeSet = true;
    areFieldsSet = false;
    computeFields();
    areAllFieldsSet = areFieldsSet = true;
  }

  /**
   * Returns the value of the given calendar field. In lenient mode,
   * all calendar fields are normalized. In non-lenient mode, all
   * calendar fields are validated and this method throws an
   * exception if any calendar fields have out-of-range values. The
   * normalization and validation are handled by the
   * {@link #complete()} method, which process is calendar
   * system dependent.
   *
   * @param field the given calendar field.
   * @return the value for the given calendar field.
   * @throws ArrayIndexOutOfBoundsException if the specified field is out of range
   *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
   * @see #set(int,int)
   * @see #complete()
   */
  public int get(int field) {
    complete();
    return internalGet(field);
  }

  /**
   * Returns the value of the given calendar field. This method does
   * not involve normalization or validation of the field value.
   *
   * @param field the given calendar field.
   * @return the value for the given calendar field.
   * @see #get(int)
   */
  protected final int internalGet(int field)
  {
    return fields[field];
  }

  /**
   * Sets the value of the given calendar field. This method does
   * not affect any setting state of the field in this
   * <code>Calendar</code> instance.
   *
   * @throws IndexOutOfBoundsException if the specified field is out of range
   *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
   * @see #areFieldsSet
   * @see #isTimeSet
   * @see #areAllFieldsSet
   * @see #set(int,int)
   */
  final void internalSet(int field, int value)
  {
    fields[field] = value;
  }

  /**
   * Sets the given calendar field to the given value. The value is not
   * interpreted by this method regardless of the leniency mode.
   *
   * @param field the given calendar field.
   * @param value the value to be set for the given calendar field.
   * @throws ArrayIndexOutOfBoundsException if the specified field is out of range
   *             (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
   * in non-lenient mode.
   * @see #set(int,int,int)
   * @see #set(int,int,int,int,int)
   * @see #set(int,int,int,int,int,int)
   * @see #get(int)
   */
  public void set(int field, int value) {
    // If the fields are partially normalized, calculate all the
    // fields before changing any fields.
    if (areFieldsSet && !areAllFieldsSet) {
      computeFields();
    }
    internalSet(field, value);
    isTimeSet = false;
    areFieldsSet = false;
    isSet[field] = true;
    stamp[field] = nextStamp++;
    if (nextStamp == Integer.MAX_VALUE) {
      adjustStamp();
    }
  }

  /**
   * Sets the values for the calendar fields <code>YEAR</code>,
   * <code>MONTH</code>, and <code>DAY_OF_MONTH</code>.
   * Previous values of other calendar fields are retained.  If this is not desired,
   * call {@link #clear()} first.
   *
   * @param year the value used to set the <code>YEAR</code> calendar field.
   * @param month the value used to set the <code>MONTH</code> calendar field.
   * Month value is 0-based. e.g., 0 for January.
   * @param date the value used to set the <code>DAY_OF_MONTH</code> calendar field.
   * @see #set(int,int)
   * @see #set(int,int,int,int,int)
   * @see #set(int,int,int,int,int,int)
   */
  public final void set(int year, int month, int date) {
    set(YEAR, year);
    set(MONTH, month);
    set(DATE, date);
  }

  /**
   * Sets the values for the calendar fields <code>YEAR</code>,
   * <code>MONTH</code>, <code>DAY_OF_MONTH</code>,
   * <code>HOUR_OF_DAY</code>, and <code>MINUTE</code>.
   * Previous values of other fields are retained.  If this is not desired,
   * call {@link #clear()} first.
   *
   * @param year the value used to set the <code>YEAR</code> calendar field.
   * @param month the value used to set the <code>MONTH</code> calendar field.
   * Month value is 0-based. e.g., 0 for January.
   * @param date the value used to set the <code>DAY_OF_MONTH</code> calendar field.
   * @param hourOfDay the value used to set the <code>HOUR_OF_DAY</code> calendar field.
   * @param minute the value used to set the <code>MINUTE</code> calendar field.
   * @see #set(int,int)
   * @see #set(int,int,int)
   * @see #set(int,int,int,int,int,int)
   */
  public final void set(int year, int month, int date, int hourOfDay, int minute) {
    set(YEAR, year);
    set(MONTH, month);
    set(DATE, date);
    set(HOUR_OF_DAY, hourOfDay);
    set(MINUTE, minute);
  }

  /**
   * Sets the values for the fields <code>YEAR</code>, <code>MONTH</code>,
   * <code>DAY_OF_MONTH</code>, <code>HOUR_OF_DAY</code>, <code>MINUTE</code>, and
   * <code>SECOND</code>.
   * Previous values of other fields are retained.  If this is not desired,
   * call {@link #clear()} first.
   *
   * @param year the value used to set the <code>YEAR</code> calendar field.
   * @param month the value used to set the <code>MONTH</code> calendar field.
   * Month value is 0-based. e.g., 0 for January.
   * @param date the value used to set the <code>DAY_OF_MONTH</code> calendar field.
   * @param hourOfDay the value used to set the <code>HOUR_OF_DAY</code> calendar field.
   * @param minute the value used to set the <code>MINUTE</code> calendar field.
   * @param second the value used to set the <code>SECOND</code> calendar field.
   * @see #set(int,int)
   * @see #set(int,int,int)
   * @see #set(int,int,int,int,int)
   */
  public final void set(int year, int month, int date, int hourOfDay, int minute, int second) {
    set(YEAR, year);
    set(MONTH, month);
    set(DATE, date);
    set(HOUR_OF_DAY, hourOfDay);
    set(MINUTE, minute);
    set(SECOND, second);
  }

  /**
   * Sets all the calendar field values and the time value
   * (millisecond offset from the <a href="#Epoch">Epoch</a>) of
   * this <code>Calendar</code> undefined. This means that {@link
   * #isSet(int) isSet()} will return <code>false</code> for all the
   * calendar fields, and the date and time calculations will treat
   * the fields as if they had never been set. A
   * <code>Calendar</code> implementation class may use its specific
   * default field values for date/time calculations. For example,
   * <code>GregorianCalendar</code> uses 1970 if the
   * <code>YEAR</code> field value is undefined.
   *
   * @see #clear(int)
   */
  public final void clear() {
    for (int i = 0; i < fields.length; ) {
      stamp[i] = fields[i] = 0; // UNSET == 0
      isSet[i++] = false;
    }
    areAllFieldsSet = areFieldsSet = false;
    isTimeSet = false;
  }

  /**
   * Sets the given calendar field value and the time value
   * (millisecond offset from the <a href="#Epoch">Epoch</a>) of
   * this <code>Calendar</code> undefined. This means that {@link
   * #isSet(int) isSet(field)} will return <code>false</code>, and
   * the date and time calculations will treat the field as if it
   * had never been set. A <code>Calendar</code> implementation
   * class may use the field's specific default value for date and
   * time calculations.
   *
   * <p>The {@link #HOUR_OF_DAY},  and
   * fields are handled independently and the <a
   * href="#time_resolution">the resolution rule for the time of
   * day</a> is applied. Clearing one of the fields doesn't reset
   * the hour of day value of this <code>Calendar</code>. Use {@link
   * #set(int,int) set(Calendar.HOUR_OF_DAY, 0)} to reset the hour
   * value.
   *
   * @param field the calendar field to be cleared.
   * @see #clear()
   */
  public final void clear(int field) {
    fields[field] = 0;
    stamp[field] = UNSET;
    isSet[field] = false;

    areAllFieldsSet = areFieldsSet = false;
    isTimeSet = false;
  }

  /**
   * Determines if the given calendar field has a value set,
   * including cases that the value has been set by internal fields
   * calculations triggered by a <code>get</code> method call.
   *
   * @param field the calendar field to test
   * @return <code>true</code> if the given calendar field has a value set;
   * <code>false</code> otherwise.
   */
  public final boolean isSet(int field)
  {
    return stamp[field] != UNSET;
  }

  /**
   * Fills in any unset fields in the calendar fields. First, the {@link
   * #computeTime()} method is called if the time value (millisecond offset
   * from the <a href="#Epoch">Epoch</a>) has not been calculated from
   * calendar field values. Then, the {@link #computeFields()} method is
   * called to calculate all calendar field values.
   */
  protected void complete() {
    if (!isTimeSet) {
      updateTime();
    }
    if (!areFieldsSet || !areAllFieldsSet) {
      computeFields(); // fills in unset fields
      areAllFieldsSet = areFieldsSet = true;
    }
  }

  final int getSetStateFields() {
    int mask = 0;
    for (int i = 0; i < fields.length; i++) {
      if (stamp[i] != UNSET) {
        mask |= 1 << i;
      }
    }
    return mask;
  }

  final void setFieldsComputed(int fieldMask) {
    if (fieldMask == ALL_FIELDS) {
      for (int i = 0; i < fields.length; i++) {
        stamp[i] = COMPUTED;
        isSet[i] = true;
      }
      areFieldsSet = areAllFieldsSet = true;
    } else {
      for (int i = 0; i < fields.length; i++) {
        if ((fieldMask & 1) == 1) {
          stamp[i] = COMPUTED;
          isSet[i] = true;
        } else {
          if (areAllFieldsSet && !isSet[i]) {
            areAllFieldsSet = false;
          }
        }
        fieldMask >>>= 1;
      }
    }
  }

  /**
   * Sets the state of the calendar fields that are <em>not</em> specified
   * by <code>fieldMask</code> to <em>unset</em>. If <code>fieldMask</code>
   * specifies all the calendar fields, then the state of this
   * <code>Calendar</code> becomes that all the calendar fields are in sync
   * with the time value (millisecond offset from the Epoch).
   *
   * @param fieldMask the field mask indicating which calendar fields are in
   * sync with the time value.
   * @exception IndexOutOfBoundsException if the specified
   *                <code>field</code> is out of range
   *               (<code>field &lt; 0 || field &gt;= FIELD_COUNT</code>).
   * @see #
   * @see #selectFields()
   */
  final void setFieldsNormalized(int fieldMask) {
    if (fieldMask != ALL_FIELDS) {
      for (int i = 0; i < fields.length; i++) {
        if ((fieldMask & 1) == 0) {
          stamp[i] = fields[i] = 0; // UNSET == 0
          isSet[i] = false;
        }
        fieldMask >>= 1;
      }
    }

    // Some or all of the fields are in sync with the
    // milliseconds, but the stamp values are not normalized yet.
    areFieldsSet = true;
    areAllFieldsSet = false;
  }

  /**
   * Returns whether the calendar fields are fully in sync with the time
   * value.
   */
  final boolean isFullyNormalized() {
    return areFieldsSet && areAllFieldsSet;
  }

  /**
   * Marks this Calendar as not sync'd.
   */
  final void setUnnormalized() {
    areFieldsSet = areAllFieldsSet = false;
  }

  /**
   * Returns whether the specified <code>field</code> is on in the
   * <code>fieldMask</code>.
   */
  static boolean isFieldSet(int fieldMask, int field) {
    return (fieldMask & (1 << field)) != 0;
  }

  final int selectFields() {
    // This implementation has been taken from the GregorianCalendar class.

    // The YEAR field must always be used regardless of its SET
    // state because YEAR is a mandatory field to determine the date
    // and the default value (EPOCH_YEAR) may change through the
    // normalization process.
    int fieldMask = YEAR_MASK;

    if (stamp[ERA] != UNSET) {
      fieldMask |= ERA_MASK;
    }
    // Find the most recent group of fields specifying the day within
    // the year.  These may be any of the following combinations:
    //   MONTH + DAY_OF_MONTH
    //   MONTH + WEEK_OF_MONTH + DAY_OF_WEEK
    //   MONTH + DAY_OF_WEEK_IN_MONTH + DAY_OF_WEEK
    //   DAY_OF_YEAR
    //   WEEK_OF_YEAR + DAY_OF_WEEK
    // We look for the most recent of the fields in each group to determine
    // the age of the group.  For groups involving a week-related field such
    // as WEEK_OF_MONTH, DAY_OF_WEEK_IN_MONTH, or WEEK_OF_YEAR, both the
    // week-related field and the DAY_OF_WEEK must be set for the group as a
    // whole to be considered.  (See bug 4153860 - liu 7/24/98.)
    int dowStamp = stamp[DAY_OF_WEEK];
    int monthStamp = stamp[MONTH];
    int domStamp = stamp[DAY_OF_MONTH];
    int womStamp = aggregateStamp(stamp[WEEK_OF_MONTH], dowStamp);
    int dowimStamp = aggregateStamp(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
    int doyStamp = stamp[DAY_OF_YEAR];
    int woyStamp = aggregateStamp(stamp[WEEK_OF_YEAR], dowStamp);

    int bestStamp = domStamp;
    if (womStamp > bestStamp) {
      bestStamp = womStamp;
    }
    if (dowimStamp > bestStamp) {
      bestStamp = dowimStamp;
    }
    if (doyStamp > bestStamp) {
      bestStamp = doyStamp;
    }
    if (woyStamp > bestStamp) {
      bestStamp = woyStamp;
    }

    /* No complete combination exists.  Look for WEEK_OF_MONTH,
     * DAY_OF_WEEK_IN_MONTH, or WEEK_OF_YEAR alone.  Treat DAY_OF_WEEK alone
     * as DAY_OF_WEEK_IN_MONTH.
     */
    if (bestStamp == UNSET) {
      womStamp = stamp[WEEK_OF_MONTH];
      dowimStamp = Math.max(stamp[DAY_OF_WEEK_IN_MONTH], dowStamp);
      woyStamp = stamp[WEEK_OF_YEAR];
      bestStamp = Math.max(Math.max(womStamp, dowimStamp), woyStamp);

      /* Treat MONTH alone or no fields at all as DAY_OF_MONTH.  This may
       * result in bestStamp = domStamp = UNSET if no fields are set,
       * which indicates DAY_OF_MONTH.
       */
      if (bestStamp == UNSET) {
        bestStamp = domStamp = monthStamp;
      }
    }

    if (bestStamp == domStamp ||
        (bestStamp == womStamp && stamp[WEEK_OF_MONTH] >= stamp[WEEK_OF_YEAR]) ||
        (bestStamp == dowimStamp && stamp[DAY_OF_WEEK_IN_MONTH] >= stamp[WEEK_OF_YEAR])) {
      fieldMask |= MONTH_MASK;
      if (bestStamp == domStamp) {
        fieldMask |= DAY_OF_MONTH_MASK;
      } else {
        assert (bestStamp == womStamp || bestStamp == dowimStamp);
        if (dowStamp != UNSET) {
          fieldMask |= DAY_OF_WEEK_MASK;
        }
        if (womStamp == dowimStamp) {
          // When they are equal, give the priority to
          // WEEK_OF_MONTH for compatibility.
          if (stamp[WEEK_OF_MONTH] >= stamp[DAY_OF_WEEK_IN_MONTH]) {
            fieldMask |= WEEK_OF_MONTH_MASK;
          } else {
            fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
          }
        } else {
          if (bestStamp == womStamp) {
            fieldMask |= WEEK_OF_MONTH_MASK;
          } else {
            assert (bestStamp == dowimStamp);
            if (stamp[DAY_OF_WEEK_IN_MONTH] != UNSET) {
              fieldMask |= DAY_OF_WEEK_IN_MONTH_MASK;
            }
          }
        }
      }
    } else {
      assert (bestStamp == doyStamp || bestStamp == woyStamp ||
          bestStamp == UNSET);
      if (bestStamp == doyStamp) {
        fieldMask |= DAY_OF_YEAR_MASK;
      } else {
        assert (bestStamp == woyStamp);
        if (dowStamp != UNSET) {
          fieldMask |= DAY_OF_WEEK_MASK;
        }
        fieldMask |= WEEK_OF_YEAR_MASK;
      }
    }

    // Find the best set of fields specifying the time of day.  There
    // are only two possibilities here; the HOUR_OF_DAY or the
    // AM_PM and the HOUR.
    int hourOfDayStamp = stamp[HOUR_OF_DAY];
    int hourStamp = aggregateStamp(stamp[HOUR], stamp[AM_PM]);
    bestStamp = (hourStamp > hourOfDayStamp) ? hourStamp : hourOfDayStamp;

    // if bestStamp is still UNSET, then take HOUR or AM_PM. (See 4846659)
    if (bestStamp == UNSET) {
      bestStamp = Math.max(stamp[HOUR], stamp[AM_PM]);
    }

    // Hours
    if (bestStamp != UNSET) {
      if (bestStamp == hourOfDayStamp) {
        fieldMask |= HOUR_OF_DAY_MASK;
      } else {
        fieldMask |= HOUR_MASK;
        if (stamp[AM_PM] != UNSET) {
          fieldMask |= AM_PM_MASK;
        }
      }
    }
    if (stamp[MINUTE] != UNSET) {
      fieldMask |= MINUTE_MASK;
    }
    if (stamp[SECOND] != UNSET) {
      fieldMask |= SECOND_MASK;
    }
    if (stamp[MILLISECOND] != UNSET) {
      fieldMask |= MILLISECOND_MASK;
    }
    if (stamp[ZONE_OFFSET] >= MINIMUM_USER_STAMP) {
      fieldMask |= ZONE_OFFSET_MASK;
    }
    if (stamp[DST_OFFSET] >= MINIMUM_USER_STAMP) {
      fieldMask |= DST_OFFSET_MASK;
    }

    return fieldMask;
  }

  private static int aggregateStamp(int stamp_a, int stamp_b) {
    if (stamp_a == UNSET || stamp_b == UNSET) {
      return UNSET;
    }
    return (stamp_a > stamp_b) ? stamp_a : stamp_b;
  }

  /**
   * Compares this <code>Calendar</code> to the specified
   * <code>Object</code>.  The result is <code>true</code> if and only if
   * the argument is a <code>Calendar</code> object of the same calendar
   * system that represents the same time value (millisecond offset from the
   * <a href="#Epoch">Epoch</a>) under the same
   * <code>Calendar</code> parameters as this object.
   *
   * <p>The <code>Calendar</code> parameters are the values represented
   * by the <code>isLenient</code>, <code>getFirstDayOfWeek</code>,
   * <code>getMinimalDaysInFirstWeek</code> and <code>getTimeZone</code>
   * methods. If there is any difference in those parameters
   * between the two <code>Calendar</code>s, this method returns
   * <code>false</code>.
   *
   * <p>Use the {@link #compareTo(Calendar) compareTo} method to
   * compare only the time values.
   *
   * @param obj the object to compare with.
   * @return <code>true</code> if this object is equal to <code>obj</code>;
   * <code>false</code> otherwise.
   */
  @SuppressWarnings("EqualsWhichDoesntCheckParameterClass")
  @Override
  public boolean equals(Object obj) {
    if (this == obj) {
      return true;
    }
    try {
      Calendar that = (Calendar)obj;
      return compareTo(getMillisOf(that)) == 0 &&
          lenient == that.lenient &&
          firstDayOfWeek == that.firstDayOfWeek &&
          minimalDaysInFirstWeek == that.minimalDaysInFirstWeek &&
          (zone instanceof TimeZone ?
              zone.equals(that.zone) :
              zone.equals(that.getTimeZone()));
    } catch (Exception e) {
      // Note: GregorianCalendar.computeTime throws
      // IllegalArgumentException if the ERA value is invalid
      // even it's in lenient mode.
    }
    return false;
  }

  /**
   * Returns a hash code for this calendar.
   *
   * @return a hash code value for this object.
   * @since 1.2
   */
  @Override
  public int hashCode() {
    // 'otheritems' represents the hash code for the previous versions.
    int otheritems = (lenient ? 1 : 0)
        | (firstDayOfWeek << 1)
        | (minimalDaysInFirstWeek << 4)
        | (zone.hashCode() << 7);
    long t = getMillisOf(this);
    return (int) t ^ (int)(t >> 32) ^ otheritems;
  }

  /**
   * Returns whether this <code>Calendar</code> represents a time
   * before the time represented by the specified
   * <code>Object</code>. This method is equivalent to:
   * <pre>{@code
   *         compareTo(when) < 0
   * }</pre>
   * if and only if <code>when</code> is a <code>Calendar</code>
   * instance. Otherwise, the method returns <code>false</code>.
   *
   * @param when the <code>Object</code> to be compared
   * @return <code>true</code> if the time of this
   * <code>Calendar</code> is before the time represented by
   * <code>when</code>; <code>false</code> otherwise.
   * @see     #compareTo(Calendar)
   */
  public boolean before(Object when) {
    return when instanceof Calendar
        && compareTo((Calendar)when) < 0;
  }

  /**
   * Returns whether this <code>Calendar</code> represents a time
   * after the time represented by the specified
   * <code>Object</code>. This method is equivalent to:
   * <pre>{@code
   *         compareTo(when) > 0
   * }</pre>
   * if and only if <code>when</code> is a <code>Calendar</code>
   * instance. Otherwise, the method returns <code>false</code>.
   *
   * @param when the <code>Object</code> to be compared
   * @return <code>true</code> if the time of this <code>Calendar</code> is
   * after the time represented by <code>when</code>; <code>false</code>
   * otherwise.
   * @see     #compareTo(Calendar)
   */
  public boolean after(Object when) {
    return when instanceof Calendar
        && compareTo((Calendar)when) > 0;
  }

  /**
   * Compares the time values (millisecond offsets from the <a
   * href="#Epoch">Epoch</a>) represented by two
   * <code>Calendar</code> objects.
   *
   * @param anotherCalendar the <code>Calendar</code> to be compared.
   * @return the value <code>0</code> if the time represented by the argument
   * is equal to the time represented by this <code>Calendar</code>; a value
   * less than <code>0</code> if the time of this <code>Calendar</code> is
   * before the time represented by the argument; and a value greater than
   * <code>0</code> if the time of this <code>Calendar</code> is after the
   * time represented by the argument.
   * @exception NullPointerException if the specified <code>Calendar</code> is
   *            <code>null</code>.
   * @exception IllegalArgumentException if the time value of the
   * specified <code>Calendar</code> object can't be obtained due to
   * any invalid calendar values.
   * @since   1.5
   */
  @Override
  public int compareTo(Calendar anotherCalendar) {
    return compareTo(getMillisOf(anotherCalendar));
  }

  /**
   * Adds or subtracts the specified amount of time to the given calendar field,
   * based on the calendar's rules. For example, to subtract 5 days from
   * the current time of the calendar, you can achieve it by calling:
   * <p><code>add(Calendar.DAY_OF_MONTH, -5)</code>.
   *
   * @param field the calendar field.
   * @param amount the amount of date or time to be added to the field.
   * @see #set(int,int)
   */
  public abstract void add(int field, int amount);

  /**
   * Sets the time zone with the given time zone value.
   *
   * @param value the given time zone.
   */
  public void setTimeZone(TimeZone value) {
    zone = value;
    sharedZone = false;
    /* Recompute the fields from the time using the new zone.  This also
     * works if isTimeSet is false (after a call to set()).  In that case
     * the time will be computed from the fields using the new zone, then
     * the fields will get recomputed from that.  Consider the sequence of
     * calls: cal.setTimeZone(EST); cal.set(HOUR, 1); cal.setTimeZone(PST).
     * Is cal set to 1 o'clock EST or 1 o'clock PST?  Answer: PST.  More
     * generally, a call to setTimeZone() affects calls to set() BEFORE AND
     * AFTER it up to the next call to complete().
     */
    areAllFieldsSet = areFieldsSet = false;
  }

  /**
   * Gets the time zone.
   *
   * @return the time zone object associated with this calendar.
   */
  public TimeZone getTimeZone() {
    // If the TimeZone object is shared by other Calendar instances, then
    // create a clone.
    if (sharedZone) {
      zone = (TimeZone) zone.clone();
      sharedZone = false;
    }
    return zone;
  }

  /**
   *
   * Returns the time zone
   */
  public TimeZone getZone() {
    return zone;
  }

  /**
   * Specifies whether or not date/time interpretation is to be lenient.  With
   * lenient interpretation, a date such as "February 942, 1996" will be
   * treated as being equivalent to the 941st day after February 1, 1996.
   * With strict (non-lenient) interpretation, such dates will cause an exception to be
   * thrown. The default is lenient.
   *
   * @param lenient <code>true</code> if the lenient mode is to be turned
   * on; <code>false</code> if it is to be turned off.
   * @see java.text.DateFormat#setLenient
   */
  public void setLenient(boolean lenient)
  {
    this.lenient = lenient;
  }

  public boolean isLenient() {
    return lenient;
  }

  public void setFirstDayOfWeek(int value) {
    if(firstDayOfWeek == value)
      return;
    firstDayOfWeek = value;
    invalidateWeekFields();
  }

  public int getFirstDayOfWeek() {
    return firstDayOfWeek;
  }

  public void setMinimalDaysInFirstWeek(int value) {
    if (minimalDaysInFirstWeek == value) {
      return;
    }
    minimalDaysInFirstWeek = value;
    invalidateWeekFields();
  }

  public int getMinimalDaysInFirstWeek() {
    return minimalDaysInFirstWeek;
  }

  /**
   * Creates and returns a copy of this object.
   *
   * @return a copy of this object.
   */
  @Override
  public Object clone() {
    try {
      Calendar other = (Calendar) super.clone();

      other.fields = new int[FIELD_COUNT];
      other.isSet = new boolean[FIELD_COUNT];
      other.stamp = new int[FIELD_COUNT];
      for (int i = 0; i < FIELD_COUNT; i++) {
        other.fields[i] = fields[i];
        other.stamp[i] = stamp[i];
        other.isSet[i] = isSet[i];
      }
      other.zone = (TimeZone) zone.clone();
      return other;
    }
    catch (CloneNotSupportedException e) {
      // this shouldn't happen, since we are Cloneable
      throw new InternalError(e);
    }
  }

  private static final String[] FIELD_NAME = {
      "ERA", "YEAR", "MONTH", "WEEK_OF_YEAR", "WEEK_OF_MONTH", "DAY_OF_MONTH",
      "DAY_OF_YEAR", "DAY_OF_WEEK", "DAY_OF_WEEK_IN_MONTH", "AM_PM", "HOUR",
      "HOUR_OF_DAY", "MINUTE", "SECOND", "MILLISECOND", "ZONE_OFFSET",
      "DST_OFFSET"
  };

  /**
   * Return a string representation of this calendar. This method
   * is intended to be used only for debugging purposes, and the
   * format of the returned string may vary between implementations.
   * The returned string may be empty but may not be <code>null</code>.
   *
   * @return  a string representation of this calendar.
   */
  @Override
  public String toString() {
    // NOTE: BuddhistCalendar.toString() interprets the string
    // produced by this method so that the Gregorian year number
    // is substituted by its B.E. year value. It relies on
    // "...,YEAR=<year>,..." or "...,YEAR=?,...".
    StringBuilder buffer = new StringBuilder(800);
    buffer.append(getClass().getName()).append('[');
    appendValue(buffer, "time", isTimeSet, time);
    buffer.append(",areFieldsSet=").append(areFieldsSet);
    buffer.append(",areAllFieldsSet=").append(areAllFieldsSet);
    buffer.append(",lenient=").append(lenient);
    buffer.append(",zone=").append(zone);
    appendValue(buffer, ",firstDayOfWeek", true,
        firstDayOfWeek);
    appendValue(buffer, ",minimalDaysInFirstWeek", true, minimalDaysInFirstWeek);
    for (int i = 0; i < FIELD_COUNT; ++i) {
      buffer.append(',');
      appendValue(buffer, FIELD_NAME[i], isSet(i), fields[i]);
    }
    buffer.append(']');
    return buffer.toString();
  }

  // =======================privates===============================

  private static void appendValue(StringBuilder sb, String item, boolean valid, long value) {
    sb.append(item).append('=');
    if (valid) {
      sb.append(value);
    } else {
      sb.append('?');
    }
  }

  /**
   * Both firstDayOfWeek and minimalDaysInFirstWeek are locale-dependent.
   * They are used to figure out the week count for a specific date for
   * a given locale. These must be set when a Calendar is constructed.
   * @param desiredLocale the given locale.
   */
  private void setWeekCountData(Locale desiredLocale) {
    /* try to get the Locale data from the cache */
    int[] data = cachedLocaleData.get(desiredLocale);
    if (data == null) {  /* cache miss */
      data = new int[2];
      data[0] = CalendarDataUtility.retrieveFirstDayOfWeek(desiredLocale);
      data[1] = CalendarDataUtility.retrieveMinimalDaysInFirstWeek(desiredLocale);
      cachedLocaleData.putIfAbsent(desiredLocale, data);
    }
    firstDayOfWeek = data[0];
    minimalDaysInFirstWeek = data[1];
  }

  /**
   * Recomputes the time and updates the status fields isTimeSet
   * and areFieldsSet.  Callers should check isTimeSet and only
   * call this method if isTimeSet is false.
   */
  private void updateTime() {
    computeTime();
    // The areFieldsSet and areAllFieldsSet values are no longer
    // controlled here (as of 1.5).
    isTimeSet = true;
  }

  private int compareTo(long t) {
    long thisTime = getMillisOf(this);
    return Long.compare(thisTime, t);
  }

  private static long getMillisOf(Calendar calendar) {
    if (calendar.isTimeSet) {
      return calendar.time;
    }
    Calendar cal = (Calendar) calendar.clone();
    cal.setLenient(true);
    return cal.getTimeInMillis();
  }

  /**
   * Adjusts the stamp[] values before nextStamp overflow. nextStamp
   * is set to the next stamp value upon the return.
   */
  private void adjustStamp() {
    int max = MINIMUM_USER_STAMP;
    int newStamp = MINIMUM_USER_STAMP;

    for (;;) {
      int min = Integer.MAX_VALUE;
      for (int v : stamp) {
        if (v >= newStamp && min > v) {
          min = v;
        }
        if (max < v) {
          max = v;
        }
      }
      if (max != min && min == Integer.MAX_VALUE) {
        break;
      }
      for (int i = 0; i < stamp.length; i++) {
        if (stamp[i] == min) {
          stamp[i] = newStamp;
        }
      }
      newStamp++;
      if (min == max) {
        break;
      }
    }
    nextStamp = newStamp;
  }

  /**
   * Sets the WEEK_OF_MONTH and WEEK_OF_YEAR fields to new values with the
   * new parameter value if they have been calculated internally.
   */
  private void invalidateWeekFields() {
    if (stamp[WEEK_OF_MONTH] != COMPUTED &&
        stamp[WEEK_OF_YEAR] != COMPUTED) {
      return;
    }

    // We have to check the new values of these fields after changing
    // firstDayOfWeek and/or minimalDaysInFirstWeek. If the field values
    // have been changed, then set the new values. (4822110)
    Calendar cal = (Calendar) clone();
    cal.setLenient(true);
    cal.clear(WEEK_OF_MONTH);
    cal.clear(WEEK_OF_YEAR);

    if (stamp[WEEK_OF_MONTH] == COMPUTED) {
      int weekOfMonth = cal.get(WEEK_OF_MONTH);
      if (fields[WEEK_OF_MONTH] != weekOfMonth) {
        fields[WEEK_OF_MONTH] = weekOfMonth;
      }
    }

    if (stamp[WEEK_OF_YEAR] == COMPUTED) {
      int weekOfYear = cal.get(WEEK_OF_YEAR);
      if (fields[WEEK_OF_YEAR] != weekOfYear) {
        fields[WEEK_OF_YEAR] = weekOfYear;
      }
    }
  }

  /**
   * Save the state of this object to a stream (i.e., serialize it).
   * <p>
   * Ideally, <code>Calendar</code> would only write out its state data and
   * the current time, and not write any field data out, such as
   * <code>fields[]</code>, <code>isTimeSet</code>, <code>areFieldsSet</code>,
   * and <code>isSet[]</code>.  <code>nextStamp</code> also should not be part
   * of the persistent state. Unfortunately, this didn't happen before JDK 1.1
   * shipped. To be compatible with JDK 1.1, we will always have to write out
   * the field values and state flags.  However, <code>nextStamp</code> can be
   * removed from the serialization stream; this will probably happen in the
   * near future.
   */
  private synchronized void writeObject(ObjectOutputStream stream) throws IOException {
    // Try to compute the time correctly, for the future (stream
    // version 2) in which we don't write out fields[] or isSet[].
    if (!isTimeSet) {
      try {
        updateTime();
      }
      catch (IllegalArgumentException e) {}
    }

    // If this Calendar has a ZoneInfo, save it and set a
    // SimpleTimeZone equivalent (as a single DST schedule) for
    // backward compatibility.
    TimeZone savedZone = null;
    if (zone instanceof TimeZone) {
      SimpleTimeZone stz = ((ZoneInfo)zone).getLastRuleInstance();
      if (stz == null) {
        stz = new SimpleTimeZone(zone.getRawOffset(), zone.getID());
      }
      savedZone = zone;
      zone = stz;
    }

    // Write out the 1.1 FCS object.
    stream.defaultWriteObject();

    // Write out the ZoneInfo object
    // 4802409: we write out even if it is null, a temporary workaround
    // the real fix for bug 4844924 in corba-iiop
    stream.writeObject(savedZone);
    if (savedZone != null) {
      zone = savedZone;
    }
  }

  private static class CalendarAccessControlContext {
    private static final AccessControlContext INSTANCE;
    static {
      RuntimePermission perm = new RuntimePermission("accessClassInPackage.sun.util.calendar");
      PermissionCollection perms = perm.newPermissionCollection();
      perms.add(perm);
      INSTANCE = new AccessControlContext(new ProtectionDomain[] {
          new ProtectionDomain(null, perms)
      });
    }
    private CalendarAccessControlContext() {
    }
  }

  /**
   * Reconstitutes this object from a stream (i.e., deserialize it).
   */
  private void readObject(ObjectInputStream stream) throws IOException, ClassNotFoundException {
    final ObjectInputStream input = stream;
    input.defaultReadObject();

    stamp = new int[FIELD_COUNT];

    // Starting with version 2 (not implemented yet), we expect that
    // fields[], isSet[], isTimeSet, and areFieldsSet may not be
    // streamed out anymore.  We expect 'time' to be correct.
    if (serialVersionOnStream >= 2)
    {
      isTimeSet = true;
      if (fields == null) {
        fields = new int[FIELD_COUNT];
      }
      if (isSet == null) {
        isSet = new boolean[FIELD_COUNT];
      }
    }
    else if (serialVersionOnStream >= 0)
    {
      for (int i=0; i<FIELD_COUNT; ++i) {
        stamp[i] = isSet[i] ? COMPUTED : UNSET;
      }
    }

    serialVersionOnStream = currentSerialVersion;

    // If there's a ZoneInfo object, use it for zone.
    TimeZone zi = null;
    try {
      zi = AccessController.doPrivileged(
          new PrivilegedExceptionAction<>() {
            @Override
            public TimeZone run() throws Exception {
              return (TimeZone) input.readObject();
            }
          },
          CalendarAccessControlContext.INSTANCE);
    } catch (PrivilegedActionException pae) {
      Exception e = pae.getException();
      if (!(e instanceof OptionalDataException)) {
        if (e instanceof RuntimeException) {
          throw (RuntimeException) e;
        } else if (e instanceof IOException) {
          throw (IOException) e;
        } else if (e instanceof ClassNotFoundException) {
          throw (ClassNotFoundException) e;
        }
        throw new RuntimeException(e);
      }
    }
    if (zi != null) {
      zone = zi;
    }

    // If the deserialized object has a SimpleTimeZone, try to
    // replace it with a ZoneInfo equivalent (as of 1.4) in order
    // to be compatible with the SimpleTimeZone-based
    // implementation as much as possible.
    if (zone instanceof SimpleTimeZone) {
      String id = zone.getID();
      TimeZone tz = TimeZone.getTimeZone(id);
      if (tz != null && tz.hasSameRules(zone) && tz.getID().equals(id)) {
        zone = tz;
      }
    }
  }
}
