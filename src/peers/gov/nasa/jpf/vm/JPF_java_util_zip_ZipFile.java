package gov.nasa.jpf.vm;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.DynamicObjectArray;

public class JPF_java_util_zip_ZipFile extends NativePeer {
  private static class ZipFileProxy {
    Map<String, byte[]> buffers = new HashMap<>();
    ZipFile zf;
  }

  private int count = 0;
  private DynamicObjectArray<ZipFileProxy> content;

  public JPF_java_util_zip_ZipFile(Config conf) {
    content = new DynamicObjectArray<>();
  }

  @MJI
  public int open0(MJIEnv env, int thisRef) {
    int ref = count++;
    try {
      ZipFileProxy zfp = createZipProxy(env, thisRef);
      content.set(ref, zfp);
      env.setReferenceField(thisRef, "comment", env.newString(zfp.zf.getComment()));
      String[] names = new String[zfp.zf.size()];
      int it = 0;
      Enumeration<? extends ZipEntry> en = zfp.zf.entries();
      while(en.hasMoreElements()) {
        ZipEntry ze = en.nextElement();
        names[it++] = ze.getName();
      }
      assert it == zfp.zf.size();
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
    if(state != 1) {
      env.throwException("java.lang.IllegalStateException", "Not open");
      return;
    }
    if(content.get(handle) == null) {
      return;
    }
    try {
      content.get(handle).zf.close();
      content.set(handle, null);
    } catch (IOException e) {
      env.throwException("java.io.IOException", "failed to close: " + e.getMessage());
    }
  }

  @MJI
  public boolean initZEFields__Ljava_lang_String_2Ljava_util_zip_ZipEntry_2__Z(MJIEnv env, int thisRef, int entryName, int zeRef) {
    ZipFileProxy zfp;
    zfp = getZFP(env, thisRef);
    if(zfp == null) {
      return false;
    }
    ZipEntry ze = zfp.zf.getEntry(env.getStringObject(entryName));
    if(ze == null) {
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
    ZipFileProxy zfp;
    zfp = getZFP(env, thisRef);
    if(zfp == null) {
      return -1;
    }
    String entryName = env.getStringObject(entryNameRef);
    if(!zfp.buffers.containsKey(entryName)) {
      ZipEntry ze = zfp.zf.getEntry(entryName);
      if(ze == null) {
        env.throwException("java.io.IOException", "Could not read " + entryName);
        return -1;
      }
      try(InputStream fis = zfp.zf.getInputStream(ze);) {
        long expandedSize = ze.getSize();
        if(expandedSize > Integer.MAX_VALUE) {
          env.throwException("java.lang.UnsupportedOperationException", "Too big");
        }
        int remaining = (int) expandedSize;
        int output = 0;
        byte[] buffer = new byte[(int) expandedSize];
        while(remaining > 0) {
          int read = fis.read(buffer, output, remaining);
          if(read == -1) {
            env.throwException("java.io.IOException", "Truncated stream?");
          }
          output += read;
          remaining -= read;
        }
        zfp.buffers.put(entryName, buffer);
      } catch (IOException e) {
        env.throwException("java.io.IOException", e.getMessage());
        return -1;
      }
    }
    return env.newByteArray(zfp.buffers.get(entryName));
  }

  @MJI
  public int getZEExtraBytes__Ljava_lang_String_2___3B(MJIEnv env, int thisRef, int entryName) {
    ZipFileProxy zfp;
    zfp = getZFP(env, thisRef);
    if(zfp == null) {
      return -1;
    }
    ZipEntry entry = zfp.zf.getEntry(env.getStringObject(entryName));
    if(entry == null) {
      return MJIEnv.NULL;
    }
    byte[] extra = entry.getExtra();
    if(extra == null) {
      return MJIEnv.NULL;
    } else {
      return env.newByteArray(extra);
    }
  }

  private ZipFileProxy getZFP(MJIEnv env, int thisRef) {
    int handle = env.getIntField(thisRef, "zipFileHandle");
    int state = env.getIntField(thisRef, "state");
    if(state != 1) {
      env.throwException("java.lang.IllegalStateException", "zip file not open");
      return null;
    }
    if(content.get(handle) != null) {
      return content.get(handle);
    } else {
      ZipFileProxy zfp;
      try {
        zfp = createZipProxy(env, thisRef);
      } catch (IOException e) {
        env.throwException("java.io.IOEXception", "failed to get zip handle");
        return null;
      }
      content.set(handle, zfp);
      return zfp;
    }
  }

  private ZipFileProxy createZipProxy(MJIEnv env, int thisRef) throws IOException {
    String name = env.getStringField(thisRef, "name");
    String charset = env.getStringField(thisRef, "charsetName");
    ZipFile zf = new ZipFile(name, Charset.forName(charset));
    ZipFileProxy zfp = new ZipFileProxy();
    zfp.zf = zf;
    return zfp;
  }
}
