package gov.nasa.jpf;

import org.junit.AfterClass;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.jupiter.api.Test;
import java.io.File;




public class StaticCallToNonStaticTest extends TestJPF {

    @Test
public void testStaticCallToNonStatic() {
  D.m();
}

    // Cleanup method to delete the temporary file after tests
    @AfterClass
    public static void cleanUp() {
        // Define the path to the temporary file
        String tempDir = System.getProperty("java.io.tmpdir");
        File tempFile = new File(tempDir, "tempFile.txt");

        // Delete the temporary file after use
        if (tempFile.exists()) {
            boolean deleted = tempFile.delete();
            if (deleted) {
                System.out.println("Temporary file deleted successfully.");
            } else {
                System.out.println("Failed to delete temporary file.");
            }
        }
    }
}
