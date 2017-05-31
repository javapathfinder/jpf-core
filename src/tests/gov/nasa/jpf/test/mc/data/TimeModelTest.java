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
package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

/**
 * regression test for TimeModel implementations
 */
public class TimeModelTest extends TestJPF {
  
  @Test
  public void testSystemTime(){
    
    if (verifyNoPropertyViolation("+vm.time.class=.vm.SystemTime")){
      long t1 = System.currentTimeMillis();
      System.out.printf("t1 = %d\n", t1);
      
      boolean b2 = Verify.getBoolean();
      long t2 = System.currentTimeMillis();
      System.out.printf("  t2 = %d\n", t2);

      boolean b3 = Verify.getBoolean();
      long t3 = System.currentTimeMillis();
      System.out.printf("    t3 = %d\n", t3);

      assertTrue((t3 >= t2) && (t2 >= t1));
    }
  }
  
  @Test
  public void testPathTime(){
    
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
    }
    
    if (verifyNoPropertyViolation("+vm.time.class=.vm.ConstInsnPathTime")){
      long t1 = System.currentTimeMillis();
      System.out.printf("t1 = %d\n", t1);
      
      boolean b2 = Verify.getBoolean(true); // we do falseFirst
      long t2 = System.currentTimeMillis();
      System.out.printf("  t2 = %d\n", t2);
      
      if (b2){ // has to be second time around
        assertTrue(t2 == Verify.getCounter(0));
      } else {
        Verify.setCounter(0,(int)t2);
      }


      boolean b3 = Verify.getBoolean(true);  // store the result so that we don't state match
      long t3 = System.currentTimeMillis();
      System.out.printf("    t3 = %d\n", t3);

      if (b3){ // has to be second time around
        assertTrue(t3 == Verify.getCounter(1));
      } else {
        Verify.setCounter(1,(int)t3);
      }
      
      assertTrue((t3 > t2) && (t2 > t1));
    }
    
    
  }
  
  
}
