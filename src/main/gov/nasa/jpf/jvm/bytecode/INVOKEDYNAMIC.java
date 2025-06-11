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

import gov.nasa.jpf.vm.*;

import java.lang.invoke.CallSite;

import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.util.Arrays;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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

  private static final Map<String, CallSite> callSiteCache = new ConcurrentHashMap<>();
  // ==> debug
  private int countPlaceholders(String recipe) {
    int count = 0;
    for (char c : recipe.toCharArray()) {
      if (c == '\u0001') count++;
    }
    return count;
  }
  // ==> debug
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

  private boolean deepEquals(ThreadInfo ti, Object val1, Object val2, String sig) {
    // identity check and null handling
    if (val1 == val2) return true;
    if (val1 == null || val2 == null) return false;

    char typeChar = sig.charAt(0);

    // arrays
    if (typeChar == '[') {
      return compareArrays(ti, val1, val2, sig);
    }

    // primitive types
    if (isPrimitiveType(typeChar)) {
      return comparePrimitives(val1, val2, typeChar);
    }

    // reference types (objects)
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
      default: return false;
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
    // string comparison
    if ("Ljava/lang/String;".equals(sig)) return compareStrings(ti, (ElementInfo) val1, (ElementInfo) val2);

    // record comparison
    ElementInfo ei1 = (ElementInfo) val1;
    ElementInfo ei2 = (ElementInfo) val2;
    ClassInfo ci = ei1.getClassInfo();

    if (ci.isRecord()) return compareRecords(ti, ei1, ei2, ci);

    //object comparison -> default (reference equality)
    return ei1 == ei2;
  }

  private boolean compareArrays(ThreadInfo ti, Object val1, Object val2, String sig) {
    ElementInfo ei1 = ti.getHeap().get((Integer) val1);
    ElementInfo ei2 = ti.getHeap().get((Integer) val2);

    // Basic validation
    if (ei1 == null || ei2 == null || !ei1.isArray() || !ei2.isArray()) return false;
    if (ei1.arrayLength() != ei2.arrayLength()) return false;

    String componentSig = sig.substring(1);
    char compType = componentSig.charAt(0);

    // Compare each element
    for (int i = 0; i < ei1.arrayLength(); i++) {
      Object elem1 = getArrayElement(ei1, i, compType);
      Object elem2 = getArrayElement(ei2, i, compType);

      if (!deepEquals(ti, elem1, elem2, componentSig)) return false;
    }

    return true;
  }

  private boolean isPrimitiveType(char typeChar) {
    return (typeChar == 'Z' || typeChar == 'B' || typeChar == 'C'
            || typeChar == 'S' || typeChar == 'I' || typeChar == 'J'
            || typeChar == 'F' || typeChar == 'D');
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
      // here, we will omputes equality for a record by comparing all components with deepEquals.
      boolean equal = deepEquals(ti, thisVal, otherVal, fi.getSignature());
      //System.out.println("Comparing " + compName + ": thisVal=" + thisVal + ", otherVal=" + otherVal + ", Result=" + equal);
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
    // System.out.println("Computed hashCode: " + hash);
    return hash;
  }

  private int computeRecordToString(ThreadInfo ti, ClassInfo ci) {
    ElementInfo ei = ti.getThisElementInfo();
    String fullName = ci.getName();
    // we will extract the name from the fall name
    // NestedRecordTest$Rectangle -> will get Rectangle
    String simpleName = fullName.substring(Math.max(fullName.lastIndexOf('.'), fullName.lastIndexOf('$')) + 1);
    StringBuilder sb = new StringBuilder(simpleName).append("[");
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    // splitting bootstrap args -> left;size
    String[] components = bmi.getBmArg().split(";");
    for (int i = 0; i < components.length; i++) {
      String compName = components[i];
      Object value = ei.getFieldValueObject(compName);
      String valueStr;
      if (value instanceof ElementInfo && ((ElementInfo) value).getClassInfo().isRecord()) {
        //compute to string recursively for nested records
        ElementInfo valueEi = (ElementInfo) value;
        ClassInfo nestedCi = valueEi.getClassInfo();
        // Temporarily set 'this' to the nested record and compute its toString
        StackFrame currentFrame = ti.getModifiableTopFrame();
        int originalThis = currentFrame.getThis();
        currentFrame.setThis(valueEi.getObjectRef());
        int stringRef = computeRecordToString(ti, nestedCi);
        valueStr = ti.getHeap().get(stringRef).asString();
        // restore original 'this'
        currentFrame.setThis(originalThis);
      } else valueStr = String.valueOf(value); // for primitives or non-records

      sb.append(compName).append("=").append(valueStr);
      if (i < components.length - 1) sb.append(", ");
    }
    sb.append("]");
    return ti.getHeap().newString(sb.toString(), ti).getObjectRef();
  }

  private Object[] popArguments(StackFrame frame, Class<?>[] argTypes) {
    System.out.println("[DEBUG] Popping " + argTypes.length + " arguments from stack");

    Object[] args = new Object[argTypes.length];
    for (int i = argTypes.length - 1; i >= 0; i--) {
      Class<?> type = argTypes[i];

      System.out.println("[DEBUG] Popping arg " + i + " of type " + type.getSimpleName() + " (stack pos before pop: " + frame.getTopPos() + ")");

      if (type == double.class) {
        long bits = frame.popLong();
        args[i] = Double.longBitsToDouble(bits);
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (double)");
      } else if (type == long.class) {
        args[i] = frame.popLong();
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (long)");
      } else if (type == float.class) {
        int bits = frame.pop();
        args[i] = Float.intBitsToFloat(bits);
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (float)");
      } else if (type == int.class) {
        args[i] = frame.pop();
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (int)");
      } else if (type == char.class) {
        args[i] = (char) frame.pop();
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (char)");
      } else if (type == short.class) {
        args[i] = (short) frame.pop();
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (short)");
      } else if (type == byte.class) {
        args[i] = (byte) frame.pop();
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (byte)");
      } else if (type == boolean.class) {
        args[i] = frame.pop() != 0;
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (boolean)");
      }
      // Handle String
      else if (type == String.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
          System.out.println("[DEBUG] Arg " + i + ": null (String)");
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          if (ei != null && ei.isStringObject()) {
            args[i] = ei.asString();
            System.out.println("[DEBUG] Arg " + i + ": \"" + args[i] + "\" (String)");
          } else {
            args[i] = (ei != null) ? ei.toString() : null;
            System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (non-String reference)");
          }
        }
      }
      // boxed primitive types
      else if (type == Byte.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getByteField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Byte)");
      } else if (type == Integer.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getIntField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Integer)");
      } else if (type == Character.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getCharField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Character)");
      } else if (type == Double.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getDoubleField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Double)");
      } else if (type == Float.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getFloatField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Float)");
      } else if (type == Long.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getLongField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Long)");
      } else if (type == Short.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getShortField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Short)");
      } else if (type == Boolean.class) {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei.getBooleanField("value");
        }
        System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (Boolean)");
      }
      // other object types
      else {
        int ref = frame.pop();
        if (ref == MJIEnv.NULL) {
          args[i] = null;
          System.out.println("[DEBUG] Arg " + i + ": null (object)");
        } else {
          ElementInfo ei = ThreadInfo.getCurrentThread().getHeap().get(ref);
          args[i] = ei;
          System.out.println("[DEBUG] Arg " + i + ": " + args[i] + " (object)");
        }
      }
    }

    return args;
  }

  private CallSite createCallSite(ThreadInfo ti, ClassInfo enclosingClass, BootstrapMethodInfo bmi) throws Throwable {
    System.out.println("[DEBUG] ===== CREATING CALLSITE =====");
    System.out.println("[DEBUG] Bootstrap method type: " + bmi.getBmType());
    System.out.println("[DEBUG] Dynamic class: " + bmi.getDynamicClassName());
    System.out.println("[DEBUG] Dynamic method: " + bmi.getDynamicMethodName());

    // Prepare for CallSite generation
    bmi.prepareForCallSiteGeneration();
    System.out.println("[DEBUG] Bootstrap method prepared for CallSite generation");

    // Create lookup object
    MethodHandles.Lookup lookup = MethodHandles.publicLookup();
    System.out.println("[DEBUG] Created lookup object: " + lookup);

    // Create method type from descriptors
    MethodType methodType = bmi.createMethodType();
    System.out.println("[DEBUG] Created method type: " + methodType);

    // Generate CallSite using BootstrapMethodInfo
    CallSite callSite = bmi.generateCallSite(lookup, samMethodName, methodType);
    System.out.println("[DEBUG] Generated CallSite: " + callSite);
    System.out.println("[DEBUG] CallSite target: " + callSite.getTarget());
    System.out.println("[DEBUG] CallSite target type: " + callSite.getTarget().type());

    return callSite;
  }

  private String createCallSiteKey(ThreadInfo ti) {
    // unique key for the call site
    String key = getMethodInfo().getFullName() + ":" + getPosition() + ":" + bootstrapMethodIndex;
    System.out.println("[DEBUG] Generated CallSite key: " + key);
    return key;
  }

  private Instruction executeCallSite(ThreadInfo ti, CallSite callSite, BootstrapMethodInfo bmi) throws Throwable {
    System.out.println("[DEBUG] ===== EXECUTING CALLSITE =====");
    StackFrame frame = ti.getModifiableTopFrame();

    MethodHandle target = callSite.getTarget();
    MethodType type = target.type();

    System.out.println("[DEBUG] CallSite target type: " + type);
    System.out.println("[DEBUG] Parameter count: " + type.parameterCount());
    System.out.println("[DEBUG] Parameter types: " + Arrays.toString(type.parameterArray()));
    System.out.println("[DEBUG] Return type: " + type.returnType());

    switch (bmi.getBmType()) {
      case STRING_CONCATENATION:
        return executeStringConcatCallSite(ti, target, type, bmi);
      case LAMBDA_EXPRESSION:
      case SERIALIZABLE_LAMBDA_EXPRESSION:
        return executeLambdaCallSite(ti, target, type, bmi);
      case RECORDS:
        return executeRecordCallSite(ti, target, type, bmi);
      case DYNAMIC:
        return executeDynamicCallSite(ti, target, type, bmi);
      default:
        throw new UnsupportedOperationException("Unsupported CallSite type: " + bmi.getBmType());
    }
  }

  private Instruction executeRecordCallSite(ThreadInfo ti, MethodHandle target, MethodType type, BootstrapMethodInfo bmi) throws Throwable {
    System.out.println("[DEBUG] === RECORD CALLSITE EXECUTION ===");
    // TODO: work to be done here
    // delegate the existing record logic for now
    ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();
    return executeRecord(ti, enclosingClass, bmi);
  }

  private Instruction executeDynamicCallSite(ThreadInfo ti, MethodHandle target, MethodType type, BootstrapMethodInfo bmi) throws Throwable {
    System.out.println("[DEBUG] === DYNAMIC CALLSITE EXECUTION ===");
    // TODO: work to be done here
    // delegating the existing dynamic logic
    StackFrame frame = ti.getModifiableTopFrame();
    return executeDynamicBootstrap(ti, frame, bmi);
  }

  private Instruction executeLambdaCallSite(ThreadInfo ti, MethodHandle target, MethodType type, BootstrapMethodInfo bmi) throws Throwable {
    // TODO: work to be done here
    return null;
  }

  private Instruction executeStringConcatCallSite(ThreadInfo ti, MethodHandle target, MethodType type, BootstrapMethodInfo bmi) throws Throwable {
    System.out.println("[DEBUG] === STRING CONCAT CALLSITE EXECUTION START ===");
    StackFrame frame = ti.getModifiableTopFrame();

    String recipe = bmi.getBmArg();
    int placeholderCount = countPlaceholders(recipe);

    System.out.println("[DEBUG] Recipe has " + placeholderCount + " placeholders");
    System.out.println("[DEBUG] Recipe: " + JPFStringConcatHelper.escapeUnicode(recipe));

    // Parse argument types
    bmi.parseSamArgumentTypes();
    Class<?>[] argTypes = bmi.getArgumentTypes();

    System.out.println("[DEBUG] Parsed argument types: " + (argTypes != null ? Arrays.toString(argTypes) : "null"));

    // Pop arguments from stack - keep your exact logic
    Object[] args = popArguments(frame, argTypes);

    // Execute through MethodHandle
    System.out.println("[DEBUG] Invoking MethodHandle with args: " + Arrays.toString(args));
    Object result = target.invokeWithArguments(args);
    System.out.println("[DEBUG] MethodHandle returned: " + result);

    // Push result - keep your exact logic
    ElementInfo stringEI = ti.getHeap().newString((String) result, ti);
    frame.pushRef(stringEI.getObjectRef());

    System.out.println("[DEBUG] === STRING CONCAT CALLSITE SUCCESS ===");
    return getNext();
  }

  private Instruction executeDynamicBootstrap(ThreadInfo ti, StackFrame frame, BootstrapMethodInfo bmi) {
    return null;
  }

  private Instruction executeLambda(ThreadInfo ti, BootstrapMethodInfo bmi) {
    return null;
  }

  private Instruction executeRecord(ThreadInfo ti, ClassInfo enclosingClass, BootstrapMethodInfo bmi) {
    StackFrame frame = ti.getModifiableTopFrame();
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
      frame.pushRef(stringRef);
    } else {
      throw new IllegalStateException("Unsupported record method: " + methodName);
    }
    return getNext();
  }

  @Override
  public Instruction execute (ThreadInfo ti) {
    System.out.println("[DEBUG] ========== INVOKEDYNAMIC CALLSITE GENERATION START ==========");
    System.out.println("[DEBUG] INVOKEDYNAMIC PC before: " + ti.getPC());
    System.out.println("[DEBUG] INVOKEDYNAMIC instruction length: " + getLength());
    System.out.println("[DEBUG] Bootstrap method index: " + bootstrapMethodIndex);

    try {
      StackFrame frame = ti.getModifiableTopFrame();
      ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();
      BootstrapMethodInfo bmi = enclosingClass.getBootstrapMethodInfo(bootstrapMethodIndex);

      System.out.println("[DEBUG] Bootstrap method type: " + bmi.getBmType());
      System.out.println("[DEBUG] SAM method name: " + samMethodName);
      System.out.println("[DEBUG] Functional interface: " + functionalInterfaceName);

      String callSiteKey = createCallSiteKey(ti);
      System.out.println("[DEBUG] CallSite key: " + callSiteKey);

      // check if we have something in the cache
      CallSite callSite = callSiteCache.get(callSiteKey);
      if (callSite == null) {
        System.out.println("[DEBUG] CallSite not found in cache - creating new one");
        callSite = createCallSite(ti, enclosingClass, bmi);
        callSiteCache.put(callSiteKey, callSite);
        System.out.println("[DEBUG] CallSite created and cached: " + callSite);
      } else {
        System.out.println("[DEBUG] CallSite found in cache: " + callSite);
      }

      return executeCallSite(ti, callSite, bmi);

    } catch (Throwable e) {
      return ti.createAndThrowException("java.lang.BootstrapMethodError", "CallSite execution failed: " + e.getMessage());
    }
  }
}