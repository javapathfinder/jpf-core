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

package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.text.DateFormat;
import java.text.Format;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

/**
 * this is just the minimal support for DateFormat.parse(String)
 */
public class JPF_java_text_DateFormat extends NativePeer {

  DateFormat getInstance (MJIEnv env, int objref) {
    Format fmt = JPF_java_text_Format.getInstance(env,objref);
    assert fmt instanceof SimpleDateFormat;

    return (DateFormat)fmt;
  }

  @MJI
  public void setTimeZone__Ljava_util_TimeZone_2__V(MJIEnv env, int objref,int timeZoneRef) {
    String timeZoneId = env.getStringField(timeZoneRef, "ID");
    TimeZone timeZone = TimeZone.getTimeZone(timeZoneId);
    DateFormat fmt = getInstance(env,objref);
    fmt.setTimeZone(timeZone);
    int calendarRef = env.getReferenceField(objref, "calendar");
    env.setReferenceField(calendarRef, "zone", timeZoneRef);
  }

  @MJI
  public int parse__Ljava_lang_String_2__Ljava_util_Date_2 (MJIEnv env, int objref, int strRef) {
    DateFormat f = getInstance(env,objref);
    String s = env.getStringObject(strRef);
    try {
      Date d = f.parse(s);
      long t = d.getTime();

      int dref = env.newObject("java.util.Date");
      env.setLongField(dref, "fastTime", t);
      return dref;

    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return 0;

    } catch (ParseException px) {
      env.throwException("java.text.ParseException", px.getMessage());
      return 0;
    }
  }
  
  @MJI
  public void setLenient__Z__V (MJIEnv env, int objref, boolean isLenient) {
    DateFormat f = getInstance(env,objref);
    f.setLenient(isLenient);
  }
  
  @MJI
  public int format__Ljava_util_Date_2__Ljava_lang_String_2 (MJIEnv env, int objref, int dateRef) {
    DateFormat fmt = getInstance(env,objref);
    if (fmt != null) {
      Date d = env.getDateObject(dateRef);
      
      String s = fmt.format(d);
      int sref = env.newString(s);
      return sref;
    }
    
    return MJIEnv.NULL;
  }
}
