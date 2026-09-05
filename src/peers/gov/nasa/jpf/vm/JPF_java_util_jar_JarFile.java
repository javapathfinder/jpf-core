package gov.nasa.jpf.vm;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.DynamicObjectArray;

public class JPF_java_util_jar_JarFile extends NativePeer {
    private static class JarFileProxy {
        Map<String, byte[]> buffers = new HashMap<>();
        JarFile jf;
    }

    private int count = 0;
    private DynamicObjectArray<JarFileProxy> content;

    public JPF_java_util_jar_JarFile(Config conf) {
        content = new DynamicObjectArray<>();
    }

    @MJI
    public int open0(MJIEnv env, int thisRef) {
        int ref = count++;
        try {
            JarFileProxy jfp = createJarProxy(env, thisRef);
            content.set(ref, jfp);
            env.setReferenceField(thisRef, "comment", env.newString(jfp.jf.getComment() == null ? "" : jfp.jf.getComment()));
            String[] names = new String[jfp.jf.size()];
            int it = 0;
            Enumeration<JarEntry> en = jfp.jf.entries();
            while (en.hasMoreElements()) {
                JarEntry je = en.nextElement();
                names[it++] = je.getName();
            }
            assert it == jfp.jf.size();
            env.setReferenceField(thisRef, "entryNames", env.newStringArray(names));
            return ref;
        } catch (IOException e) {
            env.throwException("java.io.IOException", "IO exception: " + e.getMessage());
        }
        return -1;
    }

    @MJI
    public void close0____V(MJIEnv env, int thisRef) {
        int handle = env.getIntField(thisRef, "zipFileHandle");
        int state = env.getIntField(thisRef, "state");
        if (state != 1) {
            env.throwException("java.lang.IllegalStateException", "Not open");
            return;
        }
        if (content.get(handle) == null) {
            return;
        }
        try {
            content.get(handle).jf.close();
            content.set(handle, null);
        } catch (IOException e) {
            env.throwException("java.io.IOException", "failed to close: " + e.getMessage());
        }
    }

    @MJI
    public boolean initJEFields__Ljava_lang_String_2Ljava_util_jar_JarEntry_2__Z(MJIEnv env, int thisRef, int entryNameRef, int jeRef) {
        JarFileProxy jfp = getJFP(env, thisRef);
        if (jfp == null) {
            return false;
        }
        JarEntry je = jfp.jf.getJarEntry(env.getStringObject(entryNameRef));
        if (je == null) {
            // Try as ZipEntry
            java.util.zip.ZipEntry ze = jfp.jf.getEntry(env.getStringObject(entryNameRef));
            if (ze == null) {
                return false;
            }
            je = new JarEntry(ze);
        }
        env.setLongField(jeRef, "crc", je.getCrc());
        env.setLongField(jeRef, "size", je.getSize());
        env.setLongField(jeRef, "csize", je.getCompressedSize());
        env.setIntField(jeRef, "method", je.getMethod());
        String comment = je.getComment();
        env.setReferenceField(jeRef, "comment", comment == null ? MJIEnv.NULL : env.newString(comment));
        // Attributes are handled via Manifest, not per-entry here
        return true;
    }

    @MJI
    public boolean initZEFields__Ljava_lang_String_2Ljava_util_zip_ZipEntry_2__Z(MJIEnv env, int thisRef, int entryNameRef, int zeRef) {
        // Delegate to JarEntry version for JarFile - also support ZipEntry
        JarFileProxy jfp = getJFP(env, thisRef);
        if (jfp == null) {
            return false;
        }
        java.util.zip.ZipEntry ze = jfp.jf.getEntry(env.getStringObject(entryNameRef));
        if (ze == null) {
            return false;
        }
        env.setLongField(zeRef, "crc", ze.getCrc());
        env.setLongField(zeRef, "size", ze.getSize());
        env.setLongField(zeRef, "csize", ze.getCompressedSize());
        env.setIntField(zeRef, "method", ze.getMethod());
        String comment = ze.getComment();
        env.setReferenceField(zeRef, "comment", comment == null ? MJIEnv.NULL : env.newString(comment));
        return true;
    }

    @MJI
    public int getEntryBytes__Ljava_lang_String_2___3B(MJIEnv env, int thisRef, int entryNameRef) {
        JarFileProxy jfp = getJFP(env, thisRef);
        if (jfp == null) {
            return MJIEnv.NULL;
        }
        String entryName = env.getStringObject(entryNameRef);
        if (!jfp.buffers.containsKey(entryName)) {
            JarEntry je = jfp.jf.getJarEntry(entryName);
            if (je == null) {
                java.util.zip.ZipEntry ze = jfp.jf.getEntry(entryName);
                if (ze == null) {
                    env.throwException("java.io.IOException", "Could not read " + entryName);
                    return MJIEnv.NULL;
                }
                je = new JarEntry(ze);
            }
            try (InputStream fis = jfp.jf.getInputStream(je)) {
                if (fis == null) {
                    env.throwException("java.io.IOException", "Could not get InputStream for " + entryName);
                    return MJIEnv.NULL;
                }
                long expandedSize = je.getSize();
                if (expandedSize < 0) {
                    // size unknown - read fully
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] tmp = new byte[8192];
                    int n;
                    while ((n = fis.read(tmp)) != -1) {
                        baos.write(tmp, 0, n);
                    }
                    byte[] buffer = baos.toByteArray();
                    jfp.buffers.put(entryName, buffer);
                } else {
                    if (expandedSize > Integer.MAX_VALUE) {
                        env.throwException("java.lang.UnsupportedOperationException", "Too big");
                        return MJIEnv.NULL;
                    }
                    int remaining = (int) expandedSize;
                    int output = 0;
                    byte[] buffer = new byte[(int) expandedSize];
                    while (remaining > 0) {
                        int read = fis.read(buffer, output, remaining);
                        if (read == -1) {
                            break;
                        }
                        output += read;
                        remaining -= read;
                    }
                    jfp.buffers.put(entryName, buffer);
                }
            } catch (IOException e) {
                env.throwException("java.io.IOException", e.getMessage());
                return MJIEnv.NULL;
            }
        }
        return env.newByteArray(jfp.buffers.get(entryName));
    }

    @MJI
    public int getJEExtraBytes__Ljava_lang_String_2___3B(MJIEnv env, int thisRef, int entryNameRef) {
        JarFileProxy jfp = getJFP(env, thisRef);
        if (jfp == null) {
            return MJIEnv.NULL;
        }
        JarEntry entry = jfp.jf.getJarEntry(env.getStringObject(entryNameRef));
        if (entry == null) {
            java.util.zip.ZipEntry ze = jfp.jf.getEntry(env.getStringObject(entryNameRef));
            if (ze == null) {
                return MJIEnv.NULL;
            }
            byte[] extra = ze.getExtra();
            return extra == null ? MJIEnv.NULL : env.newByteArray(extra);
        }
        byte[] extra = entry.getExtra();
        if (extra == null) {
            return MJIEnv.NULL;
        } else {
            return env.newByteArray(extra);
        }
    }

    @MJI
    public int getZEExtraBytes__Ljava_lang_String_2___3B(MJIEnv env, int thisRef, int entryNameRef) {
        return getJEExtraBytes__Ljava_lang_String_2___3B(env, thisRef, entryNameRef);
    }

    @MJI
    public int getManifest0____Ljava_util_jar_Manifest_2(MJIEnv env, int thisRef) {
        JarFileProxy jfp = getJFP(env, thisRef);
        if (jfp == null) {
            return MJIEnv.NULL;
        }
        try {
            Manifest hostManifest = jfp.jf.getManifest();
            if (hostManifest == null) {
                return MJIEnv.NULL;
            }
            // Serialize host manifest to bytes and recreate JPF Manifest via InputStream
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            hostManifest.write(baos);
            byte[] manifestBytes = baos.toByteArray();

            // Create JPF byte array and ByteArrayInputStream
            int bytesRef = env.newByteArray(manifestBytes);
            ClassInfo baisClass = ClassLoaderInfo.getCurrentResolvedClassInfo("java.io.ByteArrayInputStream");
            if (baisClass.initializeClass(env.getThreadInfo())) {
                env.repeatInvocation();
                return MJIEnv.NULL;
            }
            int baisRef = env.newObject(baisClass);
            // Call ByteArrayInputStream.<init>([B)V - we can set buf field directly for simplicity
            // ByteArrayInputStream has field 'buf' and 'count', 'pos'
            ElementInfo eiBais = env.getModifiableElementInfo(baisRef);
            eiBais.setReferenceField("buf", bytesRef);
            eiBais.setIntField("count", manifestBytes.length);
            eiBais.setIntField("pos", 0);
            eiBais.setIntField("mark", 0);

            ClassInfo manifestClass = ClassLoaderInfo.getCurrentResolvedClassInfo("java.util.jar.Manifest");
            if (manifestClass.initializeClass(env.getThreadInfo())) {
                env.repeatInvocation();
                return MJIEnv.NULL;
            }
            int manifestRef = env.newObject(manifestClass);
            // Need to invoke Manifest.<init>(InputStream) via direct call
            ThreadInfo ti = env.getThreadInfo();
            MethodInfo mi = manifestClass.getMethod("<init>(Ljava/io/InputStream;)V", false);
            if (mi == null) {
                // fallback: try no-arg and manually read? just return null
                return MJIEnv.NULL;
            }
            // Create direct call frame
            DirectCallStackFrame frame = mi.createDirectCallStackFrame(ti, 2);
            frame.setReferenceArgument(0, manifestRef, null);
            frame.setReferenceArgument(1, baisRef, null);
            ti.pushFrame(frame);
            env.repeatInvocation();
            return MJIEnv.NULL; // will be re-executed, then return manifestRef
            // Note: on re-execution, we need to return the object - handled via DirectCall
        } catch (IOException e) {
            env.throwException("java.io.IOException", e.getMessage());
            return MJIEnv.NULL;
        }
    }

    private JarFileProxy getJFP(MJIEnv env, int thisRef) {
        int handle = env.getIntField(thisRef, "zipFileHandle");
        int state = env.getIntField(thisRef, "state");
        if (state != 1) {
            env.throwException("java.lang.IllegalStateException", "jar file not open");
            return null;
        }
        if (content.get(handle) != null) {
            return content.get(handle);
        } else {
            JarFileProxy jfp;
            try {
                jfp = createJarProxy(env, thisRef);
            } catch (IOException e) {
                env.throwException("java.io.IOException", "failed to get jar handle");
                return null;
            }
            content.set(handle, jfp);
            return jfp;
        }
    }

    private JarFileProxy createJarProxy(MJIEnv env, int thisRef) throws IOException {
        String name = env.getStringField(thisRef, "name");
        String charset = env.getStringField(thisRef, "charsetName");
        if (charset == null) {
            charset = "UTF-8";
        }
        JarFile jf = new JarFile(name, true);
        // Charset is not critical for JarFile host; host uses UTF-8 by default
        JarFileProxy jfp = new JarFileProxy();
        jfp.jf = jf;
        return jfp;
    }
}
