package gov.nasa.jpf.test.java.misc;

import gov.nasa.jpf.util.test.TestJPF;
import jdk.internal.misc.Unsafe;
import org.junit.Test;

public class UnsafeTest extends TestJPF {

    private class TestClass {
        int intVar = 0;
    }

    private final Unsafe unsafe = Unsafe.getUnsafe();

    /**
     * Test NativePeer method for {@link Unsafe#objectFieldOffset(java.lang.Class, java.lang.String)}
     */
    @Test
    public void objectFieldOffset() {
        if (verifyNoPropertyViolation()) {
            unsafe.objectFieldOffset(TestClass.class, "intVar");
        }
    }

    /**
     * Test NativePeer method for {@link Unsafe#getInt(Object, long)}
     */
    @Test
    public void getInt() {
        if (verifyNoPropertyViolation()) {
            TestClass o = new TestClass();
            long intFieldOffset = unsafe.objectFieldOffset(TestClass.class, "intVar");

            assert unsafe.getInt(o, intFieldOffset) == 0;
        }
    }

    /**
     * Test NativePeer method for {@link Unsafe#putInt(Object, long, int)}
     */
    @Test
    public void putInt() {
        if (verifyNoPropertyViolation()) {
            TestClass o = new TestClass();
            long intFieldOffset = unsafe.objectFieldOffset(TestClass.class, "intVar");
            int x = 77;

            unsafe.putInt(o, intFieldOffset, x);
            assert o.intVar == x;
        }
    }

    /**
     * Test NativePeer method for {@link Unsafe#compareAndSetInt(Object, long, int, int)}
     */
    @Test
    public void compareAndSetInt() {
        if (verifyNoPropertyViolation()) {
            TestClass o = new TestClass();
            long intFieldOffset = unsafe.objectFieldOffset(TestClass.class, "intVar");
            int expected = 0;
            int x = 3;

            unsafe.compareAndSetInt(o, intFieldOffset, expected, x);
            assert o.intVar == 3;
        }
    }
}