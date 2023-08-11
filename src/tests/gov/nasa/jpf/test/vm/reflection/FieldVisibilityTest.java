package gov.nasa.jpf.test.vm.reflection;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

/**
 * Tests `Class.getField` and `Class.getDeclaredField` on fields of different visibility levels.
 */
public class FieldVisibilityTest extends TestJPF {
    public String publicField;
    String packagePrivateField;
    protected String protectedField;
    private String privateField;

    // Calls `Class.getField` on another class's public field, and checks the returned `Field` object.
    @Test
    public void getFieldPublicTest() throws NoSuchFieldException {
        if (verifyNoPropertyViolation()) {
            Field f = Integer.class.getField("MAX_VALUE");
            assertEquals("public static final int java.lang.Integer.MAX_VALUE", f.toString());
            assertEquals(Integer.class, f.getDeclaringClass());
            assertEquals("MAX_VALUE", f.getName());

            int modifiers = f.getModifiers();
            assertTrue(Modifier.isPublic(modifiers));
            assertTrue(Modifier.isStatic(modifiers));
            assertTrue(Modifier.isFinal(modifiers));
        }
    }

    @Test
    public void getFieldProtectedTest() throws NoSuchFieldException {
        if (verifyUnhandledException("java.lang.NoSuchFieldException")) {
            FieldVisibilityTest.class.getField("protectedField");
        }
    }

    @Test
    public void getFieldPackagePrivateTest() throws NoSuchFieldException {
        if (verifyUnhandledException("java.lang.NoSuchFieldException")) {
            FieldVisibilityTest.class.getField("packagePrivateField");
        }
    }

    @Test
    public void getFieldPrivateTest() throws NoSuchFieldException {
        if (verifyUnhandledException("java.lang.NoSuchFieldException")) {
            FieldVisibilityTest.class.getField("privateField");
        }
    }

    @Test
    public void getDeclaredFieldSameClassTest() throws NoSuchFieldException {
        if (verifyNoPropertyViolation()) {
            assertEquals(FieldVisibilityTest.class.getDeclaredField("publicField").getName(), "publicField");
            assertEquals(FieldVisibilityTest.class.getDeclaredField("packagePrivateField").getName(), "packagePrivateField");
            assertEquals(FieldVisibilityTest.class.getDeclaredField("protectedField").getName(), "protectedField");
            assertEquals(FieldVisibilityTest.class.getDeclaredField("privateField").getName(), "privateField");
        }
    }

    @Test
    public void getDeclaredFieldOtherClassTest() throws NoSuchFieldException {
        assertEquals(SomeClass.class.getDeclaredField("publicField").getName(), "publicField");
        assertEquals(SomeClass.class.getDeclaredField("packagePrivateField").getName(), "packagePrivateField");
        assertEquals(SomeClass.class.getDeclaredField("protectedField").getName(), "protectedField");
        assertEquals(SomeClass.class.getDeclaredField("privateField").getName(), "privateField");
    }
}

/**
 * A trivial class containing fields of different visibility levels.
 */
class SomeClass {
    public String publicField;
    String packagePrivateField;
    protected String protectedField;
    private String privateField;
}
