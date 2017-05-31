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

import java.util.Date;

public class JPF_java_util_Date extends NativePeer {

  static Date getDate (MJIEnv env, int dateRef){
    
    //<2do> that doesn't handle BaseCalendar.Date cdate yet
    long t = env.getLongField(dateRef, "fastTime");
    return new Date(t);
  }

  // avoid all the Calendar, TimeZone, CharSequence etc. frenzy just because
  // of a little Date conversion (that probably is only used in a print)
  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int dateRef){
    Date d = getDate(env,dateRef);
    String s = d.toString();

    int sRef = env.newString(s);
    return sRef;
  }
}
