package gov.nasa.jpf.test.java.util;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.logging.StreamHandler;
import java.io.ByteArrayOutputStream;
public class LoggerTest extends TestJPF {
  @Test
  public void testGetLogger() {
    if (verifyNoPropertyViolation()) {
      Logger log = Logger.getLogger("testLogger");
      assertEquals("testLogger", log.getName());
      Logger log2 = Logger.getLogger("testLogger");
      assertTrue(log == log2);
    }
  }
  @Test
  public void testLogOutput() {
    if (verifyNoPropertyViolation()) {
      Logger log = Logger.getLogger("captureLogger");
      ByteArrayOutputStream baos = new ByteArrayOutputStream();
      StreamHandler handler = new StreamHandler(baos, null);
      log.addHandler(handler);
      String message = "This is a test warning";
      log.warning(message);
      handler.flush();
      String output = baos.toString();
      assertTrue(output.contains("WARNING"));
      assertTrue(output.contains(message));
    }
  }
}
