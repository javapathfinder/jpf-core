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
package sun.net.www.protocol.http;

import gov.nasa.jpf.CachedROHttpConnection;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.net.Proxy;
import java.net.URL;

/**
 * replaced handler to support configured URLConnection classes
 */
public class Handler extends java.net.URLStreamHandler {

  protected String proxy;
  protected int proxyPort;

  public Handler() {
    proxy = null;
    proxyPort = -1;
  }

  public Handler(String proxy, int port) {
    this.proxy = proxy;
    this.proxyPort = port;
  }

  @Override
  protected int getDefaultPort() {
    return 80;
  }


  static Class<?>[] argTypes = { URL.class, Proxy.class };
  private native Class<? extends java.net.URLConnection> getConnectionClass(String url);

  
  @Override
  protected java.net.URLConnection openConnection (URL u, Proxy p) throws IOException {

    Class<? extends java.net.URLConnection> clazz = getConnectionClass(u.toString());

    if (clazz != null){
      try {
        Constructor<? extends java.net.URLConnection> ctor = clazz.getConstructor(argTypes);
        return ctor.newInstance(u, p);

      } catch (NoSuchMethodException nmx){
        throw new IOException("connection class has no suitabe ctor: " + clazz.getName());
      } catch (IllegalAccessException iax){
        throw new IOException("connection class has no public ctor: " + clazz.getName());
      } catch (InvocationTargetException itx){
        throw new IOException("exception initializing URLConnection", itx);
      } catch (InstantiationException ix){
        throw new IOException("connection class cannot be instantiated: " + clazz.getName());
      }

    } else {
      // we just go with the standard thing, hoping that we have a modeled Socket layer
      return new CachedROHttpConnection(u, p, this);
    }
  }

  @Override
  protected java.net.URLConnection openConnection(URL u) throws IOException {
    return openConnection(u, null);
  }

  //... and a lot of methods still missing
}
