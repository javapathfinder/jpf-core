package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;

/**
 * NativePeer for {@link StackStreamFactory}
 */
public class JPF_java_lang_StackStreamFactory extends NativePeer {

    /**
     * NativePeer method for {@link StackStreamFactory#checkStackWalkModes()}
     */
    @MJI
    public static boolean checkStackWalkModes____Z(MJIEnv env, int cref) {
        // supposed to return false, if StackWalker mode values do not match with JVM
        return true;
    }
}
