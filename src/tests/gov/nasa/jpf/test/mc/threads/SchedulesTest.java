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

package gov.nasa.jpf.test.mc.threads;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;



public class SchedulesTest extends TestJPF {
  
  @Test public void testSleep () {

    if (verifyNoPropertyViolation("+cg.threads.break_start=true",
                                  "+cg.threads.break_yield=true",
                                  "+cg.threads.break_sleep=true",
                                  "+listener=.listener.PathOutputMonitor",
                                  "+pom.all=test/gov/nasa/jpf/test/mc/threads/SchedulesTest-output")) {
      Runnable r = new Runnable() {

        @Override
		public void run() {
          System.out.println("T started");
          try {
            System.out.println("T sleeping");
            Thread.sleep(100);
          } catch (InterruptedException ix) {
            throw new RuntimeException("unexpected interrupt");
          }
          System.out.println("T finished");
        }
      };

      Thread t = new Thread(r);
      System.out.println("main starting T");
      t.start();

      System.out.println("main finished");
    }
  }
}
