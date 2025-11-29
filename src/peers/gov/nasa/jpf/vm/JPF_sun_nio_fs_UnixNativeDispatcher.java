package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;

public class JPF_sun_nio_fs_UnixNativeDispatcher extends NativePeer{

    @MJI
    public int init____I (MJIEnv env, int clsRef) {
        return 0;
    }

    @MJI
    public int getcwd_____3B (MJIEnv env, int clsRef) {
        String userDir = System.getProperty("user.dir");
        if(userDir==null) userDir = ".";
        return env.newByteArray(userDir.getBytes());
    }
}
