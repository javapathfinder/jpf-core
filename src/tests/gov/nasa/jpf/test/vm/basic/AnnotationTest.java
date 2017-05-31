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
package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.jvm.bytecode.GETFIELD;
import gov.nasa.jpf.jvm.bytecode.JVMInvokeInstruction;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.AnnotationInfo;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.MethodInfo;

import java.lang.annotation.Annotation;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;

import org.junit.Test;


public class AnnotationTest extends TestJPF {

  @Test //----------------------------------------------------------------------
  @A1("foo")
  public void testStringValueOk () {
    if (verifyNoPropertyViolation()) {
      try {
        java.lang.reflect.Method method =
                AnnotationTest.class.getMethod("testStringValueOk");
        A1 annotation = method.getAnnotation(A1.class);

        assert ("foo".equals(annotation.value()));
        
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A1 {
    String value();
  }


  @Test //----------------------------------------------------------------------
  @A2({"foo", "boo"})
  public void testStringArrayValueOk () {
    if (verifyNoPropertyViolation()) {
      try {
        java.lang.reflect.Method method =
                AnnotationTest.class.getMethod("testStringArrayValueOk");
        A2 annotation = method.getAnnotation(A2.class);

        Object v = annotation.value();
        assert v instanceof String[];

        String[] a = (String[])v;
        assert a.length == 2;

        assert "foo".equals(a[0]);
        assert "boo".equals(a[1]);

      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A2 {
    String[] value();
  }

  @Test //----------------------------------------------------------------------
  @A3(Long.MAX_VALUE)
  public void testLongValueOk () {
    if (verifyNoPropertyViolation()) {
      try {
        java.lang.reflect.Method method =
                AnnotationTest.class.getMethod("testLongValueOk");
        A3 annotation = method.getAnnotation(A3.class);

        assert (annotation.value() == Long.MAX_VALUE);
      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A3 {
    long value();
  }


  @Test //----------------------------------------------------------------------
  @A4(a="one",b=42.0)
  public void testNamedParamsOk () {
    if (verifyNoPropertyViolation()) {
      try {
        java.lang.reflect.Method method =
                AnnotationTest.class.getMethod("testNamedParamsOk");
        A4 annotation = method.getAnnotation(A4.class);

        assert ("one".equals(annotation.a()));
        assert ( 42.0 == annotation.b());

        System.out.println(annotation);

      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A4 {
    String a();
    double b();
  }


  @Test //----------------------------------------------------------------------
  @A5(b="foo")
  public void testPartialDefaultParamsOk () {
    if (verifyNoPropertyViolation()) {
      try {
        java.lang.reflect.Method method =
                AnnotationTest.class.getMethod("testPartialDefaultParamsOk");
        A5 annotation = method.getAnnotation(A5.class);

        assert ("whatever".equals(annotation.a()));

        System.out.println(annotation);

      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A5 {
    String a() default "whatever";
    String b();
  }

  @Test //----------------------------------------------------------------------
  @A6
  public void testSingleDefaultParamOk () {
    if (verifyNoPropertyViolation()) {
      try {
        java.lang.reflect.Method method =
                AnnotationTest.class.getMethod("testSingleDefaultParamOk");
        A6 annotation = method.getAnnotation(A6.class);
        
        assert ("whatever".equals(annotation.value()));

        System.out.println(annotation);

      } catch (SecurityException e) {
        e.printStackTrace();
      } catch (NoSuchMethodException e) {
        e.printStackTrace();
      }
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A6 {
    String value() default "whatever";
  }
  
  @A6
  @Test
  public void testAnnotationClass() throws ClassNotFoundException, NoSuchMethodException {
    if (verifyNoPropertyViolation()) { 
      Class clazz = Class.forName("gov.nasa.jpf.test.vm.basic.AnnotationTest");
      Method method = clazz.getDeclaredMethod("testAnnotationClass");
      Annotation annotations[] = method.getAnnotations();
      
      for (int i=0; i<annotations.length; i++){
        System.out.printf("  a[%d] = %s\n", i, annotations[i].toString());
      }
      
      assertEquals(2, annotations.length);
      assertNotNull(annotations[0]);
      assertNotNull(annotations[1]);      

      assertTrue(annotations[0] instanceof A6);
      assertTrue(annotations[1] instanceof Test);
    }
  }

  //--------------------------------------------------------------------

  public enum MyEnum {
    ONE, TWO
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A7 {
    MyEnum value();
  }

  @Test
  @A7(MyEnum.ONE)
  public void testEnumValue() throws ClassNotFoundException, NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Class clazz = Class.forName("gov.nasa.jpf.test.vm.basic.AnnotationTest");  // Any class outside of this file will do.
      Method method = clazz.getDeclaredMethod("testEnumValue");               // Any method with an annotation will do.
      Annotation annotations[] = method.getAnnotations();

      assertEquals(2, annotations.length);
      assertNotNull(annotations[1]);

      assertTrue(annotations[1] instanceof A7);
      A7 ann = (A7)annotations[1];
      assertTrue( ann.value() == MyEnum.ONE);
    }
  }

  //--------------------------------------------------------------------

  @Retention(RetentionPolicy.RUNTIME)
  @interface A8 {
    Class value();
  }

  @Test
  @A8(AnnotationTest.class)
  public void testClassValue() throws ClassNotFoundException, NoSuchMethodException {
    if (verifyNoPropertyViolation()){
      Class clazz = Class.forName("gov.nasa.jpf.test.vm.basic.AnnotationTest");  // Any class outside of this file will do.
      Method method = clazz.getDeclaredMethod("testClassValue");               // Any method with an annotation will do.
      Annotation annotations[] = method.getAnnotations();

      assertEquals(2, annotations.length);
      assertNotNull(annotations[1]);

      assertTrue(annotations[1] instanceof A8);
      A8 ann = (A8)annotations[1];
      assertTrue( ann.value() == AnnotationTest.class);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @interface A11 {
      Class<?>[] value();
  }

  @Test
  @A11({ AnnotationTest.class, Class.class })
  public void testClassArrayValueOk() throws ClassNotFoundException, SecurityException, NoSuchMethodException {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = Class.forName(AnnotationTest.class.getName());
      Method method = clazz.getDeclaredMethod("testClassArrayValueOk");
      Annotation[] annotations = method.getAnnotations();
      assertEquals(2, annotations.length);
      assertNotNull(annotations[1]);

      assertTrue(annotations[1] instanceof A11);
      A11 ann = (A11) annotations[1];
      assertTrue(ann.value()[0] == AnnotationTest.class);
      assertTrue(ann.value()[1] == Class.class);
    }
  }

  //-------------------------------------------------------------------
  static class MyClass {
    @A1("the answer")
    int data = 42;
  }
  
  public static class DataListener extends ListenerAdapter {

    @Override
    public void executeInstruction(VM vm, ThreadInfo ti, Instruction insnToExecute){
      if (insnToExecute instanceof GETFIELD){
        FieldInfo fi = ((GETFIELD)insnToExecute).getFieldInfo();
        if (fi.getName().equals("data")){
          AnnotationInfo ai = fi.getAnnotation("gov.nasa.jpf.test.vm.basic.AnnotationTest$A1");
          System.out.println("annotation for " + fi.getFullName() + " = " + ai);
          
          if (ai != null){
            String val = ai.getValueAsString("value");
            System.out.println("   value = " + val);
            
            if (val == null || !val.equals("the answer")){
              fail("wrong @A1 value = " + val);
            }
          } else {
            fail("no @A1 annotation for field " + fi.getFullName());
          }
        }
      }
    }
  }
  
  @Test
  public void testFieldAnnotation(){
    if (verifyNoPropertyViolation("+listener=.test.vm.basic.AnnotationTest$DataListener")){
      MyClass obj = new MyClass();
      int d = obj.data;
    }
  }
  
  //-------------------------------------------------------------------
  public static class ArgListener extends ListenerAdapter {

    @Override
    public void executeInstruction (VM vm, ThreadInfo ti, Instruction insnToExecute){
      if (insnToExecute instanceof JVMInvokeInstruction){
        MethodInfo mi = ((JVMInvokeInstruction)insnToExecute).getInvokedMethod();
        if (mi.getName().equals("foo")){
          System.out.println("-- called method: " + mi.getUniqueName());
          
          AnnotationInfo[][] pai = mi.getParameterAnnotations();
          
          assert pai != null : "no parameter annotations found";
          assert pai.length == 2 : "wrong number of parameter annotation arrays: " + pai.length;
          assert pai[0] != null : "no parameter annotation for first argument found";
          assert pai[0].length == 1 : "wrong number of annotations for first argument: "+ pai[0].length;
          assert pai[1] != null : "no parameter annotation for second argument found";
          assert pai[1].length == 0 : "wrong number of annotations for first argument: "+ pai[1].length;
          
          for (int i=0; i<pai.length; i++){
            System.out.println("-- annotations for parameter: " + i);
            AnnotationInfo[] ai = pai[i];
            if (ai != null && ai.length > 0) {
              for (int j = 0; j < ai.length; j++) {
                assert (ai[i] != null) : "null annotation for paramter: " + j;
                System.out.println(ai[i].asString());
              }
            } else {
              System.out.println("none");
            }
          }
        }
      }
    }
  }
  
  public void foo (@A1("arghh") MyClass x, String s){
    // nothing
  }
  
  @Test
  public void testParameterAnnotation(){
    if (verifyNoPropertyViolation("+listener=.test.vm.basic.AnnotationTest$ArgListener")){
      MyClass obj = new MyClass();
      foo( obj, "blah");
    }        
  }
  
  //---------------------------------------------------------------
  
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  public @interface A9 {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A10 {
  }
  
  @A9()
  public static class Parent {
  }

  @A10
  public static class Child1 extends Parent {
  }

  public static class Child2 extends Child1 {
  }
 
  @Test
  public void getAnnotationsTest () {
    if (verifyNoPropertyViolation()) {      
      assertTrue(Parent.class.getAnnotations().length == 1);
      assertTrue(Child1.class.getAnnotations().length == 2);
      assertTrue(Child2.class.getAnnotations().length == 1);
    }
  }
  

  //---------------------------------------------------------------
  // test for RuntimeVisibleAnnotations attributes that in turn have
  // element_value entries
  @Retention(RetentionPolicy.RUNTIME)
  @interface A12 { // this one has the string value
    String value();
  }

  @Retention(RetentionPolicy.RUNTIME)
  @A12("Whatever")
  @interface A13 {
    // this one has a RuntimeVisibleAnnotation attribute for A11 with a
    // String entry value
  }

  @A13 // causes loading of @C
  @Test
  public void testRecursiveRuntimeVisibleAnnotationValue(){
    if (verifyNoPropertyViolation()){
      // nothing to do other than just causing the loading of A12
    }
  }
  
  
  //---------------------------------------------------------------
  // test of char annotations
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A14 {
    char value();
  }
  
  @Test
  @A14('x')
  public void testCharAnnotation(){
    if (verifyNoPropertyViolation()){
      try {
        Class<?> clazz = Class.forName(AnnotationTest.class.getName());
        Method method = clazz.getDeclaredMethod("testCharAnnotation");
        Annotation[] annotations = method.getAnnotations();
        assertEquals(2, annotations.length);
        assertNotNull(annotations[1]);

        assertTrue(annotations[1] instanceof A14);
        A14 ann = (A14) annotations[1];
        assertTrue(ann.value() == 'x');
        
      } catch (Throwable t){
        t.printStackTrace();
        fail("unexpected exception: " + t);
      }
    }
  }
  
  //---------------------------------------------------------------
  // test of char annotations
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A15 {
    float value();
  }
  
  @Test
  @A15(12.34f)
  public void testFloatAnnotation(){
    if (verifyNoPropertyViolation()){
      try {
        Class<?> clazz = Class.forName(AnnotationTest.class.getName());
        Method method = clazz.getDeclaredMethod("testFloatAnnotation");
        Annotation[] annotations = method.getAnnotations();
        assertEquals(2, annotations.length);
        assertNotNull(annotations[1]);

        assertTrue(annotations[1] instanceof A15);
        A15 ann = (A15) annotations[1];
        assertTrue(Math.abs(ann.value() - 12.34f) < 0.00001);
        
      } catch (Throwable t){
        t.printStackTrace();
        fail("unexpected exception: " + t);
      }
    }
  }

  // test of char annotations
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A16 {
    double value();
  }
  
  @Test
  @A16(Double.MAX_VALUE)
  public void testDoubleAnnotation(){
    if (verifyNoPropertyViolation()){
      try {
        Class<?> clazz = Class.forName(AnnotationTest.class.getName());
        Method method = clazz.getDeclaredMethod("testDoubleAnnotation");
        Annotation[] annotations = method.getAnnotations();
        assertEquals(2, annotations.length);
        assertNotNull(annotations[1]);

        assertTrue(annotations[1] instanceof A16);
        A16 ann = (A16) annotations[1];
        assertTrue(ann.value() == Double.MAX_VALUE);
        
      } catch (Throwable t){
        t.printStackTrace();
        fail("unexpected exception: " + t);
      }
    }
  }

  // test of char annotations
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A17 {
    long value();
  }
  
  @Test
  @A17(Long.MAX_VALUE)
  public void testLongAnnotation(){
    if (verifyNoPropertyViolation()){
      try {
        Class<?> clazz = Class.forName(AnnotationTest.class.getName());
        Method method = clazz.getDeclaredMethod("testLongAnnotation");
        Annotation[] annotations = method.getAnnotations();
        assertEquals(2, annotations.length);
        assertNotNull(annotations[1]);

        assertTrue(annotations[1] instanceof A17);
        A17 ann = (A17) annotations[1];
        assertTrue(ann.value() == Long.MAX_VALUE);
        
      } catch (Throwable t){
        t.printStackTrace();
        fail("unexpected exception: " + t);
      }
    }
  }

}
