package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class StringConcatenationTest extends TestJPF {
    @Test
    public void testStringConcatenation_firstStringAsBmArg() {
        if (verifyNoPropertyViolation()) {
            String world = "world!";
            String actual = "Hello, " + world;
            String expected = "Hello, world!";
            assertEquals(expected, actual);
        }
    }

    @Test
    public void testStringConcatenation_SecondStringAsBmArg() {
        if (verifyNoPropertyViolation()) {
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
    public void testStringConcatenationWithEmptyString() {
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
    public void testStringConcatenationWith_typeDouble() {
        if (verifyNoPropertyViolation()) {
            double d = 17.4;
            String actual = "d has value " + d;
            String expected = "d has value 17.4";
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
            Character ch = new Character('@');
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
	
	@Test
	public void testStringConcatenationWith_toString() {
		if (verifyNoPropertyViolation()) {
			String begin = "Greetings, ";
			Person john = new Person("John");
			String actual = begin + john;
			String expected = "Greetings, John";
			assertEquals(expected, actual);
		}
	}
	
	@Test
	public void testStringConcatenationWith_toStringNested() {
		if (verifyNoPropertyViolation()) {
			String begin = "Greetings, ";
			Person harold = createRobot("Harold");
			String actual = begin + harold;
			String expected = "Greetings, [Robot] Harold";
			assertEquals(expected, actual);
		}
	}
	
	private Person createRobot(String name) {
		return new Robot(name);
	}
	
	private class Person {
		private String name;
		
		private Person(String name) {
			this.name = name;
		}
		
		public String getType() {
			return "human";
		}
		
		@Override
		public String toString() {
			return name;
		}
	}
	
	private class Robot extends Person {
		private String name;
		
		private Robot(String name) {
			super(name);
			this.name = name;
		}
		
		@Override
		public String getType() {
			return "robot";
		}
		
		@Override
		public String toString() {
			return "[Robot] " + name;
		}
	}
}
