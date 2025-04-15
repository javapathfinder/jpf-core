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
package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.vm.BootstrapMethodInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.FunctionObjectFactory;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LoadOnJPFRequired;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ByteArrayFields;
import gov.nasa.jpf.vm.FieldInfo;
import java.util.Arrays;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 *
 * Invoke dynamic method. It allows dynamic linkage between a call site and a method implementation.
 *
 * ..., [arg1, [arg2 ...]]  => ...
 */
public class INVOKEDYNAMIC extends Instruction {

  // index of a bootstrap method (index to the array bootstrapMethods[] declared in ClassInfo
  // containing this bytecode instruction)
  int bootstrapMethodIndex;

  // Free variables are those that are not defined within the lamabda body and
  // are captured from the lexical scope. Note that for instance lambda methods
  // the first captured variable always represents "this"
  String[] freeVariableTypeNames;
  byte[] freeVariableTypes;
  int freeVariableSize;

  String functionalInterfaceName;

  String samMethodName;

  int funcObjRef = MJIEnv.NULL;

  ElementInfo lastFuncObj = null;

  public INVOKEDYNAMIC () {}

  protected INVOKEDYNAMIC (int bmIndex, String methodName, String descriptor){
    bootstrapMethodIndex = bmIndex;
    samMethodName = methodName;
    freeVariableTypeNames = Types.getArgumentTypeNames(descriptor);
    freeVariableTypes = Types.getArgumentTypes(descriptor);
    functionalInterfaceName = Types.getReturnTypeSignature(descriptor);
    freeVariableSize = Types.getArgumentsSize(descriptor);
  }

  @Override
  public int getByteCode () {
    return 0xBA;
  }

  @Override
  public String toString() {
    StringBuilder args = new StringBuilder();
    if (freeVariableTypeNames != null) {
      for (int i = 0; i < freeVariableTypeNames.length; i++) {
        if (i > 0) {
          args.append(",");
        }
        args.append(freeVariableTypeNames[i]);
      }
    }
    return "invokedynamic " + bootstrapMethodIndex + " " +
            samMethodName + '(' + args + "):" + functionalInterfaceName;
  }

  /**
   * For now, INVOKEDYNAMIC works only in the context of lambda expressions.
   * Executing this returns an object that implements the functional interface
   * and contains a method which captures the behavior of the lambda expression.
   */

  private boolean deepEquals(ThreadInfo ti, Object val1, Object val2, String sig) {
    if (val1 == val2) return true;
    if (val1 == null || val2 == null) return false;

    char typeChar = sig.charAt(0);

    if (typeChar == '[') {
      return compareArrays(ti, val1, val2, sig);
    }

    if (isPrimitiveType(typeChar)) {
      return comparePrimitives(val1, val2, typeChar);
    }

    if (typeChar == 'L') {
      return compareReferenceTypes(ti, val1, val2, sig);
    }

    return false;
  }

  private boolean comparePrimitives(Object val1, Object val2, char typeChar) {
    switch (typeChar) {
      case 'Z': return ((Boolean) val1).booleanValue() == ((Boolean) val2).booleanValue();
      case 'B': return ((Byte) val1).byteValue() == ((Byte) val2).byteValue();
      case 'C': return ((Character) val1).charValue() == ((Character) val2).charValue();
      case 'S': return ((Short) val1).shortValue() == ((Short) val2).shortValue();
      case 'I': return ((Integer) val1).intValue() == ((Integer) val2).intValue();
      case 'J': return ((Long) val1).longValue() == ((Long) val2).longValue();
      case 'F': return ((Float) val1).floatValue() == ((Float) val2).floatValue();
      case 'D': return ((Double) val1).doubleValue() == ((Double) val2).doubleValue();
      default:  return false;
    }
  }

  private Object getArrayElement(ElementInfo ei, int index, char type) {
    switch (type) {
      case 'Z': return ei.getBooleanElement(index);
      case 'B': return ei.getByteElement(index);
      case 'C': return ei.getCharElement(index);
      case 'S': return ei.getShortElement(index);
      case 'I': return ei.getIntElement(index);
      case 'J': return ei.getLongElement(index);
      case 'F': return ei.getFloatElement(index);
      case 'D': return ei.getDoubleElement(index);
      default:  return ei.getReferenceElement(index);
    }
  }

  private boolean compareReferenceTypes(ThreadInfo ti, Object val1, Object val2, String sig) {
    if ("Ljava/lang/String;".equals(sig)) return compareStrings(ti, (ElementInfo) val1, (ElementInfo) val2);

    ElementInfo ei1 = (ElementInfo) val1;
    ElementInfo ei2 = (ElementInfo) val2;
    ClassInfo ci = ei1.getClassInfo();

    if (ci.isRecord()) return compareRecords(ti, ei1, ei2, ci);

    return ei1 == ei2;
  }

  private boolean compareArrays(ThreadInfo ti, Object val1, Object val2, String sig) {
    ElementInfo ei1 = ti.getHeap().get((Integer) val1);
    ElementInfo ei2 = ti.getHeap().get((Integer) val2);

    if (ei1 == null || ei2 == null || !ei1.isArray() || !ei2.isArray()) return false;
    if (ei1.arrayLength() != ei2.arrayLength()) return false;

    String componentSig = sig.substring(1);
    char compType = componentSig.charAt(0);

    for (int i = 0; i < ei1.arrayLength(); i++) {
      Object elem1 = getArrayElement(ei1, i, compType);
      Object elem2 = getArrayElement(ei2, i, compType);

      if (!deepEquals(ti, elem1, elem2, componentSig)) return false;
    }

    return true;
  }

  private boolean isPrimitiveType(char typeChar) {
    return "ZBCSIJFD".indexOf(typeChar) >= 0;
  }

  private boolean compareStrings(ThreadInfo ti, ElementInfo ei1, ElementInfo ei2) {
    byte coder1 = ei1.getByteField("coder");
    byte coder2 = ei2.getByteField("coder");

    byte[] bytes1 = ((ByteArrayFields) ti.getHeap().get(ei1.getReferenceField("value")).getFields()).asByteArray();
    byte[] bytes2 = ((ByteArrayFields) ti.getHeap().get(ei2.getReferenceField("value")).getFields()).asByteArray();

    return coder1 == coder2 && Arrays.equals(bytes1, bytes2);
  }

  private boolean compareRecords(ThreadInfo ti, ElementInfo ei1, ElementInfo ei2, ClassInfo ci) {
    if (!ci.equals(ei2.getClassInfo())) return false;

    FieldInfo[] fields = ci.getDeclaredInstanceFields();
    for (FieldInfo fi : fields) {
      Object v1 = ei1.getFieldValueObject(fi.getName());
      Object v2 = ei2.getFieldValueObject(fi.getName());

      if (!deepEquals(ti, v1, v2, fi.getSignature())) return false;
    }

    return true;
  }

  private boolean computeRecordEquals(ThreadInfo ti, ClassInfo ci, int otherRef) {
    ElementInfo thisEi = ti.getThisElementInfo();
    ElementInfo otherEi = ti.getHeap().get(otherRef);
    if (otherEi == null || !ci.equals(otherEi.getClassInfo())) {
      return false;
    }
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    String[] components = bmi.getBmArg().split(";");
    for (String compName : components) {
      FieldInfo fi = ci.getDeclaredInstanceField(compName);
      Object thisVal = thisEi.getFieldValueObject(compName);
      Object otherVal = otherEi.getFieldValueObject(compName);
      boolean equal = deepEquals(ti, thisVal, otherVal, fi.getSignature());
      if (!equal) return false;
    }
    return true;
  }

  private int computeRecordHashCode(ThreadInfo ti, ClassInfo ci) {
    ElementInfo ei = ti.getThisElementInfo();
    if (ei == null) {
      throw new IllegalStateException("No 'this' instance for hashCode computation");
    }
    int hash = 0;
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    String[] components = bmi.getBmArg().split(";");
    for (String compName : components) {
      Object value = ei.getFieldValueObject(compName);
      hash = 31 * hash + (value != null ? value.hashCode() : 0);
    }
    return hash;
  }

  private int computeRecordToString(ThreadInfo ti, ClassInfo ci) {
    ElementInfo ei = ti.getThisElementInfo();
    String fullName = ci.getName();
    String simpleName = fullName.substring(Math.max(fullName.lastIndexOf('.'), fullName.lastIndexOf('$')) + 1);
    StringBuilder sb = new StringBuilder(simpleName).append("[");
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    String[] components = bmi.getBmArg().split(";");
    for (int i = 0; i < components.length; i++) {
      String compName = components[i];
      Object value = ei.getFieldValueObject(compName);
      String valueStr;
      if (value instanceof ElementInfo && ((ElementInfo) value).getClassInfo().isRecord()) {
        ElementInfo valueEi = (ElementInfo) value;
        ClassInfo nestedCi = valueEi.getClassInfo();
        StackFrame currentFrame = ti.getModifiableTopFrame();
        int originalThis = currentFrame.getThis();
        currentFrame.setThis(valueEi.getObjectRef());
        int stringRef = computeRecordToString(ti, nestedCi);
        valueStr = ti.getHeap().get(stringRef).asString();
        currentFrame.setThis(originalThis);
      } else valueStr = String.valueOf(value);
      sb.append(compName).append("=").append(valueStr);
      if (i < components.length - 1) sb.append(", ");
    }
    sb.append("]");
    return ti.getHeap().newString(sb.toString(), ti).getObjectRef();
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();
    BootstrapMethodInfo bmi = enclosingClass.getBootstrapMethodInfo(bootstrapMethodIndex);

    String bootstrapMethodName = (bmi.getLambdaBody() != null) ? bmi.getLambdaBody().getName() : this.samMethodName;

    if (bmi.getBmType() == BootstrapMethodInfo.BMType.RECORDS) {
      String methodName = this.samMethodName;

      if ("equals".equals(methodName)) {
        int otherRef = frame.peek();
        frame.pop(2);
        boolean result = computeRecordEquals(ti, enclosingClass, otherRef);
        frame.push(result ? 1 : 0);
      } else if ("hashCode".equals(methodName)) {
        frame.pop(1);
        int hashCode = computeRecordHashCode(ti, enclosingClass);
        frame.push(hashCode);
      } else if ("toString".equals(methodName)) {
        frame.pop(1);
        int stringRef = computeRecordToString(ti, enclosingClass);
        frame.push(stringRef);
      } else {
        throw new IllegalStateException("Unsupported record method: " + methodName);
      }

      return getNext(ti);
    }

    ElementInfo ei = ti.getHeap().get(funcObjRef);
    if (ei == null || ei != lastFuncObj || freeVariableTypes.length > 0) {
      ClassInfo fiClassInfo;

      try {
        fiClassInfo = ti.resolveReferencedClass(functionalInterfaceName);
      } catch (LoadOnJPFRequired lre) {
        return ti.getPC();
      }

      if (fiClassInfo.initializeClass(ti)) {
        return ti.getPC();
      }

      VM vm = VM.getVM();
      FunctionObjectFactory funcObjFactory = vm.getFunctionObjectFacotry();

      Object[] freeVariableValues = frame.getArgumentsValues(ti, freeVariableTypes);

      funcObjRef = funcObjFactory.getFunctionObject(bootstrapMethodIndex, ti, fiClassInfo, samMethodName, bmi,
              freeVariableTypeNames, freeVariableValues);
      lastFuncObj = ti.getHeap().get(funcObjRef);
    }

    frame.pop(freeVariableSize);
    frame.pushRef(funcObjRef);

    if (funcObjRef == MJIEnv.NULL) {
      return ti.getPC();
    } else {
      return getNext(ti);
    }
  }
}