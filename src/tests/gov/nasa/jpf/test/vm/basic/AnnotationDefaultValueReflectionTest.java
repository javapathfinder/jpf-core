package gov.nasa.jpf.test.vm.basic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.Objects;

import org.junit.Test;

import gov.nasa.jpf.test.vm.basic.AnnotationToStringTest.EnumConsts;
import gov.nasa.jpf.util.test.TestJPF;

public class AnnotationDefaultValueReflectionTest extends TestJPF {
  public class NotAnAnnotation {
    public void method() {

    }
  }

  @Test
  public void testNoDefaultValue() throws NoSuchMethodException, SecurityException {
    Method m = NotAnAnnotation.class.getDeclaredMethod("method");
    assertNull(m.getDefaultValue());
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A0 {
    boolean f1() default true;

    boolean f2();
  }

  @Test
  public void testBooleanAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A0.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      boolean expected = true;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (boolean) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A1 {
    boolean[] f1() default { true, false };

    boolean[] f2();
  }

  @Test
  public void testBooleanArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A1.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      boolean[] expected = { true, false };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (boolean[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A2 {
    byte f1() default 2;

    byte f2();
  }

  @Test
  public void testByteAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A2.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      byte expected = 2;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (byte) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A3 {
    byte[] f1() default { 2, 3 };

    byte[] f2();
  }

  @Test
  public void testByteArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A3.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      byte[] expected = { 2, 3 };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (byte[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A4 {
    char f1() default 'a';

    char f2();
  }

  @Test
  public void testCharAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A4.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      char expected = 'a';
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (char) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A5 {
    char[] f1() default { 'a', 'b' };

    char[] f2();
  }

  @Test
  public void testCharArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A5.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      char[] expected = { 'a', 'b' };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (char[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A6 {
    short f1() default 0;

    short f2();
  }

  @Test
  public void testShortAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A6.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      short expected = 0;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (short) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A7 {
    short[] f1() default { 0, 1 };

    short[] f2();
  }

  @Test
  public void testShortArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A7.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      short[] expected = { 0, 1 };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (short[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A8 {
    int f1() default 4;

    int f2();
  }

  @Test
  public void testIntAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A8.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      int expected = 4;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (int) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A9 {
    int[] f1() default { 4, 5 };

    int[] f2();
  }

  @Test
  public void testIntArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A9.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      int[] expected = { 4, 5 };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (int[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A10 {
    long f1() default 9L;

    long f2();
  }

  @Test
  public void testLongAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A10.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      long expected = 9L;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (long) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A11 {
    long[] f1() default { 9L, 10L };

    long[] f2();
  }

  @Test
  public void testLongArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A11.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      long[] expected = { 9L, 10L };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (long[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A12 {
    float f1() default 0.5f;

    float f2();
  }

  @Test
  public void testFloatAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A12.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      float expected = 0.5f;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (float) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A13 {
    float[] f1() default { 0.5f, 2.0f };

    float[] f2();
  }

  @Test
  public void testFloatArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A13.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      float[] expected = { 0.5f, 2.0f };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (float[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A14 {
    double f1() default 2.0;

    double f2();
  }

  @Test
  public void testDoubleAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A14.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      double expected = 2.0;
      Object defaultValue = f1.getDefaultValue();
      assertTrue(expected == (double) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A15 {
    double[] f1() default { 2.0, 3.5 };

    double[] f2();
  }

  @Test
  public void testDoubleArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A15.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      double[] expected = { 2.0, 3.5 };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (double[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A16 {
    String f1() default "Hello";

    String f2();
  }

  @Test
  public void testStringAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A16.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      String expected = "Hello";
      Object defaultValue = f1.getDefaultValue();
      assertEquals(expected, (String) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A17 {
    String[] f1() default { "Hello", "World" };

    String[] f2();
  }

  @Test
  public void testStringArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A17.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      String[] expected = { "Hello", "World" };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (String[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A18 {
    Class<?> f1() default String.class;

    Class<?> f2();
  }

  @Test
  public void testClassAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A18.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      Class<?> expected = String.class;
      Object defaultValue = f1.getDefaultValue();
      assertEquals(expected, (Class<?>) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A19 {
    Class<?>[] f1() default { String.class, Integer.class };

    Class<?>[] f2();
  }

  @Test
  public void testClassArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A19.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      Class<?>[] expected = { String.class, Integer.class };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (Class<?>[]) defaultValue));
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A20 {
    EnumConsts f1() default EnumConsts.FIRST;

    EnumConsts f2();
  }

  @Test
  public void testEnumConstsAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A20.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      EnumConsts expected = EnumConsts.FIRST;
      Object defaultValue = f1.getDefaultValue();
      assertEquals(expected, (EnumConsts) defaultValue);
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A21 {
    EnumConsts[] f1() default { EnumConsts.FIRST, EnumConsts.SECOND };

    EnumConsts[] f2();
  }

  @Test
  public void testEnumConstsArrayAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A21.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Method f2 = klass.getDeclaredMethod("f2");
      assertNull(f2.getDefaultValue());
      EnumConsts[] expected = { EnumConsts.FIRST, EnumConsts.SECOND };
      Object defaultValue = f1.getDefaultValue();
      assertTrue(Arrays.equals(expected, (EnumConsts[]) defaultValue));
    }
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Nested {
    A0 f1() default @A0(f2 = true);
    A16 f2();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A22 {
    Nested f1() default @Nested(f2 = @A16(f1 = "Hello", f2 = "World"));
  }
  
  @Nested(f2 = @A16(f1 = "Hello", f2 = "World"))
  public static class Holder {
  }
  
  @Test
  public void testAnnotationAttributeDefaultValue() throws NoSuchMethodException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = A22.class;
      Method f1 = klass.getDeclaredMethod("f1");
      Nested expected = Holder.class.getAnnotation(Nested.class);
      Object defaultValue = f1.getDefaultValue();
      assertEquals(expected, defaultValue);
    }
  }
}
