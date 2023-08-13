package fault.injection.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class BCDEncodedISBNTest {
    @Test
    public void test10InvalidLength () {
        long digits = 0x82749173801l;
        assertTrue(!BCDEncodedISBN.check10(digits));
    }
    @Test
    public void test10InvalidDigit () {
        long digits = 0xb616046030l;
        assertTrue(!BCDEncodedISBN.check10(digits));
    }
    @Test
    public void test10Negative () {
        long digits = 0x5516046030l;
        assertTrue(!BCDEncodedISBN.check10(digits));
    }
    @Test
    public void test10Positive () {
        long digits = 0x2516046030l;
        assertTrue(BCDEncodedISBN.check10(digits));
    }
    @Test
    public void test13InvalidLength () {
        long digits = 0x193972993818233l;
        assertTrue(!BCDEncodedISBN.check13(digits));
    }
    @Test
    public void test13InvalidDigit () {
        long digits = 0x751604603077al;
        assertTrue(!BCDEncodedISBN.check13(digits));
    }
    @Test
    public void test13Negative () {
        long digits = 0x7516046630879l;
        assertTrue(!BCDEncodedISBN.check13(digits));
    }
    @Test
    public void test13Positive () {
        long digits = 0x7516046030879l;
        assertTrue(BCDEncodedISBN.check13(digits));
    }
}
