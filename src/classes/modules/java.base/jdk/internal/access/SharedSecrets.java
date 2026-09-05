package jdk.internal.access;

public class SharedSecrets {
  private static JavaUtilZipFileAccess javaUtilZipFileAccess;
  private static JavaUtilJarAccess javaUtilJarAccess;
  private static JavaSecurityPropertiesAccess javaSecurityPropertiesAccess;

  public SharedSecrets() {}

  public static JavaUtilZipFileAccess getJavaUtilZipFileAccess() { return javaUtilZipFileAccess; }
  public static void setJavaUtilZipFileAccess(JavaUtilZipFileAccess access) { javaUtilZipFileAccess = access; }

  public static JavaUtilJarAccess javaUtilJarAccess() { return javaUtilJarAccess; }
  public static void setJavaUtilJarAccess(JavaUtilJarAccess access) { javaUtilJarAccess = access; }

  public static JavaSecurityPropertiesAccess getJavaSecurityPropertiesAccess() { return javaSecurityPropertiesAccess; }
  public static void setJavaSecurityPropertiesAccess(JavaSecurityPropertiesAccess access) { javaSecurityPropertiesAccess = access; }
}
