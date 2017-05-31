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
  
  public BootstrapMethodInfo(int lambdaRefKind, ClassInfo enclosingClass, MethodInfo lambdaBody, String samDescriptor) {
    this.lambdaRefKind = lambdaRefKind;
    this.enclosingClass = enclosingClass;
    this.lambdaBody = lambdaBody;
    this.samDescriptor = samDescriptor;
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
}
