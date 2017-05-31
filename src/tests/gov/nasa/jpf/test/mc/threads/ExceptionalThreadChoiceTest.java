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
import gov.nasa.jpf.vm.Verify;
import java.io.IOException;
import java.net.SocketTimeoutException;
import org.junit.Test;

/**
 *
 */
public class ExceptionalThreadChoiceTest extends TestJPF {

  native void foo() throws IOException, SocketTimeoutException; // this gets rescheduled with exceptions
  
  @Test
  public void testExceptions (){
    if (!isJPFRun()){
      Verify.resetCounter(0);
      Verify.resetCounter(1);
      Verify.resetCounter(2);
    }
    
    if (verifyNoPropertyViolation()){
      try {
        foo();
        System.out.println("main no exception");
        Verify.incrementCounter(0);
      }  catch (SocketTimeoutException stox){ // order matters since SocketTimeoutException is also a IOException
        System.out.println("main got SocketTimeoutException");
        Verify.incrementCounter(1);
      } catch (IOException iox){
        System.out.println("main got IOException");
        Verify.incrementCounter(2);
      }
    }
    
    if (!isJPFRun()){
      assertTrue( "nominal path missing", Verify.getCounter(0) > 0);
      assertTrue( "SocketTimeoutException missing", Verify.getCounter(1) > 0);
      assertTrue( "IOException missing", Verify.getCounter(2) > 0);
    }    
  }
  
  
}
