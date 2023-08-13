package fault.injection.examples;

import gov.nasa.jpf.vm.Verify;

/*
 * International Standard Book Number (ISBN) error detection
 * BCD variant
 */
public class BCDEncodedISBN {

    // get the i-th (indexed from 0) decimal number
    static int getNumber (long digits, int i) {
        return (int) ((digits >> (i << 2)) & 15);
    }

    // calculate the check digit for ISBN-10 based on the first 9 digits
    public static int calculate10 (long digits) {
        int s = 0, t = 0;
        for (int i = 0; i < 9; ++i) {
            int digit = getNumber(digits, i);
            t += digit;
            s += t;
        }
        return (11 - (s + t) % 11) % 11;
    }

    // ISBN-10 check
    public static boolean check10 (long digits) {
        // invalid if more than 10 decimal numbers
        if ((digits >> 40) != 0)
            return false;
        for (int i = 0; i < 10; ++i) {
            if (getNumber(digits, i) > 10 || (getNumber(digits, i) == 10 && i != 9))
                return false;
        }
        return getNumber(digits, 9) == calculate10(digits);
    }

    // calculate the check digit for ISBN-13 based on the first 12 digits
    public static int calculate13 (long digits) {
        int s = 0, t = 1;
        for (int i = 0; i < 12; ++i) {
            s += getNumber(digits, i) * t;
            t = 4 - t;
        }
        return (10 - s % 10) % 10;
    }

    // ISBN-13 check
    public static boolean check13 (long digits) {
        // invalid if more than 13 decimal numbers
        if ((digits >> 52) != 0)
            return false;
        for (int i = 0; i < 13; ++i) {
            if (getNumber(digits, i) < 0 || getNumber(digits, i) > 9)
                return false;
        }
        return getNumber(digits, 12) == calculate13(digits);
    }

    // given the first 9 digits, generate the 10th, and check that one bit flip will always be detected
    public static void main (String[] args) {
        assert (args[0].length() == 9);
        long digits = 0;
        for (int i = 0; i < 9; ++i) {
            char c = args[0].charAt(i);
            assert (c >= '0' && c <= '9');
            digits |= ((long) (c - '0')) << (i << 2);
        }
        digits |= ((long) calculate10(digits)) << (9 << 2);
        assert (check10(digits));
        digits = Verify.getBitFlip(digits, 2, 40);
        if (check10(digits)) {
            System.out.println("Masked case: " + digits);
        }
    }
}
