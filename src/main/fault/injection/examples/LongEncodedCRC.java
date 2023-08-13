package fault.injection.examples;

import gov.nasa.jpf.vm.Verify;

/*
 * Cyclic redundancy check algorithm
 * Assume the CRC algorithm is performed in GF(2)
 * Assume the data length is less than 64 bits, and encode them in a variable of type long
 */
public class LongEncodedCRC {

    int n; // divisor's length
    long d; // the divisor

    LongEncodedCRC (String divisor) {
        n = divisor.length();
        d = 0;
        for (int i = 0; i < n; ++i) {
            assert (divisor.charAt(i) == '0' || divisor.charAt(i) == '1');
            d |= ((long) (divisor.charAt(n-1-i) - '0')) << i;
        }
    }

    LongEncodedCRC (long _d) {
        d = _d;
        assert (d != 0);
        for (int i = 0; i < 64; ++i)
            if ((d >> i & 1) == 1) {
                n = i;
            }
    }

    // calculate the remainder of the given bit string
    public long remainder (int len, long s) {
        long r = s << (n-1);
        len += n-1;
        for (int i = len-1; i >= n-1; --i) {
            if ((r >> i & 1) == 1) {
                r ^= d << (i-n+1);
            }
        }
        return r;
    }

    // check the data integrity
    public boolean check (int len, long dataWithCheckBits) {
        long data = dataWithCheckBits >> (n-1);
        long checkBits = dataWithCheckBits & ((1 << (n-1)) - 1);
        return remainder(len, data) == checkBits;
    }

    // check that bit flips in the data can be detected
    public static void main (String[] args) {
        LongEncodedCRC crc = new LongEncodedCRC("1011");
        int len = 14;
        long dataWithCheckBits = 0b11010011101100100l;
        assert (crc.check(len, dataWithCheckBits));
        long flippedData = Verify.getBitFlip(dataWithCheckBits, 2, 17);
        if (crc.check(len, flippedData)) {
            System.out.println("Masked case: " + flippedData);
        }
    }
}
