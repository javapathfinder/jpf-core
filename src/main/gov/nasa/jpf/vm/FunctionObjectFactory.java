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

/*
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class FunctionObjectFactory {
  
  public int getFunctionObject(int bsIdx, ThreadInfo ti, ClassInfo fiClassInfo, String samUniqueName, BootstrapMethodInfo bmi, 
                               String[] freeVariableTypeNames, Object[] freeVariableValues) {
    
    ClassLoaderInfo cli = bmi.enclosingClass.getClassLoaderInfo();
    ClassInfo funcObjType = cli.getResolvedFuncObjType(bsIdx, fiClassInfo, samUniqueName, bmi, freeVariableTypeNames);
    
    funcObjType.registerClass(ti);
    ElementInfo ei;
    Heap heap = ti.getHeap();
    MJIEnv env = new MJIEnv(ti);

    int val = ((ElementInfo)freeVariableValues[0]).getObjectRef(); // ObjRef value of String 2
    String str = env.getStringObject(val); // Second string ( for example, "world!")

    if(bmi.getBmType() == BootstrapMethodInfo.BMType.STRING_CONCATENATION_TYPE1){
      // Creating a newString for Concatenated string (example, "Hello," + "World");
      ei = heap.newString(bmi.getBmArg() + str,ti);
      freeVariableValues[0] = ei; // setting freeVariableValues to ei of new String.
    } else if (bmi.getBmType() == BootstrapMethodInfo.BMType.STRING_CONCATENATION_TYPE2) {
      ei = heap.newString(str + bmi.getBmArg() ,ti);
      freeVariableValues[0] = ei;
    } else {
      ei = heap.newObject(funcObjType, ti); // In the case of Lambda Expressions
    }

    setFuncObjFields(ei, bmi, freeVariableTypeNames, freeVariableValues);
    
    return ei.getObjectRef();
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