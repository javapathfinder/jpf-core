package gov.nasa.jpf.test.vm.basic;

import org.junit.Test;

import gov.nasa.jpf.test.vm.basic.AnnotationToStringTest.*;
import gov.nasa.jpf.util.test.TestJPF;

public class AnnotationHashCodeEqualsTest extends TestJPF {
  public class C0 {
    @A0(f1 = true, f2 = false)
    public int f1;

    @A0(f1 = true, f2 = false)
    public int f2;

    @A0(f1 = false, f2 = true)
    public int f3;
  }

  @Test
  public void testBooleanAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C0.class;
      A0 a = klass.getDeclaredField("f1").getAnnotation(A0.class);
      A0 b = klass.getDeclaredField("f2").getAnnotation(A0.class);
      A0 c = klass.getDeclaredField("f3").getAnnotation(A0.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C1 {
    @A1(f1 = { true, false }, f2 = { false, true })
    public int f1;

    @A1(f1 = { true, false }, f2 = { false, true })
    public int f2;

    @A1(f1 = { false, true }, f2 = { true, false })
    public int f3;
  }

  @Test
  public void testBooleanArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C1.class;
      A1 a = klass.getDeclaredField("f1").getAnnotation(A1.class);
      A1 b = klass.getDeclaredField("f2").getAnnotation(A1.class);
      A1 c = klass.getDeclaredField("f3").getAnnotation(A1.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C2 {
    @A2(f1 = 2, f2 = 3)
    public int f1;

    @A2(f1 = 2, f2 = 3)
    public int f2;

    @A2(f1 = 3, f2 = 2)
    public int f3;
  }

  @Test
  public void testByteAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C2.class;
      A2 a = klass.getDeclaredField("f1").getAnnotation(A2.class);
      A2 b = klass.getDeclaredField("f2").getAnnotation(A2.class);
      A2 c = klass.getDeclaredField("f3").getAnnotation(A2.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C3 {
    @A3(f1 = { 2, 3 }, f2 = { 3, 2 })
    public int f1;

    @A3(f1 = { 2, 3 }, f2 = { 3, 2 })
    public int f2;

    @A3(f1 = { 3, 2 }, f2 = { 2, 3 })
    public int f3;
  }

  @Test
  public void testByteArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C3.class;
      A3 a = klass.getDeclaredField("f1").getAnnotation(A3.class);
      A3 b = klass.getDeclaredField("f2").getAnnotation(A3.class);
      A3 c = klass.getDeclaredField("f3").getAnnotation(A3.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C4 {
    @A4(f1 = 'a', f2 = 'b')
    public int f1;

    @A4(f1 = 'a', f2 = 'b')
    public int f2;

    @A4(f1 = 'b', f2 = 'a')
    public int f3;
  }

  @Test
  public void testCharAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C4.class;
      A4 a = klass.getDeclaredField("f1").getAnnotation(A4.class);
      A4 b = klass.getDeclaredField("f2").getAnnotation(A4.class);
      A4 c = klass.getDeclaredField("f3").getAnnotation(A4.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C5 {
    @A5(f1 = { 'a', 'b' }, f2 = { 'b', 'a' })
    public int f1;

    @A5(f1 = { 'a', 'b' }, f2 = { 'b', 'a' })
    public int f2;

    @A5(f1 = { 'b', 'a' }, f2 = { 'a', 'b' })
    public int f3;
  }

  @Test
  public void testCharArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C5.class;
      A5 a = klass.getDeclaredField("f1").getAnnotation(A5.class);
      A5 b = klass.getDeclaredField("f2").getAnnotation(A5.class);
      A5 c = klass.getDeclaredField("f3").getAnnotation(A5.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C6 {
    @A6(f1 = 0, f2 = 1)
    public int f1;

    @A6(f1 = 0, f2 = 1)
    public int f2;

    @A6(f1 = 1, f2 = 0)
    public int f3;
  }

  @Test
  public void testShortAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C6.class;
      A6 a = klass.getDeclaredField("f1").getAnnotation(A6.class);
      A6 b = klass.getDeclaredField("f2").getAnnotation(A6.class);
      A6 c = klass.getDeclaredField("f3").getAnnotation(A6.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C7 {
    @A7(f1 = { 0, 1 }, f2 = { 1, 0 })
    public int f1;

    @A7(f1 = { 0, 1 }, f2 = { 1, 0 })
    public int f2;

    @A7(f1 = { 1, 0 }, f2 = { 0, 1 })
    public int f3;
  }

  @Test
  public void testShortArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C7.class;
      A7 a = klass.getDeclaredField("f1").getAnnotation(A7.class);
      A7 b = klass.getDeclaredField("f2").getAnnotation(A7.class);
      A7 c = klass.getDeclaredField("f3").getAnnotation(A7.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C8 {
    @A8(f1 = 4, f2 = 5)
    public int f1;

    @A8(f1 = 4, f2 = 5)
    public int f2;

    @A8(f1 = 5, f2 = 4)
    public int f3;
  }

  @Test
  public void testIntAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C8.class;
      A8 a = klass.getDeclaredField("f1").getAnnotation(A8.class);
      A8 b = klass.getDeclaredField("f2").getAnnotation(A8.class);
      A8 c = klass.getDeclaredField("f3").getAnnotation(A8.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C9 {
    @A9(f1 = { 4, 5 }, f2 = { 5, 4 })
    public int f1;

    @A9(f1 = { 4, 5 }, f2 = { 5, 4 })
    public int f2;

    @A9(f1 = { 5, 4 }, f2 = { 4, 5 })
    public int f3;
  }

  @Test
  public void testIntArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C9.class;
      A9 a = klass.getDeclaredField("f1").getAnnotation(A9.class);
      A9 b = klass.getDeclaredField("f2").getAnnotation(A9.class);
      A9 c = klass.getDeclaredField("f3").getAnnotation(A9.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C10 {
    @A10(f1 = 9L, f2 = 10L)
    public int f1;

    @A10(f1 = 9L, f2 = 10L)
    public int f2;

    @A10(f1 = 10L, f2 = 9L)
    public int f3;
  }

  @Test
  public void testLongAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C10.class;
      A10 a = klass.getDeclaredField("f1").getAnnotation(A10.class);
      A10 b = klass.getDeclaredField("f2").getAnnotation(A10.class);
      A10 c = klass.getDeclaredField("f3").getAnnotation(A10.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C11 {
    @A11(f1 = { 9L, 10L }, f2 = { 10L, 9L })
    public int f1;

    @A11(f1 = { 9L, 10L }, f2 = { 10L, 9L })
    public int f2;

    @A11(f1 = { 10L, 9L }, f2 = { 9L, 10L })
    public int f3;
  }

  @Test
  public void testLongArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C11.class;
      A11 a = klass.getDeclaredField("f1").getAnnotation(A11.class);
      A11 b = klass.getDeclaredField("f2").getAnnotation(A11.class);
      A11 c = klass.getDeclaredField("f3").getAnnotation(A11.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C12 {
    @A12(f1 = 0.5f, f2 = 2.0f)
    public int f1;

    @A12(f1 = 0.5f, f2 = 2.0f)
    public int f2;

    @A12(f1 = 2.0f, f2 = 0.5f)
    public int f3;
  }

  @Test
  public void testFloatAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C12.class;
      A12 a = klass.getDeclaredField("f1").getAnnotation(A12.class);
      A12 b = klass.getDeclaredField("f2").getAnnotation(A12.class);
      A12 c = klass.getDeclaredField("f3").getAnnotation(A12.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C13 {
    @A13(f1 = { 0.5f, 2.0f }, f2 = { 2.0f, 0.5f })
    public int f1;

    @A13(f1 = { 0.5f, 2.0f }, f2 = { 2.0f, 0.5f })
    public int f2;

    @A13(f1 = { 2.0f, 0.5f }, f2 = { 0.5f, 2.0f })
    public int f3;
  }

  @Test
  public void testFloatArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C13.class;
      A13 a = klass.getDeclaredField("f1").getAnnotation(A13.class);
      A13 b = klass.getDeclaredField("f2").getAnnotation(A13.class);
      A13 c = klass.getDeclaredField("f3").getAnnotation(A13.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C14 {
    @A14(f1 = 2.0, f2 = 3.5)
    public int f1;

    @A14(f1 = 2.0, f2 = 3.5)
    public int f2;

    @A14(f1 = 3.5, f2 = 2.0)
    public int f3;
  }

  @Test
  public void testDoubleAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C14.class;
      A14 a = klass.getDeclaredField("f1").getAnnotation(A14.class);
      A14 b = klass.getDeclaredField("f2").getAnnotation(A14.class);
      A14 c = klass.getDeclaredField("f3").getAnnotation(A14.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C15 {
    @A15(f1 = { 2.0, 3.5 }, f2 = { 3.5, 2.0 })
    public int f1;

    @A15(f1 = { 2.0, 3.5 }, f2 = { 3.5, 2.0 })
    public int f2;

    @A15(f1 = { 3.5, 2.0 }, f2 = { 2.0, 3.5 })
    public int f3;
  }

  @Test
  public void testDoubleArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C15.class;
      A15 a = klass.getDeclaredField("f1").getAnnotation(A15.class);
      A15 b = klass.getDeclaredField("f2").getAnnotation(A15.class);
      A15 c = klass.getDeclaredField("f3").getAnnotation(A15.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C16 {
    @A16(f1 = "Hello", f2 = "World")
    public int f1;

    @A16(f1 = "Hello", f2 = "World")
    public int f2;

    @A16(f1 = "World", f2 = "Hello")
    public int f3;
  }

  @Test
  public void testStringAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C16.class;
      A16 a = klass.getDeclaredField("f1").getAnnotation(A16.class);
      A16 b = klass.getDeclaredField("f2").getAnnotation(A16.class);
      A16 c = klass.getDeclaredField("f3").getAnnotation(A16.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C17 {
    @A17(f1 = { "Hello", "World" }, f2 = { "World", "Hello" })
    public int f1;

    @A17(f1 = { "Hello", "World" }, f2 = { "World", "Hello" })
    public int f2;

    @A17(f1 = { "World", "Hello" }, f2 = { "Hello", "World" })
    public int f3;
  }

  @Test
  public void testStringArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C17.class;
      A17 a = klass.getDeclaredField("f1").getAnnotation(A17.class);
      A17 b = klass.getDeclaredField("f2").getAnnotation(A17.class);
      A17 c = klass.getDeclaredField("f3").getAnnotation(A17.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C18 {
    @A18(f1 = String.class, f2 = Integer.class)
    public int f1;

    @A18(f1 = String.class, f2 = Integer.class)
    public int f2;

    @A18(f1 = Integer.class, f2 = String.class)
    public int f3;
  }

  @Test
  public void testClassAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C18.class;
      A18 a = klass.getDeclaredField("f1").getAnnotation(A18.class);
      A18 b = klass.getDeclaredField("f2").getAnnotation(A18.class);
      A18 c = klass.getDeclaredField("f3").getAnnotation(A18.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C19 {
    @A19(f1 = { String.class, Integer.class }, f2 = { Integer.class, String.class })
    public int f1;

    @A19(f1 = { String.class, Integer.class }, f2 = { Integer.class, String.class })
    public int f2;

    @A19(f1 = { Integer.class, String.class }, f2 = { String.class, Integer.class })
    public int f3;
  }

  @Test
  public void testClassArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C19.class;
      A19 a = klass.getDeclaredField("f1").getAnnotation(A19.class);
      A19 b = klass.getDeclaredField("f2").getAnnotation(A19.class);
      A19 c = klass.getDeclaredField("f3").getAnnotation(A19.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C20 {
    @A20(f1 = EnumConsts.FIRST, f2 = EnumConsts.SECOND)
    public int f1;

    @A20(f1 = EnumConsts.FIRST, f2 = EnumConsts.SECOND)
    public int f2;

    @A20(f1 = EnumConsts.SECOND, f2 = EnumConsts.FIRST)
    public int f3;
  }

  @Test
  public void testEnumConstsAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C20.class;
      A20 a = klass.getDeclaredField("f1").getAnnotation(A20.class);
      A20 b = klass.getDeclaredField("f2").getAnnotation(A20.class);
      A20 c = klass.getDeclaredField("f3").getAnnotation(A20.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

  public class C21 {
    @A21(f1 = { EnumConsts.FIRST, EnumConsts.SECOND }, f2 = { EnumConsts.SECOND, EnumConsts.FIRST })
    public int f1;

    @A21(f1 = { EnumConsts.FIRST, EnumConsts.SECOND }, f2 = { EnumConsts.SECOND, EnumConsts.FIRST })
    public int f2;

    @A21(f1 = { EnumConsts.SECOND, EnumConsts.FIRST }, f2 = { EnumConsts.FIRST, EnumConsts.SECOND })
    public int f3;
  }

  @Test
  public void testEnumConstsArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C21.class;
      A21 a = klass.getDeclaredField("f1").getAnnotation(A21.class);
      A21 b = klass.getDeclaredField("f2").getAnnotation(A21.class);
      A21 c = klass.getDeclaredField("f3").getAnnotation(A21.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }
  
  public class C22 {
    @A22(f1 = @Nested1(fields = @A16(f1 = "Hello", f2 = "World")), f2 = @Nested2(fields = @A1(f1 = false, f2 = false)))
    public int f1;
    
    @A22(f1 = @Nested1(fields = @A16(f1 = "Hello", f2 = "World")), f2 = @Nested2(fields = @A1(f1 = false, f2 = false)))
    public int f2;
    
    @A22(f1 = @Nested1(fields = @A16(f1 = "Hola", f2 = "Mundo")), f2 = @Nested2(fields = @A1(f1 = true, f2 = true)))
    public int f3;
  }
  
  @Test
  public void testAnnotationAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C22.class;
      A22 a = klass.getDeclaredField("f1").getAnnotation(A22.class);
      A22 b = klass.getDeclaredField("f2").getAnnotation(A22.class);
      A22 c = klass.getDeclaredField("f3").getAnnotation(A22.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }
  
  public class C23 {
    @A23(f1 = {
        @Nested1(fields = @A16(f1 = "Hello", f2 = "World")), 
        @Nested1(fields = @A16(f1 = "Bonjour", f2 = "Monde"))
    }, f2 = {
        @Nested2(fields = @A1(f1 = false, f2 = false)),
        @Nested2(fields = @A1(f1 = true, f2 = true))
    })
    public int f1;
    
    @A23(f1 = {
        @Nested1(fields = @A16(f1 = "Hello", f2 = "World")), 
        @Nested1(fields = @A16(f1 = "Bonjour", f2 = "Monde"))
    }, f2 = {
        @Nested2(fields = @A1(f1 = false, f2 = false)),
        @Nested2(fields = @A1(f1 = true, f2 = true))
    })
    public int f2;
    
    @A23(f1 = {
        @Nested1(fields = @A16(f1 = "Hola", f2 = "Mundo")), 
        @Nested1(fields = @A16(f1 = "Bonjour", f2 = "Monde"))
    }, f2 = {
        @Nested2(fields = @A1(f1 = false, f2 = true)),
        @Nested2(fields = @A1(f1 = true, f2 = false))
    })    public int f3;
  }
  
  @Test
  public void testAnnotationArrayAttributeHashCodeEquals() throws NoSuchFieldException, SecurityException {
    if(verifyNoPropertyViolation()) {
      Class<?> klass = C23.class;
      A23 a = klass.getDeclaredField("f1").getAnnotation(A23.class);
      A23 b = klass.getDeclaredField("f2").getAnnotation(A23.class);
      A23 c = klass.getDeclaredField("f3").getAnnotation(A23.class);
      assertEquals(a, b);
      assertEquals(a.hashCode(), b.hashCode());
      assertFalse(c.equals(b));
      assertFalse(c.equals(a));
    }
  }

}
