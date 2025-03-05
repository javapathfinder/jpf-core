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

package gov.nasa.jpf.test.java.lang.reflect;

import gov.nasa.jpf.test.java.lang.reflect.other_package.OtherPackageParent;
import gov.nasa.jpf.test.java.lang.reflect.other_package.OtherPackagePublicClass;
import gov.nasa.jpf.util.test.TestJPF;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Array;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

import org.junit.Test;

public class FieldTest extends TestJPF {

  public static final int testField1 = 1;

  public String testField2;

  private static final int privStaticFinalField = 10;
  private final int privFinalField = 20;
  private int privField = 30;

  public static void main (String[] args) throws SecurityException, NoSuchFieldException{
    runTestsOfThisClass(args);
  }

  @Test
  public void equalsTest () throws SecurityException, NoSuchFieldException{
    if (verifyNoPropertyViolation()){
      Field f1 = FieldTest.class.getField("testField1");
      Field f2 = FieldTest.class.getField("testField1");
      Field f3 = FieldTest.class.getField("testField2");
      Field f4 = FieldTest.class.getField("testField2");
      assertTrue(f1.equals(f2));
      assertTrue(f3.equals(f4));
      assertFalse(f1.equals(f3));
    }
  }
  @Test
  public void toStringTest () throws SecurityException, NoSuchFieldException{
    if (verifyNoPropertyViolation()){
      Field f1 = FieldTest.class.getField("testField1");
      System.out.println(f1);
      assertEquals(f1.toString(), "public static final int gov.nasa.jpf.test.java.lang.reflect.FieldTest.testField1");
      Field f2 = FieldTest.class.getField("testField2");
      assertEquals(f2.toString(), "public java.lang.String gov.nasa.jpf.test.java.lang.reflect.FieldTest.testField2");
    }
  }
  @Test
  public void toGenericStringTest() throws NoSuchFieldException {
    if (verifyNoPropertyViolation()){
      Field f1 = FieldTest.class.getField("testField1");
      String expected1 = "public static final int gov.nasa.jpf.test.java.lang.reflect.FieldTest.testField1";
      assertEquals(expected1, f1.toGenericString());

      Field f2 = FieldTest.class.getField("testField2");
      String expected2 = "public java.lang.String gov.nasa.jpf.test.java.lang.reflect.FieldTest.testField2";
      assertEquals(expected2, f2.toGenericString());
    }
  }

  @Test
  public void setFinalFieldErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      FieldTest.class.getDeclaredField("privFinalField").setInt(this, 100);
    }
  }

  @Test
  public void getPublicClassPublicFieldTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyNoPropertyViolation()) {
      assertEquals(10,  OtherPackagePublicClass.class.getField("publicStaticFinalField").get(null));

      OtherPackagePublicClass obj = new OtherPackagePublicClass(42);
      assertEquals(10, OtherPackagePublicClass.class.getField("publicStaticFinalField").get(obj));
      assertEquals(20, OtherPackagePublicClass.class.getField("publicFinalField").get(obj));
      assertEquals(42, OtherPackagePublicClass.class.getField("publicField").get(obj));
    }
  }

  @Test
  public void getOtherPackageProtectedFieldErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      OtherPackagePublicClass obj = new OtherPackagePublicClass(42);
      OtherPackagePublicClass.class.getDeclaredField("protectedField").get(obj);
    }
  }

  @Test
  public void getOtherPackagePackagePrivateFieldErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      OtherPackagePublicClass obj = new OtherPackagePublicClass(42);
      OtherPackagePublicClass.class.getDeclaredField("packagePrivateField").get(obj);
    }
  }

  @Test
  public void getOwnClassFieldTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyNoPropertyViolation()) {
      assertEquals(10,  FieldTest.class.getDeclaredField("privStaticFinalField").get(null));
      assertEquals(10,  FieldTest.class.getDeclaredField("privStaticFinalField").get(this));
      assertEquals(20,  FieldTest.class.getDeclaredField("privFinalField").get(this));
      assertEquals(30,  FieldTest.class.getDeclaredField("privField").get(this));
    }
  }

  @Test
  public void getInaccessibleClassFieldErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      Object o = OtherPackagePublicClass.getPackagePrivateObject();
      Class<?> packagePrivateClass = o.getClass();
      // This is a non-public class from a different package.
      assertFalse(Modifier.isPublic(packagePrivateClass.getModifiers()));

      Field f = packagePrivateClass.getDeclaredField("publicStaticField");
      f.get(null); // Access fields of such a class should be denied, even if the field is public.
    }
  }

  @Test
  public void getSamePackageNonPrivateFieldTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyNoPropertyViolation()) {
      assertEquals(SamePackageClass.class.getDeclaredField("publicField").get(null), 10);

      // Accessing non-private fields on a class in the same package should be allowed.
      assertEquals(SamePackageClass.class.getDeclaredField("protectedField").get(null), 20);
      assertEquals(SamePackageClass.class.getDeclaredField("packagePrivateField").get(null), 30);
    }
  }

  @Test
  public void getSamePackagePrivateFieldErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      SamePackageClass.class.getDeclaredField("privateField").get(new SamePackageClass());
    }
  }

  @Test
  public void getParentStaticProtectedFieldTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyNoPropertyViolation()) {
      SamePackageClass.getParentStaticProtectedField();
    }
  }

  @Test
  public void getParentProtectedFieldTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyNoPropertyViolation()) {
      SamePackageClass.getParentProtectedField();
    }
  }

  @Test
  public void getParentProtectedFieldOnParentInstanceErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      SamePackageClass.getParentProtectedFieldOnParentInstanceError();
    }
  }

  @Test
  public void getParentProtectedFieldOnSiblingInstanceErrorTest() throws NoSuchFieldException, IllegalAccessException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      SamePackageClass.getParentProtectedFieldOnSiblingInstanceError();
    }
  }

  enum TestEnum{
    f1, f2, f3
  }

  @Test
  public void isEnumConstantTest (){
    if (verifyNoPropertyViolation()){
      for (Field f : TestEnum.class.getFields())
        assertTrue(f.isEnumConstant());
    }
  }

  @Test
  public void hashCodeTest () throws SecurityException, NoSuchFieldException{
    if (verifyNoPropertyViolation()){
      Field f1 = FieldTest.class.getField("testField1");
      Field f2 = FieldTest.class.getField("testField1");
      Field f3 = FieldTest.class.getField("testField2");
      assertTrue(f1.hashCode() == f2.hashCode());
      assertFalse(f1.hashCode() == f3.hashCode());
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  public @interface A1 {
  }

  public class Super {
    @A1
    public int f;
  }

  public class Sub {
    public int f;
  }
  
  public static class ShortField {
    public short f;
  }

  @Test
  public void getDeclaredAnnotationsTest () throws SecurityException, NoSuchFieldException{
    if (verifyNoPropertyViolation()){
      Field f1 = Sub.class.getField("f");
      Field f2 = Super.class.getField("f");
      assertEquals(f1.getDeclaredAnnotations().length, 0);
      assertEquals(f2.getDeclaredAnnotations().length, 1);
    }
  }
  
  @Test
  public void getSetShortFieldTest() throws NoSuchFieldException, SecurityException, IllegalArgumentException, IllegalAccessException {
    if(verifyNoPropertyViolation()) {
      ShortField sf = new ShortField();
      Field f = sf.getClass().getField("f");
      f.set(sf, (short)3);
      assertEquals((short)f.get(sf), (short)3);
      
      short[] x = new short[] {1,2,3};
      Array.setShort(x, 0, (short) 3);
      assertEquals(Array.getShort(x, 0), (short)3);
    }
  }
}

/*
Helper classes:

       OtherPackageParent
         /           \
SamePackageClass   SamePackageClass2
        |
SamePackageChildClass
 */

class SamePackageClass extends OtherPackageParent {
  public static int publicField = 10;
  protected static int protectedField = 20;
  static int packagePrivateField = 30;
  private int privateField = 40;

  // Some test cases live in this separate class, because the behavior of `Field.get` can depend on the caller class.

  static void getParentStaticProtectedField() throws NoSuchFieldException, IllegalAccessException {
    Field f = OtherPackageParent.class.getDeclaredField("parentStaticProtectedField");
    TestJPF.assertEquals(40, f.get(null));
    TestJPF.assertEquals(40, f.get(new SamePackageClass()));
    TestJPF.assertEquals(40, f.get(new SamePackageClass2()));
    TestJPF.assertEquals(40, f.get(new OtherPackageParent()));
    TestJPF.assertEquals(40, f.get(new SamePackageChildClass()));
  }

  static void getParentProtectedField() throws NoSuchFieldException, IllegalAccessException {
    Field f = OtherPackageParent.class.getDeclaredField("parentStaticField");
    TestJPF.assertEquals(50, f.get(new SamePackageClass()));
    TestJPF.assertEquals(50, f.get(new SamePackageChildClass()));
  }

  static void getParentProtectedFieldOnParentInstanceError() throws NoSuchFieldException, IllegalAccessException {
    Field f = OtherPackageParent.class.getDeclaredField("parentStaticField");
    f.get(new OtherPackageParent()); // Not an instance of the caller class!
  }

  static void getParentProtectedFieldOnSiblingInstanceError() throws NoSuchFieldException, IllegalAccessException {
    Field f = OtherPackageParent.class.getDeclaredField("parentStaticField");
    f.get(new SamePackageClass2()); // Not an instance of the caller class!
  }
}

class SamePackageClass2 extends OtherPackageParent {}

class SamePackageChildClass extends SamePackageClass {}
