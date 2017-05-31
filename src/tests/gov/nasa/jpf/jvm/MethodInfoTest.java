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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassParseException;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.LocalVarInfo;
import gov.nasa.jpf.vm.MethodInfo;

import java.io.File;

import org.junit.Test;

/**
 * unit test for MethodInfos
 */
public class MethodInfoTest extends TestJPF {

  static class MyClass {
    static double staticNoArgs() {int a=42; double b=42.0; b+=a; return b;}
    static double staticInt (int intArg) {int a=42; double b=42.0; b+=a; return b;}
    static double staticIntString (int intArg, String stringArg) {int a=42; double b=42.0; b+=a; return b;}
    
    double instanceNoArgs() {int a=42; double b=42.0; b+=a; return b;}
    double instanceInt( int intArg) {int a=42; double b=42.0; b+=a; return b;}
    double instanceIntString  (int intArg, String stringArg) {int a=42; double b=42.0; b+=a; return b;}
    
    int instanceCycleMethod (int intArg, int int2Arg) {
      for (int i = 0; i < int2Arg; ++i) {
        // it's important to have a for cycle because it breaks the instruction per line monotony
        intArg += intArg;
      }
      return intArg;
    }
  }
  
  @Test
  public void testMethodArgs() {
    File file = new File("build/tests/gov/nasa/jpf/jvm/MethodInfoTest$MyClass.class");

    try {
      ClassInfo ci = new NonResolvedClassInfo( "gov.nasa.jpf.jvm.MethodInfoTest$MyClass",  file);
      MethodInfo mi;
      LocalVarInfo[] args;

      //--- the statics
      mi = ci.getMethod("staticNoArgs", "()D", false);
      System.out.println("-- checking: " + mi);
      args = mi.getArgumentLocalVars();
      assertTrue("args not empty or null", args != null && args.length == 0);

      mi = ci.getMethod("staticInt", "(I)D", false);
      System.out.println("-- checking: " + mi);
      args = mi.getArgumentLocalVars();
      assertTrue("args null", args != null);
      for (LocalVarInfo lvi : args){
        System.out.println("     " + lvi);
      }
      assertTrue(args.length == 1 && args[0].getName().equals("intArg"));

      mi = ci.getMethod("staticIntString", "(ILjava/lang/String;)D", false);
      System.out.println("-- checking: " + mi);
      args = mi.getArgumentLocalVars();
      assertTrue("args null", args != null);
      for (LocalVarInfo lvi : args){
        System.out.println("     " + lvi);
      }
      assertTrue(args.length == 2 && args[0].getName().equals("intArg") && args[1].getName().equals("stringArg"));

      
      //--- the instances
      mi = ci.getMethod("instanceNoArgs", "()D", false);
      System.out.println("-- checking: " + mi);
      args = mi.getArgumentLocalVars();
      assertTrue("args null", args != null);
      for (LocalVarInfo lvi : args){
        System.out.println("     " + lvi);
      }
      assertTrue(args.length == 1 && args[0].getName().equals("this"));
      
      mi = ci.getMethod("instanceInt", "(I)D", false);
      System.out.println("-- checking: " + mi);
      args = mi.getArgumentLocalVars();
      assertTrue("args null", args != null);
      for (LocalVarInfo lvi : args){
        System.out.println("     " + lvi);
      }
      assertTrue(args.length == 2 && args[0].getName().equals("this") && args[1].getName().equals("intArg"));

      mi = ci.getMethod("instanceIntString", "(ILjava/lang/String;)D", false);
      System.out.println("-- checking: " + mi);
      args = mi.getArgumentLocalVars();
      assertTrue("args null", args != null);
      for (LocalVarInfo lvi : args){
        System.out.println("     " + lvi);
      }
      assertTrue(args.length == 3 && args[0].getName().equals("this") 
          && args[1].getName().equals("intArg") && args[2].getName().equals("stringArg"));

    } catch (NullPointerException npe){
      npe.printStackTrace();
      fail("method not found");
    } catch (ClassParseException cfx){
      cfx.printStackTrace();
      fail(cfx.toString());
    }
  }
  
  @Test
  public void testGetInstructionsForLine () {
    File file = new File(
            "build/tests/gov/nasa/jpf/jvm/MethodInfoTest$MyClass.class");
    try {
      ClassInfo ci = new NonResolvedClassInfo("gov.nasa.jpf.jvm.MethodInfoTest$MyClass", file);
      MethodInfo mi = ci.getMethod("instanceCycleMethod", "(II)I", false);

      nextInstruction:
      for (Instruction instruction : mi.getInstructions()) {
        int l = instruction.getLineNumber();
        Instruction[] foundInstructions = mi.getInstructionsForLine(l);
        System.out.printf("%d : %s\n", l, instruction);

        for (int j=0; j<foundInstructions.length; j++){
          if (foundInstructions[j] == instruction){
            continue nextInstruction;
          }
        }
        
        fail("instruction not in list: " + instruction);
      }
    } catch (ClassParseException cfx) {
      cfx.printStackTrace();
      fail(cfx.toString());
    }
  }
}
