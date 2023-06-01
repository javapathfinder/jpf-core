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

package gov.nasa.jpf.test.vm.reflection;

import gov.nasa.jpf.util.test.TestJPF;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import org.junit.Test;

/**
 * regression test for java.lang.reflect.Proxy
 */
public class ProxyTest extends TestJPF {
  
  interface Ifc {
    int foo (int a);
  }
  
  public static class MyHandler implements InvocationHandler {
    int data;
    
    MyHandler (int d){
      data = d;
    }
    
    @Override
	public Object invoke (Object proxy, Method mth, Object[] args){
      int a = (Integer)args[0];
      System.out.println("proxy invoke of " + mth);
      //System.out.println(" on " + proxy);
      System.out.println(" with " + a);

      return Integer.valueOf(data + a);
    }
  }

  @Test
  public void testBasicProxy (){
    if (verifyNoPropertyViolation()){
      MyHandler handler = new MyHandler(42);
      Ifc proxy = (Ifc)Proxy.newProxyInstance( Ifc.class.getClassLoader(),
                                               new Class[] { Ifc.class },
                                               handler);

      int res = proxy.foo(1);
      System.out.println(res);
      assertTrue( res == 43);
    }
  }
  
  //--------------- proxy for annotation
  @Retention(RetentionPolicy.RUNTIME)
  @interface AnnoIfc {
    int baz();
  }
  
  class AnnoHandler implements InvocationHandler {
    @Override
	public Object invoke (Object proxy, Method mth, Object[] args){
      System.out.println("proxy invoke of " + mth);
      return Integer.valueOf(42);
    }
  }
  
  @Test
  public void testAnnoProxy (){
    if (verifyNoPropertyViolation()){
      InvocationHandler handler = new AnnoHandler();
      AnnoIfc proxy = (AnnoIfc)Proxy.newProxyInstance( AnnoIfc.class.getClassLoader(),
                                               new Class[] { AnnoIfc.class },
                                               handler);

      int res = proxy.baz();
      System.out.println(res);
      assertTrue( res == 42);
    }
  }

  @Test
  public void testProxyName() {
    if (verifyNoPropertyViolation()){
      MyHandler handler = new MyHandler(42);
      Ifc ifc = (Ifc) Proxy.newProxyInstance(Ifc.class.getClassLoader(),
                                             new Class[] { Ifc.class },
                                             handler);
      String proxyClassName = ifc.getClass().getName();

      for (int i = 0; i < 10; i++) {
        ifc = (Ifc) Proxy.newProxyInstance(Ifc.class.getClassLoader(),
                                           new Class[] { Ifc.class },
                                           handler);
        assertEquals(ifc.getClass().getName(), proxyClassName);
      }

      String interfaceName = Ifc.class.getName();
      String packageName = interfaceName.substring(0, interfaceName.lastIndexOf('.'));
      String desiredProxyClsName = packageName + ".$Proxy$"
          + Integer.toHexString(Ifc.class.getName().hashCode());
      assertEquals(proxyClassName, desiredProxyClsName);
    }
  }

  static class NewThread extends Thread {

    Ifc ifc = null;

    @Override
    public void run() {
        MyHandler handler = new MyHandler(42);
        ifc = (Ifc) Proxy.newProxyInstance(Ifc.class.getClassLoader(),
                                           new Class[] { Ifc.class },
                                           handler);
    }
  }

  @Test
  public void testProxyCreationInCaseOfChoiceGenerator() {
    if (verifyNoPropertyViolation()){
      NewThread t = new NewThread();
      t.start();
      MyHandler handler = new MyHandler(42);
      Ifc ifc = (Ifc) Proxy.newProxyInstance(Ifc.class.getClassLoader(),
                                             new Class[] { Ifc.class },
                                             handler);

      try {
        t.join();
      } catch (InterruptedException ie) {
        ie.printStackTrace();
      }
      Ifc ifcInOtherThread = t.ifc;
      assertEquals(ifc.getClass().getName(), ifcInOtherThread.getClass().getName());

      String interfaceName = Ifc.class.getName();
      String packageName = interfaceName.substring(0, interfaceName.lastIndexOf('.'));
      String desiredProxyClsName = packageName + ".$Proxy$"
          + Integer.toHexString(Ifc.class.getName().hashCode());
      assertEquals(ifc.getClass().getName(), desiredProxyClsName);
    }
  }

  @Test
  public void testIsProxyClass() {
    if (verifyNoPropertyViolation()){
      MyHandler handler = new MyHandler(42);
      Ifc ifc = (Ifc) Proxy.newProxyInstance(Ifc.class.getClassLoader(),
                                             new Class[] { Ifc.class },
                                             handler);
      assertTrue(Proxy.isProxyClass(ifc.getClass()));
      assertFalse(Proxy.isProxyClass(this.getClass()));
    }
  }

  @Test
  public void testGetInvocationHandler() {
    if (verifyNoPropertyViolation()){
      MyHandler handler = new MyHandler(42);
      Ifc ifc = (Ifc) Proxy.newProxyInstance(Ifc.class.getClassLoader(),
                                             new Class[] { Ifc.class },
                                             handler);
      assertTrue(handler == Proxy.getInvocationHandler(ifc));
    }
  }

  interface F {
    int add(int a, int b);
  }

  interface G {
    String concat(String s1, String s2);
  }

  public static class SimpleHandler implements InvocationHandler {

    @Override
    public Object invoke (Object proxy, Method mtd, Object[] args){
      if (mtd.getName().equals("add")) {
        int a = (int) args[0] +  (int) args[1];
        return a;
      } else if (mtd.getName().equals("concat")) {
        String s = (String) args[0] + (String) args[1];
        return s;
      }
      return null;
    }
  }

  @Test
  public void testProxyInvocation() {
    SimpleHandler h = new SimpleHandler();
    Object fg = Proxy.newProxyInstance(F.class.getClassLoader(), new Class[] { F.class, G.class }, h);
    assertEquals(((F) fg).add(1, 2), 3);
    assertEquals(((G) fg).concat("a", "b"), "ab");
  }

}
