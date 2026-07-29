package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class StringConcatBoxedFloatDoubleTest extends TestJPF {

  @Test
  public void testBoxedFloatConcat() {
    if (verifyNoPropertyViolation()) {
      Float f = 1.5f;
      String result = "f=" + f;
      assertEquals("f=1.5", result);
    }
  }

  @Test
  public void testBoxedDoubleConcat() {
    if (verifyNoPropertyViolation()) {
      Double d = 3.14;
      String result = "d=" + d;
      assertEquals("d=3.14", result);
    }
  }

  @Test
  public void testBoxedFloatAndDoubleConcat() {
    if (verifyNoPropertyViolation()) {
      Float f = 1.5f;
      Double d = 3.14;
      assertEquals("f=1.5,d=3.14", "f=" + f + ",d=" + d);
    }
  }

  @Test
  public void testBoxedFloatSpecialValues() {
    if (verifyNoPropertyViolation()) {
      assertEquals("NaN/-Infinity", "" + Float.NaN + "/" + Float.NEGATIVE_INFINITY);
    }
  }

  @Test
  public void testBoxedDoubleSpecialValues() {
    if (verifyNoPropertyViolation()) {
      assertEquals("NaN/-Infinity", "" + Double.NaN + "/" + Double.NEGATIVE_INFINITY);
    }
  }

  @Test
  public void testNullBoxedFloatDoubleConcat() {
    if (verifyNoPropertyViolation()) {
      Float f = null;
      Double d = null;
      assertEquals("f=null,d=null", "f=" + f + ",d=" + d);
    }
  }

  @Test
  public void testBoxedSpecialValuesConcat() {
    if (verifyNoPropertyViolation()) {
      Float f = Float.NaN;
      Double d = Double.NEGATIVE_INFINITY;
      assertEquals("NaN/-Infinity", "" + f + "/" + d);
    }
  }
}
