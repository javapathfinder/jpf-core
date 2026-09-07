package jdk.internal.access;

import java.util.Enumeration;
import java.util.List;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

public interface JavaUtilZipFileAccess {
  boolean startsWithLocHeader(ZipFile zip);
  List<String> getManifestAndSignatureRelatedFiles(JarFile jar);
  String getManifestName(JarFile jar, boolean b);
  int getManifestNum(JarFile jar);
  int[] getMetaInfVersions(JarFile jar);
  Enumeration<JarEntry> entries(ZipFile zip);
  Stream<JarEntry> stream(ZipFile zip);
  Stream<String> entryNameStream(ZipFile zip);
  void setExtraAttributes(ZipEntry entry, int i);
  int getExtraAttributes(ZipEntry entry);
}
