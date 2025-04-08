package gov.nasa.jpf.test.java.lang.invoke;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.*;

import java.lang.invoke.*;

public class MethodHandleTest extends TestJPF {

    public static String staticTarget(String s) {
        System.out.println("staticTarget called with: " + s);
        return "Static: " + s;
    }

    public String virtualTarget(String s) {
        System.out.println("virtualTarget called on " + this + " with: " + s);
        return "Virtual on " + this.hashCode() + ": " + s;
    }

    private String privateTarget(String s) {
        System.out.println("privateTarget called with: " + s);
        return "Private: " + s;
    }

    private String state = "default"; // Default state for the test instance

//    // Constructor for findConstructor/findSpecial test
//    public MethodHandleTest(String initial) {
//        // This constructor will be called via MethodHandle
//        System.out.println("[MHDirect] Constructor called with: " + initial);
//        this.state = initial;
//    }

    public MethodHandleTest() {
        System.out.println("Default constructor called for test instance");
        this.state = "testInstance";
    }

    @Test
    public void testDirectMethodHandleUsage() throws Throwable {
        if (verifyNoPropertyViolation()) {
            System.out.println("== Starting MethodHandleDirectTest Logic ==");
            MethodHandles.Lookup lookup = MethodHandles.lookup(); // Lookup relative to this class

            // The 'instance' for virtual/special calls will be 'this' test instance
            MethodHandleTest instance = this;
            assertEquals("testInstance", instance.state);
            System.out.println("Test instance hash: " + instance.hashCode());


            // 1. Test findStatic
            System.out.println("\nTesting findStatic");
            MethodHandle mhStatic = lookup.findStatic(MethodHandleTest.class, "staticTarget", MethodType.methodType(String.class, String.class));
            String resultStatic = (String) mhStatic.invokeExact("StaticInput");
            System.out.println("Result: " + resultStatic);
            assertEquals("Static: StaticInput", resultStatic);


            // 2. Test findVirtual
            System.out.println("\nTesting findVirtual");
            MethodHandle mhVirtual = lookup.findVirtual(MethodHandleTest.class, "virtualTarget", MethodType.methodType(String.class, String.class));
            String resultVirtual = (String) mhVirtual.invokeExact(instance, "VirtualInput");
            System.out.println("Result: " + resultVirtual);
            assertTrue(resultVirtual.startsWith("Virtual on "));
            assertTrue(resultVirtual.endsWith(": VirtualInput"));


            // 3. Test findSpecial (for private method)
            System.out.println("\nTesting findSpecial (private)");
            MethodHandle mhSpecialPrivate = lookup.findSpecial(MethodHandleTest.class, "privateTarget", MethodType.methodType(String.class, String.class), MethodHandleTest.class);
            String resultSpecialPrivate = (String) mhSpecialPrivate.invokeExact(instance, "PrivateInput");
            System.out.println("Result: " + resultSpecialPrivate);
            assertEquals("Private: PrivateInput", resultSpecialPrivate);


            // 4. Test findConstructor
            System.out.println("\nTesting findConstructor");
            MethodHandle mhConstructor = lookup.findConstructor(MethodHandleTest.class, MethodType.methodType(void.class, String.class));
            MethodHandleTest newInstance = (MethodHandleTest) mhConstructor.invokeExact("newInstanceViaHandle");
            System.out.println("New instance state: " + newInstance.state);
            assertNotNull(newInstance);
            assertEquals("newInstanceViaHandle", newInstance.state);

        }
    }
}