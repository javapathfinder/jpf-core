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

import java.util.Arrays;
import java.util.Objects;
import java.util.function.IntFunction;

import gov.nasa.jpf.annotation.MJI;

/**
 * native peer for Annotation Proxies
 * (saves us some bytecode interpretation shoe leather)
 */
public class JPF_gov_nasa_jpf_AnnotationProxyBase extends NativePeer {

  @MJI
  public int annotationType____Ljava_lang_Class_2 (MJIEnv env, int objref) {
    ClassInfo ciProxy = env.getClassInfo(objref);  // this would be the proxy
    
    // we could also pull it out from the interfaces, but we know the naming scheme
    String proxyName = ciProxy.getName();
    String annotation = proxyName.substring(0, proxyName.length() - 6); // "...$Proxy"
    ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo(annotation);
    
    return ci.getClassObjectRef();
  }
  
  @MJI
  public boolean equals__Ljava_lang_Object_2__Z(MJIEnv env, int objRef, int otherObj) {
    if(otherObj == MJIEnv.NULL) {
      return false;
    }
    ClassInfo thisProxy = env.getClassInfo(objRef);
    ClassInfo otherProxy = env.getClassInfo(otherObj);
    if(!thisProxy.equals(otherProxy)) {
      if(!otherProxy.getAllInterfaceClassInfos().containsAll(thisProxy.getAllInterfaces())) {
        return false;
      }
      // oof, someone implemented an annotation in user code. Delegate to their equals and let them deal with it
      MethodInfo mi = otherProxy.getMethod("equals(Ljava/lang/Object;)Z", true);
      assert mi != null;
      ThreadInfo ti = env.getThreadInfo();
      DirectCallStackFrame frame = ti.getReturnedDirectCall();
      if(frame == null) {
        frame = mi.createDirectCallStackFrame(ti, 0);
        frame.setReferenceArgument(0, otherObj, env.getObjectAttr(otherObj));
        frame.setReferenceArgument(1, objRef, env.getObjectAttr(objRef));
        ti.pushFrame(frame);
        return false;
      } else {
        Object attr = frame.getResultAttr();
        int ret = frame.getResult();
        env.setReturnAttribute(attr);
        return ret == 1;
      }
    }
    return annotationsEqual(env, objRef, otherObj);
  }
  
  public static boolean annotationsEqual(MJIEnv env, int aObj, int bObj) {
    if((aObj == MJIEnv.NULL) != (bObj == MJIEnv.NULL)) {
      return false;
    }
    ClassInfo thisProxy = env.getClassInfo(aObj);
    ClassInfo otherProxy = env.getClassInfo(bObj);
    if(!thisProxy.equals(otherProxy)) {
      return false;
    }
    FieldInfo[] fields = thisProxy.getDeclaredInstanceFields();
    ElementInfo aE = env.getElementInfo(aObj);
    ElementInfo bE = env.getElementInfo(bObj);
    for (int i=0; i<fields.length; i++){
      FieldInfo fi = fields[i];
      String fn = fi.getName();
      String ft = fi.getType();
      if(!fi.isReference()) {
        if(fi.is1SlotField()) {
          if(aE.get1SlotField(fi) != bE.get1SlotField(fi)) {
            return false;
          }
        } else {
          if(aE.get2SlotField(fi) != bE.get2SlotField(fi)) {
            return false;
          }
        }
      } else if(ft.equals("java.lang.Class")) {
        if(env.getReferenceField(aObj, fi) != env.getReferenceField(bObj, fi)) {
          return false;
        }
      } else if(ft.equals("java.lang.String")) {
        String aStr = env.getStringField(aObj, fn);
        String bStr = env.getStringField(bObj, fn);
        if(!Objects.equals(aStr, bStr)) {
          return false;
        }
      } else if(ft.endsWith("[]")) {
        String elemType = Types.getTypeName(Types.getArrayElementType(fi.getSignature()));
        int aArrayRef = env.getReferenceField(aObj, fi);
        int bArrayRef = env.getReferenceField(bObj, fi);
        if((aArrayRef == MJIEnv.NULL) != (bArrayRef == MJIEnv.NULL)) {
          return false;
        }
        ElementInfo aArrayContents = env.getElementInfo(aArrayRef);
        ElementInfo bArrayContents = env.getElementInfo(bArrayRef);
        assert aArrayContents.isArray() && bArrayContents.isArray();
        if(Types.isBasicType(elemType) || elemType.equals("java.lang.Class")) {
          Object rawArray1 = aArrayContents.getArrayFields().getValues();
          Object rawArray2 = bArrayContents.getArrayFields().getValues();
          if(!Objects.deepEquals(rawArray1, rawArray2)) {
            return false;
          }
        // string array
        } else if(elemType.equals("java.lang.String")) {
          if(!Arrays.equals(env.getStringArrayObject(aArrayRef), env.getStringArrayObject(bArrayRef))) {
            return false;
          }
        // either annotation or enum
        } else {
          int arrayLength1 = env.getArrayLength(aArrayRef);
          int arrayLength2 = env.getArrayLength(bArrayRef);
          if(arrayLength2 != arrayLength1) {
            return false;
          }
          for(int j = 0; j < arrayLength1; j++) {
            int elem1Ref = env.getReferenceArrayElement(aArrayRef, j);
            int elem2Ref = env.getReferenceArrayElement(bArrayRef, j);
            assert elem1Ref != MJIEnv.NULL && elem2Ref != MJIEnv.NULL;
            if(!referenceTypesEqual(env, elem1Ref, elem2Ref)) {
              return false;
            }
          }
        }
      } else {
        if(!referenceTypesEqual(env, env.getReferenceField(aObj, fi), env.getReferenceField(bObj, fi))) {
          return false;
        }
      }
    }
    return true;
  }
  
  private static boolean referenceTypesEqual(MJIEnv env, int elem1Ref, int elem2Ref) {
    ClassInfo aci = env.getClassInfo(elem1Ref);
    ClassInfo bci = env.getClassInfo(elem2Ref);
    assert aci != null && bci != null;
    if(!aci.equals(bci)) {
      return false;
    }
    if(aci.isEnum()) {
      return elem1Ref == elem2Ref;
    } else {
      return annotationsEqual(env, elem1Ref, elem2Ref);
    }
  }

  @MJI
  public int toString____Ljava_lang_String_2 (MJIEnv env, int objref){
    StringBuilder sb = new StringBuilder();
    annotationReferenceToString(env, objref, sb);
    return env.newString(sb.toString());
  }
  
  private int annotationHashCode(MJIEnv env, int objRef) {
    ClassInfo thisProxy = env.getClassInfo(objRef);
    FieldInfo[] fields = thisProxy.getDeclaredInstanceFields();
    int hashCode = 0;
    for (int i=0; i<fields.length; i++){
      FieldInfo fi = fields[i];
      int fieldHash = computeFieldHash(env, objRef, fi);
      hashCode += (127 * getStringHash(env, env.newString(fi.getName()))) ^ fieldHash;
    }
    return hashCode;
  }

  private int computeFieldHash(MJIEnv env, int objRef, FieldInfo fi) {
    String fn = fi.getName();
    String ft = fi.getType();
    if(!fi.isReference()) {
      switch(ft) {
      case "byte":
        return Byte.valueOf(env.getByteField(objRef, fn)).hashCode();
      case "boolean":
        return Boolean.valueOf(env.getBooleanField(objRef, fn)).hashCode();
      case "char":
        return Character.valueOf(env.getCharField(objRef, fn)).hashCode();
      case "short":
        return Short.valueOf(env.getShortField(objRef, fn)).hashCode();
      case "int":
        return Integer.valueOf(env.getIntField(objRef, fn)).hashCode();
      case "long":
        return Long.valueOf(env.getLongField(objRef, fn)).hashCode();
      case "float":
        return Float.valueOf(env.getFloatField(objRef, fn)).hashCode();
      case "double":
        return Double.valueOf(env.getDoubleField(objRef, fn)).hashCode();
      default:
        throw new UnsupportedOperationException();
      }
    } else if(ft.equals("java.lang.Class")) {
      return getObjectHash(env.getReferenceField(objRef, fi));
    } else if(ft.equals("java.lang.String")) {
      return getStringHash(env, env.getReferenceField(objRef, fi));
    } else if(ft.endsWith("[]")) {
      int aArrayRef = env.getReferenceField(objRef, fi);
      ElementInfo aArrayContents = env.getElementInfo(aArrayRef);
      String elemType = Types.getTypeName(Types.getArrayElementType(fi.getSignature()));
      if(Types.isBasicType(elemType)) {
        Object rawArray1 = aArrayContents.getArrayFields().getValues();
        if(rawArray1 instanceof boolean[]) {
          return Arrays.hashCode((boolean[])rawArray1);
        } else if(rawArray1 instanceof byte[]) {
          return Arrays.hashCode((byte[])rawArray1);
        } else if(rawArray1 instanceof char[]) {
          return Arrays.hashCode((char[])rawArray1);
        } else if(rawArray1 instanceof short[]) {
          return Arrays.hashCode((short[])rawArray1);
        } else if(rawArray1 instanceof int[]) {
          return Arrays.hashCode((int[])rawArray1);
        } else if(rawArray1 instanceof long[]) {
          return Arrays.hashCode((long[])rawArray1);
        } else if(rawArray1 instanceof float[]) {
          return Arrays.hashCode((float[])rawArray1);
        } else if(rawArray1 instanceof double[]) {
          return Arrays.hashCode((double[])rawArray1);
        } else {
          throw new RuntimeException();
        }
      } else if(elemType.equals("java.lang.Class")) {
        return computeObjectArrayHash(env, aArrayRef, this::getObjectHash);
      // string array
      } else if(elemType.equals("java.lang.String")) {
        return computeObjectArrayHash(env, aArrayRef, (ref) -> getStringHash(env, ref));
      } else {
        return computeObjectArrayHash(env, aArrayRef, (ref) -> hashReferenceValue(env, ref));
      }
    } else {
      return hashReferenceValue(env, env.getReferenceField(objRef, fi));
    }
  }
  
  /*
   * THIS WILL BREAK if the JDK uses a different hashcode from the one here
   */
  private int computeObjectArrayHash(MJIEnv env, int arrayRef, IntFunction<Integer> refHasher) {
    int arrayLength1 = env.getArrayLength(arrayRef);
    int hash = 1;
    for(int j = 0; j < arrayLength1; j++) {
      int elem1Ref = env.getReferenceArrayElement(arrayRef, j);
      hash = 31 * hash + refHasher.apply(elem1Ref);
    }
    return hash;
  }
  
  private int getStringHash(MJIEnv env, int strObjRef) {
    return JPF_java_lang_String.computeStringHashCode(env, strObjRef);
  }

  private int getObjectHash(int ref) {
    return ref ^ 0xABCD;
  }
  
  private int hashReferenceValue(MJIEnv env, int elem1Ref) {
    ClassInfo aci = env.getClassInfo(elem1Ref);
    if(aci.isEnum()) {
      return getObjectHash(elem1Ref);
    } else {
      return annotationHashCode(env, elem1Ref);
    }
  }

  @MJI
  public int hashCode____I(MJIEnv env, int objRef) {
    return annotationHashCode(env, objRef);
  }

  private void annotationReferenceToString(MJIEnv env, int objref, StringBuilder sb) {
    ClassInfo ci = env.getClassInfo(objref);
    String cname = ci.getName();
    int idx = cname.lastIndexOf('$');
    
    sb.append('@');
    sb.append(cname.substring(0,idx));
    
    FieldInfo[] fields = ci.getDeclaredInstanceFields();
    if (fields.length > 0){
      sb.append('(');
      for (int i=0; i<fields.length; i++){
        String fn = fields[i].getName();
        String ft = fields[i].getType();
        
        if (i>0){
          sb.append(',');
        }
        sb.append(fn);
        sb.append('=');
        
        if(ft.equals("int")) {
          sb.append(env.getIntField(objref,fn));

        } else if(ft.equals("byte")) {
          sb.append(env.getByteField(objref,fn));

        } else if(ft.equals("boolean")) {
          sb.append(env.getBooleanField(objref,fn));

        } else if(ft.equals("short")) {
          sb.append(env.getShortField(objref, fn));

        } else if(ft.equals("char")) {
          sb.append(env.getCharField(objref, fn));

        } else if(ft.equals("float")) {
          sb.append(env.getFloatField(objref, fn));

        } else if(ft.equals("long")) {
          sb.append(env.getLongField(objref, fn));

        } else if(ft.equals("double")) {
          sb.append(env.getDoubleField(objref,fn));

        } else if(ft.equals("java.lang.String")) {
          sb.append(env.getStringObject(env.getReferenceField(objref, fn)));

        } else if(ft.equals("java.lang.Class")) {
          int cref = env.getReferenceField(objref, fn);
          if (cref != MJIEnv.NULL){
            int nref = env.getReferenceField(cref, "name");
            String cn = env.getStringObject(nref);
          
            sb.append("class ");
            sb.append(cn);
          } else {
            sb.append("class ?");
          }

        } else if(ft.endsWith("[]")) {
          int ar = env.getReferenceField(objref, fn);
          int n = env.getArrayLength((ar));

          sb.append('[');

          if(ft.equals("int[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getIntArrayElement(ar,j));
            }

          } else if(ft.equals("byte[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getByteArrayElement(ar,j));
            }

          } else if(ft.equals("boolean[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getBooleanArrayElement(ar,j));
            }

          } else if(ft.equals("short[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getShortArrayElement(ar,j));
            }

          } else if(ft.equals("char[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getCharArrayElement(ar,j));
            }

          } else if(ft.equals("float[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getFloatArrayElement(ar,j));
            }

          } else if(ft.equals("long[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getLongArrayElement(ar,j));
            }

          } else if(ft.equals("double[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getDoubleArrayElement(ar,j));
            }

          } else if(ft.equals("java.lang.String[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');
              sb.append(env.getStringObject(env.getReferenceArrayElement(ar,j)));
            }

          } else if(ft.equals("java.lang.Class[]")) {
            for (int j=0; j<n; j++){
              if (j>0) sb.append(',');

              int cref = env.getReferenceArrayElement(ar,j);
              if (cref != MJIEnv.NULL){
                int nref = env.getReferenceField(cref, "name");
                String cn = env.getStringObject(nref);
              
                sb.append("class ");
                sb.append(cn);
              } else {
                sb.append("class ?");
              }

            }
          } else {
            for(int j=0; j < n; j++) {
              if (j>0) sb.append(',');

              int cref = env.getReferenceArrayElement(ar,j);
              if (cref != MJIEnv.NULL){
                referenceToString(env, sb, cref);
              } else {
                sb.append("null");
              }
            }
          }
          
          sb.append("]");
        } else {
          int eref = env.getReferenceField(objref, fn);
          if (eref != MJIEnv.NULL){
            referenceToString(env, sb, eref);
          }
        }
      }
      sb.append(')');
    }
  }

  private void referenceToString(MJIEnv env, StringBuilder sb, int eref) {
    ClassInfo eci = env.getClassInfo(eref);
    if (eci.isEnum()){
      int nref = env.getReferenceField(eref, "name");
      String en = env.getStringObject(nref);
      
      sb.append(eci.getName());
      sb.append('.');
      sb.append(en);
    } else {
      annotationReferenceToString(env, eref, sb);
    }
  }
}
