/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 * 
 *        http://www.apache.org/licenses/LICENSE-2.0. 
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and 
 * limitations under the License.
 */
package java.lang;

import java.io.BufferedInputStream;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.channels.Channel;
import java.util.Map;
import java.util.Properties;

import sun.misc.JavaLangAccess;
import sun.misc.SharedSecrets;
import sun.nio.ch.Interruptible;
import sun.reflect.ConstantPool;
import sun.reflect.annotation.AnnotationType;


public class System {

  static Properties properties;

  public static InputStream in = new InputStream() {
    public int available() { return 0; }
    public int read() { return -1; }
    public int read(byte[] b, int off, int len) { return 0; }
    public int read(byte[] b) { return 0; }
  };

  public static PrintStream out;
  public static PrintStream err;
  
  static {

    out = createSystemOut();
    err = createSystemErr();

    properties = new Properties();

    String[] kv = getKeyValuePairs();
    for (int i=0; i<kv.length; i+=2){
      String key = kv[i];
      String val = kv[i+1];

      if (key != null && val != null) {
        properties.put(kv[i], kv[i+1]);
      }
    }

    // this is the Java 6 sun.misc.SharedSecrets backdoor mechanism which I
    // would have prefered not to learn about. It's a mess WRT Java 1.5 / 6 compatibility
    // <2do> - most if this isn't supported yet
    SharedSecrets.setJavaLangAccess( createJavaLangAccess());

    // <2do> this is an approximation that isn't particularly safe since we don't
    // initialize sun.misc.VM
    //sun.misc.VM.booted();
  }

  static JavaLangAccess createJavaLangAccess () {
    return new JavaLangAccess(){
      @Override
	public ConstantPool getConstantPool(Class<?> cls) {
        throw new UnsupportedOperationException("JavaLangAccess.getConstantPool() not supported yet");
        //return cls.getConstantPool();
      }
      @Override
	public void setAnnotationType(Class<?> cls, AnnotationType type) {
        throw new UnsupportedOperationException("JavaLangAccess.setAnnotationType() not supported yet");
        //cls.setAnnotationType(type);
      }
      @Override
	public AnnotationType getAnnotationType(Class<?> cls) {
        throw new UnsupportedOperationException("JavaLangAccess.getAnnotationType() not supported yet");
        //return cls.getAnnotationType();
      }
      @Override
	public <E extends Enum<E>> E[] getEnumConstantsShared(Class<E> cls) {
        return cls.getEnumConstantsShared();
      }
      @Override
	public void blockedOn(Thread t, Interruptible b) {
        throw new UnsupportedOperationException("JavaLangAccess.blockedOn() not supported yet");
        //t.blockedOn(b);
      }
      @Override
	public void registerShutdownHook(int slot, Runnable r) {
        throw new UnsupportedOperationException("JavaLangAccess.registerShutdownHook() not supported yet");
      }
      @Override
	public int getStackTraceDepth(Throwable t) {
        return t.getStackTraceDepth();
      }
      @Override
	public StackTraceElement getStackTraceElement(Throwable t, int i) {
        StackTraceElement[] st = t.getStackTrace();
        return st[i];
      }
    };
  }

  static private native String[] getKeyValuePairs();

  static private native PrintStream createSystemOut();
  static private native PrintStream createSystemErr();

  //--- standard streams
  public static void setIn (InputStream newIn) {
    in = newIn;
  }

  public static void setOut (PrintStream newOut){
    out = newOut;
  }

  public static void setErr (PrintStream newErr) {
    err = newErr;
  }

  public static Channel inheritedChannel() {
    throw new UnsupportedOperationException("inheritedChannel() not yet supported");
  }

  //--- misc
  public static native void exit (int rc);
  public static native void arraycopy (Object src, int srcPos,
                                       Object dst, int dstPos, int len);
  public static native void gc();
  public static native void runFinalization();
  public static native void runFinalizersOnExit(boolean cond);
  static native Class<?> getCallerClass();
  public static native int identityHashCode (Object o);


  //--- time management
  public static native long currentTimeMillis();
  public static native long nanoTime();

  //--- environment
  public static native String getenv (String key);
  public static Map<String,String> getenv() {
    throw new UnsupportedOperationException("getenv() not yet supported");
  }

  //--- security manager
  static SecurityManager securityManager;

  public static void setSecurityManager (SecurityManager newManager) {
    securityManager = newManager;
  }

  public static SecurityManager getSecurityManager() {
    return securityManager;
  }

  //--- system properties

  public static Properties getProperties() {
    return properties;
  }
  public static void setProperties(Properties newProps){
    properties = newProps;
  }

  public static String getProperty (String key) {
    return properties.getProperty(key);
  }

  public static String getProperty (String key, String def){
    String v = properties.getProperty(key);
    if (v == null){
      return def;
    } else {
      return v;
    }
  }

  public static String setProperty (String key, String value){
    String oldVal = properties.getProperty(key);
    properties.put(key,value);
    return oldVal;
  }

  public static String clearProperty (String key) {
    String oldVal = properties.getProperty(key);
    properties.remove(key);
    return oldVal;
  }

  //--- native libs
  public static void load (String pathName) {
    // nothing, we don't have native libs
    // (maybe we could on-demand load peers?)
  }

  public static void loadLibrary (String libName){
    // nothing yet
  }

  public static String mapLibraryName (String libName){
    // just a placeholder (Unix flavor)
    return "lib" + libName + ".so";
  }

}
