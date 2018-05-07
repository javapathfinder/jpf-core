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
package java.net;

import java.io.IOException;
import java.security.CodeSource;
import java.security.PermissionCollection;
import java.security.SecureClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Vector;
import java.util.jar.Manifest;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 *  Model class for java.net.URLClassLoader
 */
public class URLClassLoader extends SecureClassLoader {

  private ArrayList<URL> urls = new ArrayList<URL>(0);

  public URLClassLoader(URL[] urls) {
    super();
    addURLs(urls);
  }

  public URLClassLoader(URL[] urls, ClassLoader parent) {
    super(parent);
    addURLs(urls);
  }

  private void addURLs(URL[] urls) {
    for(URL url: urls) {
      addURL(url);
    }
  }

  private native void addURL0(String url);

  protected void addURL(URL url) {
    addURL0(url.toString());
    urls.add(url);
  }

  @Override
  protected native Class<?> findClass(final String name) throws ClassNotFoundException;

  private native String findResource0 (String rname);

  @Override
  public URL findResource(final String rname) {
    URL url = null;
    String path = findResource0(rname);

    try {
      url = new URL(path);
    } catch (MalformedURLException x){
      url = null;
    }

    return url;
  }

  private native String[] findResources0 (String rname);

  @Override
  public Enumeration<URL> findResources(String name) throws IOException {
    String[] urls = findResources0(name);
    Vector<URL> list = new Vector<URL>(0);
    for(String url: urls) {
      try {
        list.add(new URL(url));
      } catch (MalformedURLException x){
        // process the rest
      }
    }

    return list.elements();
  }

  public URL[] getURLs() {
    return urls.toArray(new URL[urls.size()]);
  }

  public static URLClassLoader newInstance(URL[] urls) {
    return (new URLClassLoader(urls));
  }

  public static URLClassLoader newInstance(URL[] urls, ClassLoader parent) {
    return (new URLClassLoader(urls, parent));
  }

  //--- unsupported methods

  public URLClassLoader(URL[] urls, ClassLoader parent, URLStreamHandlerFactory factory) {
    throw new UnsupportedOperationException();
  }

  public void close() throws IOException {
    throw new UnsupportedOperationException();
  }

  protected Package definePackage(String name, Manifest man, URL url) 
      throws IllegalArgumentException {
    throw new UnsupportedOperationException();
  }

  protected PermissionCollection getPermissions(CodeSource codesource) {
    throw new UnsupportedOperationException();
  }
}
