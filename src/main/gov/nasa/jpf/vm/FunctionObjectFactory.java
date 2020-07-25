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

import java.lang.invoke.*;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class FunctionObjectFactory {
  /**
   * Return JVM class objects from JPF class labels
   *
   * @param className name of the class
   * @return the class object associated with the input
   */
  private static Class<?> parseType(String className) {
    switch (className) {
      case "byte":
        return byte.class;
      case "short":
        return short.class;
      case "int":
        return int.class;
      case "long":
        return long.class;
      case "float":
        return float.class;
      case "double":
        return double.class;
      case "char":
        return char.class;
      case "boolean":
        return boolean.class;
      //This is not how this case should be handled, but there seems to be inconsistencies in how JPF treats this label
      default:
        return Object.class;
    }
  }

  /**
   * Map the function parseType across the input array typeNames and return the result
   *
   * @param typeNames array of class labels
   * @return array of class objects corresponding to the labels
   */
  private static Class<?>[] getPTypes(String[] typeNames) {
    Class<?>[] pTypes = new Class<?>[typeNames.length];
    for (int i = 0; i < typeNames.length; i++) {
      pTypes[i] = parseType(typeNames[i]);
    }
    return pTypes;
  }

  /**
   * "Dereference" the the JPF ElementInfo object in the MJIEnv environment as return the wrapped object to JVM
   *
   * @param env MJIEnv environment
   * @param ei  referenced ElementInfo object
   * @return JVM representation of the wrapped ElementInfo object
   */
  private static Object derefElementInfo(MJIEnv env, ElementInfo ei) {
    String name = ei.getClassInfo().getName();
    int objRef = ei.getObjectRef();
    switch (name) {
      case "java.lang.Byte":
        Byte byteObject = env.getByteObject(objRef);
        return byteObject.byteValue();
      case "java.lang.Short":
        Short shortObject = env.getShortObject(objRef);
        return shortObject.shortValue();
      case "java.lang.Integer":
        Integer integerObject = env.getIntegerObject(objRef);
        return integerObject.intValue();
      case "java.lang.Long":
        Long longObject = env.getLongObject(objRef);
        return longObject.longValue();
      case "java.lang.Float":
        Float floatObject = env.getFloatObject(objRef);
        return floatObject.floatValue();
      case "java.lang.Double":
        Double doubleObject = env.getDoubleObject(objRef);
        return doubleObject.doubleValue();
      case "java.lang.Character":
        Character charObject = env.getCharObject(objRef);
        return charObject.charValue();
      case "java.lang.Boolean":
        Boolean boolObject = env.getBooleanObject(objRef);
        return boolObject.booleanValue();
      default:
        //This block is structured in this unsatisfying way due to the limitations of JPF dereferencing
        //This is likely where all subsequent errors in string concatenation will reside
        try {
          return env.getStringObject(objRef);
        } catch (Exception e) {
          try {
            return ei.toString();
          } catch (Exception ee) {
            ee.printStackTrace();
            return null;
          }
        }
    }
  }

  public int getFunctionObject(int bsIdx, ThreadInfo ti, ClassInfo fiClassInfo, String samUniqueName, BootstrapMethodInfo bmi,
                               String[] freeVariableTypeNames, Object[] freeVariableValues) {

    ClassLoaderInfo cli = bmi.enclosingClass.getClassLoaderInfo();
    ClassInfo funcObjType = cli.getResolvedFuncObjType(bsIdx, fiClassInfo, samUniqueName, bmi, freeVariableTypeNames);

    funcObjType.registerClass(ti);

    ElementInfo ei;
    Heap heap = ti.getHeap();

    if (bmi.getBmType() == BootstrapMethodInfo.BMType.STRING_CONCATENATION) {
      String concatenatedString = makeConcatWithStrings(ti, freeVariableTypeNames, freeVariableValues, bmi);
      // Creating a newString for Concatenated string (example, "Hello," + "World");
      ei = heap.newString(concatenatedString, ti);
      freeVariableValues[0] = ei; // setting freeVariableValues to ei of new String.
      freeVariableTypeNames[0] = "String";
    } else {
      ei = heap.newObject(funcObjType, ti); // In the case of Lambda Expressions
    }

    //It does not make sense to call setFuncObjFields in the case of string concatenation since the String object
    //in the JPF heap has only three fields. This call will fail for any concatenation with more than three variables.
    if (bmi.getBmType() != BootstrapMethodInfo.BMType.STRING_CONCATENATION) {
      setFuncObjFields(ei, bmi, freeVariableTypeNames, freeVariableValues);
    }

    return ei.getObjectRef();
  }

  /**
   * Concatenate the given inputs in the JVM according to the recipe given by the BootstrapMethodInfo object using
   * the bootstrap method StringConcatFactory.makeConcatWithConstants and return the resulting string
   *
   * @param ti                    ThreadInfo object
   * @param freeVariableTypeNames string representation of the input variable types
   * @param freeVariableValues    representation of the input variable values
   * @param bmi                   JPF representation of bootstrap method parameters from the constant pool
   * @return concatenated string
   */
  public String makeConcatWithStrings(ThreadInfo ti, String[] freeVariableTypeNames, Object[] freeVariableValues, BootstrapMethodInfo bmi) {
    MJIEnv env = new MJIEnv(ti);
    Class<?>[] pTypes = getPTypes(freeVariableTypeNames);
    Object[] convFreeVarVals = new Object[freeVariableValues.length];
    for (int i = 0; i < freeVariableValues.length; i++) {
      if (freeVariableValues[i] instanceof ElementInfo) {
        //Dereference composite types
        convFreeVarVals[i] = derefElementInfo(env, (ElementInfo) freeVariableValues[i]);
      } else {
        //Copy primitive types
        convFreeVarVals[i] = freeVariableValues[i];
      }
    }
    MethodType concatType = MethodType.methodType(String.class, pTypes);
    String recipe = bmi.getBmArg();
    try {
      CallSite cs = StringConcatFactory.makeConcatWithConstants(MethodHandles.lookup(), "", concatType, recipe, new Object[0]);
      MethodHandle target = cs.getTarget();
      Object result = target.invokeWithArguments(convFreeVarVals);
      return (String) result;
    } catch (Throwable e) {
      e.printStackTrace();
      return null;
    }
  }

  public void setFuncObjFields(ElementInfo funcObj, BootstrapMethodInfo bmi, String[] freeVarTypeNames, Object[] freeVarValues) {
    Fields fields = funcObj.getFields();

    for (int i = 0; i < freeVarTypeNames.length; i++) {
      String typeName = freeVarTypeNames[i];
      if (typeName.equals("byte")) {
        fields.setByteValue(i, (Byte) freeVarValues[i]);
      } else if (typeName.equals("char")) {
        fields.setCharValue(i, (Character) freeVarValues[i]);
      } else if (typeName.equals("short")) {
        fields.setShortValue(i, (Short) freeVarValues[i]);
      } else if (typeName.equals("int")) {
        fields.setIntValue(i, (Integer) freeVarValues[i]);
      } else if (typeName.equals("float")) {
        fields.setFloatValue(i, (Float) freeVarValues[i]);
      } else if (typeName.equals("long")) {
        fields.setLongValue(i, (Long) freeVarValues[i]);
      } else if (typeName.equals("double")) {
        fields.setDoubleValue(i, (Double) freeVarValues[i]);
      } else if (typeName.equals("boolean")) {
        fields.setBooleanValue(i, (Boolean) freeVarValues[i]);
      } else {
        if (freeVarValues[i] == null) {
          fields.setReferenceValue(i, MJIEnv.NULL);
        } else {
          int val = ((ElementInfo) freeVarValues[i]).getObjectRef() + 1;
          // + 1 because when object is created ( i.e GenericHeap.createObject(...)) the value of objRef is initialized
          // to the NamedField value in ElementInfo. But the value needed here is the value of arrayField which
          // NamedField value +1. This is because both array and object fields are created in GenericHeap.newString().
          fields.setReferenceValue(i, val);
        }
      }
    }
  }
}