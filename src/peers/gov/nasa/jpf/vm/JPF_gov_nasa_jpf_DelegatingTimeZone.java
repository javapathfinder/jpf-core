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
import java.util.TimeZone;

/**
 * native peer for JPFs concrete TimeZone class, which is just delegating to the
 * host VM so that we are independent of Java versions 
 */
public class JPF_gov_nasa_jpf_DelegatingTimeZone extends NativePeer {

  //--- internals
  TimeZone tz; // only used within (atomic) peer methods
  
  private TimeZone getTimeZone (MJIEnv env, int objRef){
    int rawOffset = env.getIntField(objRef, "rawOffset");
    tz.setRawOffset(rawOffset);
    return tz;
  }

  //--- native methods
  @MJI
  public void setID__Ljava_lang_String_2__V (MJIEnv env, int objRef, int idRef){
    String id = env.getStringObject(idRef);
    TimeZone tz = TimeZone.getTimeZone(id);
    int rawOffset = tz.getRawOffset();
    
    env.setReferenceField(objRef, "ID", idRef);
    env.setIntField(objRef, "rawOffset", rawOffset);
  }
  
  @MJI
  public int getOffset__IIIIII__I (MJIEnv env, int objRef,
      int era, int year, int month, int day, int dayOfWeek, int milliseconds){
    TimeZone tz = getTimeZone( env, objRef);
    return tz.getOffset(era, year, month, day, dayOfWeek, milliseconds);
  }

  @MJI
  public boolean inDaylightTime__J__Z (MJIEnv env, int objRef, long time){
    Date date = new Date(time);
    TimeZone tz = getTimeZone( env, objRef);
    return tz.inDaylightTime(date);
  }
  
  @MJI
  public boolean useDaylightTime____Z (MJIEnv env, int objRef){
    TimeZone tz = getTimeZone( env, objRef);
    return tz.useDaylightTime();
  }
}
