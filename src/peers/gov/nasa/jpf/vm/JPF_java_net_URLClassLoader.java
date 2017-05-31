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

import gov.nasa.jpf.JPF;
import java.net.MalformedURLException;
import java.net.URL;

import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.util.JPFLogger;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * Native peer for java.net.URLClassLoader
 */
public class JPF_java_net_URLClassLoader extends JPF_java_lang_ClassLoader{

  static JPFLogger log = JPF.getLogger("class");
  
  @MJI
  public void addURL0__Ljava_lang_String_2__V (MJIEnv env, int objRef, int urlRef) throws MalformedURLException {
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    String url = env.getStringObject(urlRef);

    String path = null;
    URL u = new URL(url);
    String protocol = u.getProtocol();
    if(protocol.equals("file")) {
      path = u.getFile();
    } else if(protocol.equals("jar")){
      path = url.substring(url.lastIndexOf(':')+1, url.indexOf('!'));
    } else {
      // we don't support other protocols for now!
      log.warning("unknown path element specification: ", url);
      return;
    }

    cl.addClassPathElement(path);
  }

  @MJI
  public int findClass__Ljava_lang_String_2__Ljava_lang_Class_2 (MJIEnv env, int objRef, int nameRef) {
    String typeName = env.getStringObject(nameRef);
    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);
    ThreadInfo ti = env.getThreadInfo();

    try {
      ClassInfo ci = cl.getResolvedClassInfo( typeName);
      if(!ci.isRegistered()) {
        ci.registerClass(env.getThreadInfo());
      }
      // note that we don't initialize yet
      return ci.getClassObjectRef();
          
    } catch (LoadOnJPFRequired rre) { // this classloader has a overridden loadClass 
      env.repeatInvocation();
      return MJIEnv.NULL;
      
    } catch (ClassInfoException cix){
      if (cix.getCause() instanceof ClassParseException){
        env.throwException("java.lang.ClassFormatError", typeName);
      } else {
        env.throwException("java.lang.ClassNotFoundException", typeName);
      }
      return MJIEnv.NULL;      
    }
  }

  @MJI
  public int findResource0__Ljava_lang_String_2__Ljava_lang_String_2 (MJIEnv env, int objRef, int resRef){
    String rname = env.getStringObject(resRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String resourcePath = cl.findResource(rname);

    return env.newString(resourcePath);
  }

  @MJI
  public int findResources0__Ljava_lang_String_2___3Ljava_lang_String_2 (MJIEnv env, int objRef, int resRef) {
    String rname = env.getStringObject(resRef);

    ClassLoaderInfo cl = env.getClassLoaderInfo(objRef);

    String[] resources = cl.findResources(rname);

    return env.newStringArray(resources);
  }
}
