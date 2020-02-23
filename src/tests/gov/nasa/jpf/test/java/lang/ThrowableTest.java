package gov.nasa.jpf.test.java.lang;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;

public class ThrowableTest extends TestJPF {
  @Test
  public void testSetCause() {
    if(verifyNoPropertyViolation()) {
      RuntimeException e = new RuntimeException();
      e.initCause(new NullPointerException());
    }
  }
}
