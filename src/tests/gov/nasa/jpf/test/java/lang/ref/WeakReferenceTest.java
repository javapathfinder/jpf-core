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

package gov.nasa.jpf.test.java.lang.ref;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import java.lang.ref.WeakReference;

import org.junit.Test;

public class WeakReferenceTest extends TestJPF
{
   @Test
   public void testGCClearsRef()
   {
      WeakReference<Target> ref;

      if (verifyNoPropertyViolation())
      {
         ref = new WeakReference<Target>(new Target());

         forceGC();
         
         assertNull(ref.get());
      }
   }

   @Test
   public void testStrongReferenceKeepsWeakReference()
   {
      WeakReference<Target> ref;
      Target target;

      if (verifyNoPropertyViolation())
      {
         target = new Target();
         ref    = new WeakReference<Target>(target);

         forceGC();

         assertSame(target, ref.get());
      }
   }

   /* ClassInfo.refClassInfo wasn't being set to null between JPF runs.  Thus, 
    * refClassInfo wasn't being updated.  Hence, the WeakReference below would 
    * be treated as a normal object in GC.  Re-run testGCClearsRef() to 
    * reproduce the issue.
    */
   @Test
   public void testClearClassInfoRefClassInfo()
   {
      testGCClearsRef();
   }
   
   private static void forceGC()
   {
      System.gc();         // Mark that GC is needed
      Verify.breakTransition("testForceGC"); // Cause a state to be captured and hence GC to run
   }
   
   private static class Target   // Make this object easy to find in JPF heap
   {
   }
}
