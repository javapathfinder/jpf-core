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

package gov.nasa.jpf.vm.multiProcess;

import gov.nasa.jpf.util.test.TestMultiProcessJPF;
import gov.nasa.jpf.vm.MethodInfo;

import java.lang.reflect.Method;
import java.util.List;

import org.junit.Test;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class MethodTest extends TestMultiProcessJPF {
  public native void keepMethod(Method m, int prcId);

  // To check the type safe cloning of methods
  @Test
  public void methodCloneTest() throws SecurityException, NoSuchMethodException {
    if(!isJPFRun()) {
      JPF_gov_nasa_jpf_vm_multiProcess_MethodTest.resetPrcIds();
    }

    if (mpVerifyNoPropertyViolation(2)) {
      Method m = MethodTest.class.getMethod("methodCloneTest", new Class[]{});
      int prcId = getProcessId();
      keepMethod(m, prcId);
    }

    if(!isJPFRun()) {
      List<MethodInfo> methods = JPF_gov_nasa_jpf_vm_multiProcess_MethodTest.getMethods();
      assertEquals(methods.size(), 2);
      assertTrue(methods.get(0)!=methods.get(1));
    }
  }

  @Test
  public void methodDeclaringClassTest() throws SecurityException, NoSuchMethodException {
    if (mpVerifyNoPropertyViolation(2)) {
      Class<?> cls = MethodTest.class;

      // The loader of this class should be the same as the loader that loads 
      // the class java.lang.Thread within this process
      assertEquals(cls.getClassLoader(), ClassLoader.getSystemClassLoader());
      for(Method m: cls.getDeclaredMethods()) {
        assertEquals(m.getDeclaringClass(), cls);
      }
    }
  }
}
