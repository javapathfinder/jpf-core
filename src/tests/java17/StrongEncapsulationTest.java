package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.lang.reflect.Method;

public class StrongEncapsulationTest extends TestJPF {

    @Test
    public void testAccessPrivateMethods() {
        try {
            Method method = String.class.getDeclaredMethod("valueOf", Object.class);
            method.setAccessible(true);

            String result = (String) method.invoke(null, 42);
            assertEquals("42", result);
        } catch (Exception e) {
            System.out.println("Access Denied: " + e.getMessage());
            assertTrue(e instanceof IllegalAccessException);
        }
    }
}
