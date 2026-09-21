package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class StaticCallToNonStaticTest extends TestJPF {

  @Test
  public void testStaticCallToNonStatic() {
    if (verifyUnhandledException("java.lang.IncompatibleClassChangeError")) {
      D.m(); 
    }
  }
}
