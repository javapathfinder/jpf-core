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

import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.ByteArrayFields;
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
  private boolean computeRecordEquals(ThreadInfo ti, ClassInfo ci, int otherRef) {
    ElementInfo thisEi = ti.getThisElementInfo();
    ElementInfo otherEi = ti.getHeap().get(otherRef);
    System.out.println("computeRecordEquals: thisRef======>" + thisEi.getObjectRef() + ", otherRef=" + otherRef);
    if (otherEi == null || !ci.equals(otherEi.getClassInfo())) {
      System.out.println("Early return: otherEi======>" + (otherEi == null ? "null" : otherEi.getClassInfo().getName()));
      return false;
    }
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    String[] components = bmi.getBmArg().split(";");
    System.out.println("Components: " + java.util.Arrays.toString(components));
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
    System.out.println("printing el components el gamdeen neek: " + java.util.Arrays.toString(components));
    for (String compName : components) {
      Object value = ei.getFieldValueObject(compName);
      System.out.println("Component: " + compName + ", Value: " + value);
      hash = 31 * hash + (value != null ? value.hashCode() : 0);
    }
    // System.out.println("Computed hashCode: " + hash);
    return hash;
  }

  private int computeRecordToString(ThreadInfo ti, ClassInfo ci) {
    ElementInfo ei = ti.getThisElementInfo();
    String simpleName = ci.getName().substring(ci.getName().lastIndexOf('$') + 1);
    StringBuilder sb = new StringBuilder(simpleName).append("[");
    BootstrapMethodInfo bmi = ci.getBootstrapMethodInfo(bootstrapMethodIndex);
    String[] components = bmi.getBmArg().split(";");
    for (int i = 0; i < components.length; i++) {
      Object value = ei.getFieldValueObject(components[i]);
      sb.append(components[i]).append("=").append(value);
      if (i < components.length - 1) sb.append(", ");
    }
    sb.append("]");
    System.out.println("a7a: "+ sb.toString());
    return ti.getHeap().newString(sb.toString(), ti).getObjectRef();
  }
  @Override
  public Instruction execute (ThreadInfo ti) {
    StackFrame frame = ti.getModifiableTopFrame();
    ClassInfo enclosingClass = this.getMethodInfo().getClassInfo();
    BootstrapMethodInfo bmi = enclosingClass.getBootstrapMethodInfo(bootstrapMethodIndex);

    String bootstrapMethodName = (bmi.getLambdaBody() != null) ? bmi.getLambdaBody().getName() : this.samMethodName;
    System.out.println("Bootstrap type: " + bmi.getBmType() + ", method: " + bootstrapMethodName +
            ", bootstrapIndex: " + bootstrapMethodIndex);

    if (bmi.getBmType() == BootstrapMethodInfo.BMType.RECORDS) {
      String methodName = this.samMethodName;
      System.out.println("Executing record method: " + methodName);

      if ("equals".equals(methodName)) {
        System.out.println("Stack before pop: " + frame.getTopPos() + ", " + frame.peek(1) + ", " + frame.peek(0));
        int otherRef = frame.peek();
        frame.pop(2);
        System.out.println("Calling computeRecordEquals with otherRef=" + otherRef);
        boolean result = computeRecordEquals(ti, enclosingClass, otherRef);
        frame.push(result ? 1 : 0);
      } else if ("hashCode".equals(methodName)) {
        frame.pop(1);
        System.out.println("Calling computeRecordHashCode for class: " + enclosingClass.getName());
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
    if(ei==null || ei!=lastFuncObj || freeVariableTypes.length>0) {
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
      // In case of string concat, we return a null ref as a dummy arg.
      // It is here only to be popped at the helper function's
      // (java.lang.String.generateStringByConcatenatingArgs) return.
      // We continue execution from the newly created stack frame (in the helper function).
      return ti.getPC();
    } else {
      // In other cases (for lambda expr), this return value shouldn't be null.
      // We continue execution from the following bytecode.
      return getNext(ti);
    }
  }
}
