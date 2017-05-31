/*
 * Copyright (C) 2015, United States Government, as represented by the
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
package gov.nasa.jpf.test.mc.threads;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * test deadlock detection during concurrent class init.
 *
 * This models hotspot behavior that can explode the state space
 * and hence needs to be explicitly configured to perform
 * nested locking during class init
 */
public class NestedInitTest extends TestJPF {

  //------------------------------------------------ check normal clinit execution
  static class Root {
    static int data;

    static {
      System.out.print( Thread.currentThread().getName());
      System.out.println(" in Root.<clinit>()");
      data = 40;
    }
  }

  static class Base extends Root {
    static int data;

    static {
      System.out.print( Thread.currentThread().getName());
      System.out.println(" in Base.<clinit>()");
      data = Root.data + 1;
    }
  }

  static class Derived extends Base {
    static int data;

    static {
      System.out.print( Thread.currentThread().getName());
      System.out.println(" in Derived.<clinit>()");
      data = Base.data + 1;
    }
  }

  @Test
  public void testNestedInitSingleOk() {
    if (verifyNoPropertyViolation("+jvm.nested_init")){
      new Derived(); // force clinit
      System.out.print("Derived.data = ");
      System.out.println(Derived.data);
      assertTrue(Derived.data == 42);
    }
  }

  @Test
  public void testNestedInitConcurrentOk() {
    if (verifyNoPropertyViolation("+jvm.nested_init")){
      new Thread( new Runnable(){
        public void run(){
          new Derived();
          System.out.print("t: Derived.data = ");
          System.out.println(Derived.data);
          assertTrue(Derived.data == 42);
        }
      }).start();

      new Derived(); // force clinit
      System.out.print("main: Derived.data = ");
      System.out.println(Derived.data);
      assertTrue(Derived.data == 42);
    }
  }

  //--- and now the nasty cases

  //------------------------------------------ symmetric case

  static class A {
    static final B b = new B();
  }

  static class B {
    static final A a = new A();
  }

  @Test
  public void testSymmetricDeadlock() {
    if (verifyDeadlock("+jvm.nested_init")) {
      new Thread() {
        public void run() {
          new A();
        }
      }.start();
      new B();
    }
  }

  //------------------------------------------- hierarchical case
  public static class CyclicBase {
    static CyclicDerived sub = new CyclicDerived();
  }

  public static class CyclicDerived extends CyclicBase {
  }


  @Test
  public void testCyclicHierarchyDeadlock (){
    if (verifyDeadlock("+jvm.nested_init")) {
      new Thread() {
        public void run() {
          new CyclicDerived(); // causes class inits via CyclicDerived
        }
      }.start();

      Object o = CyclicBase.sub; // causes class inits via CyclicBase
    }
  }

}
