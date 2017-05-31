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

package gov.nasa.jpf.test.basic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

import org.junit.Test;

/**
 * basic test for InstructionFactories
 */
public class InstructionFactoryTest extends TestJPF {

  /**
   * Add double
   * ..., value1, value2 => ..., result
   */
  public static class DADD extends gov.nasa.jpf.jvm.bytecode.DADD {
    @Override
    public Instruction execute (ThreadInfo ti) {
      StackFrame frame = ti.getModifiableTopFrame();
      
      double v1 = frame.popDouble();
      double v2 = frame.popDouble();
      
      double r = v1 + v2;
      System.out.printf("DADD %f + %f => %f\n", v1, v2, r);
      
      System.out.println(" ..but we negate it just for kicks..");
      r = -r;

      frame.pushDouble(r);

      return getNext(ti);
    }
  }
  
  public static class MyInsnFactory extends gov.nasa.jpf.jvm.bytecode.InstructionFactory {
    public  MyInsnFactory (Config conf){
      // nothing here
    }
    
    @Override
    public Instruction dadd() {
      return new DADD();
    }
  }
  
  @Test
  public void testDadd() {
    if (verifyNoPropertyViolation("+jvm.insn_factory.class=.test.basic.InstructionFactoryTest$MyInsnFactory")) {
      double a = 41.0;
      double b = a + 1.0;
      System.out.print("b=");
      System.out.println(b);
      assertTrue( b < 0); // since we used our own (twisted) DADD
    }
  }
}
