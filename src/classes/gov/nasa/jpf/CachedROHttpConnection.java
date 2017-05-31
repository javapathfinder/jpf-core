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

package gov.nasa.jpf;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Proxy;
import java.net.URL;

import sun.net.www.protocol.http.Handler;

/**
 * this is just a very rough abstraction at this point, which only supports
 * reading static URL contents. The data is cached for subsequent
 * access, to avoid DOS by means of model checking
 */
public class CachedROHttpConnection extends java.net.HttpURLConnection {

  @Override
  public void disconnect() {
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  @Override
  public boolean usingProxy() {
    return false;
  }

  @Override
  public void connect() throws IOException {
    //throw new UnsupportedOperationException("Not supported yet.");
  }

  String host;
  int port;

  public CachedROHttpConnection(URL u, String host, int port){
    super(u);

    this.host = host;
    this.port = port;
  }

  public CachedROHttpConnection(URL u, Proxy p, Handler handler){
    super(u);
  }

  public CachedROHttpConnection(URL u, Proxy p) {
    this (u, p, new Handler());
  }

  protected CachedROHttpConnection(URL u, Handler handler)  throws IOException {
    this(u, null, handler);
  }



  private native byte[] getContents(String url);

  @Override
  public synchronized InputStream getInputStream() throws IOException {
    byte[] data = getContents(url.toString());
    return new ByteArrayInputStream(data);
  }


}
