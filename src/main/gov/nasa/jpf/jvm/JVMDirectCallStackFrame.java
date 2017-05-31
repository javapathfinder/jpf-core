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

package gov.nasa.jpf.jvm;

import gov.nasa.jpf.vm.DirectCallStackFrame;
import gov.nasa.jpf.vm.MethodInfo;

/**
 * a direct call stackframe that supports JVM calling conventions
 */
public class JVMDirectCallStackFrame extends DirectCallStackFrame {
  
  JVMDirectCallStackFrame (MethodInfo miDirectCall, MethodInfo callee){
    super( miDirectCall, callee);
  }

  //--- return value handling
  
  @Override
  public int getResult(){
    return pop();
  }
  
  @Override
  public int getReferenceResult(){
    return pop();
  }
  
  @Override
  public long getLongResult(){
    return popLong();
  }

  @Override
  public Object getResultAttr(){
    return getOperandAttr();
  }
  
  @Override
  public Object getLongResultAttr(){
    return getLongOperandAttr();
  }

  @Override
  public void setExceptionReference (int exRef){
    clearOperandStack();
    pushRef( exRef);
  }
  
  @Override
  public int getExceptionReference(){
    return pop();
  }

  @Override
  public void setExceptionReferenceAttribute (Object attr){
    setOperandAttr(attr);
  }
  
  @Override
  public Object getExceptionReferenceAttribute (){
    return getOperandAttr();
  }
  
  //--- direct call argument initialization
  // NOTE - we don't support out-of-order arguments yet, i.e. the slotIdx is ignored
  
  
  @Override
  public int setArgument (int slotIdx, int v, Object attr){
    push(v);
    if (attr != null){
      setOperandAttr(attr);
    }
    
    return slotIdx+1;
  }
  
  @Override
  public int setReferenceArgument (int slotIdx, int ref, Object attr){
    pushRef(ref);
    if (attr != null){
      setOperandAttr(attr);
    }
    
    return slotIdx+1;
  }
  
  @Override
  public int setLongArgument (int slotIdx, long v, Object attr){
    pushLong(v);
    if (attr != null){
      setLongOperandAttr(attr);
    } 
    
    return slotIdx+2;
  }
  
  //--- DirectCallStackFrame methods don't have arguments
  
  @Override
  public void setArgumentLocal (int argIdx, int v, Object attr){
    throw new UnsupportedOperationException("direct call methods don't have arguments");
  }
  @Override
  public void setReferenceArgumentLocal (int argIdx, int v, Object attr){
    throw new UnsupportedOperationException("direct call methods don't have arguments");
  }
  @Override
  public void setLongArgumentLocal (int argIdx, long v, Object attr){
    throw new UnsupportedOperationException("direct call methods don't have arguments");
  }
}
