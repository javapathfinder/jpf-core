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

import java.lang.reflect.Modifier;

/**
 * native peer for java.lang.reflect.Field
 */
public class JPF_java_lang_reflect_Field extends NativePeer {

  // <2do> using Fields is fine, but creating them is not efficient until we get rid of the registry
  
  static final int NREG = 64;
  static FieldInfo[] registered;
  static int nRegistered;
  
  public static boolean init (Config conf){
    registered = new FieldInfo[NREG];
    nRegistered = 0;
    return true;
  }
  
  static int registerFieldInfo (FieldInfo fi) {
    int idx;
    
    for (idx=0; idx < nRegistered; idx++) {
      if (registered[idx] == fi) {
        return idx;
      }
    }
    
    if (idx == registered.length) {
      FieldInfo[] newReg = new FieldInfo[registered.length+NREG];
      System.arraycopy(registered, 0, newReg, 0, registered.length);
      registered = newReg;
    }
    
    registered[idx] = fi;
    nRegistered++;
    return idx;
  }
  
  static FieldInfo getRegisteredFieldInfo (int idx) {
    return registered[idx];
  }
  
  /**
   * <2do> that doesn't take care of class init yet
   */
  @MJI
  public int getType____Ljava_lang_Class_2 (MJIEnv env, int objRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);

    ClassInfo ci = fi.getTypeClassInfo();
    if (!ci.isRegistered()) {
      ci.registerClass(ti);
    }

    return ci.getClassObjectRef();
  }
  
  @MJI
  public int getModifiers____I (MJIEnv env, int objRef){
    FieldInfo fi = getFieldInfo(env, objRef);
    return fi.getModifiers();
  }

  protected StackFrame getCallerFrame (MJIEnv env){
    ThreadInfo ti = env.getThreadInfo();
    StackFrame frame = ti.getTopFrame(); // this is the Field.get/setX(), so we still have to get down
    return frame.getPrevious();
  }
  
  protected boolean isAccessible (MJIEnv env, FieldInfo fi, int fieldRef, int ownerRef){
    
    // note that setAccessible() even overrides final
    ElementInfo fei = env.getElementInfo(fieldRef);
    if (fei.getBooleanField("isAccessible")){
      return true;
    }
    
    if (fi.isFinal()){
      return false;
    }
    
    if (fi.isPublic()){
      return true;
    }
    
    // otherwise we have to check object identities and access modifier of the executing method
    ClassInfo ciDecl = fi.getClassInfo();
    String declPackage = ciDecl.getPackageName();
    
    StackFrame frame = getCallerFrame(env);    
    MethodInfo mi = frame.getMethodInfo();
    ClassInfo ciMethod = mi.getClassInfo();
    String mthPackage = ciMethod.getPackageName();

    if (!fi.isPrivate() && declPackage.equals(mthPackage)) {
      return true;
    }
    
    if (fi.isStatic()){
      if (ciDecl == ciMethod){
        return true;
      }
      
    } else {
      int thisRef = frame.getCalleeThis(mi);
      if (thisRef == ownerRef) { // same object
        return true;
      }
    }
    
    // <2do> lots of more checks here
    
    return false;
  }
  
  protected ElementInfo getCheckedElementInfo (MJIEnv env, FieldInfo fi, int objRef, int ownerRef, boolean isWrite){
    ElementInfo ei;

    if (!isAvailable(env, fi, ownerRef)){
      return null;
    }

    if (fi.isStatic()){
      ClassInfo fci = fi.getClassInfo();
      ei = isWrite ? fci.getModifiableStaticElementInfo() : fci.getStaticElementInfo();
    } else { // instance field
      ei = isWrite ? env.getModifiableElementInfo(ownerRef) : env.getElementInfo(ownerRef);
    }

    if (ei == null) {
      env.throwException("java.lang.NullPointerException");
      return null;
    }

    if ( !isAccessible(env, fi, objRef, ownerRef)){
      env.throwException("java.lang.IllegalAccessException", "field not accessible: " + fi);
      return null;
    }
    
    return ei;
  }
  
  protected boolean checkFieldType (MJIEnv env, FieldInfo fi, Class<?> fiType){
    if (!fiType.isInstance(fi)) {
      env.throwException("java.lang.IllegalArgumentException", "incompatible field type: " + fi);
      return false;
    }
    
    return true;
  }
  
  protected ElementInfo checkSharedFieldAccess (ThreadInfo ti, ElementInfo ei, FieldInfo fi){    
    Instruction insn = ti.getPC();
    Scheduler scheduler = ti.getScheduler();
    
    if (fi.isStatic()){      
      if (scheduler.canHaveSharedClassCG(ti, insn, ei, fi)){
        ei = scheduler.updateClassSharedness(ti, ei, fi);
        scheduler.setsSharedClassCG(ti, insn, ei, fi);
      }
      
    } else {
      if (scheduler.canHaveSharedObjectCG(ti, insn, ei, fi)){
        ei = scheduler.updateObjectSharedness(ti, ei, fi);
        scheduler.setsSharedObjectCG(ti, insn, ei, fi);
      }
    }
    
    return ei;
  }
  
  @MJI
  public boolean getBoolean__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, BooleanFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return false;
        }
        
        return ei.getBooleanField(fi);
      }
    }
    return false;
  }

  @MJI
  public byte getByte__Ljava_lang_Object_2__B (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null) {
      if (checkFieldType(env, fi, BooleanFieldInfo.class)) {
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getByteField(fi);
      }
    }
    return 0;
  }

  @MJI
  public char getChar__Ljava_lang_Object_2__C (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, CharFieldInfo.class)) {
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getCharField(fi);
      }
    }
    return 0;
  }

  @MJI
  public short getShort__Ljava_lang_Object_2__S (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, ShortFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getShortField(fi);
      }
    }
    return 0;
  }

  @MJI
  public int getInt__Ljava_lang_Object_2__I (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, IntegerFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getIntField(fi);
      }
    }
    return 0;
  }

  @MJI
  public long getLong__Ljava_lang_Object_2__J (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, LongFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getLongField(fi);
      }
    }
    return 0;
  }

  @MJI
  public float getFloat__Ljava_lang_Object_2__F (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, FloatFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getFloatField(fi);
      }
    }
    return 0;
  }

  @MJI
  public double getDouble__Ljava_lang_Object_2__D (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, false);
    if (ei != null){
      if (checkFieldType(env, fi, DoubleFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return 0;
        }
        return ei.getDoubleField(fi);
      }
    }
    return 0;
  }

  @MJI
  public int getAnnotation__Ljava_lang_Class_2__Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef, int annotationClsRef) {
    FieldInfo fi = getFieldInfo(env,objRef);
    ClassInfo aci = env.getReferredClassInfo(annotationClsRef);
    
    AnnotationInfo ai = fi.getAnnotation(aci.getName());
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
  public int getAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef){
    FieldInfo fi = getFieldInfo(env,objRef);
    AnnotationInfo[] ai = fi.getAnnotations();
    
    try {
      return env.newAnnotationProxies(ai);
    } catch (ClinitRequired x){
      env.handleClinitRequest(x.getRequiredClassInfo());
      return MJIEnv.NULL;
    }
  }

  @MJI
  public void setBoolean__Ljava_lang_Object_2Z__V (MJIEnv env, int objRef, int ownerRef,
                                                          boolean val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, BooleanFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setBooleanField(fi, val);
      }
    }
  }

  @MJI
  public void setByte__Ljava_lang_Object_2B__V (MJIEnv env, int objRef, int ownerRef, byte val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, ByteFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setByteField(fi, val);
      }
    }
  }

  @MJI
  public void setChar__Ljava_lang_Object_2C__V (MJIEnv env, int objRef, int ownerRef, char val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, CharFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setCharField(fi, val);
      }
    }
  }

  @MJI
  public void setShort__Ljava_lang_Object_2S__V (MJIEnv env, int objRef, int ownerRef,  short val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, ShortFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setShortField(fi, val);
      }
    }
  }  

  @MJI
  public void setInt__Ljava_lang_Object_2I__V (MJIEnv env, int objRef, int ownerRef, int val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, IntegerFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setIntField(fi, val);
      }
    }
  }

  @MJI
  public void setLong__Ljava_lang_Object_2J__V (MJIEnv env, int objRef, int ownerRef, long val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, LongFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setLongField(fi, val);
      }
    }
  }

  @MJI
  public void setFloat__Ljava_lang_Object_2F__V (MJIEnv env, int objRef, int ownerRef, float val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, FloatFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setFloatField(fi, val);
      }
    }
  }

  @MJI
  public void setDouble__Ljava_lang_Object_2D__V (MJIEnv env, int objRef, int ownerRef, double val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      if (checkFieldType(env, fi, DoubleFieldInfo.class)){
        ei = checkSharedFieldAccess(ti, ei, fi);
        if (ti.getVM().hasNextChoiceGenerator()) {
          env.repeatInvocation();
          return;
        }
        ei.setDoubleField(fi, val);
      }
    }
  }

  @MJI
  public int get__Ljava_lang_Object_2__Ljava_lang_Object_2 (MJIEnv env, int objRef, int ownerRef) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);
    
    ElementInfo ei = getCheckedElementInfo( env, fi, objRef, ownerRef, false); // no type check here
    if (ei == null){
      // just return, NPE already thrown by getCheckedElementInfo()
      return 0;
    }
     
    ei = checkSharedFieldAccess(ti, ei, fi);
    if (ti.getVM().hasNextChoiceGenerator()) {
      env.repeatInvocation();
      return 0;
    }
    
    if (!(fi instanceof ReferenceFieldInfo)) { // primitive type, we need to box it
      if (fi instanceof DoubleFieldInfo){
        double d = ei.getDoubleField(fi);
        return env.newDouble(d);
      } else if (fi instanceof FloatFieldInfo){
        float f = ei.getFloatField(fi);
        return env.newFloat(f);
      } else if (fi instanceof LongFieldInfo){
        long l = ei.getLongField(fi);
        return env.newLong(l);
      } else if (fi instanceof IntegerFieldInfo){
        // this might actually represent a plethora of types
        int i = ei.getIntField(fi);
        return env.newInteger(i);
      } else if (fi instanceof BooleanFieldInfo){
        boolean b = ei.getBooleanField(fi);
        return env.newBoolean(b);
      } else if (fi instanceof ByteFieldInfo){
        byte z = ei.getByteField(fi);
        return env.newByte(z);
      } else if (fi instanceof CharFieldInfo){
        char c = ei.getCharField(fi);
        return env.newCharacter(c);
      } else if (fi instanceof ShortFieldInfo){
        short s = ei.getShortField(fi);
        return env.newShort(s);
      }
      
    } else { // it's a reference
      int ref = ei.getReferenceField(fi); // we internally store it as int
      return ref;
    }
    
    env.throwException("java.lang.IllegalArgumentException", "unknown field type");
    return MJIEnv.NULL;
  }

  @MJI
  public int getDeclaringClass____Ljava_lang_Class_2 (MJIEnv env, int objref){
    FieldInfo fi = getFieldInfo(env, objref);
    ClassInfo ci = fi.getClassInfo();
    return ci.getClassObjectRef();
  }

  @MJI
  public boolean isSynthetic____Z (MJIEnv env, int objref){
    FieldInfo fi = getFieldInfo(env, objref);
    String fn = fi.getName();
    return (fn.startsWith("this$") || fn.startsWith("val$"));
  }

  @MJI
  public int getName____Ljava_lang_String_2 (MJIEnv env, int objRef) {
    FieldInfo fi = getFieldInfo(env, objRef);
    
    int nameRef = env.getReferenceField( objRef, "name");
    if (nameRef == MJIEnv.NULL) {
      nameRef = env.newString(fi.getName());
      env.setReferenceField(objRef, "name", nameRef);
    }
   
    return nameRef;
  }

  static FieldInfo getFieldInfo (MJIEnv env, int objRef) {
    int fidx = env.getIntField( objRef, "regIdx");
    assert ((fidx >= 0) || (fidx < nRegistered)) : "illegal FieldInfo request: " + fidx + ", " + nRegistered;
    
    return registered[fidx];
  }
  
  static boolean isAvailable (MJIEnv env, FieldInfo fi, int ownerRef){
    if (fi.isStatic()){
      ClassInfo fci = fi.getClassInfo();
      if (fci.initializeClass(env.getThreadInfo())){
        env.repeatInvocation();
        return false;
      }
      
    } else {
      if (ownerRef == MJIEnv.NULL){
        env.throwException("java.lang.NullPointerException");
        return false;        
      }
      // class had obviously been initialized, otherwise we won't have an instance of it
    }

    return true;
  }
  
  
  /**
   * Peer method for the <code>java.lang.reflect.Field.set</code> method.
   * 
   * <2do> refactor to make boxed type handling more efficient
   */
  @MJI
  public void set__Ljava_lang_Object_2Ljava_lang_Object_2__V (MJIEnv env, int objRef, int ownerRef, int val) {
    ThreadInfo ti = env.getThreadInfo();
    FieldInfo fi = getFieldInfo(env, objRef);

    if (!isAvailable(env, fi, ownerRef)){
      return;
    }
        
    ClassInfo ci = fi.getClassInfo();
    ClassInfo cio = env.getClassInfo(ownerRef);

    if (!fi.isStatic() && !cio.isInstanceOf(ci)) {
      env.throwException("java.lang.IllegalArgumentException", 
                         fi.getType() + "field " + fi.getName() + " does not belong to this object");
      return;
    }
    
    Object[] attrs = env.getArgAttributes();
    Object attr = (attrs==null)? null: attrs[2];
    
    String type = getValueType(env, val);
    
    if (!isAssignmentCompatible(env, fi, val)){
      env.throwException("java.lang.IllegalArgumentException", 
                         "field of type " + fi.getType() + " not assignment compatible with " + type + " object");      
    }
    
    ElementInfo ei = getCheckedElementInfo(env, fi, objRef, ownerRef, true);
    if (ei != null){
      // <2do> what about exposure?
      ei = checkSharedFieldAccess(ti, ei, fi);
      if (ti.getVM().hasNextChoiceGenerator()) {
        env.repeatInvocation();
        return;
      }

      if (!setValue(env, fi, ownerRef, val, attr)) {
        env.throwException("java.lang.IllegalArgumentException",
                "Can not set " + fi.getType() + " field " + fi.getFullName() + " to " + ((MJIEnv.NULL != val) ? env.getClassInfo(val).getName() + " object " : "null"));
      }
    }
  }

  protected String getValueType (MJIEnv env, int ref){
    if (ref != MJIEnv.NULL){
      ElementInfo eiVal = env.getElementInfo(ref);
      return eiVal.getType();
    } else {
      return null;
    }
  }
  
  protected boolean isAssignmentCompatible (MJIEnv env, FieldInfo fi, int refVal){
    if (refVal == MJIEnv.NULL){
      return true;
      
    } else {
      ElementInfo eiVal = env.getElementInfo(refVal);
      ClassInfo ciVal = eiVal.getClassInfo();
      String valClsName = ciVal.getName();
      
      if (fi.isBooleanField() && valClsName.equals("java.lang.Boolean")) return true;
      else if (fi.isByteField() && valClsName.equals("java.lang.Byte")) return true;
      else if (fi.isCharField() && valClsName.equals("java.lang.Char")) return true;
      else if (fi.isShortField() && valClsName.equals("java.lang.Short")) return true;
      else if (fi.isIntField() && valClsName.equals("java.lang.Integer")) return true;
      else if (fi.isLongField() && valClsName.equals("java.lang.Long")) return true;
      else if (fi.isFloatField() && valClsName.equals("java.lang.Float")) return true;
      else if (fi.isDoubleField() && valClsName.equals("java.lang.Double")) return true;
      else {
        return ciVal.isInstanceOf(fi.getTypeClassInfo());
      }
    }
  }
  
  protected static boolean setValue(MJIEnv env, FieldInfo fi, int obj, int value, Object attr) {
    ClassInfo fieldClassInfo = fi.getClassInfo();
    String className = fieldClassInfo.getName();
    String fieldType = fi.getType();
    ClassInfo tci = fi.getTypeClassInfo();
    
    ElementInfo ei = null;
    if (fi.isStatic()) {
      ei = fi.getClassInfo().getModifiableStaticElementInfo();
    } else {
      ei = env.getModifiableElementInfo(obj);
    }

    if (tci.isPrimitive()) {
      if (value == MJIEnv.NULL) {
        return false;
      }

      // checks whether unboxing can be done by accessing the field "value"
      final String fieldName = "value";
      FieldInfo finfo = env.getElementInfo(value).getFieldInfo(fieldName);
      if (finfo == null) {
        return false;
      }
      
      ei.setFieldAttr(fi, attr);

      if ("boolean".equals(fieldType)){
        boolean val = env.getBooleanField(value, fieldName);
        ei.setBooleanField(fi, val);
        return true;
      } else if ("byte".equals(fieldType)){
        byte val = env.getByteField(value, fieldName);
        ei.setByteField(fi, val);
        return true;
      } else if ("char".equals(fieldType)){
        char val = env.getCharField(value, fieldName);
        ei.setCharField(fi, val);
        return true;
      } else if ("short".equals(fieldType)){
        short val = env.getShortField(value, fieldName);
        ei.setShortField(fi, val);
        return true;
      } else if ("int".equals(fieldType)){
        int val = env.getIntField(value, fieldName);
        ei.setIntField(fi, val);
        return true;
      } else if ("long".equals(fieldType)){
        long val = env.getLongField(value, fieldName);
        ei.setLongField(fi, val);
        return true;
      } else if ("float".equals(fieldType)){
        float val = env.getFloatField(value, fieldName);
        ei.setFloatField(fi, val);
        return true;
      } else if ("double".equals(fieldType)){
        double val = env.getDoubleField(value, fieldName);
        ei.setDoubleField(fi, val);
        return true;
      } else {
        return false;
      }

    } else { // it's a reference
      if (value != MJIEnv.NULL) {
        ClassInfo ciValue = env.getClassInfo(value);
        if (!ciValue.isInstanceOf(tci)) {
          return false;
        }
      }

      ei.setFieldAttr(fi, attr);

      if (fi.isStatic()) {
        env.setStaticReferenceField(className, fi.getName(), value);
      } else {
        env.setReferenceField(obj, fi.getName(), value);
      }
      return true;
    }
  }

  @MJI
  public boolean equals__Ljava_lang_Object_2__Z (MJIEnv env, int objRef, int ownerRef){
    int fidx = env.getIntField(ownerRef, "regIdx");
    if (fidx >= 0 && fidx < nRegistered){
      FieldInfo fi1 = getFieldInfo(env, objRef);
      FieldInfo fi2 = getFieldInfo(env, ownerRef);
      return ((fi1.getClassInfo() == fi2.getClassInfo()) && fi1.getName().equals(fi2.getName()) && fi1.getType().equals(fi2.getType()));
    }
    return false;
  }

  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objRef){
    StringBuilder sb = new StringBuilder();
    FieldInfo fi = getFieldInfo(env, objRef);
    sb.append(Modifier.toString(fi.getModifiers()));
    sb.append(' ');
    sb.append(fi.getType());
    sb.append(' ');
    sb.append(fi.getFullName());
    int sref = env.newString(sb.toString());
    return sref;
  }

  @MJI
  public int hashCode____I (MJIEnv env, int objRef){
    FieldInfo fi = getFieldInfo(env, objRef);
    return fi.getClassInfo().getName().hashCode() ^ fi.getName().hashCode();
  }

  @MJI
  public int getDeclaredAnnotations_____3Ljava_lang_annotation_Annotation_2 (MJIEnv env, int objRef){
    return getAnnotations_____3Ljava_lang_annotation_Annotation_2(env, objRef);
  }
}
