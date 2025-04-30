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

import java.util.Arrays;

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

  public enum BMType{
    STRING_CONCATENATION,
    LAMBDA_EXPRESSION,
    SERIALIZABLE_LAMBDA_EXPRESSION,
    RECORDS, // For record synthetic methods
    DYNAMIC // this one for new type for generic bootstrap methods
  }
  BMType bmType;
  public BootstrapMethodInfo(int lambdaRefKind, ClassInfo enclosingClass, MethodInfo lambdaBody, String samDescriptor, String bmArg, BMType bmType) {
    this.lambdaRefKind = lambdaRefKind;
    this.enclosingClass = enclosingClass;
    this.lambdaBody = lambdaBody != null ? lambdaBody : enclosingClass.getMethod(samDescriptor, false); // Fallback
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
  }
  public void setResolvedArgs(Object[] args) {
    this.resolvedArgs = args;
  }
  // this can be useful cause we're using proper descriptor instead of the sam
  public void setRecordComponents(String components) { this.recordComponents = components.split(";");}

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

  public Object[] getResolvedArgs() {
    return resolvedArgs;
  }

  public String[] getRecordComponents() {
    return recordComponents;
  }

  public String getDynamicClassName() { return dynamicClassName; }

  public String getDynamicMethodName() { return dynamicMethodName; }

  public String getDynamicParameters() { return dynamicParameters; }

  public String getDynamicDescriptor() { return dynamicDescriptor; }

}
