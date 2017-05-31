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

package gov.nasa.jpf.vm;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFConfigException;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.JPFLogger;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;

/**
 * very simple URLConnection model that can be used for reading static URL contents
 *
 * The data can be stored in/read from the file system, and is cached
 * to avoid DOS by means of model checking
 */
public class JPF_gov_nasa_jpf_CachedROHttpConnection extends NativePeer {

  static JPFLogger logger = JPF.getLogger("http");

  File cacheDir;
  HashMap<String,byte[]> dataCache;
  
  public JPF_gov_nasa_jpf_CachedROHttpConnection (Config conf){
    String cacheDirPath = conf.getString("http.cache_dir");
    if (cacheDirPath != null){
      cacheDir = new File(cacheDirPath);

      if (!cacheDir.exists()){
        cacheDir.mkdir();
      }
      if (!cacheDir.isDirectory()){
        throw new JPFConfigException("illegal http.cache_dir entry: " + cacheDirPath);
      }
    }

    dataCache = new HashMap<String,byte[]>();
  }

  private static String getCacheFileName( String url){
    String fn = url.replace('/', '^');
    fn = fn.replace(':', '%');

    return fn;
  }

  private byte[] getDataFromCachedFile (String url){
    byte[] data = null;
    String cacheFileName = getCacheFileName(url);
    File cacheFile = new File(cacheDir, cacheFileName);
    if (cacheFile.isFile()) {
      try {
        data = FileUtils.getContents(cacheFile);
      } catch (IOException iox) {
        logger.warning("can't read http data from cached file ", cacheFile.getPath());
      }

      if (data != null) {
        logger.info("reading contents of ", url, " from file ", cacheFile.getPath());
        dataCache.put(url, data);
      }
    }

    return data;
  }

  private byte[] getDataFromURL (String surl){
    byte[] data = null;

    try {
      URL url = new URL(surl);
      InputStream is = url.openStream();

      if (is != null) {
        ByteArrayOutputStream os = new ByteArrayOutputStream(is.available());
        byte[] buf = new byte[1024];

        for (int n = is.read(buf); n >= 0; n = is.read(buf)) {
          os.write(buf, 0, n);
        }
        is.close();

        data = os.toByteArray();
        dataCache.put(surl, data);

        logger.info("reading contents of ", surl, " from server");

        if (cacheDir != null) {
          String cacheFileName = getCacheFileName(surl);
          File cacheFile = new File(cacheDir, cacheFileName);
          try {
            FileUtils.setContents(cacheFile, data);
            logger.info("storing contents of ", surl, " to file ", cacheFile.getPath());
          } catch (IOException iox) {
            logger.warning("can't store to cache directory ", cacheFile.getPath());
          }
        }

        return data;
      }
    } catch (MalformedURLException mux){
      logger.warning("mallformed URL ", surl);
    } catch (IOException ex) {
      logger.warning("reading URL data ", surl, " failed with ", ex.getMessage());
    }

    return data;
  }

  @MJI
  public int getContents__Ljava_lang_String_2___3B (MJIEnv env, int objRef, int surlRef){
    String url = env.getStringObject(surlRef);

    // first we check if it's already cached in memory
    byte[] data = dataCache.get(url);

    if (data != null){
      logger.info("using cached contents of ", url);

    } else {
      // see if we can get it from the cacheDir
      if (cacheDir != null){
        data = getDataFromCachedFile(url);
      }

      // if that didn't produce anything, we have to reach out to the net
      if (data == null){
        data = getDataFromURL( url);
      }
    }

    if (data != null){
      return env.newByteArray(data);
    } else {
      return MJIEnv.NULL;
    }
  }
}
