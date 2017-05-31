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

/**
 * peer for java.lang.ThreadLocal
 */
public class JPF_java_lang_ThreadLocal extends NativePeer {
  
  @MJI
  public int getEntry____Ljava_lang_ThreadLocal$Entry_2 (MJIEnv env, int objRef){
    ThreadInfo ti = env.getThreadInfo();
    int tObjRef = ti.getThreadObjectRef();
    
    int aRef = env.getReferenceField(tObjRef, "threadLocals");
    if (aRef == MJIEnv.NULL){
      return MJIEnv.NULL;
    }
    int[] erefs = env.getReferenceArrayObject(aRef);
    int len = env.getArrayLength(aRef);
    
    
    for (int i=0; i<len; i++){
      int e = erefs[i];
      int k = env.getReferenceField(e, "ref");
      
      if (k == objRef){
        return e;
      }
    }
    
    return MJIEnv.NULL;
  }
  
  @MJI
  public void addEntry__Ljava_lang_ThreadLocal$Entry_2 (MJIEnv env, int objRef, int eRef){
    ThreadInfo ti = env.getThreadInfo();
    int tObjRef = ti.getThreadObjectRef();
    int aRef = env.getReferenceField(tObjRef, "threadLocals");
    
    if (aRef == MJIEnv.NULL){ // first entry
      int newaRef = env.newObjectArray("Ljava/lang/ThreadLocal.Entry;", 1);
      env.setReferenceArrayElement(newaRef, 0, eRef);
      env.setReferenceField(tObjRef, "threadLocals", newaRef);
      
    } else {
      int len = env.getArrayLength(aRef);
      int[] erefs = env.getReferenceArrayObject(aRef);

      // new key/value
      int newaRef = env.newObjectArray("Ljava/lang/ThreadLocal.Entry;", len+1);
      env.arrayCopy(aRef, 0, newaRef, 0, len);
      env.setReferenceArrayElement(newaRef, len, eRef);
      env.setReferenceField(tObjRef, "threadLocals", newaRef);
    }
    
  }
  
  @MJI
  public void removeEntry__Ljava_lang_ThreadLocal$Entry_2 (MJIEnv env, int objRef, int eRef){
    ThreadInfo ti = env.getThreadInfo();
    int tObjRef = ti.getThreadObjectRef();
    int aRef = env.getReferenceField(tObjRef, "threadLocals");
    
    if (aRef != MJIEnv.NULL){
      int len = env.getArrayLength(aRef);
      int[] erefs = env.getReferenceArrayObject(aRef);
      int newaRef = env.newObjectArray("Ljava/lang/ThreadLocal.Entry;", len-1);

      for (int i=0, j=0; i<len; i++){
        int e = erefs[i];
        if (e != eRef){
          env.setReferenceArrayElement(newaRef, j++, e);
        }
      }
      
      env.setReferenceField(tObjRef, "threadLocals", newaRef);      
    }
  }
}
