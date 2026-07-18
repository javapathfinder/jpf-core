package gov.nasa.jpf.vm;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import java.util.Properties;

/**
 * MJI NativePeer class for jdk.internal.misc.VM
 */
public class JPF_jdk_internal_misc_VM extends NativePeer {
    
    private static Properties savedProps = new Properties();

    @MJI
    public void initialize____V(MJIEnv env, int clsObjRef) {
        // Initialize saved properties from system properties
        Properties sysProps = System.getProperties();
        for (String key : sysProps.stringPropertyNames()) {
            savedProps.setProperty(key, sysProps.getProperty(key));
        }
    }

    @MJI
    public void saveAndRemoveProperties__Ljava_util_Properties_2__V(MJIEnv env, int clsObjRef, int propsRef) {
        if (propsRef != MJIEnv.NULL) {
            // Get the Properties object from JPF heap
            ElementInfo eiProps = env.getElementInfo(propsRef);
            // For now, just copy all current system properties
            // A full implementation would iterate through the JPF Properties object
            Properties sysProps = System.getProperties();
            for (String key : sysProps.stringPropertyNames()) {
                savedProps.setProperty(key, sysProps.getProperty(key));
            }
        }
    }

    @MJI
    public int getSavedProperty__Ljava_lang_String_2__Ljava_lang_String_2(MJIEnv env, int clsObjRef, int keyRef) {
        String key = env.getStringObject(keyRef);
        String value = savedProps.getProperty(key);
        return (value != null) ? env.newString(value) : MJIEnv.NULL;
    }

    @MJI
    public void initializeFromArchive(MJIEnv env, int clsObjRef, int cRef) {
        // We don't support CDS so we don't need to implement it,
        // which doesn't affect our correctness.
    }
}