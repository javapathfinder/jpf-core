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
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;


/**
 * MJI NativePeer class for java.lang.StringBuffer library abstraction
 */
public class JPF_java_lang_StringBuffer extends NativePeer {
  
  boolean hasSharedField = false; // Java 1.4 has, 1.5 doesn't

  @MJI
  public void $clinit____V (MJIEnv env, int clsObjRef) {
    // apparently, Java 1.5 has changed the implementation of class
    // StringBuffer so that it doesn't use the 'shared' state anymore
    // (which was a performance hack to avoid copying the char array
    // data when creating String objects from subsequently unmodified
    // StringBuffers
    // adding this little extra logic here also serves the purpose of
    // avoiding a native ObjectStreamClass method which is called during
    // the static StringBuffer init
    ClassInfo ci = env.getClassInfo();
    if (ci.getInstanceField("shared") != null) {
      hasSharedField = true;
    }
  }
  
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
    
    if (hasSharedField) {
      env.setBooleanField(objref, "shared", false);
    }
    env.setIntField(objref, "count", n);
    
    return objref;
  }

/*
  public static int append__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int sbref) {
    int vref = env.getReferenceField(sbref, "value");
    int sbCount = env.getIntField(sbref, "count");

    // how braindead, how lazy
    char[] b = env.getCharArrayObject(vref);
    String s = new String(b, 0, sbCount);
    
    return appendString(env, objref, s);
  }
*/

  @MJI
  public int append__Ljava_lang_String_2__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int sref) {
    String s = env.getStringObject(sref);    
    if (s == null) s = "null";
    
    return appendString(env, objref, s);
  }
  
  @MJI
  public int append__I__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, int i) {
    String s = Integer.toString(i);
    
    return appendString(env, objref, s);
  }

  @MJI
  public int append__F__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, float f) {
    String s = Float.toString(f);
    
    return appendString(env, objref, s);
  }

  @MJI
  public int append__D__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, double d) {
    String s = Double.toString(d);
    
    return appendString(env, objref, s);
  }
  
  @MJI
  public int append__J__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, long l) {
    String s = Long.toString(l);
    
    return appendString(env, objref, s);
  }

  @MJI
  public int append__Z__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, boolean b) {
    String s = b ? "true" : "false";
    
    return appendString(env, objref, s);
  }
 
/*
  public static int append__B__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, byte b) {
    return append__C__Ljava_lang_StringBuffer_2(env, objref, (char)b);
  }
*/
 
  @MJI
  public int append__C__Ljava_lang_StringBuffer_2 (MJIEnv env, int objref, char c) {
    int aref = env.getReferenceField(objref, "value");
    int alen = env.getArrayLength(aref);
    int count = env.getIntField(objref, "count");
    int n = count +1;
    
    if (n < alen) {
      env.setCharArrayElement(aref, count, c);
    } else {
      int m = 3 * alen / 2;
      int arefNew = env.newCharArray(m);
      for (int i=0; i<count; i++) {
        env.setCharArrayElement(arefNew, i, env.getCharArrayElement(aref, i));
      }
      env.setCharArrayElement(arefNew, count, c);
      env.setReferenceField(objref, "value", arefNew);
    }
    
    if (hasSharedField) {
      env.setBooleanField(objref, "shared", false);
    }
    env.setIntField(objref, "count", n);
    
    return objref;
    
  }
}

