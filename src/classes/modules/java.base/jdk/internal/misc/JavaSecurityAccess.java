package jdk.internal.misc;

import java.security.PrivilegedAction;
import java.security.PrivilegedExceptionAction;

/**
 * Minimal stub for JPF – contains only the methods
 * used by ProtectionDomain / AccessController.
 */
// Provides access to package-private methods in java.net.InetAddress that Java 11 networking code requires
public interface JavaSecurityAccess {

    <T> T doIntersectionPrivilege(PrivilegedAction<T> action);

    <T> T doIntersectionPrivilege(PrivilegedAction<T> action,
                                  java.security.AccessControlContext ctx,
                                  java.security.AccessControlContext ctx2);

    <T> T doPrivileged(PrivilegedExceptionAction<T> action,
                       java.security.AccessControlContext ctx)
            throws Exception;
}
