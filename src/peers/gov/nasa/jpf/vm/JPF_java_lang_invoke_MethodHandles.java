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

public class JPF_java_lang_invoke_MethodHandles extends NativePeer {

  @MJI
  public int privateLookupIn(MJIEnv env, int clsRef, int targetCls, int lookupRef) {
    return lookupRef;
  }

  public static final class Lookup extends NativePeer {
    @MJI
    public int
    findVarHandle__Ljava_lang_Class_2Ljava_lang_String_2Ljava_lang_Class_2__Ljava_lang_invoke_VarHandle_2(
        MJIEnv env, int objRef, int classRef, int stringRef, int typeRef) {
      ClassInfo ci = env.getClassInfo(classRef);
      int nameCls = env.getReferenceField(classRef, "name");
      String clsName = env.getStringObject(nameCls);
      ci = env.getSystemClassLoaderInfo().loadClass(clsName);
      int nameType = env.getReferenceField(typeRef, "name");
      String typeName = env.getStringObject(nameType);
      String varName = env.getStringObject(stringRef);
      FieldInfo fi = ci.getStaticField(varName);
      if (fi == null) {
        fi = ci.getInstanceField(varName);
      }
      if (fi == null) {
        throw new IllegalArgumentException("Cannot find field: " + varName + " on class: " + clsName);
      }
      int jpfFieldVarHandle = env.newObject("java.base$&$java.lang.invoke.JPFFieldVarHandle");
      env.setIntField(jpfFieldVarHandle, "fieldRef", fi.getFieldIndex());
      env.setIntField(jpfFieldVarHandle, "classRef", classRef);
      return jpfFieldVarHandle;
    }

    private Class resolveTypeName(String typeName) throws ClassNotFoundException {
      Class type;
      if (typeName.equals("int")) {
        type = int.class;
      } else if (typeName.equals("boolean")) {
        type = boolean.class;
      } else if (typeName.equals("short")) {
        type = short.class;
      } else if (typeName.equals("float")) {
        type = float.class;
      } else if (typeName.equals("double")) {
        type = double.class;
      } else if (typeName.equals("char")) {
        type = char.class;
      } else if (typeName.equals("long")) {
        type = long.class;
      } else {
        type = ClassLoader.getSystemClassLoader().loadClass(typeName);
      }
      return type;
    }

    // public java.lang.invoke.MethodHandle findStatic(java.lang.Class<?>, java.lang.String, java.lang.invoke.MethodType) throws java.lang.NoSuchMethodException, java.lang.IllegalAccessException;
    @MJI
    public int findStatic__Ljava_lang_Class_2Ljava_lang_String_2Ljava_lang_invoke_MethodType_2__Ljava_lang_invoke_MethodHandle_2(
        MJIEnv env, int objRef, int classRef, int nameRef, int methodTypeRef) 
        throws NoSuchMethodException, IllegalAccessException {
      String targetClsName = env.getStringObject(env.getReferenceField(classRef, "name")); // This part is tricky, don't use ci.getName(), it will return "Class.class"
      String methodName = env.getStringObject(nameRef);
      ClassInfo targetCls = env.getSystemClassLoaderInfo().loadClass(targetClsName);
      if (targetCls == null) {
        throw new NoSuchMethodException("Cannot find class: " + targetClsName);
      }

      for(String method : targetCls.methods.keySet()) {
        System.out.println(method);
      }
      // TODO: add MethodType to the method name
      MethodInfo mi = targetCls.getMethod(methodName + "(I)I", true);
      if (mi == null) {
        throw new NoSuchMethodException("Cannot find method: " + methodName + " on class: " + targetClsName);
      }

      // TODO: return the method handle
      return mi.getGlobalId();
    }
  }

}
