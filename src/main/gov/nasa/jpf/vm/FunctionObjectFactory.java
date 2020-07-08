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
import java.util.Arrays;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class FunctionObjectFactory {
  /**
   * ADD DOCUMENTATION!!
   * @param className
   * @return
   */
  private static Class<?> parseType(String className) {
    switch (className) {
      case "byte":
      case "java.lang.Byte":
        return byte.class;
      case "short":
      case "java.lang.Short":
        return short.class;
      case "int":
      case "java.lang.Integer":
        return int.class;
      case "long":
      case "java.lang.Long":
        return long.class;
      case "float":
      case "java.lang.Float":
        return float.class;
      case "double":
      case "java.lang.Double":
        return double.class;
      case "char":
      case "java.lang.Character":
        return char.class;
      case "boolean":
      case "java.lang.Boolean":
        return boolean.class;
      case "String":
        return String.class;
      default:
        try {
          return Class.forName(className);
        } catch (ClassNotFoundException e) {
          e.printStackTrace();
        }
    }
    return null;
  }

  /**
   * ADD DOCS!!!
   * @param typeNames
   * @return
   */
  private static Class<?>[] getPTypes (String[] typeNames) {
    Class<?>[] pTypes = new Class<?>[typeNames.length];
    for (int i = 0; i < typeNames.length; i++) {
      pTypes[i] = parseType(typeNames[i]);
    }
    return pTypes;
  }

  /**
   * ADD DOCUMENTATION
   * @param env
   * @param ei
   * @return
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
        return env.getStringObject(objRef);
    }
  }
  
  public int getFunctionObject(int bsIdx, ThreadInfo ti, ClassInfo fiClassInfo, String samUniqueName, BootstrapMethodInfo bmi,
                                         String[] freeVariableTypeNames, Object[] freeVariableValues) {
    
    ClassLoaderInfo cli = bmi.enclosingClass.getClassLoaderInfo();
    ClassInfo funcObjType = cli.getResolvedFuncObjType(bsIdx, fiClassInfo, samUniqueName, bmi, freeVariableTypeNames);
    
    funcObjType.registerClass(ti);

    ElementInfo ei;
    Heap heap = ti.getHeap();

    if(bmi.getBmType() == BootstrapMethodInfo.BMType.STRING_CONCATENATION){
      String concatenatedString =  makeConcatWithStrings(ti, freeVariableTypeNames, freeVariableValues, bmi);
      // Creating a newString for Concatenated string (example, "Hello," + "World");
      ei = heap.newString(concatenatedString,ti);
      freeVariableValues[0] = ei; // setting freeVariableValues to ei of new String.
      freeVariableTypeNames[0] = "String";
    }else {
      ei = heap.newObject(funcObjType, ti); // In the case of Lambda Expressions
    }


    if (bmi.getBmType() != BootstrapMethodInfo.BMType.STRING_CONCATENATION) {
      setFuncObjFields(ei, bmi, freeVariableTypeNames, freeVariableValues);
    }

    return ei.getObjectRef();
  }

  public String makeConcatWithStrings(ThreadInfo ti, String[] freeVariableTypeNames, Object[] freeVariableValues, BootstrapMethodInfo bmi ){
    MJIEnv env = new MJIEnv(ti);
    System.err.println(Arrays.toString(freeVariableTypeNames));
    Class<?>[] pTypes = getPTypes(freeVariableTypeNames);
    //Convert ElementInfo types to JVM types.
    Object[] convFreeVarVals = new Object[freeVariableValues.length];
    for (int i = 0; i < freeVariableValues.length; i++) {
      if (freeVariableValues[i] instanceof ElementInfo) {
        convFreeVarVals[i] = derefElementInfo(env, (ElementInfo) freeVariableValues[i]);
      } else {
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
//    MJIEnv env = new MJIEnv(ti);
//    String concatenatedString = new String();
//    String bmArg = bmi.getBmArg();
//    System.err.println("Concatenated string: " + concatenatedString);
//    System.err.println("Bootstrap Method argument: " + bmArg);
//    int markerPos = -1;
//    int val;
//    int markerCharCount = 0;
//    String markerCharacterValue = new String();
//    while (( markerPos = bmArg.indexOf(Character.toString((char)1) )) != -1 ||
//            ( markerPos = bmArg.indexOf(Character.toString((char)2) )) != -1) {
//      //val = ((ElementInfo)freeVariableValues[markerCharCount]).getObjectRef();
//
//      try {
//        val = ((ElementInfo)freeVariableValues[markerCharCount]).getObjectRef();
//        markerCharacterValue = env.getStringObject(val);
//      }catch (Exception notStringException){
//        try{
//          val = ((ElementInfo)freeVariableValues[markerCharCount]).getObjectRef();
//          markerCharacterValue = Byte.toString((env.getByteObject(val)));
//        }catch (Exception notByteException){
//          markerCharacterValue = (freeVariableValues[markerCharCount]).toString();
//        }
//      }
//
//      concatenatedString = concatenatedString + bmArg.substring(0, markerPos) + markerCharacterValue;
//      System.err.println("Concatenated string: " + concatenatedString);
//      bmArg = bmArg.substring(markerPos+1);
//      System.err.println("Bootstrap Method argument: " + bmArg);
//      markerCharCount++;
//    }
//    concatenatedString = concatenatedString + bmArg;
//    System.err.println("Concatenated string: " + concatenatedString);
//    System.err.println();
//    return concatenatedString;
  }

  public void setFuncObjFields(ElementInfo funcObj, BootstrapMethodInfo bmi, String[] freeVarTypeNames, Object[] freeVarValues) {
    Fields fields = funcObj.getFields();
    
    for(int i = 0; i<freeVarTypeNames.length; i++) {
      String typeName = freeVarTypeNames[i];
      if (typeName.equals("byte")) {
        fields.setByteValue(i, (Byte)freeVarValues[i]);
      } else if (typeName.equals("char")) {
        fields.setCharValue(i, (Character)freeVarValues[i]);
      } else if (typeName.equals("short")) {
        fields.setShortValue(i, (Short)freeVarValues[i]);
      } else if (typeName.equals("int")) {
        fields.setIntValue(i, (Integer)freeVarValues[i]);
      } else if (typeName.equals("float")) {
        fields.setFloatValue(i, (Float)freeVarValues[i]);
      } else if (typeName.equals("long")) {
        fields.setLongValue(i, (Long)freeVarValues[i]);
      } else if (typeName.equals("double")) {
        fields.setDoubleValue(i, (Double)freeVarValues[i]);
      } else if (typeName.equals("boolean")) {
        fields.setBooleanValue(i, (Boolean)freeVarValues[i]);
      } else {
        if(freeVarValues[i] == null) {
          fields.setReferenceValue(i, MJIEnv.NULL); 
        } else {
          int val = ((ElementInfo)freeVarValues[i]).getObjectRef() + 1;
          // + 1 because when object is created ( i.e GenericHeap.createObject(...)) the value of objRef is initialized
          // to the NamedField value in ElementInfo. But the value needed here is the value of arrayField which
          // NamedField value +1. This is because both array and object fields are created in GenericHeap.newString().
          fields.setReferenceValue(i, val);
        }
      }
    }
  }
}