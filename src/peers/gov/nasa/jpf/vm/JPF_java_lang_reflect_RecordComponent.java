/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0.
 */
package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.RecordComponentInfo;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.JPF_java_lang_reflect_Method;
import gov.nasa.jpf.vm.Types;

/**
 * Native peer for java.lang.reflect.RecordComponent.
 * Provides reflection access to record component metadata stored in RecordComponentInfo.
 *
 * regIdx stores the component index within the declaring record class.
 * We recover the declaring ClassInfo via the class of the RecordComponent object itself.
 */
public class JPF_java_lang_reflect_RecordComponent extends NativePeer {

  private static RecordComponentInfo getRecordComponentInfo(MJIEnv env, int objRef) {
    // regIdx is the index of this component in the record's component array
    int regIdx = env.getIntField(objRef, "regIdx");
    // get the ElementInfo for this RecordComponent object
    ElementInfo ei = env.getElementInfo(objRef);
    // get the ClassInfo of the class that declared this record component
    // by walking up to find the enclosing record class via the class object reference
    ClassInfo ci = ei.getClassInfo();
    // ci is java.lang.reflect.RecordComponent - we need the record class itself
    // stored implicitly: we resolve via the thread's current class loader
    // We store classRef in the object to avoid this lookup each time
    int classRef = env.getReferenceField(objRef, "clazz");
    if (classRef == MJIEnv.NULL) {
      return null;
    }
    ClassInfo recordCI = env.getReferredClassInfo(classRef);
    RecordComponentInfo[] components = recordCI.getRecordComponents();
    if (components == null || regIdx < 0 || regIdx >= components.length) {
      return null;
    }
    return components[regIdx];
  }

  @MJI
  public int getType____Ljava_lang_Class_2(MJIEnv env, int objRef) {
    RecordComponentInfo rci = getRecordComponentInfo(env, objRef);
    if (rci == null) return MJIEnv.NULL;
    ThreadInfo ti = env.getThreadInfo();
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(
        Types.getTypeName(rci.getDescriptor()));
    if (!ci.isRegistered()) {
      ci.registerClass(ti);
    }
    return ci.getClassObjectRef();
  }

  @MJI
  public int getName____Ljava_lang_String_2(MJIEnv env, int objRef) {
    RecordComponentInfo rci = getRecordComponentInfo(env, objRef);
    if (rci == null) return MJIEnv.NULL;
    return env.newString(rci.getName());
  }

  @MJI
  public int getAccessor____Ljava_lang_reflect_Method_2(MJIEnv env, int objRef) {
    RecordComponentInfo rci = getRecordComponentInfo(env, objRef);
    if (rci == null) return MJIEnv.NULL;

    // get the record class
    int classRef = env.getReferenceField(objRef, "clazz");
    if (classRef == MJIEnv.NULL) return MJIEnv.NULL;
    ClassInfo recordCI = env.getReferredClassInfo(classRef);

    // accessor method has same name as component, no args, returns component type
    String accessorSignature = rci.getName() + "()" + rci.getDescriptor();
    MethodInfo mi = recordCI.getMethod(accessorSignature, false);
    if (mi == null) return MJIEnv.NULL;

    ClassInfo methodCI = JPF_java_lang_Class.getInitializedClassInfo(env, "java.lang.reflect.Method");
    if (methodCI == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }

    return JPF_java_lang_reflect_Method.createMethodObject(env, methodCI, mi);
  }

  @MJI
  public int getGenericSignature____Ljava_lang_String_2(MJIEnv env, int objRef) {
    RecordComponentInfo rci = getRecordComponentInfo(env, objRef);
    if (rci == null) return MJIEnv.NULL;
    String sig = rci.getSignature();
    return sig != null ? env.newString(sig) : MJIEnv.NULL;
  }

  @MJI
  public int getAnnotations_____3Ljava_lang_annotation_Annotation_2(MJIEnv env, int objRef) {
    RecordComponentInfo rci = getRecordComponentInfo(env, objRef);
    if (rci == null) return env.newObjectArray("Ljava/lang/annotation/Annotation;", 0);
    AnnotationInfo[] ai = rci.getAnnotations();
    if (ai == null || ai.length == 0) return env.newObjectArray("Ljava/lang/annotation/Annotation;", 0);
    try {
      return env.newAnnotationProxies(ai);
    } catch (gov.nasa.jpf.vm.ClinitRequired x) {
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }
  }

  @MJI
  public int getDeclaredAnnotations_____3Ljava_lang_annotation_Annotation_2(MJIEnv env, int objRef) {
    RecordComponentInfo rci = getRecordComponentInfo(env, objRef);
    if (rci == null) return env.newObjectArray("Ljava/lang/annotation/Annotation;", 0);
    AnnotationInfo[] ai = rci.getDeclaredAnnotations();
    if (ai == null || ai.length == 0) return env.newObjectArray("Ljava/lang/annotation/Annotation;", 0);
    try {
      return env.newAnnotationProxies(ai);
    } catch (gov.nasa.jpf.vm.ClinitRequired x) {
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }
  }
}
