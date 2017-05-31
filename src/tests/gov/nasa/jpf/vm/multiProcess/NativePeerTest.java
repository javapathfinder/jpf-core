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

import org.junit.Test;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class NativePeerTest extends TestMultiProcessJPF {

  native void incNativeCounters();

  native int getNativeCounter();

  // To make sure that native peers are kept separately - note different class
  // with the same fully qualified name still share the same NativePeer class
  // but they keep different instances of it
  @Test
  public void nativePeerTest() {
    // Note that this code is executed 4 times (twice by each process main thread).
    // Since we do not restore NativePeer states the maximum value of counter in 
    // NativePeer should be 2
    if (mpVerifyNoPropertyViolation(2, "+vm.max_transition_length=MAX")) { // make sure we don't get a spurious CG
      incNativeCounters();

      int i = getNativeCounter();
      assertTrue(i==1 || i==2);
    }

    if(!isJPFRun()) {
      // To make sure this code is executed 4 times
      assertEquals(JPF_gov_nasa_jpf_vm_multiProcess_NativePeerTest.getStaticNativeCounter(), 4);
    }
  }
}
