package fault.injection.examples;

/*
 * Cyclic redundancy check algorithm
 * Assume the CRC algorithm is performed in GF(2)
 */
public class CRC {

    int n; // divisor's length
    int[] d; // the divisor

    CRC (String divisor) {
        n = divisor.length();
        d = new int[n];
        for (int i = 0; i < n; ++i) {
            assert (divisor.charAt(i) == '0' || divisor.charAt(i) == '1');
            d[i] = divisor.charAt(n-1-i) - '0';
        }
    }

    // calculate the remainder of the given bit string
    public String remainder (String s) {
        // convert string to int array
        String padded = new String(s);
        for (int i = 0; i < n-1; ++i)
            padded += "0";
        int len = padded.length();
        int[] r = new int[len];
        for (int i = 0; i < len; ++i)
            r[i] = padded.charAt(len-1-i) - '0';
        for (int i = len-1; i >= n-1; --i) {
            if (r[i] == 1) {
                for (int j = 0; j < n; ++j)
                    r[i-j] ^= d[n-1-j];
            }
        }
        String res = "";
        for (int i = n-2; i >= 0; --i)
            res += r[i];
        return res;
    }

    // check the data integrity
    public boolean check (String s, String r) {
        if (s == null)
            return false;
        for (char c : s.toCharArray()) {
            if (c != '0' && c != '1')
                return false;
        }
        return remainder(s).equals(r);
    }
}
