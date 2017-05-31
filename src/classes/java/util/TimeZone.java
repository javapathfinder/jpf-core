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
package java.util;

import java.io.Serializable;
import java.util.Date;

/**
 * a concrete TimeZone that forwards to the host VM. This is required to avoid Java version compatibility
 * problems
 * Note that we drop the abstract modifier
 */
public class TimeZone implements Serializable, Cloneable {

  private static final long serialVersionUID = 1L;

  private int rawOffset; 
  private String ID;
  
  private static TimeZone defaultZone;
  
  // public styles
  public static final int SHORT = 0;
  public static final int LONG = 1;
  
  // we keep construction on the peer side
  private static native TimeZone createDefaultZone();
  
  // both are always cloned
  public static native TimeZone getTimeZone (String ID);
  public static TimeZone getDefault() {
    return (TimeZone) (getDefaultRef().clone());
  }

  // called internally (e.g. by java.util.Date) - no clone here
  static TimeZone getDefaultRef(){
    if (defaultZone == null){
      defaultZone = createDefaultZone();
    }
    
    return defaultZone;
  }
  
  // clone handles CloneNotSupportedException
  @Override
  public Object clone() {
    try {
      return super.clone();
    } catch (CloneNotSupportedException e) {
      throw new InternalError();
    }
  }
  
  public static void setDefault (TimeZone tz) {
    if (tz == null){ // that's a reset according to the API docs
      defaultZone = createDefaultZone();
    } else {
      defaultZone = tz;
    }
    
    setDefaultValues(defaultZone); // remember on the native side
  }
  private static native void setDefaultValues (TimeZone tz); 
  
  public static native String[] getAvailableIDs();
  public static native String[] getAvailableIDs(int rawOffset);
  
  // the public TimeZone() constructor of the original class can only be called from the
  // concrete derived classes we want to skip anyways
  
  public TimeZone (String ID){
    setID(ID);
  }
 
  // this will set ID and rawOffset
  public native void setID (String ID);
  
  public String getID (){
    return ID;
  }
  
  public native int getOffset (int era, int year, int month, int day, int dayOfWeek, int milliseconds);

  public native int getOffset (long date);
  
  // this is not public in Java 1.7
  native int getOffsets (long date, int[] offsets);
  
  public int getRawOffset (){
    return rawOffset;
  }
  public void setRawOffset (int offsetMillis){
    rawOffset = offsetMillis;
  }
  
  public boolean inDaylightTime (Date date) {
    return inDaylightTime(date.getTime());
  }
  private native boolean inDaylightTime (long time); 


  public native boolean useDaylightTime();

  public native boolean observesDaylightTime();
  
  public native int getDSTSavings();
  
  public String getDisplayName(){
    // <2do> should use Locale.Category.DISPLAY in Java 1.7
    return getDisplayName( false, LONG, Locale.getDefault());
  }
  public String getDisplayName (Locale locale) {
    return getDisplayName( false, LONG, locale);    
  }
  public String getDisplayName (boolean daylight, int style){
    return getDisplayName( daylight, style, Locale.getDefault());    
  }
  public native String getDisplayName (boolean daylight, int style, Locale locale);
}
