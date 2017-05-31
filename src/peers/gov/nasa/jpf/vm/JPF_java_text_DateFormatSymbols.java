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

import java.text.DateFormatSymbols;

public class JPF_java_text_DateFormatSymbols extends NativePeer {
  @MJI
  public void initializeData__Ljava_util_Locale_2__V (MJIEnv env, int objRef, int localeRef) {
    DateFormatSymbols dfs = new DateFormatSymbols();
    
    String[] eras = dfs.getEras();
    env.setReferenceField(objRef, "eras", env.newStringArray(eras));
    
    String[] months = dfs.getMonths();
    env.setReferenceField(objRef, "months", env.newStringArray(months));
    
    String[] shortMonths = dfs.getShortMonths();
    env.setReferenceField(objRef, "shortMonths", env.newStringArray(shortMonths));
    
    String[] weekdays = dfs.getWeekdays();
    env.setReferenceField(objRef, "weekdays", env.newStringArray(weekdays));
    
    String[] shortWeekdays = dfs.getShortWeekdays();
    env.setReferenceField(objRef, "shortWeekdays", env.newStringArray(shortWeekdays));
    
    String[] ampms = dfs.getAmPmStrings();
    env.setReferenceField(objRef, "ampms", env.newStringArray(ampms));
    
    String[][] zoneStrings = dfs.getZoneStrings();
    int aaref = env.newObjectArray("[Ljava.lang.String;", zoneStrings.length);
    env.setReferenceField(objRef, "zoneStrings", aaref);
    for (int i=0; i<zoneStrings.length; i++){
      env.setReferenceArrayElement(aaref, i, env.newStringArray(zoneStrings[i]));
    }
    
    String localPatternChars = dfs.getLocalPatternChars();
    env.setReferenceField(objRef, "localPatternChars", env.newString(localPatternChars));

  }
}
