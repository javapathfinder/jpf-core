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
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.util.StringMatcher;

/**
 * native peer to configure concrete URLConnection classes for specific URLs
 *
 * We use model class configuration in case somebody wants to implement logic
 * in there that requires backtracking, and doesn't want to do this on the
 * native peer level
 *
 * example config:
 *   http.connection= http://*.dtd => gov.nasa.jpf.CachedROHttpConnection, http://foo.com/* -- x.y.MyHttpConnection
 */
public class JPF_sun_net_www_protocol_http_Handler extends NativePeer {

  static JPFLogger logger = JPF.getLogger("http");


  static class MapEntry {
    StringMatcher matcher;
    String clsName;

    MapEntry (StringMatcher m, String c){
      matcher = m;
      clsName = c;
    }
  }

  MapEntry[] map;

  public JPF_sun_net_www_protocol_http_Handler (Config conf){
    String[] specs = conf.getCompactTrimmedStringArray("http.connection");
    if (specs != null){
      map = new MapEntry[specs.length];

      for (int i=0; i<specs.length; i++){
        String s = specs[i];
        MapEntry e = null;

        int idx = s.indexOf("--");
        if (idx > 0 && idx < (s.length() - 3)){
          String pattern = s.substring(0, idx).trim();
          String clsName = s.substring(idx+2).trim();

          if (!pattern.isEmpty() && !clsName.isEmpty()){
            StringMatcher matcher = new StringMatcher(pattern);
            e = new MapEntry(matcher, clsName);

            logger.info("mapping URL pattern ", pattern, " to ", clsName);
          }
        }

        if (e == null){
          throw new JPFConfigException("not a valid http.connection spec: " + s);
        }

        map[i] = e;
      }
    }

  }

  @MJI
  public int getConnectionClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env, int objref, int surlRef){
    String url = env.getStringObject(surlRef);

    if (map != null){
      for (int i = 0; i < map.length; i++) {
        if (map[i].matcher.matches(url)) {
          String clsName = map[i].clsName;

          ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(clsName);

          // this might re-execute if there is a clinit
          int clsObjRef = JPF_java_lang_Class.getClassObject(env, ci);

          if (clsObjRef != MJIEnv.NULL){
            logger.info("using ", clsName, " for URL ", url);
          }
          return clsObjRef;
        }
      }
    }

    // didn't find any match
    return MJIEnv.NULL;
  }
}
