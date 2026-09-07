package jdk.internal.access;

public class SharedSecrets {
  private static JavaSecurityPropertiesAccess javaSecurityPropertiesAccess;

  public SharedSecrets() {}

  public static JavaSecurityPropertiesAccess getJavaSecurityPropertiesAccess() { return javaSecurityPropertiesAccess; }
  public static void setJavaSecurityPropertiesAccess(JavaSecurityPropertiesAccess access) { javaSecurityPropertiesAccess = access; }
}
