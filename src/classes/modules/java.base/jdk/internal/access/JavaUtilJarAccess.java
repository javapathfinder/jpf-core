package jdk.internal.access;

import java.io.IOException;
import java.net.URL;
import java.security.CodeSource;
import java.util.Enumeration;
import java.util.List;
import java.util.jar.Attributes;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

public interface JavaUtilJarAccess {
  boolean jarFileHasClassPathAttribute(JarFile jar) throws IOException;
  CodeSource[] getCodeSources(JarFile jar, URL url);
  CodeSource getCodeSource(JarFile jar, URL url, String name);
  Enumeration<String> entryNames(JarFile jar, CodeSource[] cs);
  Enumeration<JarEntry> entries2(JarFile jar);
  void setEagerValidation(JarFile jar, boolean b);
  List<Object> getManifestDigests(JarFile jar);
  Attributes getTrustedAttributes(Manifest man, String name);
  void ensureInitialization(JarFile jar);
  boolean isInitializing();
  JarEntry entryFor(JarFile jar, String name);
}
