package fault.injection.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CRCTest {
    CRC crc;
    @Test(expected = AssertionError.class)
    public void testIllegal () {
        crc = new CRC("102");
    }
    @Test
    public void testNull () {
        crc = new CRC("101");
        assertTrue(!crc.check(null, null));
    }
    @Test
    public void testPositive () {
        crc = new CRC("1011");
        assertTrue(crc.check("11010011101100", "100"));
    }
    @Test
    public void testNegative () {
        crc = new CRC("1011");
        assertTrue(!crc.check("11010011101100", "101"));
        assertTrue(!crc.check("11010011101100", "200"));
        assertTrue(!crc.check("11010011101100", null));
    }
}
