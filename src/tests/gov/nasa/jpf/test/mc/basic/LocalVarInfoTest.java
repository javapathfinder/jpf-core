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
import gov.nasa.jpf.jvm.bytecode.JVMLocalVariableInstruction;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;

import java.util.ArrayList;

import org.junit.Test;


/**
 * regression test for LocalVarInfo lookup
 */
public class LocalVarInfoTest extends TestJPF {
  
  
  public static class TestLookupListener extends ListenerAdapter{
    
    MethodInfo logMethod = null;
    static ArrayList<String> log;

    public TestLookupListener(){
      log = new ArrayList<String>();
    }

    @Override
    public void methodEntered (VM vm, ThreadInfo ti, MethodInfo mi){
      if (mi.getUniqueName().equals("testLookup()V")){
        logMethod = mi;
        System.out.println("---- " + mi.getUniqueName() + " entered");
        System.out.println(" LocalVarInfos (should have {'this', 'x', 'y'} : ");
        LocalVarInfo[] lvs = mi.getLocalVars(); 
        for (LocalVarInfo lv : lvs){
          System.out.println("    " + lv);
        }
        System.out.println();
        
        assertTrue( lvs.length == 3);
      }
    }

    @Override
    public void methodExited (VM vm, ThreadInfo ti, MethodInfo mi){
      if (mi == logMethod){
        logMethod = null;
      }      
    }

    @Override
    public void instructionExecuted(VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
      if (executedInsn.getMethodInfo() == logMethod){
        System.out.printf(" %2d: %s", executedInsn.getPosition(), executedInsn);
        if (executedInsn instanceof JVMLocalVariableInstruction){
          JVMLocalVariableInstruction lvinsn = (JVMLocalVariableInstruction)executedInsn;
          LocalVarInfo lv = lvinsn.getLocalVarInfo(); 
          System.out.print(" : " + lv);

          log.add( executedInsn.getClass().getSimpleName() + " " + lv.getName());
        }
        System.out.println();
      }
    }
  }
  
  static String[] expected = {
    "ALOAD this",
    "ISTORE x",
    "ILOAD x",
    "ISTORE y",
    "ILOAD y"
  };
  
  @Test
  public void testLookup (){
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.LocalVarInfoTest$TestLookupListener")){
      // DON'T CHANGE THIS CODE!
      // this should be a sequence of 
      
      //  aload this
      //  ..
      //  istore x
      //  iload x 
      //  istore y
      //  ..
      //  iload y
      //  ..
      
      int x = 42;
      int y = x;
      System.out.println(y);
    }
    
    if (!isJPFRun()){
      checkLog();
    }
  }
  
  private void checkLog(){
    System.out.println("--- local var access log: ");
    int i = 0;
    assertTrue(TestLookupListener.log.size() == expected.length);
    for (String s : TestLookupListener.log) {
      System.out.println(s);
      assertTrue(s.equals(expected[i++]));
    }
  }
}
