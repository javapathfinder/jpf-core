package fault.injection.examples;

import static org.junit.Assert.assertTrue;

import org.junit.Test;

public class CheckISBNTest {
    @Test
    public void test10Null () {
        assertTrue(!CheckISBN.check10(null));
    }
    @Test
    public void test10InvalidLength () {
        int[] digits = new int[8];
        assertTrue(!CheckISBN.check10(digits));
    }
    @Test
    public void test10InvalidDigit () {
        int[] digits = {0, 3, 0, 6, 4, 0, 6, 1, 6, 11};
        assertTrue(!CheckISBN.check10(digits));
    }
    @Test
    public void test10Negative () {
        int[] digits = {0, 3, 0, 6, 4, 0, 6, 1, 5, 5};
        assertTrue(!CheckISBN.check10(digits));
    }
    @Test
    public void test10Positive () {
        int[] digits = {0, 3, 0, 6, 4, 0, 6, 1, 5, 2};
        assertTrue(CheckISBN.check10(digits));
    }
    @Test
    public void test13Null () {
        assertTrue(!CheckISBN.check13(null));
    }
    @Test
    public void test13InvalidLength () {
        int[] digits = new int[20];
        assertTrue(!CheckISBN.check13(digits));
    }
    @Test
    public void test13InvalidDigit () {
        int[] digits = {10, 7, 7, 0, 3, 0, 6, 4, 0, 6, 1, 5, 7};
        assertTrue(!CheckISBN.check13(digits));
    }
    @Test
    public void test13Negative () {
        int[] digits = {9, 7, 8, 0, 3, 6, 6, 4, 0, 6, 1, 5, 7};
        assertTrue(!CheckISBN.check13(digits));
    }
    @Test
    public void test13Positive () {
        int[] digits = {9, 7, 8, 0, 3, 0, 6, 4, 0, 6, 1, 5, 7};
        assertTrue(CheckISBN.check13(digits));
    }
}
