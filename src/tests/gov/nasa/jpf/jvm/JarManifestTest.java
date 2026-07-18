package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * Test for Issue #473: Support for package metadata in JarClassFileContainer.
 * This test verifies that JPF correctly loads Implementation-Version from a JAR manifest.
 */
public class JarManifestTest extends TestJPF {

    @Test
    public void testManifestVersion() {
    // Updated path based on your image
        if (verifyNoPropertyViolation("+classpath=src/tests/test.jar")) {
            try {
                // Replace "testjar.Hello" with the EXACT class name inside test.jar
                Class<?> clazz = Class.forName("testjar.Hello"); 
                Package pkg = clazz.getPackage();
                
                if (pkg == null) {
                    System.out.println("DEBUG: Package object is null!");
                } else {
                    String version = pkg.getImplementationVersion();
                    System.out.println("JPF Implementation-Version: " + version);
                    assertEquals("1.2.3", version);
                }
            } catch (Exception e) {
                // This will print the exception to the JPF console 
                // so you can see why it's failing.
                e.printStackTrace();
            }
        }
    } 
}