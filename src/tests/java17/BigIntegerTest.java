package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.math.BigInteger;

public class BigIntegerTest extends TestJPF {
    @Test
    public void testBigIntegerMethods() {
        if (verifyNoPropertyViolation()) {
            BigInteger num = new BigInteger("97");
            assertTrue(num.isProbablePrime(10));
        }
    }
}
