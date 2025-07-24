package jdk.internal.misc;

import java.net.InetAddress;

/**
 * Interface for accessing package-private methods in java.net.InetAddress
 * Required for Java 11 compatibility
 */
//  Handles Java 11 security manager and access control requirements
public interface JavaNetInetAddressAccess {
    /**
     * Get the original host name from an InetAddress
     */
    String getOriginalHostName(InetAddress ia);

    /**
     * Get the address bytes from an InetAddress
     */
    byte[] getRawAddress(InetAddress ia);

    /**
     * Create an InetAddress from raw bytes and host name
     */
    InetAddress getByAddress(String hostName, byte[] addr);
}
