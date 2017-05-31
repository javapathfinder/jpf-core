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
package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 * test case for the shared object attribute detection, which is required by POR
 * NOTE: these test cases only make sense when executed under JPF, since they
 * depend on race conditions that are most likely not experienced when running on
 * a normal VM
 *
 */
public class SharedRefTest extends TestJPF implements Runnable {
  
  static class SharedOrNot {
    boolean changed;
  }
  
  SharedOrNot o;

  public SharedRefTest () {
    // only for JUnit
  }

  SharedRefTest (SharedOrNot o) {
    // don't make this public or JUnit will choke
    this.o = o;
  }
  
  @Override
  public void run () {
    boolean b = o.changed;
    o.changed = !b;
    assert o.changed != b : "Argh, data race for o";
  }
  
  /**
   * this on should produce an AssertionError under JPF
   */
  @Test public void testShared () {
    if (verifyAssertionError()) {
      SharedOrNot s = new SharedOrNot();

      Thread t1 = new Thread(new SharedRefTest(s));
      Thread t2 = new Thread(new SharedRefTest(s));

      t1.start();
      t2.start();
    }
  }
  
  /**
   * and this one shouldn't
   */
  @Test public void testNonShared () {
    if (verifyNoPropertyViolation()) {
      SharedOrNot s = new SharedOrNot();
      Thread t1 = new Thread(new SharedRefTest(s));

      s = new SharedOrNot();
      Thread t2 = new Thread(new SharedRefTest(s));

      t1.start();
      t2.start();
    }
  }

  static SharedRefTest rStatic = new SharedRefTest( new SharedOrNot());
  
  @Test
  public void testSharedStaticRoot () {
    if (verifyAssertionError()) {
      Thread t = new Thread(rStatic);

      t.start();

      rStatic.o.changed = false; // why wouldn't 'true' trigger an assertion :)
    }
  }
  
  //--- test explicit sharedness management
  
  static class Global {
    public static Global x = new Global();
   
    int d;
  }
  
  @Test
  public void testShareControl () {
    if (verifyNoPropertyViolation()) {
      Verify.setShared( Global.class, false);
      Verify.freezeSharedness( Global.class, true);

      Verify.setShared( Global.x, false);
      Verify.freezeSharedness( Global.x, true);
      
      // now references to Global.x should not break anymore
     
      Thread t = new Thread() {
        @Override
		public void run() {
          Verify.println("T inc");
          Global.x.d++;
          Verify.println("T dec");
          Global.x.d--;
          assertTrue( Global.x.d == 0);
        }
      };
      
      t.start();
      
      Verify.println("M inc");
      Global.x.d++;
      Verify.println("M dec");
      Global.x.d--;
      assertTrue( Global.x.d == 0);
    }
  }

}
