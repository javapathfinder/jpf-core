package gov.nasa.jpf.test.vm.basic;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class AnnotationToStringTest extends TestJPF {
  private static final String TTFF_NESTED = "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$Nested2("
      + "fields=@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A1(f1=[false,false],f2=[true,true]))";
  private static final String HELLO_WORLD_NESTED = "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$Nested1("
      + "fields=@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A16(f1=Hello,f2=World))";


  public static enum EnumConsts {
    FIRST, SECOND
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A0 {
    boolean f1();

    boolean f2();
  }

  @A0(f1 = true, f2 = false)
  public class C0 {
  }

  @Test
  public void testBooleanAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C0.class;
      A0 a = klass.getAnnotation(A0.class);
      System.out.println(a.toString());
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A0(f1=true,f2=false)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A1 {
    boolean[] f1();

    boolean[] f2();
  }

  @A1(f1 = { true, false }, f2 = { false, true })
  public class C1 {
  }

  @Test
  public void testBooleanArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C1.class;
      A1 a = klass.getAnnotation(A1.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A1(f1=[true,false],f2=[false,true])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A2 {
    byte f1();

    byte f2();
  }

  @A2(f1 = 0, f2 = 1)
  public class C2 {
  }

  @Test
  public void testByteAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C2.class;
      A2 a = klass.getAnnotation(A2.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A2(f1=0,f2=1)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A3 {
    byte[] f1();

    byte[] f2();
  }

  @A3(f1 = { 0, 1 }, f2 = { 1, 0 })
  public class C3 {
  }

  @Test
  public void testByteArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C3.class;
      A3 a = klass.getAnnotation(A3.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A3(f1=[0,1],f2=[1,0])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A4 {
    char f1();

    char f2();
  }

  @A4(f1 = 'a', f2 = 'b')
  public class C4 {
  }

  @Test
  public void testCharAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C4.class;
      A4 a = klass.getAnnotation(A4.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A4(f1=a,f2=b)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A5 {
    char[] f1();

    char[] f2();
  }

  @A5(f1 = { 'a', 'b' }, f2 = { 'b', 'a' })
  public class C5 {
  }

  @Test
  public void testCharArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C5.class;
      A5 a = klass.getAnnotation(A5.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A5(f1=[a,b],f2=[b,a])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A6 {
    short f1();

    short f2();
  }

  @A6(f1 = 0, f2 = 1)
  public class C6 {
  }

  @Test
  public void testShortAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C6.class;
      A6 a = klass.getAnnotation(A6.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A6(f1=0,f2=1)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A7 {
    short[] f1();

    short[] f2();
  }

  @A7(f1 = { 0, 1 }, f2 = { 1, 0 })
  public class C7 {
  }

  @Test
  public void testShortArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C7.class;
      A7 a = klass.getAnnotation(A7.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A7(f1=[0,1],f2=[1,0])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A8 {
    int f1();

    int f2();
  }

  @A8(f1 = 0, f2 = 1)
  public class C8 {
  }

  @Test
  public void testIntAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C8.class;
      A8 a = klass.getAnnotation(A8.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A8(f1=0,f2=1)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A9 {
    int[] f1();

    int[] f2();
  }

  @A9(f1 = { 0, 1 }, f2 = { 1, 0 })
  public class C9 {
  }

  @Test
  public void testIntArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C9.class;
      A9 a = klass.getAnnotation(A9.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A9(f1=[0,1],f2=[1,0])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A10 {
    long f1();

    long f2();
  }

  @A10(f1 = 1L, f2 = 10L)
  public class C10 {
  }

  @Test
  public void testLongAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C10.class;
      A10 a = klass.getAnnotation(A10.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A10(f1=1,f2=10)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A11 {
    long[] f1();

    long[] f2();
  }

  @A11(f1 = { 1L, 10L }, f2 = { 10L, 1L })
  public class C11 {
  }

  @Test
  public void testLongArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C11.class;
      A11 a = klass.getAnnotation(A11.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A11(f1=[1,10],f2=[10,1])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A12 {
    float f1();

    float f2();
  }

  @A12(f1 = 0.5f, f2 = 2.0f)
  public class C12 {
  }

  @Test
  public void testFloatAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C12.class;
      A12 a = klass.getAnnotation(A12.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A12(f1=0.5,f2=2.0)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A13 {
    float[] f1();

    float[] f2();
  }

  @A13(f1 = { 0.5f, 2.0f }, f2 = { 2.0f, 0.5f })
  public class C13 {
  }

  @Test
  public void testFloatArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C13.class;
      A13 a = klass.getAnnotation(A13.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A13(f1=[0.5,2.0],f2=[2.0,0.5])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A14 {
    double f1();

    double f2();
  }

  @A14(f1 = 2.0, f2 = 3.5)
  public class C14 {
  }

  @Test
  public void testDoubleAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C14.class;
      A14 a = klass.getAnnotation(A14.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A14(f1=2.0,f2=3.5)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A15 {
    double[] f1();

    double[] f2();
  }

  @A15(f1 = { 2.0, 3.5 }, f2 = { 3.5, 2.0 })
  public class C15 {
  }

  @Test
  public void testDoubleArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C15.class;
      A15 a = klass.getAnnotation(A15.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A15(f1=[2.0,3.5],f2=[3.5,2.0])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A16 {
    String f1();

    String f2();
  }

  @A16(f1 = "Hello", f2 = "World")
  public class C16 {
  }

  @Test
  public void testStringAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C16.class;
      A16 a = klass.getAnnotation(A16.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A16(f1=Hello,f2=World)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A17 {
    String[] f1();

    String[] f2();
  }

  @A17(f1 = { "Hello", "World" }, f2 = { "World", "Hello" })
  public class C17 {
  }

  @Test
  public void testStringArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C17.class;
      A17 a = klass.getAnnotation(A17.class);
      System.out.println(a.toString());
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A17(f1=[Hello,World],f2=[World,Hello])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A18 {
    Class<?> f1();

    Class<?> f2();
  }

  @A18(f1 = String.class, f2 = Integer.class)
  public class C18 {
  }

  @Test
  public void testClassAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C18.class;
      A18 a = klass.getAnnotation(A18.class);
      System.out.println(a.toString());
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A18(f1=class java.lang.String,f2=class java.lang.Integer)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A19 {
    Class<?>[] f1();

    Class<?>[] f2();
  }

  @A19(f1 = { String.class, Integer.class }, f2 = { Integer.class, String.class })
  public class C19 {
  }

  @Test
  public void testClassArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C19.class;
      A19 a = klass.getAnnotation(A19.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A19(f1=[class java.lang.String,class java.lang.Integer],"
          + "f2=[class java.lang.Integer,class java.lang.String])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A20 {
    EnumConsts f1();

    EnumConsts f2();
  }

  @A20(f1 = EnumConsts.FIRST, f2 = EnumConsts.SECOND)
  public class C20 {
  }

  @Test
  public void testEnumConstsAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C20.class;
      A20 a = klass.getAnnotation(A20.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A20(f1=gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$EnumConsts.FIRST,"
          + "f2=gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$EnumConsts.SECOND)");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A21 {
    EnumConsts[] f1();

    EnumConsts[] f2();
  }

  @A21(f1 = { EnumConsts.FIRST, EnumConsts.SECOND }, f2 = { EnumConsts.SECOND, EnumConsts.FIRST })
  public class C21 {
  }

  @Test
  public void testEnumConstsArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C21.class;
      A21 a = klass.getAnnotation(A21.class);
      assertEquals(a.toString(), "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A21("
          + "f1=[gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$EnumConsts.FIRST,gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$EnumConsts.SECOND],"
          + "f2=[gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$EnumConsts.SECOND,gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$EnumConsts.FIRST])");
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface Nested1 {
    A16 fields();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface Nested2 {
    A1 fields();
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A22 {
    Nested1 f1();
    Nested2 f2();
  }
  
  @A22(f1 = @Nested1(fields = @A16(f1 = "Hello", f2 = "World")), f2 = @Nested2(fields = @A1(f1 = {false, false}, f2 = {true, true})))
  public class C22 {
    
  }
  
  @Test
  public void testAnnotationAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C22.class;
      A22 a = klass.getAnnotation(A22.class);
      String toString = "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A22(f1=" + HELLO_WORLD_NESTED + ",f2=" + TTFF_NESTED + ")";
      assertEquals(a.toString(), a.toString(), toString);
    }
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  public @interface A23 {
    Nested1[] f1();
    Nested2[] f2();
  }
  
  @A23(f1 = {
    @Nested1(fields = @A16(f1 = "Hello", f2 = "World")), @Nested1(fields = @A16(f1 = "Hola", f2 = "Mundo"))
  }, f2 = {
    @Nested2(fields = @A1(f1 = {false, false}, f2 = {true, true})), @Nested2(fields = @A1(f1 = {true,true}, f2 = {false,false}))
  })
  public class C23 {
    
  }
  
  @Test
  public void testAnnotationArrayAttributeToString() {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C23.class;
      A23 a = klass.getAnnotation(A23.class);
      String holaMundo = HELLO_WORLD_NESTED.replace("Hello", "Hola").replace("World", "Mundo");
      String trueTrueString = TTFF_NESTED.replaceAll("false", "TMP").replaceAll("true", "false").replaceAll("TMP", "true"); 
      String toString = "@gov.nasa.jpf.test.vm.basic.AnnotationToStringTest$A23(f1=[" + HELLO_WORLD_NESTED + "," + holaMundo + "],"
          + "f2=[" + TTFF_NESTED + "," + trueTrueString + "])";
      assertEquals(a.toString(), toString);
    }
  }
}
