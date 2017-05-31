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
 * Ensures that a recursive lock/unlock doesn't leave the lock in an acquired state.
 */
public class UnlockNonSharedTest extends TestJPF
{
   @Test
   public void test() throws InterruptedException
   {
      Runnable task, nop;
      Thread thread;
      
      if (verifyNoPropertyViolation())
      {
         Verify.setProperties("vm.por.skip_local_sync=true");

         nop = new Runnable()
         {
            @Override
			public void run()
            {
               // nothing to do
            }
         };
         
         task = new Runnable()
         {
            private final Object m_lock = new Object();
            private       int    m_count;
            
            @Override
			public void run()
            {
               synchronized (m_lock)
               {
                  m_count++;
               }
            }
         };
         
         task.run();                    // Acquire m_lock in a single-threaded state so that MONITOREXIT.isShared() will return false.  (The bug would cause ei.unlock() to not be called).
         
         thread = new Thread(task);     // Create a thread to acquire m_lock.
         
         thread.setDaemon(false);
         thread.start();                // To reproduce the bug, this thread shouldn't start executing until after the main thread exits.  This thread should then execute to the point just before acquiring m_lock.
         
         thread = new Thread(nop);      // Create another thread.  This thread must not be able to reach m_lock.
   
         thread.setDaemon(false);
         thread.start();                // This thread should start and exit before the above thread acquires m_lock.  Due to the bug, the above thread won't be able to acquire the lock and hence a "deadlock" will ensue.
      }
   }
}
