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

import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.BootstrapMethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ByteArrayFields;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.JPFStringConcatHelper;
import gov.nasa.jpf.vm.LoadOnJPFRequired;
import gov.nasa.jpf.vm.FunctionObjectFactory;
import gov.nasa.jpf.vm.VM;

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;

import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * @author Mahmoud Khawaja <mahmoud.khawaja97@gmail.com>
 * Invoke dynamic method. It allows dynamic linkage between a call site and a method implementation.
 */
public class INVOKEDYNAMIC extends Instruction {

  // ==================== FIELDS ====================

  int bootstrapMethodIndex;
  String[] freeVariableTypeNames;
  byte[] freeVariableTypes;
  int freeVariableSize;
  String functionalInterfaceName;
  String samMethodName;
  int funcObjRef = MJIEnv.NULL;
  ElementInfo lastFuncObj = null;

  private static final Map<String, CallSite> callSiteCache = new ConcurrentHashMap<>();

  // ==================== CONSTRUCTORS ====================
  public INVOKEDYNAMIC() {}

  protected INVOKEDYNAMIC(int bmIndex, String methodName, String descriptor) {
    bootstrapMethodIndex = bmIndex;
    samMethodName = methodName;
    freeVariableTypeNames = Types.getArgumentTypeNames(descriptor);
    freeVariableTypes = Types.getArgumentTypes(descriptor);
    functionalInterfaceName = Types.getReturnTypeSignature(descriptor);
    freeVariableSize = Types.getArgumentsSize(descriptor);
  }

  // ==================== BASIC INSTRUCTION METHODS ====================

  @Override
  public int getByteCode() {
    return 0xBA;
  }

  @Override
  public String toString() {
    StringBuilder args = new StringBuilder();
    if (freeVariableTypeNames != null) {
      for (int i = 0; i < freeVariableTypeNames.length; i++) {
        if (i > 0) args.append(",");
        args.append(freeVariableTypeNames[i]);
      }
    }
    return "invokedynamic " + bootstrapMethodIndex + " " +
            samMethodName + '(' + args + "):" + functionalInterfaceName;
  }

  // ==================== MAIN EXECUTION ====================

  @Override
  public Instruction execute(ThreadInfo ti) {

    try {
      ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();
      BootstrapMethodInfo bmi = enclosingClass.getBootstrapMethodInfo(bootstrapMethodIndex);

      // direct approach
      if (bmi.getBmType() == BootstrapMethodInfo.BMType.LAMBDA_EXPRESSION || bmi.getBmType() == BootstrapMethodInfo.BMType.SERIALIZABLE_LAMBDA_EXPRESSION) {
        return executeLambda(ti, null, null, bmi);
      }else if (bmi.getBmType() == BootstrapMethodInfo.BMType.RECORDS) {
        return executeRecord(ti, bmi);
      }

      String callSiteKey = createCallSiteKey(ti);
      CallSite callSite = getOrCreateCallSite(callSiteKey, ti, enclosingClass, bmi);
      return executeCallSite(ti, callSite, bmi);

    } catch (Throwable e) {
      return ti.createAndThrowException("java.lang.BootstrapMethodError",
              "CallSite execution failed: " + e.getMessage());
    }
  }

  // ==================== CALLSITE MANAGEMENT ====================

  private String createCallSiteKey(ThreadInfo ti) {
    return getMethodInfo().getFullName() + ":" + getPosition() + ":" + bootstrapMethodIndex;
  }

  private CallSite getOrCreateCallSite(String key, ThreadInfo ti, ClassInfo enclosingClass,
                                       BootstrapMethodInfo bmi) throws Throwable {
    CallSite callSite = callSiteCache.get(key);
    if (callSite == null) {
      callSite = createCallSite(ti, enclosingClass, bmi);
      callSiteCache.put(key, callSite);
    }
    return callSite;
  }

  private CallSite createCallSite(ThreadInfo ti, ClassInfo enclosingClass,
                                  BootstrapMethodInfo bmi) throws Throwable {
    bmi.prepareForCallSiteGeneration();
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();

    MethodType methodType = (bmi.getBmType() == BootstrapMethodInfo.BMType.STRING_CONCATENATION)
            ? createStringConcatMethodType(bmi)
            : bmi.createMethodType();

    return bmi.generateCallSite(lookup, samMethodName, methodType);
  }

  // ==================== CALLSITE EXECUTION ====================

  private Instruction executeCallSite(ThreadInfo ti, CallSite callSite,
                                      BootstrapMethodInfo bmi) throws Throwable {
    // call site generation works only in the context of string concat
    MethodHandle target = callSite.getTarget();
    MethodType type = target.type();
    return executeStringConcatenation(ti, target, type, bmi);
  }

  // ==================== STRING CONCATENATION EXECUTION ====================

  private Instruction executeStringConcatenation(ThreadInfo ti, MethodHandle target,
                                                 MethodType type, BootstrapMethodInfo bmi) throws Throwable {
    StackFrame frame = ti.getModifiableTopFrame();

    String recipe = bmi.getBmArg();

    // Pop arguments and execute
    Object[] args = popArguments(frame, type.parameterArray());
    String result = (String) target.invokeWithArguments(args);


    // Push result
    ElementInfo stringEI = ti.getHeap().newString(result, ti);
    frame.pushRef(stringEI.getObjectRef());

    return getNext();
  }

  private MethodType createStringConcatMethodType(BootstrapMethodInfo bmi) {

    List<Class<?>> paramTypes = new ArrayList<>();
    if (freeVariableTypeNames != null) {
      for (String typeName : freeVariableTypeNames) {
        Class<?> paramType = convertTypeNameToClass(typeName);
        paramTypes.add(paramType);
      }
    }

    return MethodType.methodType(String.class, paramTypes.toArray(new Class<?>[0]));
  }

  // ==================== LAMBDA HANDLING ====================

  private Instruction executeLambda(ThreadInfo ti, MethodHandle target, MethodType type, BootstrapMethodInfo bmi) {
    StackFrame frame = ti.getModifiableTopFrame();

    ElementInfo ei = ti.getHeap().get(funcObjRef);
    if(ei == null || ei != lastFuncObj || freeVariableTypes.length > 0) {
      ClassInfo fiClassInfo;

      // First, resolve the functional interface
      try {
        fiClassInfo = ti.resolveReferencedClass(functionalInterfaceName);
      } catch(LoadOnJPFRequired lre) {
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
      return getNext();
    }
  }

  // ==================== ARGUMENT HANDLING ====================

  private Object[] popArguments(StackFrame frame, Class<?>[] argTypes) {

    Object[] args = new Object[argTypes.length];
    for (int i = argTypes.length - 1; i >= 0; i--) {
      args[i] = popSingleArgument(frame, argTypes[i]);
    }
    return args;
  }

  private Object popSingleArgument(StackFrame frame, Class<?> type) {
    // Handle primitive types
    if (type.isPrimitive()) {
      return popPrimitiveArgument(frame, type);
    }

    // Handle reference types
    int ref = frame.pop();
    if (ref == MJIEnv.NULL) {
      return null;
    }

    ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
    if (ei == null) {
      return null;
    }

    if (ei.isStringObject()) {
      return ei.asString();
    }

    return convertBoxedPrimitive(ei, type);
  }

  private Object popPrimitiveArgument(StackFrame frame, Class<?> type) {
    if (type == double.class) {
      return Double.longBitsToDouble(frame.popLong());
    } else if (type == long.class) {
      return frame.popLong();
    } else if (type == float.class) {
      return Float.intBitsToFloat(frame.pop());
    } else if (type == int.class) {
      return frame.pop();
    } else if (type == char.class) {
      return (char) frame.pop();
    } else if (type == short.class) {
      return (short) frame.pop();
    } else if (type == byte.class) {
      return (byte) frame.pop();
    } else if (type == boolean.class) {
      return frame.pop() != 0;
    }
    throw new IllegalArgumentException("Unknown primitive type: " + type);
  }

  private Object convertBoxedPrimitive(ElementInfo ei, Class<?> expectedType) {
    String className = ei.getClassInfo().getName();

    switch (className) {
        case "java.lang.Byte":    return ei.getByteField("value");
      case "java.lang.Character": return ei.getCharField("value");
      case "java.lang.Short":   return ei.getShortField("value");
      case "java.lang.Integer": return ei.getIntField("value");
      case "java.lang.Long":    return ei.getLongField("value");
      case "java.lang.Float":   return Float.intBitsToFloat(ei.getIntField("value"));
      case "java.lang.Double":  return Double.longBitsToDouble(ei.getLongField("value"));
      case "java.lang.Boolean": return ei.getBooleanField("value");
      case "java.lang.String":  return ei.asString();
      default: return ei;
    }
  }

  // ==================== TYPE CONVERSION ====================

  private Class<?> convertTypeNameToClass(String typeName) {
    switch (typeName) {
      case "boolean": return boolean.class;
      case "byte":    return byte.class;
      case "char":    return char.class;
      case "short":   return short.class;
      case "int":     return int.class;
      case "long":    return long.class;
      case "float":   return float.class;
      case "double":  return double.class;
      case "java.lang.String":    return String.class;
      case "java.lang.Character": return Character.class;
      case "java.lang.Byte":      return Byte.class;
      case "java.lang.Integer":   return Integer.class;
      case "java.lang.Long":      return Long.class;
      case "java.lang.Float":     return Float.class;
      case "java.lang.Double":    return Double.class;
      case "java.lang.Boolean":   return Boolean.class;
      case "java.lang.Short":     return Short.class;
      default: return Object.class;
    }
  }

  // ==================== RECORD OPERATIONS ====================

  private Instruction executeRecord(ThreadInfo ti, BootstrapMethodInfo bmi) {
    StackFrame frame = ti.getModifiableTopFrame();
    ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();

    switch (samMethodName) {
      case "equals":
        return executeRecordEquals(ti, frame, enclosingClass, bmi);
      case "hashCode":
        return executeRecordHashCode(ti, frame, enclosingClass, bmi);
      case "toString":
        return executeRecordToString(ti, frame, enclosingClass, bmi);
      default:
        throw new IllegalStateException("Unsupported record method: " + samMethodName);
    }
  }

  private Instruction executeRecordEquals(ThreadInfo ti, StackFrame frame,
                                          ClassInfo enclosingClass, BootstrapMethodInfo bmi) {
    int otherRef = frame.peek();
    frame.pop(2);
    boolean result = computeRecordEquals(ti, enclosingClass, otherRef);
    frame.push(result ? 1 : 0);
    return getNext();
  }

  private Instruction executeRecordHashCode(ThreadInfo ti, StackFrame frame,
                                            ClassInfo enclosingClass, BootstrapMethodInfo bmi) {
    frame.pop(1);
    int hashCode = computeRecordHashCode(ti, enclosingClass);
    frame.push(hashCode);
    return getNext();
  }

  private Instruction executeRecordToString(ThreadInfo ti, StackFrame frame,
                                            ClassInfo enclosingClass, BootstrapMethodInfo bmi) {
    frame.pop(1);
    int stringRef = computeRecordToString(ti, enclosingClass);
    frame.pushRef(stringRef);
    return getNext();
  }

  // ==================== RECORD COMPUTATION METHODS ====================

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

      if (!deepEquals(ti, thisVal, otherVal, fi.getSignature())) {
        return false;
      }
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
    String simpleName = fullName.substring(Math.max(fullName.lastIndexOf('.'),
            fullName.lastIndexOf('$')) + 1);

    StringBuilder sb = new StringBuilder(simpleName).append("[");
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    String[] components = bmi.getBmArg().split(";");

    for (int i = 0; i < components.length; i++) {
      String compName = components[i];
      Object value = ei.getFieldValueObject(compName);
      String valueStr = formatRecordComponent(ti, value);

      sb.append(compName).append("=").append(valueStr);
      if (i < components.length - 1) sb.append(", ");
    }

    sb.append("]");
    return ti.getHeap().newString(sb.toString(), ti).getObjectRef();
  }

  private String formatRecordComponent(ThreadInfo ti, Object value) {
    if (value instanceof ElementInfo && ((ElementInfo) value).getClassInfo().isRecord()) {
      ElementInfo valueEi = (ElementInfo) value;
      ClassInfo nestedCi = valueEi.getClassInfo();

      StackFrame currentFrame = ti.getModifiableTopFrame();
      int originalThis = currentFrame.getThis();
      currentFrame.setThis(valueEi.getObjectRef());

      int stringRef = computeRecordToString(ti, nestedCi);
      String result = ti.getHeap().get(stringRef).asString();

      currentFrame.setThis(originalThis);
      return result;
    }
    return String.valueOf(value);
  }

  // ==================== DEEP EQUALS IMPLEMENTATION ====================

  private boolean deepEquals(ThreadInfo ti, Object val1, Object val2, String sig) {
    if (val1 == val2) return true;
    if (val1 == null || val2 == null) return false;

    char typeChar = sig.charAt(0);

    if (typeChar == '[') return compareArrays(ti, val1, val2, sig);
    if (isPrimitiveType(typeChar)) return comparePrimitives(val1, val2, typeChar);
    if (typeChar == 'L') return compareReferenceTypes(ti, val1, val2, sig);

    return false;
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
      default: return false;
    }
  }

  private boolean compareReferenceTypes(ThreadInfo ti, Object val1, Object val2, String sig) {
    if ("Ljava/lang/String;".equals(sig)) {
      return compareStrings(ti, (ElementInfo) val1, (ElementInfo) val2);
    }

    ElementInfo ei1 = (ElementInfo) val1;
    ElementInfo ei2 = (ElementInfo) val2;
    ClassInfo ci = ei1.getClassInfo();

    if (ci.isRecord()) {
      return compareRecords(ti, ei1, ei2, ci);
    }

    return ei1 == ei2;
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

  private boolean isPrimitiveType(char typeChar) {
    return (typeChar == 'Z' || typeChar == 'B' || typeChar == 'C'
            || typeChar == 'S' || typeChar == 'I' || typeChar == 'J'
            || typeChar == 'F' || typeChar == 'D');
  }
}
