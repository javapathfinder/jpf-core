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
package gov.nasa.jpf.test.java.concurrent;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import java.util.concurrent.Exchanger;
import java.util.concurrent.TimeUnit;
import org.junit.Test;

/**
 * regression test for java.util.concurrent.Exchanger
 */
public class ExchangerTest extends TestJPF  {

  static Exchanger<String> exchanger = new Exchanger<String>();
  
  static class ExTo extends Thread {
    @Override
	public void run() {
      try {
        //interrupt();
        System.out.println("T now exchanging..");

        String response = exchanger.exchange("hi", 1000, TimeUnit.MILLISECONDS);

        System.out.print("T got: ");
        System.out.println(response);
        
        assertTrue(response.equals("there"));
        Verify.setBitInBitSet(0, 0, true);
        
      } catch (Throwable x) {
        System.out.print("T got exception: ");
        System.out.println(x);
        Verify.setBitInBitSet(0, 1, true);
      }
    }
  }
  
  @Test
  public void testTimeoutExchange() {
    
    if (verifyNoPropertyViolation()){
      Thread t = new ExTo();
      t.start();
    
      try {
        System.out.println("M now exchanging..");

        String response = exchanger.exchange("there", 100, TimeUnit.MILLISECONDS);

        System.out.print("M got: ");
        System.out.println(response);
        assertTrue(response.equals("hi"));
        Verify.setBitInBitSet(0, 2, true);

        
      } catch (Throwable x){
        System.out.print("M got exception: ");
        System.out.println( x);
        Verify.setBitInBitSet(0, 3, true);
      }     
      
    } else { // not executed under JPF
      // check if we saw each path at least once
      assertTrue( Verify.getBitInBitSet(0, 0));
      assertTrue( Verify.getBitInBitSet(0, 1));
      assertTrue( Verify.getBitInBitSet(0, 2));
      assertTrue( Verify.getBitInBitSet(0, 3));
    }
  }
}
