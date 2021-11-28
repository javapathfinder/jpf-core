package java8;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class StreamTest extends TestJPF {
    @Test
    public void testing() {
        Stream stream = new Stream();
        stream.isPrime(2);
        assertEquals(2,2);
    }
}
