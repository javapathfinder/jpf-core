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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassLoaderInfo;

import org.junit.Test;

/*
 * VM.registerStartupClass must be kept in sync with ClassInfo.registerClass.
 * This test ensures that the interfaces of the main class are registered 
 * properly.  The old VM.registerStartupClass code wasn't initializing the
 * class object of the interfaces.
 */
public class InitializeInterfaceClassObjectRefTest extends TestJPF implements InitializeInterfaceClassObjectRefTestInterface
{
   @Test
   public void test()
   {
      if (verifyUnhandledExceptionDetails(RuntimeException.class.getName(), "This test throws an expected exception.", "+log.finest+=,gov.nasa.jpf.vm.ClassInfo"))
      {
         // Throw an exception to avoid backtracking.  Backtracking will wipe out the class object ref.
         throw new RuntimeException("This test throws an expected exception.");
      }
      else
      {
         ClassInfo ci = ClassLoaderInfo.getCurrentResolvedClassInfo( InitializeInterfaceClassObjectRefTestInterface.class.getName());
         
         if (ci.getClassObjectRef() < 0)
            throw new AssertionError("ci.getClassObjectRef() < 0 : " + ci.getClassObjectRef());
      }
   }
}

interface InitializeInterfaceClassObjectRefTestInterface
{
}
