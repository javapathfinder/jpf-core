/*
 * Copyright (c) 2015, 2018, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.  Oracle designates this
 * particular file as subject to the "Classpath" exception as provided
 * by Oracle in the LICENSE file that accompanied this code.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */

package jdk.xml.internal;

import java.io.*;
import java.net.URL;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import java.util.Properties;

/**
 * MJI model for the SecuritySupport class
 *
 * Aims to eliminate the problems caused by
 * executing code with privileges in order to extend
 * support for SAXParserTest for Java 11
 *
 */
public class SecuritySupport {

  static final Properties cacheProps = new Properties();
  static volatile boolean firstTime = true;

  /**
   * Creates and returns a new FileInputStream from a file.
   * @param file the specified file
   * @return the FileInputStream
   * @throws FileNotFoundException if the file is not found
   */
  public static FileInputStream getFileInputStream(final File file) throws FileNotFoundException {
    try {
      return AccessController.doPrivileged((PrivilegedExceptionAction<FileInputStream>) () -> new FileInputStream(file));
    } catch (PrivilegedActionException e) {
      throw (FileNotFoundException) e.getException();
    }
  }

  public static ClassLoader getContextClassLoader() {
    return AccessController.doPrivileged((PrivilegedAction<ClassLoader>) () -> {
      ClassLoader cl = Thread.currentThread().getContextClassLoader();
      if (cl == null)
        cl = ClassLoader.getSystemClassLoader();
      return cl;
    });
  }

  /**
   * Reads a system property
   *
   * @param propName the name of the property
   * @return the value of the property
   */
  public static String getSystemProperty(final String propName) {
    return System.getProperty(propName);
  }

  /**
   * Reads JAXP system property in this order: system property,
   * $java.home/conf/jaxp.properties if the system property is not specified
   *
   * @param <T> the type of the property value
   * @param type the type of the property value
   * @param propName the name of the property
   * @param defValue the default value
   * @return the value of the property, or the default value if no system
   * property is found
   */
  public static <T> T getJAXPSystemProperty(Class<T> type, String propName, String defValue) {
    String value = getJAXPSystemProperty(propName);
    if (value == null) {
      value = defValue;
    }
    if (Integer.class.isAssignableFrom(type)) {
      return type.cast(Integer.parseInt(value));
    } else if (Boolean.class.isAssignableFrom(type)) {
      return type.cast(Boolean.parseBoolean(value));
    }
    return type.cast(value);
  }

  /**
   * Reads JAXP system property in this order: system property,
   * $java.home/conf/jaxp.properties if the system property is not specified
   *
   * @param propName the name of the property
   * @return the value of the property
   */
  public static String getJAXPSystemProperty(String propName) {
    String value = getSystemProperty(propName);
    if (value == null) {
      value = readJAXPProperty(propName);
    }
    return value;
  }

  /**
   * Reads the specified property from $java.home/conf/jaxp.properties
   *
   * @param propName the name of the property
   * @return the value of the property
   */
  public static String readJAXPProperty(String propName) {
    String value = null;
    InputStream is = null;
    try {
      if(firstTime) {
        synchronized(cacheProps) {
          if(firstTime) {
            String configFile = System.getProperty("java.home") + File.separator + "conf" + File.separator + "jaxp.properties";
            File f = new File(configFile);
            if (f.exists()) {
              is = getFileInputStream(f);
              cacheProps.load(is);
            }
            firstTime = false;
          }
        }
      }
      value = cacheProps.getProperty(propName);

    } catch (IOException ex) {
    } finally {
      if (is != null) {
        try {
          is.close();
        } catch (IOException ex) {}
      }
    }

    return value;
  }

  /**
   * Check the protocol used in the systemId against allowed protocols
   *
   * @param systemId the Id of the URI
   * @param allowedProtocols a list of allowed protocols separated by comma
   * @param accessAny keyword to indicate allowing any protocol
   * @return the name of the protocol if rejected, null otherwise
   */
  public static String checkAccess(String systemId, String allowedProtocols,
                                   String accessAny) throws IOException {
    if (systemId == null || (allowedProtocols != null &&
        allowedProtocols.equalsIgnoreCase(accessAny))) {
      return null;
    }

    String protocol;
    if (!systemId.contains(":")) {
      protocol = "file";
    } else {
      URL url = new URL(systemId);
      protocol = url.getProtocol();
      if (protocol.equalsIgnoreCase("jar")) {
        String path = url.getPath();
        protocol = path.substring(0, path.indexOf(":"));
      } else if (protocol.equalsIgnoreCase("jrt")) {
        // if the systemId is "jrt" then allow access if "file" allowed
        protocol = "file";
      }
    }

    if (isProtocolAllowed(protocol, allowedProtocols)) {
      //access allowed
      return null;
    } else {
      return protocol;
    }
  }

  /**
   * Check if the protocol is in the allowed list of protocols. The check
   * is case-insensitive while ignoring whitespaces.
   *
   * @param protocol a protocol
   * @param allowedProtocols a list of allowed protocols
   * @return true if the protocol is in the list
   */
  private static boolean isProtocolAllowed(String protocol, String allowedProtocols) {
    if (allowedProtocols == null) {
      return false;
    }
    String temp[] = allowedProtocols.split(",");
    for (String t : temp) {
      t = t.trim();
      if (t.equalsIgnoreCase(protocol)) {
        return true;
      }
    }
    return false;
  }

  /**
   * Checks whether the file exists.
   * @param f the specified file
   * @return true if the file exists, false otherwise
   */
  private static boolean doesFileExist(File f) {
    return f.exists();
  }
}
