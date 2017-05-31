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

package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.INVOKESTATIC;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.VirtualInvocation;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ElementInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.ThreadInfo;

import org.junit.Test;


/**
 * testing various aspects of listeners on INVOKE instructions
 */
public class InvokeListenerTest extends TestJPF {

  //--- this is only used outside JPF execution
  public static class Listener extends ListenerAdapter {

    void checkArgs (ThreadInfo ti, Instruction insn, boolean isPostExec){
      if (insn instanceof JVMInvokeInstruction){
        JVMInvokeInstruction call = (JVMInvokeInstruction)insn;
        MethodInfo mi = call.getInvokedMethod(ti);
        String miSignature = mi.getUniqueName();
        String mname = mi.getName();

        if (miSignature.equals("testInstanceMethod(DI)D")){
          Object[] args = call.getArgumentValues(ti);
          ElementInfo ei = getTarget(ti,call);
          log(mname, ei, args, isPostExec);
          assert ((Double)args[0]) == 42.0;
          assert ((Integer)args[1]) == 1;

        } else if (miSignature.equals("testStaticMethod(I)I")){
          Object[] args = call.getArgumentValues(ti);
          ElementInfo ei = getTarget(ti,call);
          log(mname, ei, args, isPostExec);
          assert ((Integer)args[0]) == 42;

        } else if (miSignature.equals("testNativeInstanceMethod(DI)D")){
          Object[] args = call.getArgumentValues(ti);
          ElementInfo ei = getTarget(ti,call);
          log(mname, ei, args, isPostExec);
          assert ((Double)args[0]) == 42.0;
          assert ((Integer)args[1]) == 1;

        }
      }
    }

    ElementInfo getTarget (ThreadInfo ti, JVMInvokeInstruction call){
      if (call instanceof VirtualInvocation){
        int objRef = ((VirtualInvocation)call).getCalleeThis(ti);
        return ti.getElementInfo(objRef);
      } else if (call instanceof INVOKESTATIC){
        return ((INVOKESTATIC)call).getInvokedMethod().getClassInfo().getStaticElementInfo();
      } else {
        return null;
      }
    }

    void log (String mname, ElementInfo ei, Object[] args, boolean isPostExec){
      System.out.print(isPostExec ? "# instructionExecuted: " : "# executeInstruction: ");

      System.out.print(ei);
      System.out.print('.');
      System.out.print(mname);

      System.out.print(" (");
      for (int i=0; i<args.length; i++) {
        if (i >0) System.out.print(',');
        System.out.print( args[i]);
      }
      System.out.println(")");
    }

    @Override
    public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute){
      checkArgs(ti, insnToExecute, false);
    }

    @Override
    public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
      checkArgs(ti, executedInsn, true);
    }

  }

  double testInstanceMethod (double d, int c){
    return d + c;
  }
  @Test public void testInstanceMethod (){
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.InvokeListenerTest$Listener")){
      testInstanceMethod(42.0, 1);
    }
  }
  
  int testStaticMethod (int a){
    return a + 1;
  }
  @Test public void testStaticMethod (){
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.InvokeListenerTest$Listener")){
      testStaticMethod(42);
    }
  }

  native double testNativeInstanceMethod (double d, int c);
  @Test public void testNativeInstanceMethod (){
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.InvokeListenerTest$Listener")){
      testNativeInstanceMethod(42.0, 1);
    }
  }


}
