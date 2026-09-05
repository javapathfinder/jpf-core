package jdk.internal.misc;

import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface JavaUtilZipFileAccess {
  boolean startsWithLocHeader(ZipFile zip);
  List<String> getManifestAndSignatureRelatedFiles(java.util.jar.JarFile jar);
  String getManifestName(java.util.jar.JarFile jar, boolean b);
  int getManifestNum(java.util.jar.JarFile jar);
  int[] getMetaInfVersions(java.util.jar.JarFile jar);
  Enumeration<JarEntry> entries(ZipFile zip);
  Stream<JarEntry> stream(ZipFile zip);
  Stream<String> entryNameStream(ZipFile zip);
  void setExtraAttributes(ZipEntry entry, int i);
  int getExtraAttributes(ZipEntry entry);
}
