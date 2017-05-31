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
package gov.nasa.jpf.test.vm.basic;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

import gov.nasa.jpf.test.java.net.LoadUtility;
import org.junit.Test;

/**
 * JPF part of assertion test
 */
public class AssertTest extends LoadUtility {

  @Test public void testAssertionViolation () {
    if (verifyAssertionErrorDetails("oops, assertion failed")){
      int i = 1;
      assert i == 0 : "oops, assertion failed";
    }
  }

  @Test public void testNoAssertionViolation () {
    if (verifyNoPropertyViolation("+vm.disable_assertions=*AssertTest")){
      int i = 1;
      assert i == 0 : "oops, assertion failed";
    }
  }

  public static void invokeAssertFalse(ClassLoader loader, String cname) throws ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException  {
    Class<?> c = loader.loadClass(cname);
    Method m = c.getMethod("assertFalse", new Class<?>[0]);
    m.invoke(null, new Object[0]);
  }

  @Test
  public void testSetClassAssertionStatus1() {
    if (verifyAssertionErrorDetails("oops, assertion failed")) {
      ClassLoader cl = ClassLoader.getSystemClassLoader();

      // this should change the "desiredAssertionStatus()" return value to false, 
      // but it shouldn't change the actual assertion status since the class has 
      // been already initialized
      cl.setClassAssertionStatus("gov.nasa.jpf.test.vm.basic.AssertTest", false);
      assertFalse(AssertTest.class.desiredAssertionStatus());

      // throw AssertionError
      assert false : "oops, assertion failed";
    }
  }

  @Test
  public void testSetClassAssertionStatus2() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalAccessException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader loader = new URLClassLoader(urls);

      String c1 = pkg + ".Class1";
      String c2 = pkg + ".Class2";
      loader.setClassAssertionStatus(c1, false);

      try {
        invokeAssertFalse(loader, c1);
      } catch(Exception ae) {
        fail("setClassAssertionStatus should have avoided this!");
      }

      loader.setClassAssertionStatus(c2, false);
      assertFalse(loader.loadClass(c2).desiredAssertionStatus());
      try {
        invokeAssertFalse(loader, c2);
        fail();
      } catch (InvocationTargetException e) {
        // success
      }
    }
    movePkgBack();
  }

  @Test
  public void testSetPackageAssertionStatus() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalAccessException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader loader = new URLClassLoader(urls);
      loader.setPackageAssertionStatus(pkg, false);

      String c1 = pkg + ".Class1";
      try {
        invokeAssertFalse(loader, c1);
      } catch(Exception ae) {
        fail("setPackageAssertionStatus should have avoided this!");
      }

      loader.setPackageAssertionStatus(pkg, true);
      assertTrue(loader.loadClass(c1).desiredAssertionStatus());
      try {
        invokeAssertFalse(loader, c1);
      } catch(Exception ae) {
        fail("setPackageAssertionStatus shouldn't change actual assertion status");
      }
    }
    movePkgBack();
  }

  @Test
  public void testSetDefaultAssertionStatus() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalAccessException {
    movePkgOut();
    if (verifyNoPropertyViolation()) {
      URL[] urls = { new URL(dirUrl) };
      URLClassLoader loader = new URLClassLoader(urls);
      loader.setDefaultAssertionStatus(false);

      String c1 = pkg + ".Class1";
      try {
        invokeAssertFalse(loader, c1);
      } catch(Exception ae) {
        fail("setDefaultAssertionStatus should have avoided this!");
      }

      // shouldn't have any effect on the actual assertion status of the class since 
      // it has been already initialized
      loader.setDefaultAssertionStatus(true);
      assertTrue(loader.loadClass(c1).desiredAssertionStatus());
      try {
        invokeAssertFalse(loader, c1);
      } catch(Exception ae) {
        fail("setDefaultAssertionStatus shouldn't change actual assertion status");
      }
    }
    movePkgBack();
  }
  
  @Test
  public void testClearAssertionStatus() throws MalformedURLException, ClassNotFoundException, SecurityException, NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    movePkgOut();
    if (verifyNoPropertyViolation("+vm.disable_assertions=*")) {

      URL[] urls = { new URL(dirUrl) };
      URLClassLoader loader = new URLClassLoader(urls);
      loader.setDefaultAssertionStatus(true);

      String c1 = pkg + ".Class1";
      try {
        invokeAssertFalse(loader, c1);
        fail();
      } catch(InvocationTargetException ae) {
        // success!
      }
      
      // shouldn't have any effect on the actual assertion status of the class since 
      // it has been already initialized
      loader.clearAssertionStatus();
      assertFalse(loader.loadClass(c1).desiredAssertionStatus());
      try {
        invokeAssertFalse(loader, c1);
        fail();
      } catch(Exception ae) {
        // success!
      }

      String c3 = pkg + ".Class3";
      try {
        invokeAssertFalse(loader, c3);
      } catch(Exception ae) {
        fail("clearAssertionStatus() should have set the assertion status for the " +
        		"loader to false");
      }
    }
    movePkgBack();
  }
}
