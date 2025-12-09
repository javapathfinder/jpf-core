package gov.nasa.jpf.test.java.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.util.logging.Logger;

public class LoggerTest extends TestJPF {

    @Test
    public void testGetLogger() {
        if (verifyNoPropertyViolation()) {
            System.out.println("--- calling getLogger");
            Logger log = Logger.getLogger("testLogger");
            assertNotNull(log);
            System.out.println("--- success: " + log.getName());
        }
    }
}