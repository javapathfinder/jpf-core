package fault.injection.examples;

/*
 * International Standard Book Number (ISBN) error detection
 */
public class CheckISBN {
    // calculate the check digit for ISBN-10 based on the first 9 digits
    public static int calculate10 (int[] digits) {
        int s = 0, t = 0;
        for (int i = 0; i < 9; ++i) {
            t += digits[i];
            s += t;
        }
        return (11 - (s + t) % 11) % 11;
    }
    // ISBN-10 check
    public static boolean check10 (int[] digits) {
        if (digits == null || digits.length != 10)
            return false;
        for (int i = 0; i < 10; ++i) {
            if (digits[i] < 0 || digits[i] > 10 || (digits[i] == 10 && i != 9))
                return false;
        }
        return digits[9] == calculate10(digits);
    }

    // calculate the check digit for ISBN-13 based on the first 12 digits
    public static int calculate13 (int[] digits) {
        int s = 0, t = 1;
        for (int i = 0; i < 12; ++i) {
            s += digits[i] * t;
            t = 4 - t;
        }
        return (10 - s % 10) % 10;
    }
    // ISBN-13 check
    public static boolean check13 (int[] digits) {
        if (digits == null || digits.length != 13)
            return false;
        for (int i = 0; i < 13; ++i) {
            if (digits[i] < 0 || digits[i] > 9)
                return false;
        }
        return digits[12] == calculate13(digits);
    }
}
