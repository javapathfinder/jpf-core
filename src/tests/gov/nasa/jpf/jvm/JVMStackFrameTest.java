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
import gov.nasa.jpf.vm.LocalVarInfo;
import org.junit.Test;


/**
 * unit test for StackFrame operations
 */
public class JVMStackFrameTest extends TestJPF {

  @Test
  public void testDup2_x1() {
    // 1 2 3  => 2 3.1 2 3

    JVMStackFrame frame = new JVMStackFrame(0, 10);

    frame.push(1);
    frame.push(2);
    frame.push(3);
    frame.printOperands(System.out);

    frame.dup2_x1();
    frame.printOperands(System.out);

    assert frame.getTopPos() == 4;
    assert frame.peek(4) == 2;
    assert frame.peek(3) == 3;
    assert frame.peek(2) == 1;
    assert frame.peek(1) == 2;
    assert frame.peek(0) == 3;
  }

  @Test
  public void testDup2_x1_Attrs() {
    // 1 2 3  => 2 3.1 2 3

    JVMStackFrame frame = new JVMStackFrame(0, 10);

    frame.push(1); frame.setOperandAttr("1");
    frame.push(2); frame.setOperandAttr("2");
    frame.push(3); frame.setOperandAttr("3");
    frame.printOperands(System.out);

    frame.dup2_x1();
    frame.printOperands(System.out);

    assert frame.getTopPos() == 4;
    assert frame.peek(4) == 2 && frame.getOperandAttr(4) == "2"; // same const pool string
    assert frame.peek(3) == 3 && frame.getOperandAttr(3) == "3";
    assert frame.peek(2) == 1 && frame.getOperandAttr(2) == "1";
    assert frame.peek(1) == 2 && frame.getOperandAttr(1) == "2";
    assert frame.peek(0) == 3 && frame.getOperandAttr(0) == "3";
  }


  @Test
  public void testDup2_x2() {
    // 1 2 3 4  => 3 4.1 2 3 4

    JVMStackFrame frame = new JVMStackFrame(0, 10);

    frame.push(1);
    frame.push(2);
    frame.push(3);
    frame.push(4);
    frame.printOperands(System.out);

    frame.dup2_x2();
    frame.printOperands(System.out);

    assert frame.getTopPos() == 5;
    assert frame.peek(5) == 3;
    assert frame.peek(4) == 4;
    assert frame.peek(3) == 1;
    assert frame.peek(2) == 2;
    assert frame.peek(1) == 3;
    assert frame.peek(0) == 4;
  }

  @Test
  public void testDup2_x2_Attrs() {
    // 1 2 3 4  => 3 4.1 2 3 4

    JVMStackFrame frame = new JVMStackFrame(0, 10);

    frame.push(1); frame.setOperandAttr("1");
    frame.push(2); frame.setOperandAttr("2");
    frame.push(3); frame.setOperandAttr("3");
    frame.push(4); frame.setOperandAttr("4");
    frame.printOperands(System.out);

    frame.dup2_x2();
    frame.printOperands(System.out);

    assert frame.getTopPos() == 5;
    assert frame.peek(5) == 3 && frame.getOperandAttr(5) == "3";  // same const pool string
    assert frame.peek(4) == 4 && frame.getOperandAttr(4) == "4";
    assert frame.peek(3) == 1 && frame.getOperandAttr(3) == "1";
    assert frame.peek(2) == 2 && frame.getOperandAttr(2) == "2";
    assert frame.peek(1) == 3 && frame.getOperandAttr(1) == "3";
    assert frame.peek(0) == 4 && frame.getOperandAttr(0) == "4";
  }

  @Test
  public void testPushLong() {
    // Push/Pop long value and also  JVMStackFrame.getLocalValueObject

    JVMStackFrame frame = new JVMStackFrame(0, 2);

    long value = 0x123456780ABCDEFL;
    frame.pushLong(value);

    Object obj_Long = frame.getLocalValueObject(new LocalVarInfo("testLong", "J", "J", 0, 0, 0));
    assert obj_Long != null;
    assert obj_Long instanceof Long;

    long result_getLocValObj = (Long) obj_Long;
    long result_popLong = frame.popLong();

    assert result_getLocValObj == value;
    assert result_popLong == value;
  }

  @Test
  public void testPushDouble() {
    // Push/Pop double value and also  JVMStackFrame.getLocalValueObject

    JVMStackFrame frame = new JVMStackFrame(2, 10);
    // Initialize local values and the stack frame
    frame.push(1);
    frame.push(2);
    frame.push(3);

    double value = Math.PI;

    frame.pushDouble(value);

    Object obj_Double = frame.getLocalValueObject(new LocalVarInfo("testDouble", "D", "D", 0, 0, frame.getTopPos() - 1));
    assert obj_Double != null;
    assert obj_Double instanceof Double;

    double result_getLocValObj = (Double) obj_Double;
    double result_popLong = frame.popDouble();

    assert result_getLocValObj == value;
    assert result_popLong == value;

    assert frame.peek(0) == 3;
    assert frame.peek(1) == 2;
    assert frame.peek(2) == 1;
  }

}
