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
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.MethodInfoRegistry;
import gov.nasa.jpf.util.RunListener;
import gov.nasa.jpf.util.RunRegistry;

import java.lang.reflect.Modifier;
import java.util.ArrayList;

public class JPF_java_lang_reflect_Method extends NativePeer {

  static MethodInfoRegistry registry;

  // class init - this is called automatically from the NativePeer ctor
  public static boolean init (Config conf) {
    // this is an example of how to handle cross-initialization between
    // native peers - this might also get explicitly called by the java.lang.Class
    // peer, since it creates Method objects. Here we have to make sure
    // we only reset between JPF runs

    if (registry == null){
      registry = new MethodInfoRegistry();
      
      RunRegistry.getDefaultRegistry().addListener( new RunListener() {
        @Override
		public void reset (RunRegistry reg){
          registry = null;
        }
      });
    }
    return true;
  }

  static int createMethodObject (MJIEnv env, ClassInfo ciMth, MethodInfo mi){
    // note - it is the callers responsibility to ensure Method is properly initialized    
    int regIdx = registry.registerMethodInfo(mi);
    int eidx = env.newObject( ciMth);
    ElementInfo ei = env.getModifiableElementInfo(eidx);
    
    ei.setIntField("regIdx", regIdx);
    ei.setBooleanField("isAccessible", mi.isPublic());
    
    return eidx;
  }
  
  // this is NOT an MJI method, but it is used outside this package, so
  // we have to add 'final'
  public static final MethodInfo getMethodInfo (MJIEnv env, int objRef){
    return registry.getMethodInfo(env,objRef, "regIdx");
  }
  
  @MJI
  public int getName____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    MethodInfo mi = getMethodInfo(env, objRef);
    
    int nameRef = env.getReferenceField( objRef, "name");
    if (nameRef == MJIEnv.NULL) {
      nameRef = env.newString(mi.getName());
      env.setReferenceField(objRef, "name", nameRef);
    }
   
    return nameRef;
  }

  @MJI
  public int getModifiers____I (MJIEnv env, int objRef){
    MethodInfo mi = getMethodInfo(env, objRef);
    return mi.getModifiers();
  }
  
  static int getParameterTypes( MJIEnv env, MethodInfo mi) {
    ThreadInfo ti = env.getThreadInfo();
    String[] argTypeNames = mi.getArgumentTypeNames();
    int[] ar = new int[argTypeNames.length];

    for (int i = 0; i < argTypeNames.length; i++) {
      ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(argTypeNames[i]);
      if (!ci.isRegistered()) {
        ci.registerClass(ti);
      }

      ar[i] = ci.getClassObjectRef();
    }

    int aRef = env.newObjectArray("Ljava/lang/Class;", argTypeNames.length);
    for (int i = 0; i < argTypeNames.length; i++) {
      env.setReferenceArrayElement(aRef, i, ar[i]);
    }

    return aRef;
  }
  
  @MJI
  public int getParameterTypes_____3Ljava_lang_Class_2 (MJIEnv env, int objRef){
    return getParameterTypes(env, getMethodInfo(env, objRef));
  }
  
  int getExceptionTypes(MJIEnv env, MethodInfo mi) {
    ThreadInfo ti = env.getThreadInfo();
    String[] exceptionNames = mi.getThrownExceptionClassNames();
     
    if (exceptionNames == null) {
      exceptionNames = new String[0];
    }
     
    int[] ar = new int[exceptionNames.length];
     
    for (int i = 0; i < exceptionNames.length; i++) {
      ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(exceptionNames[i]);
      if (!ci.isRegistered()) {
        ci.registerClass(ti);
      }
       
      ar[i] = ci.getClassObjectRef();
    }
     
    int aRef = env.newObjectArray("Ljava/lang/Class;", exceptionNames.length);
    for (int i = 0; i < exceptionNames.length; i++) {
      env.setReferenceArrayElement(aRef, i, ar[i]);
    }
     
    return aRef;
  }
  
  @MJI
  public int getExceptionTypes_____3Ljava_lang_Class_2 (MJIEnv env, int objRef) {
    return getExceptionTypes(env, getMethodInfo(env, objRef));
  }
  
  @MJI
  public int getReturnType____Ljava_lang_Class_2 (MJIEnv env, int objRef){
    MethodInfo mi = getMethodInfo(env, objRef);
    ThreadInfo ti = env.getThreadInfo();

    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(mi.getReturnTypeName());
    if (!ci.isRegistered()) {
      ci.registerClass(ti);
    }

    return ci.getClassObjectRef();
  }
  
  @MJI
  public int getDeclaringClass____Ljava_lang_Class_2 (MJIEnv env, int objRef){
    MethodInfo mi = getMethodInfo(env, objRef);    
    ClassInfo ci = mi.getClassInfo();
    // it's got to be registered, otherwise we wouldn't be able to acquire the Method object
    return ci.getClassObjectRef();
  }
    
  static int createBoxedReturnValueObject (MJIEnv env, MethodInfo mi, DirectCallStackFrame frame) {
    byte rt = mi.getReturnTypeCode();
    int ret = MJIEnv.NULL;
    ElementInfo rei;
    Object attr = null;

    if (rt == Types.T_DOUBLE) {
      attr = frame.getLongResultAttr();
      double v = frame.getDoubleResult();
      ret = env.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Double"));
      rei = env.getModifiableElementInfo(ret);
      rei.setDoubleField("value", v);
    } else if (rt == Types.T_FLOAT) {
      attr = frame.getResultAttr();
      float v = frame.getFloatResult();
      ret = env.newObject(ClassLoaderInfo.getSystemResolvedClassInfo("java.lang.Float"));
      rei = env.getModifiableElementInfo(ret);
      rei.setFloatField("value", v);
    } else if (rt == Types.T_LONG) {
      attr = frame.getLongResultAttr();
      long v = frame.getLongResult();
      ret = env.valueOfLong(v);
    } else if (rt == Types.T_BYTE) {
      attr = frame.getResultAttr();
      int v = frame.getResult(); 
      ret = env.valueOfByte((byte)v);
    } else if (rt == Types.T_CHAR) {
      attr = frame.getResultAttr();
      int v = frame.getResult(); 
      ret = env.valueOfCharacter((char)v);
    } else if (rt == Types.T_SHORT) {
      attr = frame.getResultAttr();
      int v = frame.getResult(); 
      ret = env.valueOfShort((short)v);
    } else if (rt == Types.T_INT) {
      attr = frame.getResultAttr();
      int v = frame.getResult(); 
      ret = env.valueOfInteger(v);
    } else if (rt == Types.T_BOOLEAN) {
      attr = frame.getResultAttr();
      int v = frame.getResult();
      ret = env.valueOfBoolean((v == 1)? true: false);
    } else if (mi.isReferenceReturnType()){ 
      attr = frame.getResultAttr();
      ret = frame.getReferenceResult();
    }

    env.setReturnAttribute(attr);
    return ret;
  }

  static boolean pushUnboxedArguments (MJIEnv env, MethodInfo mi, DirectCallStackFrame frame, int argIdx, int argsRef) {
    ElementInfo source;
    ClassInfo sourceClass;
    String destTypeNames[];
    int nArgs, passedCount, sourceRef;
    byte sourceType, destTypes[];

    destTypes     = mi.getArgumentTypes();
    destTypeNames = mi.getArgumentTypeNames();
    nArgs         = destTypeNames.length;
    
    // according to the API docs, passing null instead of an empty array is allowed for no args
    passedCount   = (argsRef != MJIEnv.NULL) ? env.getArrayLength(argsRef) : 0;
    
    if (nArgs != passedCount) {
      env.throwException(IllegalArgumentException.class.getName(), "Wrong number of arguments passed.  Actual = " + passedCount + ".  Expected = " + nArgs);
      return false;
    }
    
    for (int i = 0; i < nArgs; i++) {
      sourceRef = env.getReferenceArrayElement(argsRef, i);

      // we have to handle null references explicitly
      if (sourceRef == MJIEnv.NULL) {
        if ((destTypes[i] != Types.T_REFERENCE) && (destTypes[i] != Types.T_ARRAY)) {
          env.throwException(IllegalArgumentException.class.getName(), "Wrong argument type at index " + i + ".  Actual = (null).  Expected = " + destTypeNames[i]);
          return false;
        } 
         
        frame.pushRef(MJIEnv.NULL);
        continue;
      }

      source      = env.getElementInfo(sourceRef);
      sourceClass = source.getClassInfo();   
      sourceType = getSourceType( sourceClass, destTypes[i], destTypeNames[i]);

      Object attr = env.getElementInfo(argsRef).getFields().getFieldAttr(i);
      if ((argIdx = pushArg( argIdx, frame, source, sourceType, destTypes[i], attr)) < 0 ){
        env.throwException(IllegalArgumentException.class.getName(), "Wrong argument type at index " + i + ".  Source Class = " + sourceClass.getName() + ".  Dest Class = " + destTypeNames[i]);
        return false;        
      }
    }
    
    return true;
  }

  // this returns the primitive type in case we have to unbox, and otherwise checks reference type compatibility
  private static byte getSourceType (ClassInfo ciArgVal, byte destType, String destTypeName){
    switch (destType){
    // the primitives
    case Types.T_BOOLEAN:
    case Types.T_BYTE:
    case Types.T_CHAR:
    case Types.T_SHORT:
    case Types.T_INT:
    case Types.T_LONG:
    case Types.T_FLOAT:
    case Types.T_DOUBLE:
      return Types.getUnboxedType(ciArgVal.getName());
      
    case Types.T_ARRAY:
    case Types.T_REFERENCE: // check if the source type is assignment compatible with the destType
      if (ciArgVal.isInstanceOf(destTypeName)){
        return destType;
      }
    }
    
    return Types.T_NONE;
  }
  
  // do the proper type conversion - Java is pretty forgiving here and does
  // not throw exceptions upon value truncation
  private static int pushArg( int argIdx, DirectCallStackFrame frame, ElementInfo eiArg, byte srcType, byte destType, Object attr){    
    switch (srcType) {
    case Types.T_DOUBLE:
    {
      double v = eiArg.getDoubleField("value");
      if (destType == Types.T_DOUBLE){
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_FLOAT: // covers float, double
    {
      float v = eiArg.getFloatField("value");
      switch (destType){
      case Types.T_FLOAT:
        return frame.setFloatArgument( argIdx, v, attr);
      case Types.T_DOUBLE:
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_LONG:
    {
      long v = eiArg.getLongField("value");
      switch (destType){
      case Types.T_LONG:
        return frame.setLongArgument(argIdx, v, attr);
      case Types.T_FLOAT:
        return frame.setFloatArgument(argIdx, v, attr);
      case Types.T_DOUBLE:
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_INT:
    { 
      int v = eiArg.getIntField("value");
      switch (destType){
      case Types.T_INT:
        return frame.setArgument( argIdx, v, attr);
      case Types.T_LONG:
        return frame.setLongArgument( argIdx, v, attr);
      case Types.T_FLOAT:
        return frame.setFloatArgument(argIdx, v, attr);
      case Types.T_DOUBLE:
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_SHORT:
    { 
      int v = eiArg.getShortField("value");
      switch (destType){
      case Types.T_SHORT:
      case Types.T_INT:
        return frame.setArgument( argIdx, v, attr);
      case Types.T_LONG:
        return frame.setLongArgument( argIdx, v, attr);
      case Types.T_FLOAT:
        return frame.setFloatArgument(argIdx, v, attr);
      case Types.T_DOUBLE:
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_BYTE:
    { 
      byte v = eiArg.getByteField("value");
      switch (destType){
      case Types.T_BYTE:
      case Types.T_SHORT:
      case Types.T_INT:
        return frame.setArgument( argIdx, v, attr);
      case Types.T_LONG:
        return frame.setLongArgument( argIdx, v, attr);
      case Types.T_FLOAT:
        return frame.setFloatArgument(argIdx, v, attr);
      case Types.T_DOUBLE:
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_CHAR:
    {
      char v = eiArg.getCharField("value");
      switch (destType){
      case Types.T_CHAR:
      case Types.T_INT:
        return frame.setArgument( argIdx, v, attr);
      case Types.T_LONG:
        return frame.setLongArgument( argIdx, v, attr);
      case Types.T_FLOAT:
        return frame.setFloatArgument(argIdx, v, attr);
      case Types.T_DOUBLE:
        return frame.setDoubleArgument( argIdx, v, attr);
      }
      return -1;
    }
    case Types.T_BOOLEAN:
    {
      boolean v = eiArg.getBooleanField("value");
      if (destType == Types.T_BOOLEAN){
        return frame.setArgument( argIdx, v ? 1 : 0, attr);
      }
      return -1;
    }
    case Types.T_ARRAY:
    {
      int ref =  eiArg.getObjectRef();
      if (destType == Types.T_ARRAY){
        return frame.setReferenceArgument( argIdx, ref, attr);
      }
      return -1;
    }
    case Types.T_REFERENCE:
    {
      int ref =  eiArg.getObjectRef();
      if (destType == Types.T_REFERENCE){
        return frame.setReferenceArgument( argIdx, ref, attr);
      }
      return -1;
    }
    default:
      // T_VOID, T_NONE
      return -1;
    }
  }

  @MJI
  public int invoke__Ljava_lang_Object_2_3Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int mthRef, int objRef, int argsRef) {
    ThreadInfo ti = env.getThreadInfo();
    MethodInfo miCallee = getMethodInfo(env, mthRef);
    ClassInfo calleeClass = miCallee.getClassInfo();
    DirectCallStackFrame frame = ti.getReturnedDirectCall();
    
    if (frame == null){ // first time

      //--- check the instance we are calling on
      if (!miCallee.isStatic()) {
        if (objRef == MJIEnv.NULL){
          env.throwException("java.lang.NullPointerException");
          return MJIEnv.NULL;
          
        } else {
          ElementInfo eiObj = ti.getElementInfo(objRef);
          ClassInfo objClass = eiObj.getClassInfo();
        
          if (!objClass.isInstanceOf(calleeClass)) {
            env.throwException(IllegalArgumentException.class.getName(), "Object is not an instance of declaring class.  Actual = " + objClass + ".  Expected = " + calleeClass);
            return MJIEnv.NULL;
          }
        }
      }

      //--- check accessibility
      ElementInfo eiMth = ti.getElementInfo(mthRef);
      if (! (Boolean) eiMth.getFieldValueObject("isAccessible")) {
        StackFrame caller = ti.getTopFrame().getPrevious();
        ClassInfo callerClass = caller.getClassInfo();

        if (callerClass != calleeClass) {
          env.throwException(IllegalAccessException.class.getName(), "Class " + callerClass.getName() +
                  " can not access a member of class " + calleeClass.getName()
                  + " with modifiers \"" + Modifier.toString(miCallee.getModifiers()));
          return MJIEnv.NULL;
        }
      }
      
      //--- push the direct call
      frame = miCallee.createDirectCallStackFrame(ti, 0);
      frame.setReflection();
      
      int argOffset = 0;
      if (!miCallee.isStatic()) {
        frame.setReferenceArgument( argOffset++, objRef, null);
      }
      if (!pushUnboxedArguments( env, miCallee, frame, argOffset, argsRef)) {
        // we've got a IllegalArgumentException
        return MJIEnv.NULL;  
      }
      ti.pushFrame(frame);
      
      
      //--- check for and push required clinits
      if (miCallee.isStatic()){
        calleeClass.initializeClass(ti);
      }
      
      return MJIEnv.NULL; // reexecute
      
    } else { // we have returned from the direct call
      return createBoxedReturnValueObject( env, miCallee, frame);
    }
  }
  

  // this one has to collect annotations upwards in the inheritance chain
  static int getAnnotations (MJIEnv env, MethodInfo mi){
    String mname = mi.getName();
    String msig = mi.genericSignature;
    ArrayList<AnnotationInfo> aiList = new ArrayList<AnnotationInfo>();
    
    // our own annotations
    ClassInfo ci = mi.getClassInfo();
    for (AnnotationInfo ai : mi.getAnnotations()) {
      aiList.add(ai);
    }
    
    // our superclass annotations
    for (ci = ci.getSuperClass(); ci != null; ci = ci.getSuperClass()){
      mi = ci.getMethod(mname, msig, false);
      if (mi != null){
        for (AnnotationInfo ai: mi.getAnnotations()){
          aiList.add(ai);
        }        
      }
    }

    try {
      return env.newAnnotationProxies(aiList.toArray(new AnnotationInfo[aiList.size()]));
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }    
  }

  @MJI
  public int getAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int mthRef){
    return getAnnotations( env, getMethodInfo(env,mthRef));
  }
  
  // the following ones consist of a package default implementation that is shared with
  // the constructor peer, and a public model method
  static int getAnnotation (MJIEnv env, MethodInfo mi, int annotationClsRef){
    ClassInfo aci = env.getReferredClassInfo(annotationClsRef);
    
    AnnotationInfo ai = mi.getAnnotation(aci.getName());
    if (ai != null){
      ClassInfo aciProxy = aci.getAnnotationProxy();
      try {
        return env.newAnnotationProxy(aciProxy, ai);
      } catch (ClinitRequired x){
        env.handleClinitRequest(x.getRequiredClassInfo());
        return MJIEnv.NULL;
      }
    }
    
    return MJIEnv.NULL;
  }  

  @MJI
  public int getAnnotation__Ljava_lang_Class_2__Ljava_lang_annotation_Annotation_2 (MJIEnv env, int mthRef, int annotationClsRef) {
    return getAnnotation(env, getMethodInfo(env,mthRef), annotationClsRef);
  }
  
  static int getDeclaredAnnotations (MJIEnv env, MethodInfo mi){
    AnnotationInfo[] ai = mi.getAnnotations();

    try {
      return env.newAnnotationProxies(ai);
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }    
  }

  @MJI
  public int getDeclaredAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int mthRef){
    return getDeclaredAnnotations( env, getMethodInfo(env,mthRef));
  }
  
  static int getParameterAnnotations (MJIEnv env, MethodInfo mi){
    AnnotationInfo[][] pa = mi.getParameterAnnotations();
    // this should always return an array object, even if the method has no arguments
    
    try {
      int paRef = env.newObjectArray("[Ljava/lang/annotation/Annotation;", pa.length);
      
      for (int i=0; i<pa.length; i++){
        int eRef = env.newAnnotationProxies(pa[i]);
        env.setReferenceArrayElement(paRef, i, eRef);
      }

      return paRef;
      
    } catch (ClinitRequired x){ // be prepared that we might have to initialize respective annotation classes
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }    
  }

  @MJI
  public int getParameterAnnotations_____3_3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int mthRef){
    return getParameterAnnotations( env, getMethodInfo(env,mthRef));
  }

  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objRef){
    StringBuilder sb = new StringBuilder();
    
    MethodInfo mi = getMethodInfo(env, objRef);

    sb.append(Modifier.toString(mi.getModifiers()));
    sb.append(' ');

    sb.append(mi.getReturnTypeName());
    sb.append(' ');

    sb.append(mi.getClassName());
    sb.append('.');

    sb.append(mi.getName());

    sb.append('(');
    
    String[] at = mi.getArgumentTypeNames();
    for (int i=0; i<at.length; i++){
      if (i>0) sb.append(',');
      sb.append(at[i]);
    }
    
    sb.append(')');
    
    int sref = env.newString(sb.toString());
    return sref;
  }

  @MJI
  public boolean equals__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int mthRef){
    ElementInfo ei = env.getElementInfo(mthRef);
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo(JPF_java_lang_Class.METHOD_CLASSNAME);

    if (ei.getClassInfo() == ci){
      MethodInfo mi1 = getMethodInfo(env, objRef);
      MethodInfo mi2 = getMethodInfo(env, mthRef);
      if (mi1.getClassInfo() == mi2.getClassInfo()){
        if (mi1.getName().equals(mi2.getName())){
          if (mi1.getReturnType().equals(mi2.getReturnType())){
            byte[] params1 = mi1.getArgumentTypes();
            byte[] params2 = mi2.getArgumentTypes();
            if (params1.length == params2.length){
              for (int i = 0; i < params1.length; i++){
                if (params1[i] != params2[i]){
                  return false;
                }
              }
              return true;
            }
          }
        }
      }
    }
    return false;
  }

  @MJI
  public int hashCode____I (MJIEnv env, int objRef){
    MethodInfo mi = getMethodInfo(env, objRef);
    return mi.getClassName().hashCode() ^ mi.getName().hashCode();
  }
}
