package fault.injection.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class LongEncodedCRCTest {
    LongEncodedCRC crc;
    @Test
    public void testPositive () {
        crc = new LongEncodedCRC("1011");
        assertTrue(crc.check(14, 0b11010011101100100l));
    }
    @Test
    public void testNegative () {
        crc = new LongEncodedCRC("1011");
        assertTrue(!crc.check(14, 0b11010011101100101l));
    }
}
