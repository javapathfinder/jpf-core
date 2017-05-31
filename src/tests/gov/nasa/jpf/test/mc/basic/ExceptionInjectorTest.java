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

import java.io.File;
import java.io.IOException;

import org.junit.Test;

/**
 * regression test for ExceptionInjector listener
 */
public class ExceptionInjectorTest extends TestJPF {
  
  @Test
  public void testAbsLine () {
    if (verifyNoPropertyViolation("+listener=.listener.ExceptionInjector",
                                  "+ei.exception=java.lang.ArithmeticException@gov.nasa.jpf.test.mc.basic.ExceptionInjectorTest:41")){
      boolean handled = false;
      try {
        int x = 10;
        int y = 2;
        int z = x / y;                    // <<<< perfectly fine, but we want it to blow up , line 41
      } catch (ArithmeticException ax){   // ..so that we can check the handler
        handled = true;
        System.out.println("got it handled");
        ax.printStackTrace();
      }

      assert handled : "failed to throw exception";
    }
  }

  static class Zapp extends RuntimeException {
    Zapp (String details){
      super(details);
    }
  }

  // NOTE - offsets count from the first statement line in the method body
  @Test
  public void testMethodOffset () {
    if (verifyNoPropertyViolation("+listener=.listener.ExceptionInjector",
                                  "+ei.exception=gov.nasa.jpf.test.mc.basic.ExceptionInjectorTest$Zapp(\"gotcha\")@gov.nasa.jpf.test.mc.basic.ExceptionInjectorTest.testMethodOffset():6")){
      boolean handled = false;
      try {
        int x = 10;
        int y = 2;
        int z = x / y;    // <<<< method offset +6: perfectly fine, but we want it to blow up
      } catch (Zapp x){   // ..so that we can check the handler
        handled = true;
        System.out.println(x);
      }

      assert handled : "failed to throw exception";
    }
  }

  @Test
  public void testCallee () {
    if (verifyNoPropertyViolation("+listener=.listener.ExceptionInjector",
                                  "+ei.exception=java.io.IOException@java.io.File.createTempFile(java.lang.String,java.lang.String)")){
      boolean handled = false;
      try {
        File f = File.createTempFile("foo", "bar");
      } catch (IOException x){  // if the temp file could not be created (how do you force this?)
        handled = true;
        x.printStackTrace();
      }

      assert handled : "failed to throw exception";
    }

  }

}
