package gov.nasa.jpf.test.java.lang;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;
public class NestmateTest extends TestJPF{

    public class Inner {}

    @Test
    public void testNestHost() {
        if (verifyNoPropertyViolation()) {
            // Top level class should be its own host
            Class<?> host = NestmateTest.class.getNestHost();
            assertEquals(NestmateTest.class, host);

            // Inner class host should be the outer class
            Class<?> innerHost = Inner.class.getNestHost();
            assertEquals(NestmateTest.class, innerHost);

            System.out.println("testNestHost passed!");
        }
    }

    @Test
    public void testIsNestmateOf() {
        if (verifyNoPropertyViolation()) {
            // They should be nestmates of each other
            assertTrue(NestmateTest.class.isNestmateOf(Inner.class));
            assertTrue(Inner.class.isNestmateOf(NestmateTest.class));

            System.out.println("testIsNestmateOf passed!");
        }
    }
}
