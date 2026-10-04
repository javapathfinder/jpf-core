package gov.nasa.jpf.jvm;

import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;

public class InvokeStaticTest extends TestJPF {

    static class D {
        // Initially static method
        public static String m(String s) {
            return s;
        }
    }

    static class C {
        public static void main(String[] args) {
            int i = 123;
            D.m("foobar");  // This is a valid static method call
            System.out.println(i);
        }
    }

    @Test
    public void testInvokeStaticSuccess() {
        // Test should pass if static method invocation works fine
        if (verifyNoPropertyViolation()) {
            C.main(new String[0]);
        }
    }

    @Test
    public void testInvokeStaticFailure() {
        if (verifyUnhandledException("java.lang.IncompatibleClassChangeError")) {
            // Simulate making D.m() non-static to trigger the error
            DNonStatic d = new DNonStatic();
            d.m("foobar");  // This should cause the failure because the method is now non-static
        }
    }

    // Simulate a non-static version of class D for this test
    static class DNonStatic {
        // Non-static method, which should fail when invoked via INVOKESTATIC
        public String m(String s) {
            return s;
        }
    }
}
