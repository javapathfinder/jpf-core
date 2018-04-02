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

/**
 * (incomplete) model class for java.text.SimpleDate. See Format for details
 * about the native formatter delegation
 */
public class SimpleDateFormat extends DateFormat {

  // see DecimalFormat comments why we use explicit init0()'s

  private native void init0();
  private native void init0(String pattern);
  private native void init0(int timeStyle, int dateStyle);

  public SimpleDateFormat () {
    init0();
    initializeCalendar();
  }

  public SimpleDateFormat (String pattern) {
    if(pattern == null) {
      throw new NullPointerException();
    }
    init0(pattern);
    initializeCalendar();
  }

  public SimpleDateFormat (String pattern, Locale locale) {
    // <2do> bluntly ignoring locale for now
    this(pattern);
  }

  SimpleDateFormat (int timeStyle, int dateStyle, Locale locale){
    init0(timeStyle, dateStyle);
    initializeCalendar();
  }

  // unfortunately we can't override the DateFormat.format(String) because
  // it is final, and hence the compiler can do a INVOKE_SPECIAL
  native String format0 (long dateTime);

  @Override
  public StringBuffer format (Date date, StringBuffer sb, FieldPosition pos) {
    String s = format0(date.getTime());
    sb.append(s);

    // we don't do FieldPositions yet

    return sb;
  }


  @Override
  public Date parse (String arg0, ParsePosition arg1) {
    // TODO Auto-generated method stub
    return null;
  }

  private void initializeCalendar() {
    if (calendar == null) {
        calendar = Calendar.getInstance(TimeZone.getDefault(), Locale.getDefault());
    }
  }

  native public void applyPattern(String pattern);
  
}
