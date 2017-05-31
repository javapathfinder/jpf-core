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

import java.util.regex.Pattern;

public class JPF_java_util_regex_Pattern extends NativePeer {

  @MJI
  public int split0__Ljava_lang_String_2I___3Ljava_lang_String_2(MJIEnv env,int patRef,int strRef,int limit){
    String s = env.getStringObject(strRef);
    String patSpec = env.getStringField(patRef,"regex");
    int patFlags = env.getIntField(patRef, "flags");

    // <2do> this is not very efficient - it should use a pattern cache
    Pattern p = Pattern.compile(patSpec,patFlags);
    String[] result=p.split(s,limit);

    return env.newStringArray(result);
  }

}
