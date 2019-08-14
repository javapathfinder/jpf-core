package java11;

import org.junit.*;
import gov.nasa.jpf.util.test.TestJPF;

public class StringConcatenationTest extends TestJPF {
    @Test
    public void testStringConcatenation_firstStringAsBmArg(){
        if(verifyNoPropertyViolation()) {
            String world = "world!";
            String actual = "Hello, " + world;
            String expected = "Hello, world!";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenation_SecondStringAsBmArg(){
        if(verifyNoPropertyViolation()) {
            String world = "world, ";
            String actual = world + "Hello!";
            String expected = "world, Hello!";
            assertEquals(expected, actual);
        }
    }
}
