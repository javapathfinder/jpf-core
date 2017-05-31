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

public class JPF_java_lang_StringBuilder extends NativePeer {
  
  int appendString (MJIEnv env, int objref, String s) {
    int slen = s.length();
    int aref = env.getReferenceField(objref, "value");
    int alen = env.getArrayLength(aref);
    int count = env.getIntField(objref, "count");
    int i, j;
    int n = count + slen;
    
    if (n < alen) {
      for (i=count, j=0; i<n; i++, j++) {
        env.setCharArrayElement(aref, i, s.charAt(j));
      }
    } else {
      int m = 3 * alen / 2;
      if (m < n) {
        m = n;
      }
      int arefNew = env.newCharArray(m);
      for (i=0; i<count; i++) {
        env.setCharArrayElement(arefNew, i, env.getCharArrayElement(aref, i));
      }
      for (j=0; i<n; i++, j++) {
        env.setCharArrayElement(arefNew, i, s.charAt(j));
      }
      env.setReferenceField(objref, "value", arefNew);
    }
    
    env.setIntField(objref, "count", n);
    
    return objref;
  }

  // we skip the AbstractStringBuilder ctor here, which is a bit dangerous
  // This is only justified because StringBuilders are used everywhere (implicitly)
  @MJI
  public void $init____V (MJIEnv env, int objref){
    int aref = env.newCharArray(16);
    env.setReferenceField(objref, "value", aref);
  }
  @MJI
  public void $init__I__V (MJIEnv env, int objref, int len){
    int aref = env.newCharArray(len);
    env.setReferenceField(objref, "value", aref);
  }
  @MJI
  public void $init__Ljava_lang_String_2__V (MJIEnv env, int objref, int sRef){
    if (sRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException");
      return;
    }
    
    char[] src = env.getStringChars(sRef);
    int aref = env.newCharArray(src.length + 16);
    char[] dst = env.getCharArrayObject(aref);
    System.arraycopy(src,0,dst,0,src.length);
    
    env.setReferenceField(objref, "value", aref);
    env.setIntField(objref, "count", src.length);
  }  
  
  
  @MJI
  public int append__Ljava_lang_String_2__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, int sref) {
    String s = env.getStringObject(sref);
    
    if (s == null) s = "null";
    
    return appendString(env, objref, s);
  }
  
  @MJI
  public int append__I__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, int i) {
    String s = Integer.toString(i);
    
    return appendString(env, objref, s);
  }

  @MJI
  public int append__F__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, float f) {
    String s = Float.toString(f);
    
    return appendString(env, objref, s);
  }

  @MJI
  public int append__D__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, double d) {
    String s = Double.toString(d);
    
    return appendString(env, objref, s);
  }
  
  @MJI
  public int append__J__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, long l) {
    String s = Long.toString(l);
    
    return appendString(env, objref, s);
  }

  @MJI
  public int append__Z__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, boolean b) {
    String s = b ? "true" : "false";
    
    return appendString(env, objref, s);
  }
  
  @MJI
  public int append__C__Ljava_lang_StringBuilder_2 (MJIEnv env, int objref, char c) {
    int aref = env.getReferenceField(objref, "value");
    int alen = env.getArrayLength(aref);
    int count = env.getIntField(objref, "count");
    int i;
    int n = count +1;
    
    if (n < alen) {
      env.setCharArrayElement(aref, count, c);
    } else {
      int m = 3 * alen / 2;
      int arefNew = env.newCharArray(m);
      for (i=0; i<count; i++) {
        env.setCharArrayElement(arefNew, i, env.getCharArrayElement(aref, i));
      }
      env.setCharArrayElement(arefNew, count, c);
      env.setReferenceField(objref, "value", arefNew);
    }
    
    env.setIntField(objref, "count", n);
    
    return objref;
    
  }

  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objref) {
    int aref = env.getReferenceField(objref, "value");
    int count = env.getIntField(objref, "count");

    char[] buf = env.getCharArrayObject(aref);
    String s = new String(buf, 0, count);
    return env.newString(s);
  }
}
