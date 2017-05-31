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
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * (incomplete) native peer for SimpleDateFormat. See Format for details
 * about native formatter delegation
 */
public class JPF_java_text_SimpleDateFormat extends NativePeer {

  SimpleDateFormat getInstance (MJIEnv env, int objref) {
    Format fmt = JPF_java_text_Format.getInstance(env,objref);
    assert fmt instanceof SimpleDateFormat;

    return (SimpleDateFormat)fmt;
  }

  @MJI
  public void init0____V (MJIEnv env, int objref) {
    SimpleDateFormat fmt = new SimpleDateFormat();
    JPF_java_text_Format.putInstance(env,objref,fmt);
  }

  @MJI
  public void init0__Ljava_lang_String_2__V (MJIEnv env, int objref, int patternref) {
    String pattern = env.getStringObject(patternref);

    SimpleDateFormat fmt = new SimpleDateFormat(pattern);
    JPF_java_text_Format.putInstance(env,objref,fmt);
  }

  @MJI
  public void init0__II__V (MJIEnv env, int objref, int timeStyle, int dateStyle) {
    // we are lost here - can't call this SimpleDateFormat ctor because it's package private
    // (this is called - and has to be intercepted - from the DateFormat.getInstance() factory)

    DateFormat fmt = null;

    if (timeStyle < 0) {
      fmt = DateFormat.getDateInstance(dateStyle);
    } else if (dateStyle < 0) {
      fmt = DateFormat.getTimeInstance(timeStyle);
    } else {
      fmt = DateFormat.getDateTimeInstance(dateStyle, timeStyle);
    }

    JPF_java_text_Format.putInstance(env,objref,fmt);
  }

  @MJI
  public int format0 (MJIEnv env, int objref, long dateTime) {
    Date date = new Date(dateTime);
    SimpleDateFormat f = getInstance(env,objref);
    String s = f.format(date);
    return env.newString(s);
  }
  
  @MJI
  public void applyPattern__Ljava_lang_String_2__V (MJIEnv env, int objRef, int patternRef) {
    SimpleDateFormat format = getInstance (env, objRef);
    String pattern = env.getStringObject(patternRef);
    format.applyPattern(pattern);
  }
}
