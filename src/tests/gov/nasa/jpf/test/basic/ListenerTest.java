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
package gov.nasa.jpf.test.basic;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.SingleProcessVM;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

public class ListenerTest extends TestJPF {

  public static class Listener extends ListenerAdapter {
    @Override
    public void searchStarted (Search search){
      System.out.println("-- listener got searchStarted() notification");
      Verify.incrementCounter(0);
    }
  }
  
  public static class TestVM extends SingleProcessVM {
    public TestVM (JPF jpf, Config config){
      super(jpf, config);
      
      jpf.addListener( new Listener());
    }
  }
  
  @Test
  public void testPendingListeners (){
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation("+vm.class=gov.nasa.jpf.test.basic.ListenerTest$TestVM")){
      System.out.println("this is verified by JPF");
    }
    
    if (!isJPFRun()){
      assertTrue("init listener got no searchStarted() notification", Verify.getCounter(0) == 1);
    }
  }
  
  // <2do> ... and tons more to follow
}
