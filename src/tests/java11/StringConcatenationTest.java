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

    @Test
    public void testStringConcatenationWith_typeByte() {
        if (verifyNoPropertyViolation()) {
            Byte value = 10;
            String actual = "Value=" + value;
            String expected = "Value=10";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenationWith_typeInt() {
        if (verifyNoPropertyViolation()) {
            int num = 20;
            String actual = "10+" + num + " = 30";
            String expected = "10+20 = 30";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenationWith_typeChar() {
        if (verifyNoPropertyViolation()) {
            char ch = '@';
            String actual = "xyz" + ch + "gmail.com";
            String expected = "xyz@gmail.com";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenationWith_typeFloat() {
        if (verifyNoPropertyViolation()) {
            float num = 99;
            String actual = "Success is " + num + "% failure.";
            String expected = "Success is 99.0% failure.";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenationWith_multipleStrings() {
        if (verifyNoPropertyViolation()) {
            String a = "This ";
            String b = "is ";
            String c = "a ";
            String d = "test ";
            String e = "string.";
            String actual = a + b + c + d + e;
            String expected = "This is a test string.";
            assertEquals(actual, expected);
        }
    }

    @Test
    public void testStringConcatenationWith_mixedTypes() {
        if (verifyNoPropertyViolation()) {
            char ch = '@';
            byte b = 10;
            String name = "xyz";
            char dot = '.';
            int num = 10;
            String provider = "gmail";
            String topLevelDomain = "com";
            String actual = name + b + num + ch + provider + dot + topLevelDomain;
            String expected = "xyz1010@gmail.com";
            assertEquals(expected, actual);
        }
    }
}
