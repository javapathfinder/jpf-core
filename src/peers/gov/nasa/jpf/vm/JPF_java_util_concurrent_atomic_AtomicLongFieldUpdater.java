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
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MJIEnv;


/**
 * a full peer for the AtomicLongFieldUpdater
 */
public class JPF_java_util_concurrent_atomic_AtomicLongFieldUpdater extends AtomicFieldUpdater {

  @MJI
  public void $init__Ljava_lang_Class_2Ljava_lang_String_2__V (MJIEnv env, int objRef,
                                 int tClsObjRef, int fNameRef) {

    // direct Object subclass, so we don't have to call a super ctor

    ClassInfo ci = env.getReferredClassInfo(tClsObjRef);
    String fname = env.getStringObject(fNameRef);
    FieldInfo fi = ci.getInstanceField(fname);

    ClassInfo fci = fi.getTypeClassInfo();

    if (!fci.isPrimitive() || !fci.getName().equals("long")) {
      // that's also just an approximation, but we need to check
      env.throwException("java.lang.RuntimeException", "wrong field type");
    }

    int fidx = fi.getFieldIndex();
    env.setIntField(objRef, "fieldId", fidx);
  }

  @MJI
  public boolean compareAndSet__Ljava_lang_Object_2JJ__Z (MJIEnv env, int objRef, int tRef, long fExpect, long fUpdate){
    if (tRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException", "AtomicFieldUpdater called on null object");
      return false;
    }
    
    ThreadInfo ti = env.getThreadInfo();
    ElementInfo ei = ti.getModifiableElementInfo(tRef);
    FieldInfo fi = getFieldInfo( ti.getElementInfo(objRef), ei);

    if (reschedulesAccess(ti, ei, fi)){
      env.repeatInvocation();
      return false;
    }

    long v = ei.getLongField(fi);
    if (v == fExpect) {
      ei.setLongField(fi, fUpdate);
      return true;
    } else {
      return false;
    }
  }

  @MJI
  public boolean weakCompareAndSet__Ljava_lang_Object_2JJ__Z (MJIEnv env, int objRef, int tRef, long fExpect, long fUpdate){
    return(compareAndSet__Ljava_lang_Object_2JJ__Z(env, objRef, tRef, fExpect, fUpdate));
  }

  @MJI
  public void set__Ljava_lang_Object_2J__V (MJIEnv env, int objRef, int tRef, long fNewValue){
    if (tRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException", "AtomicFieldUpdater called on null object");
      return;
    }
    
    ThreadInfo ti = env.getThreadInfo();
    ElementInfo ei = ti.getModifiableElementInfo(tRef);
    FieldInfo fi = getFieldInfo( ti.getElementInfo(objRef), ei);

    if (reschedulesAccess(ti, ei, fi)){
      env.repeatInvocation();
      return;
    }

    ei.setLongField(fi, fNewValue);
  }

  @MJI
  public void lazySet__Ljava_lang_Object_2J__V (MJIEnv env, int objRef, int tRef, long fNewValue){
     set__Ljava_lang_Object_2J__V(env, objRef, tRef, fNewValue);
  }

  @MJI
  public long get__Ljava_lang_Object_2__J (MJIEnv env, int objRef, int tRef){
    if (tRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException", "AtomicFieldUpdater called on null object");
      return 0;
    }
    
    ThreadInfo ti = env.getThreadInfo();
    ElementInfo ei = ti.getElementInfo(tRef);
    FieldInfo fi = getFieldInfo( ti.getElementInfo(objRef), ei);

    if (reschedulesAccess(ti, ei, fi)){
      env.repeatInvocation();
      return 0;
    }

    return ei.getLongField(fi);
  }

  @MJI
  public long getAndSet__Ljava_lang_Object_2J__J (MJIEnv env, int objRef, int tRef, long fNewValue){
    if (tRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException", "AtomicFieldUpdater called on null object");
      return 0;
    }
    
    ThreadInfo ti = env.getThreadInfo();
    ElementInfo ei = ti.getModifiableElementInfo(tRef);
    FieldInfo fi = getFieldInfo( ti.getElementInfo(objRef), ei);

    if (reschedulesAccess(ti, ei, fi)){
      env.repeatInvocation();
      return 0;
    }
    
    long result = ei.getLongField(fi);
    ei.setLongField(fi, fNewValue);

    return result;
  }

  @MJI
  public long getAndAdd__Ljava_lang_Object_2J__J (MJIEnv env, int objRef, int tRef, long fDelta){
    if (tRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException", "AtomicFieldUpdater called on null object");
      return 0;
    }
    
    ThreadInfo ti = env.getThreadInfo();
    ElementInfo ei = ti.getModifiableElementInfo(tRef);
    FieldInfo fi = getFieldInfo( ti.getElementInfo(objRef), ei);

    if (reschedulesAccess(ti, ei, fi)){
      env.repeatInvocation();
      return 0;
    }
    
    long result = ei.getLongField(fi);
    ei.setLongField(fi, result + fDelta);

    return result;
  }
}
