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
  
  public int getFunctionObject(int bsIdx, ThreadInfo ti, ClassInfo fiClassInfo, String samUniqueName,
                               BootstrapMethodInfo bmi, String[] freeVariableTypeNames, Object[] freeVariableValues) {
    
    ClassLoaderInfo cli = bmi.enclosingClass.getClassLoaderInfo();
    ClassInfo funcObjType = cli.getResolvedFuncObjType(bsIdx, fiClassInfo, samUniqueName, bmi, freeVariableTypeNames);
    
    funcObjType.registerClass(ti);
    ElementInfo ei;
    Heap heap = ti.getHeap();


    if(bmi.getBmType() == BootstrapMethodInfo.BMType.STRING_CONCATENATION){
      String concatenatedString =  makeConcatWithStrings(ti, freeVariableValues, bmi);
      // Creating a newString for Concatenated string (example, "Hello," + "World");
      ei = heap.newString(concatenatedString,ti);
      freeVariableValues[0] = ei; // setting freeVariableValues to ei of new String.
    }else {
      ei = heap.newObject(funcObjType, ti); // In the case of Lambda Expressions
    }

    setFuncObjFields(ei, bmi, freeVariableTypeNames, freeVariableValues);
    
    return ei.getObjectRef();
  }

  public String makeConcatWithStrings( ThreadInfo ti, Object[] freeVariableValues, BootstrapMethodInfo bmi ){
    MJIEnv env = new MJIEnv(ti);
    String[] markerCharStrings = new String[freeVariableValues.length];
    int markerCharCount = 0;
    /* Store the marker characters string value in makerCharStrings i.e,
       \u0001" are "\u0001 = how are you?.
       Here "how" and "you?" are represented by marker characters respectively.
     */
    for (int i=0; i<freeVariableValues.length;i++){
      int val = ((ElementInfo)freeVariableValues[i]).getObjectRef(); // ObjRef value of marker character
      markerCharStrings[i] = env.getStringObject(val); // the string equal to the marker character
    }

    String bmArg = bmi.getBmArg();

    for( int pos = 0; pos < bmArg.length(); pos++){
      char ch = bmArg.charAt(pos);
      int markerCharacterVal = (int) ch;
      if( markerCharacterVal == 1 || markerCharacterVal == 2){
        // replace marker character with actual string.
        bmArg = bmArg.substring(0, pos)+ markerCharStrings[markerCharCount] + bmArg.substring(pos+1);
        markerCharCount++;
      }
    }
    return bmArg;
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