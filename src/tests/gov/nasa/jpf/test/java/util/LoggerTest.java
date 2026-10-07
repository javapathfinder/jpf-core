package gov.nasa.jpf.test.java.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.util.logging.Logger;

/**
 * Regression test for issue #341:
 * java.util.logging.Logger.getLogger crashes in JPF due to heavy
 * JDK initialization (LogManager, Thread, AccessController, file system).
 *
 * With model classes for Logger, LogManager, and Level, these calls
 * should work without property violations.
 */
public class LoggerTest extends TestJPF {

  @Test
  public void testGetLogger() {
    if (verifyNoPropertyViolation()) {
      Logger log = Logger.getLogger("testLogger");
      assertNotNull(log);
      assertEquals("testLogger", log.getName());
    }
  }

  @Test
  public void testGetLoggerWithResourceBundle() {
    if (verifyNoPropertyViolation()) {
      Logger log = Logger.getLogger("bundleLogger", "myBundle");
      assertNotNull(log);
      assertEquals("bundleLogger", log.getName());
    }
  }

  @Test
  public void testLoggerMethods() {
    if (verifyNoPropertyViolation()) {
      Logger logger = Logger.getLogger("blah");
      logger.info("hello");
      logger.warning("warn");
      logger.severe("sev");
      logger.config("cfg");
      logger.fine("fine");
      logger.finer("finer");
      logger.finest("finest");
    }
  }

  @Test
  public void testGetAnonymousLogger() {
    if (verifyNoPropertyViolation()) {
      Logger log = Logger.getAnonymousLogger();
      assertNotNull(log);
    }
  }

  @Test
  public void testSameLoggerReturned() {
    if (verifyNoPropertyViolation()) {
      Logger log1 = Logger.getLogger("shared");
      Logger log2 = Logger.getLogger("shared");
      assertTrue("Same logger should be returned for same name", log1 == log2);
    }
  }
}
