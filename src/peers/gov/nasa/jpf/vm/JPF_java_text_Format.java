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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.text.Format;
import java.util.HashMap;

/**
 * native peer for java.text.Format delegation. This is the place where
 * we keep a map between real formatters and their JPF counterparts
 * (which are just proxies)
 */
public class JPF_java_text_Format extends NativePeer {

  static HashMap<Integer,Format> formatters;

  public static boolean init (Config conf){
    formatters = new HashMap<Integer,Format>();
    return true;
  }
  
  static void putInstance (MJIEnv env, int objref, Format fmt) {
    int id = env.getIntField(objref,  "id");
    formatters.put(new Integer(id), fmt);
  }

  static Format getInstance (MJIEnv env, int objref) {
    // <2do> that's braindead
    
    int id = env.getIntField(objref,  "id");
    return formatters.get(id);
  }

  
}
