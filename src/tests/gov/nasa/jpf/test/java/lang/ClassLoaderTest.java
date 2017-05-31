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
package gov.nasa.jpf.test.java.lang;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;

import org.junit.Test;

/**
 * test of java.lang.ClassLoader API
 */
public class ClassLoaderTest extends TestJPF {
  
  @Test
  public void testGetResource() {
    if(verifyNoPropertyViolation()) {
      testGetResourceImpl(new TestClassLoader());
    }
  }

  @Test
  public void testGetResources() throws IOException{
    if(verifyNoPropertyViolation()) {
      testGetResourcesImpl(new TestClassLoader());
    }
  }

  @Test
  public void testGetResourceAsStream() throws IOException{
    if(verifyNoPropertyViolation()) {
      testGetResourceAsStreamImpl(new TestClassLoader());
    }
  }

  @Test
  public void testLoadClass() {
    if(verifyNoPropertyViolation()) {
      ClassLoader classLoader = new TestClassLoader();
      try {
        classLoader.loadClass("non_existing_class");
        fail();
      }catch(ClassNotFoundException e) {}
    }
  }

  @Test
  public void testLoadClass2() {
    if(verifyNoPropertyViolation()) {
      ClassLoader classLoader = new TestClassLoader();
      try {
        classLoader.loadClass(ClassLoader.class.getName());
      }catch(ClassNotFoundException e) {
        fail(e.getMessage());
      }
    }
  }

  @Test
  public void testGetSystemResource() {
    if(verifyNoPropertyViolation()) {
      testGetResourceImpl( ClassLoader.getSystemClassLoader());
    }
  }

  @Test
  public void testGetSystemResources() throws IOException{
    if(verifyNoPropertyViolation()) {
      testGetResourcesImpl( ClassLoader.getSystemClassLoader());
    }
  }

  @Test
  public void testGetSystemResourceAsStream() throws IOException{
    if(verifyNoPropertyViolation()) {
      testGetResourceAsStreamImpl( ClassLoader.getSystemClassLoader());
    }
  }

  @Test
  public void testGetSystemClassLoader() {
    if(verifyNoPropertyViolation()) {
      ClassLoader classLoader = new TestClassLoader();
      assertNotNull(ClassLoader.getSystemClassLoader());
      assertNull(ClassLoader.getSystemClassLoader().getParent());
      assertFalse(classLoader.equals(ClassLoader.getSystemClassLoader()));
    }
  }

  @Test
  public void testGetParent() {
    if(verifyNoPropertyViolation()) {
      ClassLoader classLoader = new TestClassLoader();
      assertNotNull(classLoader.getParent());
      assertEquals(classLoader.getParent(),ClassLoader.getSystemClassLoader());
    }
  }

  @Test
  public void testGetParent2() {
    if(verifyNoPropertyViolation()) {
      ClassLoader parentClassLoader = new TestClassLoader();
      ClassLoader classLoader = new TestClassLoader(parentClassLoader);
      assertEquals(parentClassLoader, classLoader.getParent());
    }
  }

  @Test
  public void testFoundResources() throws IOException {
    if(verifyNoPropertyViolation()) {
      TestClassLoader classLoader = new TestClassLoader();
      Enumeration<URL> enm = classLoader.findResources("not_existing_resource"); 
      assertNotNull(enm);
      assertFalse(enm.hasMoreElements());
    }
  }

  private void testGetResourceImpl(ClassLoader classLoader) {
    assertNull(classLoader.getResource("not_existing_resource"));
    assertNotNull(classLoader.getResource("DiningPhil.class"));
    assertNull(classLoader.getResource("ClassLoader.class"));
    assertNotNull(classLoader.getResource("java/lang/ClassLoader.class"));
  }

  private void testGetResourcesImpl(ClassLoader classLoader) throws IOException{
    assertFalse(classLoader.getResources("not_existing_resources").hasMoreElements());

    Enumeration<?> e = classLoader.getResources("DiningPhil.class");
    assertTrue(e.hasMoreElements());
    assertNotNull(e.nextElement());
    assertFalse(e.hasMoreElements());

    e = classLoader.getResources("ClassLoader.class");
    assertFalse(e.hasMoreElements());

    // It should find at least two resources: 1. model class, 2. JDK class
    e = classLoader.getResources("java/lang/ClassLoader.class");
    assertTrue(e.hasMoreElements());
    assertNotNull(e.nextElement());
    assertTrue(e.hasMoreElements());
    assertNotNull(e.nextElement());
  }

  private void testGetResourceAsStreamImpl(ClassLoader classLoader) throws IOException{
    assertNull(classLoader.getResourceAsStream("not_existing_resources"));
    InputStream is = classLoader.getResourceAsStream("DiningPhil.class");
    assertNotNull(is);
    assertTrue(is.read() > 0);
  }

  class TestClassLoader extends ClassLoader {
      
    public TestClassLoader() {
      super();
    }

    public TestClassLoader(ClassLoader parent) {
      super(parent);
    }

    @Override
	protected Enumeration<URL> findResources(String name) throws IOException {
      return super.findResources(name);
    }
  }
}
