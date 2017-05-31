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
package gov.nasa.jpf.test.java.net;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;

import org.junit.Test;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * test of java.lang.ClassLoader API
 */
public class URLClassLoaderTest extends LoadUtility {

  public class TestClassLoader extends URLClassLoader {

    public TestClassLoader(URL[] urls) {
        super(urls);
    }

    public TestClassLoader(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }
    
    @Override
	public Class<?> findClass(String name) throws ClassNotFoundException {
        return super.findClass(name);
    }

    public Class<?> getLoadedClass(String name) {
      return findLoadedClass(name);
    }

    public Class<?> delegateTofindSystemClass(String cname) throws ClassNotFoundException {
      return this.findSystemClass(cname);
    }

    @Override
	protected Package[] getPackages() {
      return super.getPackages();
    }

    @Override
	protected Package getPackage(String name) {
      return super.getPackage(name);
    }
  }

  @Test
  public void testConstructor_NullPointerException() {
    if (verifyUnhandledException("java.lang.NullPointerException")) {
      new URLClassLoader(null);
    }
  }

  @Test 
  public void testConstructorEmptyURLs () {
    if (verifyNoPropertyViolation()) {
      URLClassLoader cl = new URLClassLoader(new URL[0]);
      assertNotNull(cl.getParent());
      assertEquals(cl.getParent(), ClassLoader.getSystemClassLoader());
    }
  }

  @Test
  public void testConstructorParent() {
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[0];
      ClassLoader parent = new TestClassLoader(urls);
      URLClassLoader cl =  new URLClassLoader(urls, parent);

      assertNotNull(parent.getParent());
      assertEquals(parent.getParent(), ClassLoader.getSystemClassLoader());

      assertNotNull(cl.getParent());
      assertEquals(cl.getParent(), parent);
    }
  }

  @Test
  public void testLoadClass_NoClassDefFoundError() throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.NoClassDefFoundError")) {
      URL[] urls = new URL[0];
      URLClassLoader cl = new URLClassLoader(urls);
      cl.loadClass("java/lang/Class");
    }
  }

  @Test
  public void testLoadClass_ClassNotFoundException() throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      URL[] urls = new URL[0];
      URLClassLoader cl =  new URLClassLoader(urls);
      cl.loadClass("java.lang.Does_Not_Exist");
    }
  }

  @Test
  public void testLoadClass_ClassNotFoundException2() throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      URL[] urls = new URL[0];
      URLClassLoader cl =  new URLClassLoader(urls);
      cl.loadClass("java.lang.Class.class");
    }
  }

  @Test
  public void testSystemLoaderLoadClass() throws ClassNotFoundException {
   if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[0];
      ClassLoader systemCl = ClassLoader.getSystemClassLoader();
      ClassLoader parent = new TestClassLoader(urls);
      URLClassLoader cl =  new URLClassLoader(urls, parent);

      String cname = "java.lang.Class";
      Class<?> c1 = systemCl.loadClass(cname);
      Class<?> c2 = parent.loadClass(cname);
      Class<?> c3 = cl.loadClass(cname);

      assertSame(c1, c2);
      assertSame(c1, c3);
      // this test fails on the host VM, cause java.lang.Class is loaded by
      // bootstrap classloader and therefore c1.getClassLoader() returns null,
      // but the test passes on JPF.
      assertSame(c1.getClassLoader(), systemCl);
    }
  }

  @Test
  public void testFindLoadedClass() throws ClassNotFoundException, MalformedURLException {
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[0];
      TestClassLoader ucl1 = new TestClassLoader(urls);
      TestClassLoader ucl2 = new TestClassLoader(urls, ucl1);

      String cname = "java.lang.Class";

      Class<?> c = ucl2.loadClass(cname);
      assertNotNull(c);
      assertEquals(c.getName(), cname);

      // systemClassLoader is going to be the defining classloader
      assertNull(ucl2.getLoadedClass(cname));
      assertNull(ucl1.getLoadedClass(cname));
    }
  }

  @Test
  public void testNonSystemLoaderLoadClass() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      // create a url from a dir
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader cl =  new URLClassLoader(urls);

      String cname = pkg + ".Class1";
      Class<?> cls = cl.loadClass(cname);

      assertEquals(cls.getClassLoader(), cl);
      assertFalse(cls.getClassLoader() == ClassLoader.getSystemClassLoader());

      assertEquals(cls.getInterfaces().length, 2);
      for(Class<?>ifc: cls.getInterfaces()) {
        assertEquals(cls.getClassLoader(), ifc.getClassLoader());
      }

      // create a url from jar
      urls[0] = new URL(jarUrl);
      cl =  new URLClassLoader(urls);
      cls = cl.loadClass(cname);

      assertEquals(cls.getClassLoader(), cl);
      assertFalse(cls.getClassLoader() == ClassLoader.getSystemClassLoader());
      assertEquals(cls.getInterfaces().length, 2);
      for(Class<?>ifc: cls.getInterfaces()) {
        assertEquals(cls.getClassLoader(), ifc.getClassLoader());
      }
    }
    movePkgBack();
  }

  @Test
  public void testFindResource() throws MalformedURLException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader cl =  new URLClassLoader(urls);

      String resClass1 = pkg + "/Class1.class";
      URL url = cl.findResource(resClass1);
      String expectedUrl = dirUrl + "/" + resClass1;
      assertEquals(url.toString(), expectedUrl);

      String resInterface1 = pkg + "/Interface1.class";
      url = cl.findResource(resInterface1);
      expectedUrl = dirUrl + "/" + resInterface1;
      assertEquals(url.toString(), expectedUrl);

      url = cl.findResource("non_existence_resource");
      assertNull(url);

      url = cl.findResource("java/lang/Class.class");
      assertNull(url);

      // create a url from jar
      urls[0] = new URL(jarUrl);
      cl =  new URLClassLoader(urls);
      url = cl.findResource(resClass1);
      expectedUrl = jarUrl + resClass1;
      assertEquals(url.toString(), expectedUrl);

      url = cl.findResource(resInterface1);
      expectedUrl = jarUrl + resInterface1;
      assertEquals(url.toString(), expectedUrl);

      url = cl.findResource("non_existence_resource");
      assertNull(url);

      url = cl.findResource("java/lang/Class.class");
      assertNull(url);
    }
    movePkgBack();
  }

  @Test
  public void testFindResources() throws IOException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl), new URL(jarUrl), new URL(jarUrl) };
      URLClassLoader cl =  new URLClassLoader(urls);
      String resource = pkg + "/Class1.class";
      Enumeration<URL> e = cl.findResources(resource);

      List<String> urlList = new ArrayList<String>();
      while(e.hasMoreElements()) {
        urlList.add(e.nextElement().toString());
      }

      assertTrue(urlList.contains(jarUrl + resource));
      assertTrue(urlList.contains(dirUrl + "/" + resource));

      // we added the same url path twice, but findResource return value should only 
      // include one entry for the same resource
      assertEquals(urlList.size(), 2);

      e = cl.findResources(null);
      assertNotNull(e);
      assertFalse(e.hasMoreElements());
    }
    movePkgBack();
  }

  @Test
  public void testGetURLs() throws MalformedURLException {
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[5];
      urls[0] = new URL("file://" + "/x/y/z/" );
      urls[1] = new URL("file://" + "/a/b/c/" );
      urls[2] = new URL("file://" + "/a/b/c/" );
      urls[3] = new URL(dirUrl);;
      urls[4] = new URL(jarUrl);

      URLClassLoader cl =  new URLClassLoader(urls);
      URL[] clUrls = cl.getURLs();

      assertEquals(clUrls.length, urls.length);
      for (int i=0; i<urls.length; i++) {
        assertEquals(clUrls[i], urls[i]);
      }
    }
  }

  @Test
  public void testNewInstance1() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[1];
      urls[0] = new URL(dirUrl);
      URLClassLoader cl =  URLClassLoader.newInstance(urls);
      Class<?> c = cl.loadClass(pkg + ".Class1");
      assertNotNull(c);
      assertSame(c.getClassLoader(), cl);
      URL resource = cl.getResource(pkg + "/Interface1.class");
      assertNotNull(resource);
    }
    movePkgBack();
  }

  @Test
  public void testNewInstance2() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = new URL[1];
      urls[0] = new URL(dirUrl);
      URLClassLoader parent =  URLClassLoader.newInstance(urls);
      URLClassLoader cl =  URLClassLoader.newInstance(urls, parent);
      assertSame(parent, cl.getParent());

      Class<?> c = cl.loadClass(pkg + ".Class1");
      assertNotNull(c);
      assertSame(c.getClassLoader(), parent);

      String resName = pkg + "/Interface1.class";
      URL resource = cl.getResource(resName);
      assertNotNull(resource);

      resource = cl.getParent().getResource(resName);
      assertNotNull(resource);
    }
    movePkgBack();
  }

  public class Standard extends URLClassLoader {
    public Standard (URL[] urls) {
      super(urls);
    }

    public Standard(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }
  }

  public class Custom extends URLClassLoader {
    public Custom (URL[] urls) {
      super(urls);
    }
    
    public Custom(URL[] urls, ClassLoader parent) {
      super(urls, parent);
    }
    
    @Override
	protected Class<?> findClass(String name) throws ClassNotFoundException {
      return super.findClass(name);
    }
    
    @Override
	public Class<?> loadClass(String name) throws ClassNotFoundException {
      return super.loadClass(name);
    }
  }
    
  @Test
  public void testClassResolution() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      // create a url from a dir
      URL[] urls = { new URL(dirUrl) };
      String cname = pkg + ".Class1";
      String objClass = "java.lang.Object";

      Standard cl1 = new Standard(new URL[0]);
      Standard cl2 = new Standard(urls, cl1);
      Standard cl3 =  new Standard(urls, cl2);

      Class<?> c = cl3.loadClass(cname);
      assertEquals(c.getClassLoader(), cl2);

      c = cl3.loadClass(objClass);
      assertEquals(c.getClassLoader(), ClassLoader.getSystemClassLoader());

      Custom cl4 = new Custom(urls, null);
      Standard cl5 = new Standard(urls, cl4);

      c = cl5.loadClass(cname);  // delegates to cl4 (Custom)
      assertEquals(c.getClassLoader(), cl4);
      
      Class<?> c4 = cl4.loadClass(cname);
      assertSame(c, c4);

      c = cl5.loadClass(objClass);
      assertEquals(c.getClassLoader(), ClassLoader.getSystemClassLoader());
      assertSame(c, cl4.loadClass(objClass));

      cl4 = new Custom(urls, cl3);
      cl5 = new Standard(urls, cl4);

      c = cl5.loadClass(cname);
      assertEquals(c.getClassLoader(), cl2);
      assertSame(c, cl4.loadClass(cname));

      c = cl5.loadClass(objClass);
      assertEquals(c.getClassLoader(), ClassLoader.getSystemClassLoader());
      assertSame(c, cl4.loadClass(objClass));
    }
    movePkgBack();
  }

  @Test
  public void testFindSystemClass() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader loader = new TestClassLoader(urls);
      assertNotNull(loader.delegateTofindSystemClass("java.lang.Class"));

      String cname = pkg + ".Class1";
      assertNotNull(loader.loadClass(cname));

      try {
        loader.delegateTofindSystemClass(cname);
      } catch(ClassNotFoundException e) {
        
      }
    }
    movePkgBack();
  }

  @Test
  public void testFindSystemClass_ClassNotFoundException() throws MalformedURLException, ClassNotFoundException {
    movePkgOut();
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader cl = new TestClassLoader(urls);
      String cname = pkg + ".Class1";

      // this should fail, cause our SystemClassLoader cannot find a non-standard 
      // class that is not on the classpath
      cl.delegateTofindSystemClass(cname);
    }
    movePkgBack();
  }

  @Test
  public void testGetPackages() throws ClassNotFoundException, MalformedURLException {
    movePkgOut();
    if(verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader cl = new TestClassLoader(urls);
      Package[] pkgs = cl.getPackages();

      boolean java_lang = false;
      boolean classloader_specific_tests = false;
      for(int i=0; i<pkgs.length; i++) {
        if(pkgs[i].getName().equals("java.lang")) {
          java_lang = true;
        } else if(pkgs[i].getName().equals("classloader_specific_tests")) {
          classloader_specific_tests = true;
        }
      }
      assertTrue(java_lang && !classloader_specific_tests);

      String cname = pkg + ".Class1";
      cl.loadClass(cname);
      pkgs = cl.getPackages();
      for(int i=0; i<pkgs.length; i++) {
        if(pkgs[i].getName().equals("java.lang")) {
          java_lang = true;
        } else if(pkgs[i].getName().equals("classloader_specific_tests")) {
          classloader_specific_tests = true;
        }
      }
      assertTrue(java_lang && classloader_specific_tests);
    }
    movePkgBack();
  }

  @Test
  public void testGetPackage() throws ClassNotFoundException, MalformedURLException {
    movePkgOut();
    if(verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader cl = new TestClassLoader(urls);
      assertNotNull(cl.getPackage("java.lang"));
      assertNull(cl.getPackage("non_existing_package"));
      assertNull(cl.getPackage("classloader_specific_tests"));

      String cname = pkg + ".Class1";
      cl.loadClass(cname);
      assertNotNull(cl.getPackage("classloader_specific_tests"));
    }
    movePkgBack();
  }

  @Test
  public void testThrownException() throws ClassNotFoundException, MalformedURLException, SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    movePkgOut();
    if(verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      TestClassLoader loader = new TestClassLoader(urls);
      String cname = pkg + ".Class1";

      Class<?> c = loader.loadClass(cname);
      Method m = c.getMethod("causeArithmeticException", new Class<?>[0]);

      try {
        m.invoke(null, new Object[0]);
        fail("Should have thrown java.lang.ArithmeticException: division by zero");
        
      } catch (InvocationTargetException ite) {
        Throwable cause = ite.getCause();
        assertTrue( cause instanceof ArithmeticException && cause.getMessage().equals("division by zero"));
      }
    }
    movePkgBack();
  }
}
