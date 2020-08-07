package amgadTests;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class DispatchTest extends TestJPF {
  class X1 {
    public int callToX1() {
      return f();
    }

    private int f() {
      return 1;
    }
  }

  class X2 extends X1 {
    public int f() {
      return 2;
    }
  }

  class X3 extends X2 {}

  @Test
  public void dispatchTest() {
    int expected = 1;
    assertEquals(expected, (new X3()).callToX1());
  }
}