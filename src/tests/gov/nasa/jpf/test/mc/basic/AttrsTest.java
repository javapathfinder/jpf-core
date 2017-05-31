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
import gov.nasa.jpf.jvm.bytecode.DSTORE;
import gov.nasa.jpf.jvm.bytecode.INVOKEVIRTUAL;
import gov.nasa.jpf.jvm.bytecode.ISTORE;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.jvm.bytecode.LRETURN;
import gov.nasa.jpf.util.ObjectList;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 * raw test for field/operand/local attribute handling
 */
public class AttrsTest extends TestJPF {

//------------ this part we only need outside of JPF execution
  static class AttrType {
    @Override
	public String toString() {
      return "<an AttrType>";
    }
  }
  static final AttrType ATTR = new AttrType();
  static final Class<?> ATTR_CLASS = ATTR.getClass();

  public static class IntListener extends ListenerAdapter {

    public IntListener () {}

    @Override
    public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
      MethodInfo mi = executedInsn.getMethodInfo();

      // not very efficient, but who cares - it's a small test
      if (executedInsn instanceof ISTORE){
        if (mi.getName().equals("testIntPropagation")){
          ISTORE istore = (ISTORE)executedInsn;
          String localName = istore.getLocalVariableName();
          int localIndex = istore.getLocalVariableIndex();

          if (localName.equals("i")){
            StackFrame frame = ti.getModifiableTopFrame();
            frame.setLocalAttr(localIndex, ATTR);
            
            Object a = frame.getLocalAttr(localIndex, ATTR_CLASS);
            System.out.println("'i' attribute set to: " + a);

          } else if (localName.equals("j")){
            StackFrame frame = ti.getTopFrame();
            
            Object a = frame.getLocalAttr(localIndex, ATTR_CLASS);
            System.out.println("'j' AttrType attribute: " + a);
          }
        }
      }
    }
  }
  
  static int sInt;
  int iInt;

  static double sDouble;
  double iDouble;

  int echoInt (int a){
    return a;
  }

  @Test public void testIntPropagation () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$IntListener")) {
      int i = 42; // this gets attributed
      Verify.setLocalAttribute("i", 42); // this overwrites whatever the ISTORE listener did set on 'i'
      int attr = Verify.getLocalAttribute("i");
      Verify.println("'i' attribute after Verify.setLocalAttribute(\"i\",42): " + attr);
      assertTrue( attr == 42);

      iInt = echoInt(i); // return val -> instance field
      sInt = iInt; // instance field -> static field
      int j = sInt; // static field -> local - now j should have the initial i attribute, and value 42
      
      attr = Verify.getLocalAttribute("j");
      Verify.println("'j' attribute after assignment: " + attr);
      assertTrue( attr == 42);
    }
  }
  
  //----------------------------------------------------------------------------------------------
  
  public static class DoubleListener extends ListenerAdapter {

    public DoubleListener () {}

    @Override
    public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
      MethodInfo mi = executedInsn.getMethodInfo();

      if (executedInsn instanceof DSTORE){
        if (mi.getName().equals("testDoublePropagation")){
          DSTORE dstore = (DSTORE)executedInsn;
          String localName = dstore.getLocalVariableName();
          int localIndex = dstore.getLocalVariableIndex();

          if (localName.equals("d")){
            StackFrame frame = ti.getModifiableTopFrame();

            System.out.print("listener setting 'd' attr = ");
            frame.setLocalAttr(localIndex, ATTR);
            Object a = frame.getLocalAttr(localIndex);
            System.out.println( a);

          } else if (localName.equals("r")){
            StackFrame frame = ti.getTopFrame();
            Object a = frame.getLocalAttr(localIndex, ATTR_CLASS);
            System.out.println("'r' attribute: " + a);
            
            /** get's overwritten in the model class
            if (a != ATTR){
              throw new JPFException("attribute propagation failed");
            }
            **/
          }
        }

      }
    }
  }

  @Test public void testDoublePropagation () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$DoubleListener")) {
      double d = 42.0; // this gets attributed
      Verify.setLocalAttribute("d", 42);  // this overwrites whatever the DSTORE listener did set on 'd'
      int attr = Verify.getLocalAttribute("d");
      assert attr == 42;

      // some noise on the stack
      iDouble = echoDouble(d);
      sDouble = iDouble;

      //double r = sDouble; // now r should have the same attribute
      double r = echoDouble(d);

      attr = Verify.getLocalAttribute("r");
      Verify.print("@ 'r' attribute after assignment: " + attr);
      Verify.println();

      assert attr == 42;
    }
  }

  
  //-----------------------------------------------------------------------------------------------

  public static class InvokeListener extends ListenerAdapter {

    @Override
    public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){
      if (executedInsn instanceof JVMInvokeInstruction) {
        JVMInvokeInstruction call = (JVMInvokeInstruction)executedInsn;
        MethodInfo mi = call.getInvokedMethod();
        String mName = mi.getName();
        if (mName.equals("goModel") || mName.equals("goNative")) {
          Object[] a = call.getArgumentAttrs(ti);
          assert a != null & a.length == 3;

          System.out.println("listener notified of: " + mName + "(), attributes= "
                             + a[0] + ',' + a[1] + ',' + a[2]);

          // note - this is only acceptable if we know exactly there are just
          // single attrs
          
          assert a[0] instanceof Integer && a[1] instanceof Integer;
          assert (((Integer)a[0]).intValue() == 1) &&
                 (((Integer)a[1]).intValue() == 2) &&
                 (((Integer)a[2]).intValue() == 3);
        }
      }
    }
  }
  
  @Test public void testInvokeListener () {
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$InvokeListener")) {
      Verify.setLocalAttribute("this", 1);

      double d = 42.0;
      Verify.setLocalAttribute("d", 2);

      int i = 42;
      Verify.setLocalAttribute("i", 3);

      double result = goNative(d, i); // that's going to be listened on
      int attr = Verify.getLocalAttribute("result");

      Verify.print("@ 'result' attribute: " + attr);
      Verify.println();

      assert attr == 6;

      int r = goModel(d, i);  // that's listened for, too
      assert r == 6;
    }
  }


  native double goNative (double d, int i);

  @Test public void testNativeMethod () {
    if (verifyNoPropertyViolation()) {
      Verify.setLocalAttribute("this", 1);

      double d = 42.0;
      Verify.setLocalAttribute("d", 2);

      int i = 42;
      Verify.setLocalAttribute("i", 3);

      double result = goNative(d, i);
      int attr = Verify.getLocalAttribute("result");

      Verify.print("@ 'result' attribute: " + attr);
      Verify.println();

      assert attr == 6;
    }
  }


  int goModel (double d, int i) {
    int a1 = Verify.getLocalAttribute("d");
    int a2 = Verify.getLocalAttribute("i");

    return a1*a2;
  }

  double echoDouble (double d){
    return d;
  }


  @Test public void testExplicitRef () {
    if (verifyNoPropertyViolation()) {
      int attr = Verify.getFieldAttribute(this, "iDouble");
      Verify.print("@ 'iDouble' attribute before set: ", Integer.toString(attr));
      Verify.println();

      Verify.setFieldAttribute(this, "iDouble", 42);

      attr = Verify.getFieldAttribute(this, "iDouble");
      Verify.print("@ 'iDouble' attribute after set: ", Integer.toString(attr));
      Verify.println();

      assert attr == 42;
    }
  }

  @Test public void testExplicitArrayRef () {
    if (verifyNoPropertyViolation()) {
      int attr;
      double[] myArray = new double[10];

      attr = Verify.getElementAttribute(myArray, 5);
      Verify.print("@ 'myArray[5]' attribute before set: ", Integer.toString(attr));
      Verify.println();

      Verify.setElementAttribute(myArray, 5, 42);

      attr = Verify.getElementAttribute(myArray, 5);
      Verify.print("@ 'myArray[5]' attribute after set: ", Integer.toString(attr));
      Verify.println();

      assert attr == 42;
    }
  }

  @Test public void testArraycopy () {
    if (verifyNoPropertyViolation()) {
      int attr;
      double[] a1 = new double[10];
      double[] a2 = new double[10];

      Verify.setElementAttribute(a1, 3, 42);
      System.arraycopy(a1, 1, a2, 0, 3);

      attr = Verify.getElementAttribute(a2, 2);
      assert attr == 42;
    }
  }

  double ddd;

  @Test public void testArrayPropagation() {
    if (verifyNoPropertyViolation()) {

      int attr;
      double[] a1 = new double[10];
      double[] a2 = new double[10];

      Verify.setElementAttribute(a1, 3, 42);

      //attr = Verify.getElementAttribute(a1,3);
      //System.out.println(attr);

      ddd = a1[3];
      //Verify.setFieldAttribute(this,"ddd",42);
      //attr = Verify.getFieldAttribute(this,"ddd");
      //System.out.println("@ ddd : " + attr);

      double d = ddd;
      //ccc = d;
      //attr = Verify.getFieldAttribute(this,"ccc");
      //System.out.println("ccc ; " + attr);

      //double d = a1[3]; // now d should have the attr
      a2[0] = d;
      attr = Verify.getElementAttribute(a2, 0);
      System.out.println("@ a2[0] : " + attr);

      assert attr == 42;
    }
  }

  @Test public void testBacktrack() {
    if (verifyNoPropertyViolation()) {
      int v = 42; // need to init or the compiler does not add it to the name table
      Verify.setLocalAttribute("v", 42);

      boolean b = Verify.getBoolean(); // restore point
      System.out.println(b);

      int attr = Verify.getLocalAttribute("v");
      System.out.println(attr);

      Verify.setLocalAttribute("v", -1);
      attr = Verify.getLocalAttribute("v");
      System.out.println(attr);
    }
  }
  
  @Test public void testInteger() {
    if (verifyNoPropertyViolation()) {
      int v = 42;
      Verify.setLocalAttribute("v", 4200);

      // explicit
      Integer o = new Integer(v);
      int j = o.intValue();
      int attr = Verify.getLocalAttribute("j");
      assert attr == 4200;

      // semi autoboxed
      j = o;
      boolean b = Verify.getBoolean(); // just cause some backtracking damage
      attr = Verify.getLocalAttribute("j");
      assert attr == 4200;

    /** this does not work because of cached, preallocated Integer objects)
    // fully autoboxed
    Integer a = v;
    j = a;
    attr = Verify.getLocalAttribute("j");
    assert attr == 4200;
     **/
    }
  }
  
  @Test public void testObjectAttr(){

    if (verifyNoPropertyViolation()){
      Integer o = new Integer(41);
      Verify.setObjectAttribute(o, 42);

      boolean b = Verify.getBoolean();

      int attr = Verify.getObjectAttribute(o);
      System.out.println("object attr = " + attr);
      assert attr == 42;
    }
  }
  
  //--- the multiple attributes tests
  
  @Test
  public void testIntAttrList(){
    if (verifyNoPropertyViolation()){
      int var = 42;
      Verify.addLocalAttribute("var", Integer.valueOf(var));
      Verify.addLocalAttribute("var", Integer.valueOf(-var));
      
      int x = var;
      int[] attrs = Verify.getLocalAttributes("x");
      for (int i=0; i<attrs.length; i++){
        System.out.printf("[%d] = %d\n", i, attrs[i]);
      }
      
      assertTrue( attrs.length == 2);
      assertTrue( attrs[0] == -var); // lifo
      assertTrue( attrs[1] == var);
    }
  }
  
  
  public static class MixedAttrTypeListener extends ListenerAdapter {
    
    public MixedAttrTypeListener() {}
    
    @Override
    public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute){
      
      if (insnToExecute instanceof INVOKEVIRTUAL){
        MethodInfo callee = ((INVOKEVIRTUAL)insnToExecute).getInvokedMethod();
        if (callee.getUniqueName().equals("foo(J)J")){
          System.out.println("--- pre-exec foo() invoke interception, setting arg attrs");
          
          StackFrame frame = ti.getModifiableTopFrame();
          
          // we are still in the caller stackframe
          frame.addLongOperandAttr("foo-arg");
          
          Long v = Long.valueOf( frame.peekLong());
          frame.addLongOperandAttr( v);
          
          System.out.println("   operand attrs:");
          for (Object a: frame.longOperandAttrIterator()){
            System.out.println(a);
          }
        }
        
      } else if (insnToExecute instanceof LRETURN){
        MethodInfo mi = insnToExecute.getMethodInfo();
        if (mi.getUniqueName().equals("foo(J)J")){
          System.out.println("--- pre-exec foo() return interception");
          StackFrame frame = ti.getModifiableTopFrame();
          int varIdx = frame.getLocalVariableSlotIndex("x");
          Object attr = frame.getLocalAttr(varIdx);

          System.out.println("  got 'x' attributes");
          for (Object a: frame.localAttrIterator(varIdx)){
            System.out.println(a);
          }                  
          
          assertTrue(attr.equals("foo-arg"));
          
          System.out.println("  setting lreturn operand attrs");
          frame.addLongOperandAttr("returned");
          
          for (Object a: frame.longOperandAttrIterator()){
            System.out.println(a);
          }          
        }
      }
    }
    
    @Override
    public void instructionExecuted (VM vm, ThreadInfo ti, Instruction nextInsn, Instruction executedInsn){

      if (executedInsn instanceof INVOKEVIRTUAL){
        MethodInfo callee = ((INVOKEVIRTUAL)executedInsn).getInvokedMethod();
        if (callee.getUniqueName().equals("foo(J)J")){
          System.out.println("--- post-exec foo() invoke interception");
 
          StackFrame frame = ti.getModifiableTopFrame(); // we are now in the callee
          int varIdx = frame.getLocalVariableSlotIndex("x");

          for (Object a: frame.localAttrIterator(varIdx)){
            System.out.println(a);
          }
          
          Object attrs = frame.getLocalAttr(varIdx);
          assertTrue( ObjectList.size(attrs) == 2);
          
          Object sAttr = frame.getLocalAttr(varIdx, String.class);
          assertTrue( sAttr != null && sAttr.equals("foo-arg"));
          assertTrue( frame.getNextLocalAttr(varIdx, String.class, sAttr) == null);
          
          Object lAttr = frame.getLocalAttr(varIdx, Long.class);
          assertTrue( lAttr != null && lAttr.equals( frame.getLongLocalVariable(varIdx)));
          assertTrue( frame.getNextLocalAttr(varIdx, Long.class, lAttr) == null);
          
          frame.removeLocalAttr(varIdx, lAttr);
          System.out.println("  removing " + lAttr);
          for (Object a: frame.localAttrIterator(varIdx)){
            System.out.println(a);
          }
        }
        
      } else if (executedInsn instanceof LRETURN){
        MethodInfo mi = executedInsn.getMethodInfo();
        if (mi.getUniqueName().equals("foo(J)J")){
          StackFrame frame = ti.getTopFrame();
          
          System.out.println("--- post-exec foo() return interception");
          for (Object a: frame.longOperandAttrIterator()){
            System.out.println(a);
          }
          
          String a = frame.getLongOperandAttr(String.class);
          assertTrue( a.equals("returned"));
          
          a = frame.getNextLongOperandAttr(String.class, a);
          assertTrue( a.equals("foo-arg"));
          
          a = frame.getNextLongOperandAttr(String.class, a);
          assertTrue(a == null);
        }        
      }
    }
  }
    
  long foo (long x){
    return x;
  }
  
  @Test
  public void testListenerMixedLongAttrLists(){
    if (verifyNoPropertyViolation("+listener=.test.mc.basic.AttrsTest$MixedAttrTypeListener")){
      foo(42);
    }
  }
}
