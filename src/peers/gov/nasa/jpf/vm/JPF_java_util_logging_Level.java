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

import java.util.logging.Level;

/**
 * this is only a skeleton to make basic logging work under JPF
 */
public class JPF_java_util_logging_Level extends NativePeer {
  @MJI
  public int getLocalizedName____Ljava_lang_String_2 (MJIEnv env, int objRef){
    Level level = null;    
    int val = env.getIntField(objRef, "value");
    
    switch (val){
    case Integer.MIN_VALUE : 
      level = Level.ALL; break; 
    case 300 :
      level = Level.FINEST; break;
    case 400 :
      level = Level.FINER; break;
    case 500 :
      level = Level.FINE; break;
    case 700 :
      level = Level.CONFIG; break;
    case 800 :
      level = Level.INFO; break;
    case 900 :
      level = Level.WARNING; break;
    case 1000 :
      level = Level.SEVERE; break;
    case Integer.MAX_VALUE :
      level = Level.OFF; break;      
    }
    
    String localizedName = (level != null) ? level.getLocalizedName() : "UNKNOWN";    
    return env.newString(localizedName); 
  }
}
