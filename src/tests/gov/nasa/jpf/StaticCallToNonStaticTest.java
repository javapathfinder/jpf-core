package gov.nasa.jpf.test;

import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;

public class StaticCallToNonStaticTest extends TestJPF {

    // A non-static method
    public void nonStaticMethod() {
        System.out.println("Non-static method executed.");
    }

    // Test case to verify JPF behavior
    @Test
    public void testStaticCallToNonStatic() {
        // Using JPF’s verification mechanism
        if (verifyNoPropertyViolation()) {
            // Simulating a static method call to a non-static method
            StaticCallToNonStaticTest instance = null;
            try {
                instance.nonStaticMethod(); // This should fail because it's a static call to a non-static method
            } catch (NullPointerException e) {
                // Expected exception: this will be caught as instance is null
                System.out.println("Caught expected NullPointerException: " + e.getMessage());
            }
        }
    }
}
