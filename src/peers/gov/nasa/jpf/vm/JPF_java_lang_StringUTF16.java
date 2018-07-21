package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;

import java.nio.ByteOrder;

public class JPF_java_lang_StringUTF16 extends NativePeer {

    /**
     * NativePeer method for {@link StringUTF16#isBigEndian()}
     */
    @MJI
    public static boolean isBigEndian____Z (MJIEnv env, int cref) {
        return ByteOrder.nativeOrder() == ByteOrder.BIG_ENDIAN;
    }
}
