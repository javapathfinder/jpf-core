package gov.nasa.jpf.report;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * This class tests those methods of the ConsolePublisher class that are relevant
 * to the report.console.start property.
 *
 * @author Franck van Breugel
 */
public class ConsolePublisherTest extends TestJPF {
    /**
     * Runs the tests with the given names.
     *
     * @param testMethods names of the test methods to be run.
     */
    public static void main(String[] testMethods) {
	runTestsOfThisClass(testMethods);
    }

    /**
     * Tests the value dtg for the property report.console.start.
     */
    @Test
    public void testDTG(){
	PrintStream out = System.out;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(stream));
	if (verifyNoPropertyViolation("+report.publisher=console", "+report.console.start=dtg")){
	    // do nothing
	} else {
	    System.setOut(out);
	    assertTrue("output does not contain \"started:\"", stream.toString().contains("started: "));
	}
    }

    /**
     * Tests the value jpf for the property report.console.start.
     */
    @Test
    public void testJPF(){
	PrintStream out = System.out;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(stream));
	if (verifyNoPropertyViolation("+report.publisher=console", "+report.console.start=jpf")){
	    // do nothing
	} else {
	    System.setOut(out);
	    assertTrue("output does not contain \"JavaPathfinder", stream.toString().contains("JavaPathfinder"));
	}
    }

    /**
     * Tests the value platform for the property report.console.start.
     */
    @Test
    public void testPlatform(){
	PrintStream out = System.out;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(stream));
	if (verifyNoPropertyViolation("+report.publisher=console", "+report.console.start=platform")){
	    // do nothing
	} else {
	    System.setOut(out);
	    assertTrue("output does not contain \"hostname:\"", stream.toString().contains("hostname: "));
	    assertTrue("output does not contain \"arch:\"", stream.toString().contains("arch: "));
	    assertTrue("output does not contain \"os:\"", stream.toString().contains("os: "));
	    assertTrue("output does not contain \"java:\"", stream.toString().contains("java: "));
	}
    }
    
       /**
     * Tests the value sut for the property report.console.start.
     */
    @Test
    public void testSUT(){
	PrintStream out = System.out;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(stream));
	if (verifyNoPropertyViolation("+report.publisher=console", "+report.console.start=sut")){
	    // do nothing
	} else {
	    System.setOut(out);
	    assertTrue("output does not contain \"system under test\"", stream.toString().contains("system under test"));
	}
    }

    /**
     * Tests the value user for the property report.console.start.
     */
    @Test
    public void testUser(){
	PrintStream out = System.out;
	ByteArrayOutputStream stream = new ByteArrayOutputStream();
	System.setOut(new PrintStream(stream));
	if (verifyNoPropertyViolation("+report.publisher=console", "+report.console.start=user")){
	    // do nothing
	} else {
	    System.setOut(out);
	    assertTrue("output does not contain \"user:\"", stream.toString().contains("user: "));
	}
    }
}
