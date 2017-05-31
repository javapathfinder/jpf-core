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

import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Types;
import gov.nasa.jpf.vm.bytecode.InvokeInstruction;


/**
 * abstraction for all invoke instructions
 */
public abstract class JVMInvokeInstruction extends InvokeInstruction implements JVMInstruction {
  /* Those are all straight from the class file.
   * Note that we can't directly resolve to MethodInfo objects because
   * the corresponding class might not be loaded yet (has to be done
   * on execution)
   */
  protected String cname;
  protected String mname;
  protected String signature;

  protected int argSize = -1;

  /** to cache the last callee object */
  protected int lastObj = Integer.MIN_VALUE;

  /**
   * watch out - this is only const for static and special invocation
   * all virtuals will use it only as a cache
   */
  protected MethodInfo invokedMethod;

  protected Object[] arguments; // temporary cache for arg values (for listeners)

  protected JVMInvokeInstruction (String clsName, String methodName, String signature){
    this.cname = Types.getClassNameFromTypeName(clsName);
    this.signature = signature;
    this.mname = MethodInfo.getUniqueName(methodName, signature);
  }

  protected JVMInvokeInstruction () {}

  @Override
  public int getLength() {
    return 3; // opcode, index1, index2
  }
  
  // only useful from post-exec notifications
  public int getLastObjRef() {
    return lastObj;
  }

  /**
   * this is for explicit initialization (not BCEL)
   */
  public void setInvokedMethod (String clsName, String mthName, String sig) {
    cname = clsName;
    mname = mthName + sig;
    signature = sig;
  }

  /**
   * be aware of that this might differ from getInvokedMethod(), since it only
   * denotes the target type info we have at the static point of the call, i.e.
   * before dynamic dispatching
   */
  @Override
  public String getInvokedMethodClassName() {
    return cname;
  }

  @Override
  public String getInvokedMethodSignature() {
    return signature;
  }

  @Override
  public String getInvokedMethodName () {
    return mname;
  }

  public abstract MethodInfo getInvokedMethod (ThreadInfo ti);

  @Override
  public MethodInfo getInvokedMethod () {
    if (invokedMethod == null){
      invokedMethod = getInvokedMethod(ThreadInfo.getCurrentThread());
    }

    return invokedMethod;
  }

  @Override
  public boolean isCompleted(ThreadInfo ti) {
    Instruction nextPc = ti.getNextPC();

    if (nextPc == null || nextPc == this){
      return false;
    }

    if (invokedMethod != null){
      MethodInfo topMethod = ti.getTopFrame().getMethodInfo();
      if (invokedMethod.isMJI() && (topMethod == mi)) {
        // same frame -> this was a native method that already returned
        return true;
      }

      if (topMethod == invokedMethod){
        return true;
      }
    }

    // <2do> how do we account for exceptions?

    return false;
  }

  StackFrame getCallerFrame (ThreadInfo ti, MethodInfo callee) {
    return ti.getStackFrameExecuting(this, 0);
  }

  //--- invocation processing

  protected void setupCallee (ThreadInfo ti, MethodInfo callee){
    ClassInfo ciCaller = callee.getClassInfo();
    StackFrame frame = ciCaller.createStackFrame( ti, callee);
    
    ti.pushFrame(frame);
    ti.enter();
  }
  
  /**
   * this is a little helper to find out about call argument values from listeners that
   * don't want to dig through MethodInfos and Types. Reference arguments are returned as
   * either ElementInfos or 'null', all others are boxed (i.e. a 'double' is returned as
   * a 'Double' object).
   * It goes without saying that this method can only be called during an executeInstruction()
   * or instructionExecuted() notification for the corresponding JVMInvokeInstruction
   * We use the caller frame to retrieve the arguments (instead of the locals of
   * the callee) since that works for both pre- and post-exec notification
   */
  public Object[] getArgumentValues (ThreadInfo ti) {    
    MethodInfo callee = getInvokedMethod(ti);
    StackFrame frame = getCallerFrame(ti, callee);
    
    assert frame != null : "can't find caller stackframe for: " + this;
    return frame.getCallArguments(ti);
  }

  public Object[] getArgumentAttrs (ThreadInfo ti) {
    MethodInfo callee = getInvokedMethod(ti);
    StackFrame frame = getCallerFrame(ti, callee);

    assert frame != null : "can't find caller stackframe for: " + this;
    return frame.getArgumentAttrs(callee);
  }
  
  
  /**
   * check if there is any argument attr of the specified type
   * (use this before using any of the more expensive retrievers)
   */
  public boolean hasArgumentAttr (ThreadInfo ti, Class<?> type){
    MethodInfo callee = getInvokedMethod(ti);
    StackFrame frame = getCallerFrame(ti, callee);

    assert frame != null : "no caller stackframe for: " + this;
    return frame.hasArgumentAttr(callee,type);
  }

  /**
   * do we have a reference argument that has an object attribute?
   * less efficient, but still without object creation
   */
  public boolean hasArgumentObjectAttr (ThreadInfo ti, Class<?> type){
    MethodInfo callee = getInvokedMethod(ti);
    StackFrame frame = getCallerFrame(ti, callee);

    assert frame != null : "no caller stackframe for: " + this;
    return frame.hasArgumentObjectAttr(ti,callee,type);
  }

  /**
   * this is slot size, i.e. includes 'this' for InstanceInvocations 
   */
  abstract public int getArgSize();
  
  public int getReturnType() {
    return Types.getReturnBuiltinType(signature);
  }

  public boolean isReferenceReturnType() {
    int r = Types.getReturnBuiltinType(signature);
    return ((r == Types.T_REFERENCE) || (r == Types.T_ARRAY));
  }

  public String getReturnTypeName() {
    return Types.getReturnTypeName(signature);
  }

  public Object getFieldOrArgumentValue (String id, ThreadInfo ti){
    Object v = null;

    if ((v = getArgumentValue(id,ti)) == null){
      v = getFieldValue(id, ti);
    }

    return v;
  }

  public abstract Object getFieldValue (String id, ThreadInfo ti);


  /**
   * <2do> - this relies on same order of arguments and LocalVariableTable entries, which
   * seems to hold for javac, but is not required by the VM spec, which only
   * says that arguments are stored in consecutive slots starting at 0
   */
  public Object getArgumentValue (String id, ThreadInfo ti){
    MethodInfo mi = getInvokedMethod();
    LocalVarInfo localVars[] = mi.getLocalVars();
    Object[] args = getArgumentValues(ti);

    if (localVars != null){
      int j = mi.isStatic() ? 0 : 1;

      for (int i=0; i<args.length; i++, j++){
        Object a = args[i];
        if (id.equals(localVars[j].getName())){
          return a;
        }
      }
    }

    return null;
  }
    
  @Override
  public void accept(JVMInstructionVisitor insVisitor) {
	  insVisitor.visit(this);
  }

  @Override
  public Instruction typeSafeClone(MethodInfo mi) {
    JVMInvokeInstruction clone = null;

    try {
      clone = (JVMInvokeInstruction) super.clone();

      // reset the method that this insn belongs to
      clone.mi = mi;

      clone.invokedMethod = null;
    } catch (CloneNotSupportedException e) {
      e.printStackTrace();
    }

    return clone;
  }
}
