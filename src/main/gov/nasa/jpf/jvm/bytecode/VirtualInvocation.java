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

import gov.nasa.jpf.vm.ClassChangeException;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;


/**
 * a base class for virtual call instructions
 */
public abstract class VirtualInvocation extends InstanceInvocation {

  // note that we can't null laseCalleeCi and invokedMethod in cleanupTransients()
  // since we use it as an internal optimization (loops with repeated calls on the
  // same object)
  
  ClassInfo lastCalleeCi; // cached for performance

  protected VirtualInvocation () {}

  protected VirtualInvocation (String clsDescriptor, String methodName, String signature){
    super(clsDescriptor, methodName, signature);
  }

  @Override
  public String toPostExecString(){
    StringBuilder sb = new StringBuilder();
    sb.append(getMnemonic());
    sb.append(' ');
    
    if (invokedMethod != null){
      sb.append( lastCalleeCi.getName());
      sb.append('@');
      sb.append(Integer.toHexString(lastObj));
      sb.append('.');
      sb.append(invokedMethod.getUniqueName());

      if (invokedMethod.isMJI()){
        sb.append(" [native]");
      }
      
    } else { // something went wrong, the method wasn't found
      if (lastCalleeCi != null){
        sb.append( lastCalleeCi.getName());
      } else {
        sb.append(cname);
      }
      sb.append('@');
      if (lastObj == MJIEnv.NULL){
        sb.append("<null>");
      } else {
        sb.append(Integer.toHexString(lastObj));
      }
      sb.append('.');
      sb.append(mname);
      sb.append(signature);
      sb.append(" (?)");
    }
    
    return sb.toString();
  }
  
  @Override
  public Instruction execute (ThreadInfo ti) {
    int objRef = ti.getCalleeThis(getArgSize());
    MethodInfo callee;

    if (objRef == MJIEnv.NULL) {
      lastObj = MJIEnv.NULL;
      return ti.createAndThrowException("java.lang.NullPointerException", "Calling '" + mname + "' on null object");
    }

    try {
      callee = getInvokedMethod(ti, objRef);
    } catch (ClassChangeException ccx){
      return ti.createAndThrowException("java.lang.IncompatibleClassChangeError", ccx.getMessage());
    }
    
    ElementInfo ei = ti.getElementInfo(objRef);
    
    if (callee == null) {
      String clsName = ti.getClassInfo(objRef).getName();
      return ti.createAndThrowException("java.lang.NoSuchMethodError", clsName + '.' + mname);
    } else {
      if (callee.isAbstract()){
        return ti.createAndThrowException("java.lang.AbstractMethodError", callee.getFullName() + ", object: " + ei);
      }
    }

    if (callee.isSynchronized()) {
      ei = ti.getScheduler().updateObjectSharedness(ti, ei, null); // locks most likely belong to shared objects
      if (reschedulesLockAcquisition(ti, ei)){
        return this;
      }
    }

    setupCallee( ti, callee); // this creates, initializes and pushes the callee StackFrame

    return ti.getPC(); // we can't just return the first callee insn if a listener throws an exception
  }
  
  /**
   * If the current thread already owns the lock, then the current thread can go on.
   * For example, this is a recursive acquisition.
   */
  protected boolean isLockOwner(ThreadInfo ti, ElementInfo ei) {
    return ei.getLockingThread() == ti;
  }

  /**
   * If the object will still be owned, then the current thread can go on.
   * For example, all but the last monitorexit for the object.
   */
  protected boolean isLastUnlock(ElementInfo ei) {
    return ei.getLockCount() == 1;
  }


  @Override
  public MethodInfo getInvokedMethod(ThreadInfo ti){
    int objRef;

    if (ti.getNextPC() == null){ // this is pre-exec
      objRef = ti.getCalleeThis(getArgSize());
    } else {                     // this is post-exec
      objRef = lastObj;
    }

    return getInvokedMethod(ti, objRef);
  }

  public MethodInfo getInvokedMethod (ThreadInfo ti, int objRef) {

    if (objRef != MJIEnv.NULL) {
      lastObj = objRef;

      ClassInfo cci = ti.getClassInfo(objRef);

      if (lastCalleeCi != cci) { // callee ClassInfo has changed
        lastCalleeCi = cci;
        invokedMethod = cci.getMethod(mname, true);

        if (invokedMethod == null) {
          invokedMethod = cci.getDefaultMethod(mname);
                    
          if (invokedMethod == null){
            lastObj = MJIEnv.NULL;
            lastCalleeCi = null;
          }
        }
      }

    } else {
      lastObj = MJIEnv.NULL;
      lastCalleeCi = null;
      invokedMethod = null;
    }

    return invokedMethod;
  }

  @Override
  public Object getFieldValue (String id, ThreadInfo ti){
    int objRef = getCalleeThis(ti);
    ElementInfo ei = ti.getElementInfo(objRef);

    Object v = ei.getFieldValueObject(id);

    if (v == null){ // try a static field
      v = ei.getClassInfo().getStaticFieldValueObject(id);
    }

    return v;
  }
  
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction typeSafeClone(MethodInfo clonedMethod) {
    VirtualInvocation clone = null;

    try {
      clone = (VirtualInvocation) super.clone();

      // reset the method that this insn belongs to
      clone.mi = clonedMethod;

      clone.lastCalleeCi = null;
      clone.invokedMethod = null;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return clone;
  }
}
