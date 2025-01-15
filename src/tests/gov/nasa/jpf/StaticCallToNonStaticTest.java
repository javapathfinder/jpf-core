package gov.nasa.jpf.test;

import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;

public class StaticCallToNonStaticTest extends TestJPF {

    @Test
    public void testStaticCallToNonStatic() {
        if (verifyNoPropertyViolation()) {
            try {
                // Attempting to call non-static method m() on class D without an instance
                D.m(); // This should throw IncompatibleClassChangeError after D.java is recompiled without static
            } catch (IncompatibleClassChangeError e) {
                // Expected exception: this is the behavior we want to test
                System.out.println("Caught expected IncompatibleClassChangeError: " + e.getMessage());
            }
        }
    }
}
