package jdk.internal.misc;

import java.util.Properties;

public class VM {
    
    private static native void initialize();
    
    public static native void saveAndRemoveProperties(Properties props);
    
    public static native String getSavedProperty(String key);
    
    static {
        initialize();
    }
}