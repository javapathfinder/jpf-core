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

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.annotation.MJI;


/**
 * MJI NativePeer class for java.lang.Class library abstraction
 */
public class JPF_java_lang_Class extends NativePeer {
  
  static final String FIELD_CLASSNAME = "java.lang.reflect.Field";
  static final String METHOD_CLASSNAME = "java.lang.reflect.Method";
  static final String CONSTRUCTOR_CLASSNAME = "java.lang.reflect.Constructor";
  
  public static boolean init (Config conf){
    // we create Method and Constructor objects, so we better make sure these
    // classes are initialized (they already might be)
    JPF_java_lang_reflect_Method.init(conf);
    JPF_java_lang_reflect_Constructor.init(conf);
    return true;
  }
  
  @MJI
  public boolean isArray____Z (MJIEnv env, int robj) {
    ClassInfo ci = env.getReferredClassInfo( robj);
    return ci.isArray();
  }

  @MJI
  public int getComponentType____Ljava_lang_Class_2 (MJIEnv env, int robj) {
    if (isArray____Z(env, robj)) {
      ThreadInfo ti = env.getThreadInfo();
      Instruction insn = ti.getPC();
      ClassInfo ci = env.getReferredClassInfo( robj).getComponentClassInfo();

    if (ci.initializeClass(ti)){
        env.repeatInvocation();
        return MJIEnv.NULL;
      }

      return ci.getClassObjectRef();
    }

    return MJIEnv.NULL;
  }

  @MJI
  public boolean isInstance__Ljava_lang_Object_2__Z (MJIEnv env, int robj,
                                                         int r1) {
    ElementInfo sei = env.getStaticElementInfo(robj);
    ClassInfo   ci = sei.getClassInfo();
    ClassInfo   ciOther = env.getClassInfo(r1);
    return (ciOther.isInstanceOf(ci));
  }

  @MJI
  public boolean isInterface____Z (MJIEnv env, int robj){
    ClassInfo ci = env.getReferredClassInfo( robj);
    return ci.isInterface();
  }
  
  @MJI
  public boolean isAssignableFrom__Ljava_lang_Class_2__Z (MJIEnv env, int rcls,
                                                              int r1) {
    ElementInfo sei1 = env.getStaticElementInfo(rcls);
    ClassInfo   ci1 = sei1.getClassInfo();

    ElementInfo sei2 = env.getStaticElementInfo(r1);
    ClassInfo   ci2 = sei2.getClassInfo();

    return ci2.isInstanceOf( ci1);
  }
  
  @MJI
  public int getAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int robj){    
    ClassInfo ci = env.getReferredClassInfo( robj);
    AnnotationInfo[] ai = ci.getAnnotations();

    try {
      return env.newAnnotationProxies(ai);
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }
  }
  
  @MJI
  public int getAnnotation__Ljava_lang_Class_2__Ljava_lang_annotation_Annotation_2 (MJIEnv env, int robj,
                                                                                int annoClsRef){
    ClassInfo ci = env.getReferredClassInfo( robj);
    ClassInfo aci = env.getReferredClassInfo(annoClsRef);
    
    AnnotationInfo ai = ci.getAnnotation(aci.getName());
    if (ai != null){
      ClassInfo aciProxy = aci.getAnnotationProxy();
      
      try {
        return env.newAnnotationProxy(aciProxy, ai);
      } catch (ClinitRequired x){
        env.handleClinitRequest(x.getRequiredClassInfo());
        return MJIEnv.NULL;
      }
    } else {
      return MJIEnv.NULL;
    }
  }
  
  @MJI
  public int getPrimitiveClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env,
                                                            int rcls, int stringRef) {
    // we don't really have to check for a valid class name here, since
    // this is a package default method that just gets called from
    // the clinit of box classes
    // note this does NOT return the box class (e.g. java.lang.Integer), which
    // is a normal, functional class, but a primitive class (e.g. 'int') that
    // is rather a strange beast (not even Object derived)
    
    ClassLoaderInfo scli = env.getSystemClassLoaderInfo(); // this is the one responsible for builtin classes
    String primClsName = env.getStringObject(stringRef); // always initialized
    
    ClassInfo ci = scli.getResolvedClassInfo(primClsName);
    return ci.getClassObjectRef();
  }

  @MJI
  public boolean desiredAssertionStatus____Z (MJIEnv env, int robj) {
    ClassInfo ci = env.getReferredClassInfo(robj);
    return ci.desiredAssertionStatus();
  }

  public static int getClassObject (MJIEnv env, ClassInfo ci){
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();

    if (ci.initializeClass(ti)){
      env.repeatInvocation();
      return MJIEnv.NULL;
    }

    StaticElementInfo ei = ci.getStaticElementInfo();
    int ref = ei.getClassObjectRef();

    return ref;
  }
  
  @MJI
  public int forName__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env,
                                                                       int rcls,
                                                                       int clsNameRef) {
    if (clsNameRef == MJIEnv.NULL){
      env.throwException("java.lang.NullPointerException", "no class name provided");
      return MJIEnv.NULL;
    }
    
    String clsName = env.getStringObject(clsNameRef);
    
    if (clsName.isEmpty()){
      env.throwException("java.lang.ClassNotFoundException", "empty class name");
      return MJIEnv.NULL;  
    }
    
    ThreadInfo ti = env.getThreadInfo();
    MethodInfo mi = ti.getTopFrame().getPrevious().getMethodInfo();
    // class of the method that includes the invocation of Class.forName() 
    ClassInfo cls = mi.getClassInfo();

    String name;
    // for array type, the component terminal must be resolved
    if(clsName.charAt(0)=='[') {
      name = Types.getComponentTerminal(clsName);
    } else{
      name = clsName;
    }

    // make the classloader of the class including the invocation of 
    // Class.forName() resolve the class with the given name
    try {
      cls.resolveReferencedClass(name);
    } catch(LoadOnJPFRequired lre) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }

    // The class obtained here is the same as the resolved one, except
    // if it represents an array type
    ClassInfo ci = cls.getClassLoaderInfo().getResolvedClassInfo(clsName);

    return getClassObject(env, ci);
  }

  /**
   * this is an example of a native method issuing direct calls - otherwise known
   * as a round trip.
   * We don't have to deal with class init here anymore, since this is called
   * via the class object of the class to instantiate
   */
  @MJI
  public int newInstance____Ljava_lang_Object_2 (MJIEnv env, int robj) {
    ThreadInfo ti = env.getThreadInfo();
    DirectCallStackFrame frame = ti.getReturnedDirectCall();
    
    ClassInfo ci = env.getReferredClassInfo(robj);   // what are we
    MethodInfo miCtor = ci.getMethod("<init>()V", true); // note there always is one since something needs to call Object()

    if (frame == null){ // first time around
      if(ci.isAbstract()){ // not allowed to instantiate
        env.throwException("java.lang.InstantiationException");
        return MJIEnv.NULL;
      }

      // <2do> - still need to handle protected
      if (miCtor.isPrivate()) {
        env.throwException("java.lang.IllegalAccessException", "cannot access non-public member of class " + ci.getName());
        return MJIEnv.NULL;
      }

      int objRef = env.newObjectOfUncheckedClass(ci);  // create the thing

      frame = miCtor.createDirectCallStackFrame(ti, 1);
      // note that we don't set this as a reflection call since it is supposed to propagate exceptions
      frame.setReferenceArgument(0, objRef, null);
      frame.setLocalReferenceVariable(0, objRef);        // (1) store ref for retrieval during re-exec
      ti.pushFrame(frame);
      
      // check if we have to push clinits
      ci.initializeClass(ti);
      
      env.repeatInvocation();
      return MJIEnv.NULL;
      
    } else { // re-execution
      int objRef = frame.getLocalVariable(0); // that's the object ref we set in (1)
      return objRef;
    }      
  }
  
  @MJI
  public int getSuperclass____Ljava_lang_Class_2 (MJIEnv env, int robj) {
    ClassInfo ci = env.getReferredClassInfo( robj);
    ClassInfo sci = ci.getSuperClass();
    if (sci != null) {
      return sci.getClassObjectRef();
    } else {
      return MJIEnv.NULL;
    }
  }

  int getMethod (MJIEnv env, int clsRef, ClassInfo ciMethod, String mname, int argTypesRef,
                        boolean isRecursiveLookup, boolean publicOnly) {

    ClassInfo ci = env.getReferredClassInfo( clsRef);
    
    StringBuilder sb = new StringBuilder(mname);
    sb.append('(');
    int nParams = argTypesRef != MJIEnv.NULL ? env.getArrayLength(argTypesRef) : 0;
    for (int i=0; i<nParams; i++) {
      int cRef = env.getReferenceArrayElement(argTypesRef, i);
      ClassInfo cit = env.getReferredClassInfo( cRef);
      String tname = cit.getName();
      String tcode = tname;
      tcode = Types.getTypeSignature(tcode, false);
      sb.append(tcode);
    }
    sb.append(')');
    String fullMthName = sb.toString();

    MethodInfo mi = ci.getReflectionMethod(fullMthName, isRecursiveLookup);
    if (mi == null || (publicOnly && !mi.isPublic())) {
      env.throwException("java.lang.NoSuchMethodException", ci.getName() + '.' + fullMthName);
      return MJIEnv.NULL;
      
    } else {
      return createMethodObject(env, ciMethod, mi);      
    }
  }

  int createMethodObject (MJIEnv env, ClassInfo objectCi, MethodInfo mi) {
    // NOTE - we rely on Constructor and Method peers being initialized
    if (mi.isCtor()){
      return JPF_java_lang_reflect_Constructor.createConstructorObject(env, objectCi, mi);
    } else {
      return JPF_java_lang_reflect_Method.createMethodObject(env, objectCi, mi);      
    }
  }
  
  @MJI
  public int getDeclaredMethod__Ljava_lang_String_2_3Ljava_lang_Class_2__Ljava_lang_reflect_Method_2 (MJIEnv env, int clsRef,
                                                                                                     int nameRef, int argTypesRef) {
    ClassInfo mci = getInitializedClassInfo(env, METHOD_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    String mname = env.getStringObject(nameRef);
    return getMethod(env, clsRef, mci, mname, argTypesRef, false, false);
  }

  @MJI
  public int getDeclaredConstructor___3Ljava_lang_Class_2__Ljava_lang_reflect_Constructor_2 (MJIEnv env,
                                                                                               int clsRef,
                                                                                               int argTypesRef){
    ClassInfo mci = getInitializedClassInfo(env, CONSTRUCTOR_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    int ctorRef =  getMethod(env,clsRef, mci, "<init>",argTypesRef,false, false);
    return ctorRef;
  }
  
  @MJI
  public int getMethod__Ljava_lang_String_2_3Ljava_lang_Class_2__Ljava_lang_reflect_Method_2 (MJIEnv env, int clsRef,
                                                                                                     int nameRef, int argTypesRef) {
    ClassInfo mci = getInitializedClassInfo(env, METHOD_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    String mname = env.getStringObject(nameRef);
    return getMethod( env, clsRef, mci, mname, argTypesRef, true, true);
  }

  private void addDeclaredMethodsRec (boolean includeSuperClasses, HashMap<String,MethodInfo>methods, ClassInfo ci){
    
    if (includeSuperClasses){ // do NOT include Object methods for interfaces
      ClassInfo sci = ci.getSuperClass();
      if (sci != null){
        addDeclaredMethodsRec( includeSuperClasses, methods,sci);
      }
    }

    ClassLoaderInfo cl = ci.getClassLoaderInfo();
    for (String ifcName : ci.getDirectInterfaceNames()){
      ClassInfo ici = cl.getResolvedClassInfo(ifcName); // has to be already defined, so no exception
      addDeclaredMethodsRec( includeSuperClasses, methods,ici);
    }

    for (MethodInfo mi : ci.getDeclaredMethodInfos()) {
      // filter out non-public, <clinit> and <init>
      if (mi.isPublic() && (mi.getName().charAt(0) != '<')) {
        String mname = mi.getUniqueName();

        if (!(ci.isInterface() && methods.containsKey(mname))){
          methods.put(mname, mi);
        }
      }
    }
  }

  @MJI
  public int getMethods_____3Ljava_lang_reflect_Method_2 (MJIEnv env, int objref) {
    ClassInfo mci = getInitializedClassInfo(env, METHOD_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    ClassInfo ci = env.getReferredClassInfo(objref);

    // collect all the public, non-ctor instance methods
    if (!ci.isPrimitive()) {
      HashMap<String,MethodInfo> methods = new HashMap<String,MethodInfo>();
      addDeclaredMethodsRec( !ci.isInterface(), methods,ci);
      
      int n = methods.size();
      int aref = env.newObjectArray("Ljava/lang/reflect/Method;", n);
      int i=0;

      for (MethodInfo mi : methods.values()){
        int mref = createMethodObject(env, mci, mi);
        env.setReferenceArrayElement(aref,i++,mref);
      }

      return aref;

    } else {
      return env.newObjectArray("Ljava/lang/reflect/Method;", 0);
    }
  }
  
  @MJI
  public int getDeclaredMethods_____3Ljava_lang_reflect_Method_2 (MJIEnv env, int objref) {
    ClassInfo mci = getInitializedClassInfo(env, METHOD_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    ClassInfo ci = env.getReferredClassInfo(objref);
    MethodInfo[] methodInfos = ci.getDeclaredMethodInfos();
    
    // we have to filter out the ctors and the static init
    int nMth = methodInfos.length;
    for (int i=0; i<methodInfos.length; i++){
      if (methodInfos[i].getName().charAt(0) == '<'){
        methodInfos[i] = null;
        nMth--;
      }
    }
    
    int aref = env.newObjectArray("Ljava/lang/reflect/Method;", nMth);
    
    for (int i=0, j=0; i<methodInfos.length; i++) {
      if (methodInfos[i] != null){
        int mref = createMethodObject(env, mci, methodInfos[i]);
        env.setReferenceArrayElement(aref,j++,mref);
      }
    }
    
    return aref;
  }
  
  int getConstructors (MJIEnv env, int objref, boolean publicOnly){
    ClassInfo mci = getInitializedClassInfo(env, CONSTRUCTOR_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    ClassInfo ci = env.getReferredClassInfo(objref);
    ArrayList<MethodInfo> ctors = new ArrayList<MethodInfo>();
    
    // we have to filter out the ctors and the static init
    for (MethodInfo mi : ci.getDeclaredMethodInfos()){
      if (mi.getName().equals("<init>")){
        if (!publicOnly || mi.isPublic()) {
          ctors.add(mi);
        }
      }
    }
    
    int nCtors = ctors.size();
    int aref = env.newObjectArray("Ljava/lang/reflect/Constructor;", nCtors);
    
    for (int i=0; i<nCtors; i++){
      env.setReferenceArrayElement(aref, i, createMethodObject(env, mci, ctors.get(i)));
    }
    
    return aref;
  }
  
  @MJI
  public int getConstructors_____3Ljava_lang_reflect_Constructor_2 (MJIEnv env, int objref){
    return getConstructors(env, objref, true);
  }  
  
  @MJI
  public int getDeclaredConstructors_____3Ljava_lang_reflect_Constructor_2 (MJIEnv env, int objref){
    return getConstructors(env, objref, false);
  }
  
  @MJI
  public int getConstructor___3Ljava_lang_Class_2__Ljava_lang_reflect_Constructor_2 (MJIEnv env, int clsRef,
                                                                                       int argTypesRef){
    ClassInfo mci = getInitializedClassInfo(env, CONSTRUCTOR_CLASSNAME);
    if (mci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    // <2do> should only return a public ctor 
    return getMethod(env,clsRef, mci, "<init>",argTypesRef,false,true);
  }
  
  // this is only used for system classes such as java.lang.reflect.Method
  ClassInfo getInitializedClassInfo (MJIEnv env, String clsName){
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();
    ClassInfo ci = ClassLoaderInfo.getSystemResolvedClassInfo( clsName);
    
    if (ci.initializeClass(ti)){
      return null;
    } else {
      return ci;
    }    
  }
  
  @MJI
  public void initialize0____V (MJIEnv env, int clsObjRef){
    ClassInfo ci = env.getReferredClassInfo( clsObjRef);
    ci.initializeClass(ThreadInfo.currentThread);
  }

  Set<ClassInfo> getInitializedInterfaces (MJIEnv env, ClassInfo ci){
    ThreadInfo ti = env.getThreadInfo();
    Instruction insn = ti.getPC();

    Set<ClassInfo> ifcs = ci.getAllInterfaceClassInfos();
    for (ClassInfo ciIfc : ifcs){
    if (ciIfc.initializeClass(ti)){
        return null;
      } 
    }

    return ifcs;
  }
  
  static int createFieldObject (MJIEnv env, FieldInfo fi, ClassInfo fci){
    int regIdx = JPF_java_lang_reflect_Field.registerFieldInfo(fi);
    
    int eidx = env.newObject(fci);
    ElementInfo ei = env.getModifiableElementInfo(eidx);    
    ei.setIntField("regIdx", regIdx);
    
    return eidx;
  }
  
  @MJI
  public int getDeclaredFields_____3Ljava_lang_reflect_Field_2 (MJIEnv env, int objRef) {
    ClassInfo fci = getInitializedClassInfo(env, FIELD_CLASSNAME);
    if (fci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }

    ClassInfo ci = env.getReferredClassInfo(objRef);
    int nInstance = ci.getNumberOfDeclaredInstanceFields();
    int nStatic = ci.getNumberOfStaticFields();
    int aref = env.newObjectArray("Ljava/lang/reflect/Field;", nInstance + nStatic);
    int i, j=0;
    
    for (i=0; i<nStatic; i++) {
      FieldInfo fi = ci.getStaticField(i);
      env.setReferenceArrayElement(aref, j++, createFieldObject(env, fi, fci));
    }    
    
    for (i=0; i<nInstance; i++) {
      FieldInfo fi = ci.getDeclaredInstanceField(i);
      env.setReferenceArrayElement(aref, j++, createFieldObject(env, fi, fci));
    }
    
    return aref;
  }
  
  @MJI
  public int getFields_____3Ljava_lang_reflect_Field_2 (MJIEnv env, int clsRef){
    ClassInfo fci = getInitializedClassInfo(env, FIELD_CLASSNAME);
    if (fci == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
        
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    // interfaces might not be initialized yet, so we have to check first
    Set<ClassInfo> ifcs = getInitializedInterfaces( env, ci);
    if (ifcs == null) {
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    
    ArrayList<FieldInfo> fiList = new ArrayList<FieldInfo>();
    for (; ci != null; ci = ci.getSuperClass()){
      // the host VM returns them in order of declaration, but the spec says there is no guaranteed order so we keep it simple
      for (FieldInfo fi : ci.getDeclaredInstanceFields()){
        if (fi.isPublic()){
          fiList.add(fi);
        }
      }
      for (FieldInfo fi : ci.getDeclaredStaticFields()){
        if (fi.isPublic()){
          fiList.add(fi);
        }
      }
    }
    
    for (ClassInfo ciIfc : ifcs){
      for (FieldInfo fi : ciIfc.getDeclaredStaticFields()){
        fiList.add(fi); // there are no non-public fields in interfaces
      }      
    }

    int aref = env.newObjectArray("Ljava/lang/reflect/Field;", fiList.size());
    int j=0;
    for (FieldInfo fi : fiList){
      env.setReferenceArrayElement(aref, j++, createFieldObject(env, fi, fci));
    }
    
    return aref;
  }
    
  int getField (MJIEnv env, int clsRef, int nameRef, boolean isRecursiveLookup) {    
    ClassInfo ci = env.getReferredClassInfo( clsRef);
    String fname = env.getStringObject(nameRef);
    FieldInfo fi = null;
    
    if (isRecursiveLookup) {
      fi = ci.getInstanceField(fname);
      if (fi == null) {
        fi = ci.getStaticField(fname);
      }      
    } else {
        fi = ci.getDeclaredInstanceField(fname);
        if (fi == null) {
          fi = ci.getDeclaredStaticField(fname);
        }
    }
    
    if (fi == null) {      
      env.throwException("java.lang.NoSuchFieldException", fname);
      return MJIEnv.NULL;
      
    } else {
      // don't do a Field clinit before we know there is such a field
      ClassInfo fci = getInitializedClassInfo( env, FIELD_CLASSNAME);
      if (fci == null) {
        env.repeatInvocation();
        return MJIEnv.NULL;
      }
      
      return createFieldObject( env, fi, fci);
    }
  }
  
  @MJI
  public int getDeclaredField__Ljava_lang_String_2__Ljava_lang_reflect_Field_2 (MJIEnv env, int clsRef, int nameRef) {
    return getField(env,clsRef,nameRef, false);
  }  
 
  @MJI
  public int getField__Ljava_lang_String_2__Ljava_lang_reflect_Field_2 (MJIEnv env, int clsRef, int nameRef) {
    return getField(env,clsRef,nameRef, true);    
  }

  @MJI
  public int getModifiers____I (MJIEnv env, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    return ci.getModifiers();
  }

  @MJI
  public int getEnumConstants_____3Ljava_lang_Object_2 (MJIEnv env, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    
    if (env.requiresClinitExecution(ci)){
      env.repeatInvocation();
      return 0;
    }

    if (ci.getSuperClass().getName().equals("java.lang.Enum")) {      
      ArrayList<FieldInfo> list = new ArrayList<FieldInfo>();
      String cName = ci.getName();
      
      for (FieldInfo fi : ci.getDeclaredStaticFields()) {
        if (fi.isFinal() && cName.equals(fi.getType())){
          list.add(fi);
        }
      }
      
      int aRef = env.newObjectArray(cName, list.size());      
      StaticElementInfo sei = ci.getStaticElementInfo();
      int i=0;
      for (FieldInfo fi : list){
        env.setReferenceArrayElement( aRef, i++, sei.getReferenceField(fi));
      }
      return aRef;
    }
    
    return MJIEnv.NULL;
  }
    
  @MJI
  public int getInterfaces_____3Ljava_lang_Class_2 (MJIEnv env, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    int aref = MJIEnv.NULL;
    ThreadInfo ti = env.getThreadInfo();
    
    // contrary to the API doc, this only returns the interfaces directly
    // implemented by this class, not it's bases
    // <2do> this is not exactly correct, since the interfaces should be ordered
    Set<ClassInfo> interfaces = ci.getInterfaceClassInfos();
    aref = env.newObjectArray("Ljava/lang/Class;", interfaces.size());

    int i=0;
    for (ClassInfo ifc: interfaces){
      env.setReferenceArrayElement(aref, i++, ifc.getClassObjectRef());
    }
    
    return aref;
  }


  /**
   * <2do> needs to load from the classfile location, NOT the MJIEnv (native) class
   *
   * @author Sebastian Gfeller (sebastian.gfeller@gmail.com)
   * @author Tihomir Gvero (tihomir.gvero@gmail.com)
   */
  @MJI
  public int getByteArrayFromResourceStream__Ljava_lang_String_2___3B(MJIEnv env, int clsRef, int nameRef) {
    String name = env.getStringObject(nameRef);

    // <2do> this is not loading from the classfile location! fix it
    InputStream is = env.getClass().getResourceAsStream(name);
    if (is == null){
      return MJIEnv.NULL;
    }
    // We assume that the entire input stream can be read at the moment,
    // although this could break.
    byte[] content = null;
    try {
      content = new byte[is.available()];
      is.read(content);
    } catch (IOException e) {
      throw new RuntimeException(e);
    }
    // Now if everything worked, the content should be in the byte buffer.
    // We put this buffer into the JPF VM.
    return env.newByteArray(content);
  }

  @MJI
  public int getEnclosingClass____Ljava_lang_Class_2 (MJIEnv env, int clsRef) {
    ClassInfo ciEncl = env.getReferredClassInfo( clsRef).getEnclosingClassInfo();
    
    if (ciEncl == null){
      return MJIEnv.NULL;
    }

    if (ciEncl.initializeClass(env.getThreadInfo())) {
      env.repeatInvocation();
      return 0;
    }

    return ciEncl.getClassObjectRef();
  }

  @MJI
  public int getDeclaredClasses_____3Ljava_lang_Class_2 (MJIEnv env, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    String[] innerClassNames =  ci.getInnerClasses();
    int aref = MJIEnv.NULL;
    ThreadInfo ti = env.getThreadInfo();
    
    MethodInfo mi = ti.getTopFrame().getPrevious().getMethodInfo();
    // class of the method that includes the invocation of Class.getDeclaredClasses 
    ClassInfo cls = mi.getClassInfo();

    // first resolve all the inner classes
    int length = innerClassNames.length;
    ClassInfo[] resolvedInnerClass = new ClassInfo[length];
    for(int i=0; i<length; i++) {
      try {
        resolvedInnerClass[i] = cls.resolveReferencedClass(innerClassNames[i]);
      } catch(LoadOnJPFRequired lre) {
        env.repeatInvocation();
        return MJIEnv.NULL;
      }
    }

    aref = env.newObjectArray("Ljava/lang/Class;", innerClassNames.length);
    for (int i=0; i<length; i++){
      ClassInfo ici = resolvedInnerClass[i];
      if (!ici.isRegistered()) {
        ici.registerClass(ti);
      }
      env.setReferenceArrayElement(aref, i, ici.getClassObjectRef());
    }
    
    return aref;
  }

  private String getCanonicalName (ClassInfo ci){
    if (ci.isArray()){
      String canonicalName = getCanonicalName(ci.getComponentClassInfo());
      if (canonicalName != null){
        return canonicalName + "[]";
      } else{
        return null;
      }
    }
    if (isLocalOrAnonymousClass(ci)) {
      return null;
    }
    if (ci.getEnclosingClassInfo() == null){
      return ci.getName();
    } else{
      String enclosingName = getCanonicalName(ci.getEnclosingClassInfo());
      if (enclosingName == null){ return null; }
      return enclosingName + "." + ci.getSimpleName();
    }
  }

  @MJI
  public int getCanonicalName____Ljava_lang_String_2 (MJIEnv env, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    return env.newString(getCanonicalName(ci));
  }

  @MJI
  public boolean isAnnotation____Z (MJIEnv env, int clsObjRef){
    ClassInfo ci = env.getReferredClassInfo(clsObjRef);
    return (ci.getModifiers() & 0x2000) != 0;
  }
  
  @MJI
  public boolean isAnnotationPresent__Ljava_lang_Class_2__Z (MJIEnv env, int clsObjRef, int annoClsObjRef){
    ClassInfo ci = env.getReferredClassInfo(clsObjRef);
    ClassInfo ciAnno = env.getReferredClassInfo(annoClsObjRef);
    
    return ci.getAnnotation( ciAnno.getName()) != null;    
  }
  
  @MJI
  public int getDeclaredAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int robj){
    ClassInfo ci = env.getReferredClassInfo(robj);

    try{
      return env.newAnnotationProxies(ci.getDeclaredAnnotations());
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }
  }

  @MJI
  public int getEnclosingConstructor____Ljava_lang_reflect_Constructor_2 (MJIEnv env, int robj){
    ClassInfo mci = getInitializedClassInfo(env, CONSTRUCTOR_CLASSNAME);
    if (mci == null){
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    ClassInfo ci = env.getReferredClassInfo(robj);
    MethodInfo enclosingMethod = ci.getEnclosingMethodInfo();

    if ((enclosingMethod != null) && enclosingMethod.isCtor()){ 
      return createMethodObject(env, mci, enclosingMethod); 
    }
    return MJIEnv.NULL;
  }

  @MJI
  public int getEnclosingMethod____Ljava_lang_reflect_Method_2 (MJIEnv env, int robj){
    ClassInfo mci = getInitializedClassInfo(env, METHOD_CLASSNAME);
    if (mci == null){
      env.repeatInvocation();
      return MJIEnv.NULL;
    }
    ClassInfo ci = env.getReferredClassInfo(robj);
    MethodInfo enclosingMethod = ci.getEnclosingMethodInfo();

    if ((enclosingMethod != null) && !enclosingMethod.isCtor()){ 
      return createMethodObject(env, mci, enclosingMethod); 
    }
    return MJIEnv.NULL;
  }

  @MJI
  public boolean isAnonymousClass____Z (MJIEnv env, int robj){
    ClassInfo ci = env.getReferredClassInfo(robj);
    String cname = null;
    if (ci.getName().contains("$")){
      cname = ci.getName().substring(ci.getName().lastIndexOf('$') + 1);
    }
    return (cname == null) ? false : cname.matches("\\d+?");
  }

  @MJI
  public boolean isEnum____Z (MJIEnv env, int robj){
    ClassInfo ci = env.getReferredClassInfo(robj);
    return ci.isEnum();
  }

  // Similar to getEnclosingClass() except it returns null for the case of
  // anonymous class.
  @MJI
  public int getDeclaringClass____Ljava_lang_Class_2 (MJIEnv env, int clsRef){
    ClassInfo ci = env.getReferredClassInfo(clsRef);
    if (isLocalOrAnonymousClass(ci)){
      return MJIEnv.NULL;
    } else{
      return getEnclosingClass____Ljava_lang_Class_2(env, clsRef);
    }
  }

  @MJI
  public boolean isLocalClass____Z (MJIEnv env, int robj){
    ClassInfo ci = env.getReferredClassInfo(robj);
    return isLocalOrAnonymousClass(ci) && !isAnonymousClass____Z(env, robj);
  }

  private boolean isLocalOrAnonymousClass (ClassInfo ci){
    return (ci.getEnclosingMethodInfo() != null);
  }

  @MJI
  public boolean isMemberClass____Z (MJIEnv env, int robj){
    ClassInfo ci = env.getReferredClassInfo(robj);
    return (ci.getEnclosingClassInfo() != null) && !isLocalOrAnonymousClass(ci);
  }

  /**
   * Append the package name prefix of the class represented by robj, if the name is not 
   * absolute. OW, remove leading "/". 
   */
  @MJI
  public int getResolvedName__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int robj, int resRef){
    String rname = env.getStringObject(resRef);
    ClassInfo ci = env.getReferredClassInfo(robj);
    if (rname == null) {
      return MJIEnv.NULL;
    }
    if (!rname.startsWith("/")) {
      ClassInfo c = ci;
      while (c.isArray()) {
          c = c.getComponentClassInfo();
      }
      String baseName = c.getName();
      int index = baseName.lastIndexOf('.');
      if (index != -1) {
        rname = baseName.substring(0, index).replace('.', '/')
            +"/"+rname;
      }
    } else {
        rname = rname.substring(1);
    }

    return env.newString(rname);
  }
}
