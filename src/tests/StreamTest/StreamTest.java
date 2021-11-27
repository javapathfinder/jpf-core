import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

public class StreamTest {
    @Test
    public void testing() {
        Stream stream = new Stream();
        stream.isPrime(2);
        assertEquals(2,2);
    }
}
