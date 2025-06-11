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

import gov.nasa.jpf.jvm.ClassFile;
import gov.nasa.jpf.jvm.JVMClassInfo;

import java.lang.invoke.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 *
 * For now, this is only used to capture boostrap methods for lambda expression,
 * which link the method representing the lambda body to a single abstract method
 * (SAM) declared in a functional interface. References to bootstrap methods are
 * provided by the invokedynamic bytecode instruction.
 */
public class BootstrapMethodInfo {

  int lambdaRefKind;

  // method capturing lambda body to be linked to the function method of function object
  MethodInfo lambdaBody;

  // class containing lamabda expression
  ClassInfo enclosingClass;

  // descriptor of a SAM declared within the functional interface
  String samDescriptor;

  String bmArg;

  String dynamicClassName;
  String dynamicMethodName;
  String dynamicParameters;
  private String dynamicDescriptor;
  private int[] cpArgs;
  private Object[] resolvedArgs;
  private String[] recordComponents;
  private Class<?>[] argTypes;

  public enum BMType{
    STRING_CONCATENATION,
    LAMBDA_EXPRESSION,
    SERIALIZABLE_LAMBDA_EXPRESSION,
    RECORDS, // For record synthetic methods
    DYNAMIC // this one for new type for generic bootstrap methods
  }

  BMType bmType;


  public void parseArgumentTypes() {
    if (dynamicDescriptor == null) return;

    // getting parameter types from method descriptor (e.g., "(Ljava/lang/String;I)V")
    int start = dynamicDescriptor.indexOf('(');
    int end = dynamicDescriptor.indexOf(')');
    if (start == -1 || end == -1) return;

    String paramDesc = dynamicDescriptor.substring(start + 1, end);
    this.argTypes = parseTypes(paramDesc);
  }

  private Class<?>[] parseTypes(String desc) {
    List<Class<?>> types = new ArrayList<>();
    int i = 0;
    while (i < desc.length()) {
      char c = desc.charAt(i);
      switch (c) {
        case 'B': types.add(byte.class); i++; break;
        case 'C': types.add(char.class); i++; break;
        case 'D': types.add(double.class); i++; break;
        case 'F': types.add(float.class); i++; break;
        case 'I': types.add(int.class); i++; break;
        case 'J': types.add(long.class); i++; break;
        case 'S': types.add(short.class); i++; break;
        case 'Z': types.add(boolean.class); i++; break;
        case 'L':
          int end = desc.indexOf(';', i);
          String clsName = desc.substring(i + 1, end).replace('/', '.');
          try {
            types.add(Class.forName(clsName));
          } catch (ClassNotFoundException e) {
            types.add(Object.class); // fallback here
          }
          i = end + 1;
          break;
        case '[':
          // handling arrays as objects
          types.add(Object.class);
          while (desc.charAt(i) == '[') i++;
          if (desc.charAt(i) == 'L') i = desc.indexOf(';', i) + 1;
          else i++;
          break;
        default:
          i++;
      }
    }
    return types.toArray(new Class<?>[0]);
  }

  public void parseSamArgumentTypes() {
    System.out.println("[DEBUG] Parsing SAM descriptor: " + samDescriptor);
    if (samDescriptor == null) return;

    int start = samDescriptor.indexOf('(');
    int end = samDescriptor.indexOf(')');
    if (start == -1 || end == -1) {
      System.out.println("[ERROR] Invalid SAM descriptor format");
      return;
    }

    String paramDesc = samDescriptor.substring(start + 1, end);
    System.out.println("[DEBUG] Parameter descriptor: " + paramDesc);

    this.argTypes = parseTypes(paramDesc);
    System.out.println("[DEBUG] Parsed argTypes: " + Arrays.toString(argTypes));
  }

  public void resolveBootstrapArguments() {
    if (cpArgs == null || cpArgs.length == 0) {
      resolvedArgs = new Object[0];
      return;
    }

    resolvedArgs = new Object[cpArgs.length];
    ClassFile cf = null;

    if (enclosingClass instanceof JVMClassInfo) {
      cf = ((JVMClassInfo) enclosingClass).getClassFile();
    }

    if (cf != null) {
      for (int i = 0; i < cpArgs.length; i++) {
        resolvedArgs[i] = cf.getConstantValue(cpArgs[i]);
      }
    }
  }

  public BootstrapMethodInfo(int lambdaRefKind, ClassInfo enclosingClass, MethodInfo lambdaBody, String samDescriptor,
                             String bmArg, BMType bmType) {
    this.lambdaRefKind = lambdaRefKind;
    this.enclosingClass = enclosingClass;
    this.lambdaBody = lambdaBody;
    this.samDescriptor = samDescriptor;
    this.bmArg = bmArg;
    this.bmType = bmType;
  }

  /**
   * Constructor for constructing {@link BootstrapMethodInfo} for bootstrap methods
   * with arbitrary number of bootstrap method arguments
   */
  public BootstrapMethodInfo(ClassInfo enclosingClass, int[] cpArgs) {
    this.enclosingClass = enclosingClass;
    this.bmType = BMType.DYNAMIC;
    this.cpArgs = Arrays.copyOf(cpArgs, cpArgs.length);
    this.lambdaRefKind = 0;
    this.lambdaBody = null;
    this.samDescriptor = null;
    this.dynamicDescriptor = null;
    this.bmArg = "";
  }

  public void setDynamicMetadata(int refKind, String className, String methodName,
                                 String parameters, String descriptor) {
    this.lambdaRefKind = refKind;
    this.dynamicClassName = className;
    this.dynamicMethodName = methodName;
    this.dynamicParameters = parameters;
    this.dynamicDescriptor = descriptor;
    parseArgumentTypes();
  }

  public void setResolvedArgs(Object[] args) {
    this.resolvedArgs = args;
  }

  // this can be useful cause we're using proper descriptor instead of the sam
  public void setRecordComponents(String components) { this.recordComponents = components.split(";");}

  public void prepareForCallSiteGeneration() {
    // Resolve bootstrap arguments if not already done
    if (resolvedArgs == null) resolveBootstrapArguments();

    switch (bmType) {
      case STRING_CONCATENATION:
      case LAMBDA_EXPRESSION:
      case SERIALIZABLE_LAMBDA_EXPRESSION:
        if (samDescriptor != null) parseSamArgumentTypes();
        break;
      case RECORDS:
      case DYNAMIC:
        if (dynamicDescriptor != null) parseArgumentTypes();
        break;
    }

    System.out.println("[DEBUG] Prepared CallSite generation for " + bmType + " with " + (resolvedArgs != null ? resolvedArgs.length : 0) + " resolved args");
  }

  public CallSite generateCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    if (resolvedArgs == null) resolveBootstrapArguments();
    switch (bmType) {
      case STRING_CONCATENATION:
        return createStringConcatCallSite(lookup, name, methodType);
      case LAMBDA_EXPRESSION:
      case SERIALIZABLE_LAMBDA_EXPRESSION:
        return createLambdaCallSite(lookup, name, methodType);
      case RECORDS:
        return createRecordCallSite(lookup, name, methodType);
      case DYNAMIC:
        return createDynamicCallSite(lookup, name, methodType);
      default:
        throw new BootstrapMethodError("Unsupported bootstrap method type: " + bmType);
    }
  }

  private CallSite createStringConcatCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    // TODO: we need to implement a JPFStringConcatHelper first to handle the recipes
    throw new UnsupportedOperationException("Later");
  }

  private CallSite createLambdaCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    // TODO: Implement proper Lambda CallSite generation
    throw new UnsupportedOperationException("Later");

  }

  private CallSite createRecordCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    // TODO: Implement proper record CallSite generation
    throw new UnsupportedOperationException("Later");
  }

  private CallSite createDynamicCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    // TODO: Implement proper dynamic CallSite generation
    throw new UnsupportedOperationException("later");
  }



  @Override
  public String toString() {
    return "BootstrapMethodInfo[" + enclosingClass.getName() + "." + lambdaBody.getBaseName() +
            "[SAM descriptor:" + samDescriptor + "]]";
  }

  public MethodInfo getLambdaBody() {
    return lambdaBody;
  }

  public String getSamDescriptor() {
    return samDescriptor;
  }

  public int getLambdaRefKind () {
    return lambdaRefKind;
  }

  public String getBmArg(){ return bmArg;}

  public BMType getBmType() { return bmType;}


  public int[] getCpArgs() {return Arrays.copyOf(cpArgs, cpArgs.length);}

  public Object[] getResolvedArgs() {return resolvedArgs;}

  public String[] getRecordComponents() {return recordComponents;}

  public String getDynamicClassName() { return dynamicClassName; }

  public String getDynamicMethodName() { return dynamicMethodName; }

  public String getDynamicParameters() { return dynamicParameters; }

  public String getDynamicDescriptor() { return dynamicDescriptor; }

}
