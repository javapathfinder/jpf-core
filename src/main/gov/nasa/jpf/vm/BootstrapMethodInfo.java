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

import java.lang.invoke.CallSite;
import java.lang.invoke.MethodHandle;
import java.lang.invoke.MethodHandles;
import java.lang.invoke.MethodType;
import java.lang.invoke.ConstantCallSite;

import java.util.Arrays;
import java.util.List;
import java.util.ArrayList;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * @author Mahmoud Khawaja <mahmoud.khawaja97@gmail.com>
 * Bootstrap method information for dynamic call site generation.
 * we support call site generation for string concat for now
 */
public class BootstrapMethodInfo {

  public enum BMType {
    STRING_CONCATENATION,
    LAMBDA_EXPRESSION,
    SERIALIZABLE_LAMBDA_EXPRESSION,
    RECORDS,
    DYNAMIC
  }

  // ==================== FIELDS ====================

  final ClassInfo enclosingClass;
  private final BMType bmType;
  private final int[] cpArgs;

  // Lambda-specific fields
  private int lambdaRefKind;
  private MethodInfo lambdaBody;
  private String samDescriptor;

  // General fields
  private String bmArg;
  private Object[] resolvedArgs;
  private Class<?>[] argumentTypes;

  // Dynamic method fields
  private String dynamicClassName;
  private String dynamicMethodName;
  private String dynamicParameters;
  private String dynamicDescriptor;

  // Record-specific fields
  private String[] recordComponents;

  // ==================== CONSTRUCTORS ====================

  /**
   * Constructor for lambda expressions
   */
  public BootstrapMethodInfo(int lambdaRefKind, ClassInfo enclosingClass, MethodInfo lambdaBody,
                             String samDescriptor, String bmArg, BMType bmType) {
    this.lambdaRefKind = lambdaRefKind;
    this.enclosingClass = enclosingClass;
    this.lambdaBody = lambdaBody;
    this.samDescriptor = samDescriptor;
    this.bmArg = bmArg;
    this.bmType = bmType;
    this.cpArgs = null;
  }

  /**
   * Constructor for dynamic bootstrap methods
   */
  public BootstrapMethodInfo(ClassInfo enclosingClass, int[] cpArgs) {
    this.enclosingClass = enclosingClass;
    this.bmType = BMType.DYNAMIC;
    this.cpArgs = cpArgs != null ? Arrays.copyOf(cpArgs, cpArgs.length) : null;
    this.lambdaRefKind = 0;
    this.lambdaBody = null;
    this.samDescriptor = null;
    this.dynamicDescriptor = null;
    this.bmArg = "";
  }

  // ==================== CONFIGURATION METHODS ====================

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

  public void setRecordComponents(String components) {
    if (components != null && !components.isEmpty()) {
      this.recordComponents = components.split(";");
    }
  }

  // ==================== CALLSITE GENERATION ====================

  public void prepareForCallSiteGeneration() {
    if (resolvedArgs == null) {
      resolveBootstrapArguments();
    }

    parseTypesBasedOnBMType();
    logPreparationComplete();
  }

  public CallSite generateCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    if (resolvedArgs == null) {
      resolveBootstrapArguments();
    }

    return createCallSiteByType(lookup, name, methodType);
  }

  // ==================== PRIVATE HELPER METHODS ====================

  private void parseTypesBasedOnBMType() {
    switch (bmType) {
      case STRING_CONCATENATION:
        // we don't need to parse the sam for string concat we will use actual method type
        break;
      case LAMBDA_EXPRESSION:
      case SERIALIZABLE_LAMBDA_EXPRESSION:
        if (samDescriptor != null) parseSamArgumentTypes();
        break;
      case RECORDS:
      case DYNAMIC:
        if (dynamicDescriptor != null) parseArgumentTypes();
        break;
    }
  }

  private CallSite createCallSiteByType(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
      return switch (bmType) {
          case STRING_CONCATENATION -> createStringConcatCallSite(lookup, name, methodType);
          case LAMBDA_EXPRESSION, SERIALIZABLE_LAMBDA_EXPRESSION -> createLambdaCallSite(lookup, name, methodType);
          case RECORDS -> createRecordCallSite(lookup, name, methodType);
          case DYNAMIC -> createDynamicCallSite(lookup, name, methodType);
          default -> throw new BootstrapMethodError("Unsupported bootstrap method type: " + bmType);
      };
  }

  private CallSite createStringConcatCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    debugLog("Creating string concat CallSite");
    debugLog("Using actual methodType: " + methodType);

    StringConcatConfig config = extractStringConcatConfig(methodType);
    MethodHandle target = createStringConcatHandle(lookup, methodType, config);

    return new ConstantCallSite(target);
  }

  private StringConcatConfig extractStringConcatConfig(MethodType methodType) {
    Class<?>[] paramTypes = methodType.parameterArray();
    String recipe = extractRecipe();
    Object[] constants = extractConstants();

    debugLog("Recipe: " + JPFStringConcatHelper.escapeUnicode(recipe));
    debugLog("Constants: " + Arrays.toString(constants));
    debugLog("Parameter types: " + Arrays.toString(paramTypes));

    return new StringConcatConfig(recipe, constants, paramTypes);
  }

  private String extractRecipe() {
    if (resolvedArgs != null && resolvedArgs.length > 0 && resolvedArgs[0] instanceof String) {
      return (String) resolvedArgs[0];
    }

    if (bmArg != null && !bmArg.isEmpty()) {
      return bmArg;
    }

    debugLog("No recipe available - creating default");
    return "";
  }

  private Object[] extractConstants() {
    if (resolvedArgs != null && resolvedArgs.length > 1) {
      return Arrays.copyOfRange(resolvedArgs, 1, resolvedArgs.length);
    }
    return new Object[0];
  }

  private MethodHandle createStringConcatHandle(MethodHandles.Lookup lookup, MethodType methodType,
                                                StringConcatConfig config) throws Throwable {
    MethodHandle target = MethodHandles.lookup().findStatic(
            BootstrapMethodInfo.class,
            "concatVarArgsIndividual",
            MethodType.methodType(String.class, String.class, Class[].class, Object[].class, Object[].class)
    );

    return target
            .bindTo(config.recipe)
            .bindTo(config.paramTypes)
            .bindTo(config.constants)
            .asCollector(Object[].class, config.paramTypes.length)
            .asType(methodType);
  }

  // ==================== TYPE PARSING ====================

  public void parseArgumentTypes() {
    if (dynamicDescriptor == null) return;

    String parameterDescriptor = extractParameterDescriptor(dynamicDescriptor);
    this.argumentTypes = parseTypeDescriptor(parameterDescriptor);
  }

  public void parseSamArgumentTypes() {
    debugLog("Parsing SAM descriptor: " + samDescriptor);
    if (samDescriptor == null) return;

    String parameterDescriptor = extractParameterDescriptor(samDescriptor);
    this.argumentTypes = parseTypeDescriptor(parameterDescriptor);
    debugLog("Parsed argTypes: " + Arrays.toString(argumentTypes));
  }

  private String extractParameterDescriptor(String descriptor) {
    int start = descriptor.indexOf('(');
    int end = descriptor.indexOf(')');

    if (start == -1 || end == -1) {
      debugLog("Invalid descriptor format: " + descriptor);
      return "";
    }

    return descriptor.substring(start + 1, end);
  }

  private Class<?>[] parseTypeDescriptor(String descriptor) {
    if (descriptor.isEmpty()) return new Class<?>[0];

    List<Class<?>> types = new ArrayList<>();
    TypeDescriptorParser parser = new TypeDescriptorParser(descriptor);

    while (parser.hasNext()) {
      Class<?> type = parser.parseNextType();
      if (type != null) {
        types.add(type);
      }
    }

    return types.toArray(new Class<?>[0]);
  }

  // ==================== METHOD TYPE CREATION ====================

  public MethodType createMethodType() {
    String descriptor = (samDescriptor != null) ? samDescriptor : dynamicDescriptor;
    if (descriptor == null) {
      return MethodType.methodType(Object.class);
    }

    try {
      Class<?>[] paramTypes = extractParameterTypes(descriptor);
      Class<?> returnType = extractReturnType(descriptor);
      return MethodType.methodType(returnType, paramTypes);
    } catch (Exception e) {
      debugLog("Failed to parse method type from: " + descriptor);
      return MethodType.methodType(Object.class);
    }
  }

  private Class<?>[] extractParameterTypes(String descriptor) {
    String parameterDescriptor = extractParameterDescriptor(descriptor);
    return parseTypeDescriptor(parameterDescriptor);
  }

  private Class<?> extractReturnType(String descriptor) {
    int end = descriptor.indexOf(")");
    if (end == -1 || end >= descriptor.length() - 1) {
      return Object.class;
    }

    String returnDescriptor = descriptor.substring(end + 1);
    if ("V".equals(returnDescriptor)) { // void return type
      return void.class;
    }

    Class<?>[] returnTypes = parseTypeDescriptor(returnDescriptor);
    return (returnTypes.length > 0) ? returnTypes[0] : Object.class;
  }

  // ==================== BOOTSTRAP ARGUMENT RESOLUTION ====================

  public void resolveBootstrapArguments() {
    if (cpArgs == null || cpArgs.length == 0) {
      resolvedArgs = new Object[0];
      return;
    }

    ClassFile classFile = getClassFile();
    if (classFile == null) {
      resolvedArgs = new Object[0];
      return;
    }

    resolvedArgs = new Object[cpArgs.length];
    for (int i = 0; i < cpArgs.length; i++) {
      resolvedArgs[i] = classFile.getConstantValue(cpArgs[i]);
    }
  }

  private ClassFile getClassFile() {
    return (enclosingClass instanceof JVMClassInfo)
            ? ((JVMClassInfo) enclosingClass).getClassFile()
            : null;
  }

  // ==================== STUB METHODS FOR FUTURE IMPLEMENTATION ====================

  private CallSite createLambdaCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    throw new UnsupportedOperationException("later");
  }

  private CallSite createRecordCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    throw new UnsupportedOperationException("later");
  }

  private CallSite createDynamicCallSite(MethodHandles.Lookup lookup, String name, MethodType methodType) throws Throwable {
    throw new UnsupportedOperationException("later");
  }

  // ==================== PUBLIC METHODS ====================

  public static String concatVarArgsIndividual(String recipe, Class<?>[] argTypes, Object[] constants, Object[] args) {
    debugLog("concatVarArgsIndividual called");
    debugLog("Recipe: " + JPFStringConcatHelper.escapeUnicode(recipe));
    debugLog("ArgTypes: " + Arrays.toString(argTypes));
    debugLog("Constants: " + Arrays.toString(constants));
    debugLog("Args: " + Arrays.toString(args));

    return JPFStringConcatHelper.concatenate(recipe, argTypes, constants, args);
  }

  // ==================== UTILITY METHODS ====================

  private void logPreparationComplete() {
    debugLog("Prepared CallSite generation for " + bmType +
            " with " + (resolvedArgs != null ? resolvedArgs.length : 0) + " resolved args");
  }

  private static void debugLog(String message) {
    System.out.println("[DEBUG] " + message);
  }

  // ==================== GETTERS ====================

  public MethodInfo getLambdaBody() { return lambdaBody; }
  public String getSamDescriptor() { return samDescriptor; }
  public int getLambdaRefKind() { return lambdaRefKind; }
  public String getBmArg() { return bmArg; }
  public BMType getBmType() { return bmType; }
  public int[] getCpArgs() { return cpArgs != null ? Arrays.copyOf(cpArgs, cpArgs.length) : null; }
  public Object[] getResolvedArgs() { return resolvedArgs; }
  public String[] getRecordComponents() { return recordComponents; }
  public String getDynamicClassName() { return dynamicClassName; }
  public String getDynamicMethodName() { return dynamicMethodName; }
  public String getDynamicParameters() { return dynamicParameters; }
  public String getDynamicDescriptor() { return dynamicDescriptor; }
  public Class<?>[] getArgumentTypes() { return argumentTypes != null ? argumentTypes : new Class<?>[0]; }

  @Override
  public String toString() {
    return "BootstrapMethodInfo[" + enclosingClass.getName() +
            (lambdaBody != null ? "." + lambdaBody.getBaseName() : "") +
            " [Type:" + bmType + "][SAM:" + samDescriptor + "]]";
  }

  // ==================== INNER CLASSES ====================

  /**
   * Configuration holder for string concatenation
   */
    private record StringConcatConfig(String recipe, Object[] constants, Class<?>[] paramTypes) {}

  /**
   * Parser for type descriptors
   */
  private static class TypeDescriptorParser {
    private final String descriptor;
    private int position;

    TypeDescriptorParser(String descriptor) {
      this.descriptor = descriptor;
      this.position = 0;
    }

    boolean hasNext() {
      return position < descriptor.length();
    }

    Class<?> parseNextType() {
      if (!hasNext()) return null;

      char typeChar = descriptor.charAt(position++);

        return switch (typeChar) {
            case 'B' -> byte.class;
            case 'C' -> char.class;
            case 'D' -> double.class;
            case 'F' -> float.class;
            case 'I' -> int.class;
            case 'J' -> long.class;
            case 'S' -> short.class;
            case 'Z' -> boolean.class;
            case 'L' -> parseObjectType();
            case '[' -> parseArrayType();
            default -> Object.class;
        };
    }

    private Class<?> parseObjectType() {
      int start = position;
      int end = descriptor.indexOf(';', start);
      if (end == -1) return Object.class;

      String className = descriptor.substring(start, end).replace('/', '.');
      position = end + 1;

      try {
        return Class.forName(className);
      } catch (ClassNotFoundException e) {
        return Object.class;
      }
    }

    private Class<?> parseArrayType() {
      // Skip array dimensions
      while (position < descriptor.length() && descriptor.charAt(position) == '[') {
        position++;
      }

      // For now, treat all arrays as Object.class
      if (position < descriptor.length() && descriptor.charAt(position) == 'L') { // L is object prefix
        parseObjectType(); // Consume the object type
      } else if (position < descriptor.length()) {
        position++; // Consume primitive type
      }

      return Object.class;
    }
  }
}
