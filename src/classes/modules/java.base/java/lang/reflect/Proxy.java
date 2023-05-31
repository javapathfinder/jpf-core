/*
 * Copyright (C) 2014, United States Government, as represented by the
 * Administrator of the National Aeronautics and Space Administration.
 * All rights reserved.
 *
 * The Java Pathfinder core (jpf-core) platform is licensed under the
 * Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0.
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package java.lang.reflect;

import java.util.Objects;

public class Proxy {

  protected InvocationHandler h;

  private Proxy() { }

  protected Proxy(InvocationHandler handler) {
    Objects.requireNonNull(handler);
    this.h = handler;
  }

  //
  // APIs for internal usage
  //
  private static native Class<?> defineClass0(ClassLoader loader, String name, byte[] b, int off, int len);
  // Proxy's implementation is uniquely defined by List<interface>.
  // We give them canonical names to avoid redundant generation of class files and loading of classes.
  private static native String getProxyClassCanonicalName(Class<?>[] interfaces);
  private static native Class<?> getCachedProxyClass(String proxyName);

  //
  // Public APIs of Proxy class
  //
  public static native boolean isProxyClass(Class<?> cl);
  public static native InvocationHandler getInvocationHandler(Object proxy);

  @Deprecated
  public static Class<?> getProxyClass(ClassLoader loader,
                                       Class<?>[] interfaces) throws IllegalArgumentException {
    if (loader == null) {
      throw new IllegalArgumentException("loader cannot be null");
    }
    if (interfaces == null) {
      throw new NullPointerException("interface array cannot be null");
    }
    for (Class<?> intf : interfaces) {
      if (intf == null) {
        throw new NullPointerException("interface arrray element cannot be null");
      }
    }

    String proxyName = getProxyClassCanonicalName(interfaces);
    if (proxyName == null) {
      throw new IllegalArgumentException("non-public interfaces from different packages");
    }
    Class<?> cachedProxy = getCachedProxyClass(proxyName);
    if (cachedProxy != null) {
      return cachedProxy;
    }

    byte[] proxyClassFile = ProxyGenerator.generateProxyClass(proxyName, interfaces, Modifier.PUBLIC | Modifier.FINAL);
    Class<?> proxyClass = defineClass0(loader, proxyName, proxyClassFile, 0, proxyClassFile.length);
    return proxyClass;
  }

  public static Object newProxyInstance(ClassLoader loader,
                                        Class<?>[] interfaces,
                                        InvocationHandler handler) {
    if (handler == null) {
      throw new NullPointerException("handler cannot be null");
    }

    Class<?> proxyClass = getProxyClass(loader, interfaces);
    Object proxyObj = null;
    try {
      proxyObj = proxyClass.getDeclaredConstructor(InvocationHandler.class).newInstance(handler);
    } catch (Exception e) {
      e.printStackTrace();
    }
    return proxyObj;
  }
}
