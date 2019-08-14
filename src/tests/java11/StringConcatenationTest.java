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

    @Test
    public void testStringConcatenation() {
        if (verifyNoPropertyViolation()) {
            String sunny = "sunny";
            String tomorrow = "tomorrow";
            String actual = "The weather will be " + sunny + " " + tomorrow + ".";
            String expected = "The weather will be sunny tomorrow.";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenationWithEmptyString(){
        if (verifyNoPropertyViolation()) {
            String[] strings = {"The", "weather", "will", "be", "sunny", "tomorrow"};
            String actual = "";
            for (int i = 0; i < strings.length; i++) {
                actual = actual + ((i == 0) ? "" : " ") + strings[i];
            }
            actual = actual + ".";
            String expected = "The weather will be sunny tomorrow.";
            assertEquals(expected, actual);

            actual = "" + "" + "Hello";
            expected = "Hello";
            assertEquals(expected, actual);
        }
    }
}
