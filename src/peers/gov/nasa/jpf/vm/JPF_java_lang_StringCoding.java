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

/**
 * we are not really interested in model checking this, so we intercept
 * and ignore
 * <2do> at some point we should probably do proper decoding/encoding,
 * but the java.lang.StringCoding class is unfortunately not public,
 * and it would be a pain to work around the access restrictions
 */
public class JPF_java_lang_StringCoding extends NativePeer {

  @MJI
  public int decode___3BII___3C (MJIEnv env, int clsObjRef,
      int bref, int off, int len) {

    
    int cref = env.newCharArray(len);
    for (int i=0,j=off; i<len; i++,j++) {
      env.setCharArrayElement(cref, i, (char)env.getByteArrayElement(bref,j));
    }
    
    return cref;
  }
  
  @MJI
  public int encode___3CII___3B (MJIEnv env, int clsObjRef,
      int cref, int off, int len) {

    int bref = env.newByteArray(len);
    for (int i=0,j=off; i<len; i++,j++) {
      env.setByteArrayElement(bref, i, (byte)env.getCharArrayElement(cref,j));
    }

    return bref; 
  }
}
