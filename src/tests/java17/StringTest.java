package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class StringTest extends TestJPF {
    @Test
    public void testStringMethods() {
        if (verifyNoPropertyViolation()) {
            String text = "Hello\nWorld";

            assertEquals("    Hello\n    World\n", text.indent(4));

            assertEquals("HELLO\nWORLD", text.transform(String::toUpperCase));

            assertEquals("Hello, Java 17!", "Hello, %s!".formatted("Java 17"));
        }
    }
}
