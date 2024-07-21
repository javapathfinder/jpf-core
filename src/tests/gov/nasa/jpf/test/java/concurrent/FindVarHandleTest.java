package gov.nasa.jpf.test.java.concurrent;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.lang.invoke.MethodHandles;
import java.lang.invoke.VarHandle;

public class FindVarHandleTest extends TestJPF {
    static class TestClass {
        public int instanceField;
        public static int staticField;
    }



    // Below three methods are to test "instance fields"

    @Test
    public void testFindVarHandleInstanceField() throws NoSuchFieldException, IllegalAccessException {
        if (verifyNoPropertyViolation()){
            VarHandle handle = MethodHandles.lookup().findVarHandle(TestClass.class, "instanceField", int.class);
            assertNotNull(handle);
        }
    }

    @Test
    public void testFindVarHandleStaticField() throws NoSuchFieldException, IllegalAccessException  {
        if (verifyUnhandledException("java.lang.IllegalAccessException")){
            VarHandle handle = MethodHandles.lookup().findVarHandle(TestClass.class, "staticField", int.class);
            assertNotNull(handle);
        }
    }

    @Test
    public void testFindVarHandleNoSuchField() throws NoSuchFieldException, IllegalAccessException  {
        if (verifyUnhandledException("java.lang.NoSuchFieldException")){
            VarHandle handle = MethodHandles.lookup().findVarHandle(TestClass.class, "notExistingField", int.class);
            assertNotNull(handle);
        }
    }

}
