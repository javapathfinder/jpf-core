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

import org.junit.Test;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ThreadInfo;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 * 
 * Test suit for FinalizerThread & FinalizeThreadInfo
 */
public class FinalizerThreadTest extends TestJPF {
  
  static class Finalize {
    static void createFinalize() {
      new Finalize();
    }
    
    @Override
    protected void finalize() throws Throwable {
      System.out.println("finalizer executing... ");
      throw new Exception();
    }
  }
  
  @Test
  public void testExceptionFromFinalizer (){
    if (verifyNoPropertyViolation( "+vm.process_finalizers=true")){
      // FinalizerThread should swallow the exception thrown in the finalize() method
      new Finalize();
    }
  }
  
  public static class FinalizerThreadListener extends ListenerAdapter {

    @Override
    public void stateAdvanced(Search search){
      if(search.isEndState()) {
        ThreadInfo currTi = search.getVM().getCurrentThread();
        ThreadInfo finalizerTi = search.getVM().getFinalizerThread();
        
        // make sure a finalizer thread exists
        assertTrue(finalizerTi!=null);
        
        // make sure the thread leading to the end state is finalizer
        assertEquals(currTi, currTi);
      }
    }
  }
  
  private static String[] JPF_ARGS = { "+vm.process_finalizers=true",
                                       "+listener=gov.nasa.jpf.test.mc.threads.FinalizerThreadTest$FinalizerThreadListener"};  
  @Test
  public void testFinalizerThreadRunning () {
    if (verifyNoPropertyViolation(JPF_ARGS)){
      Finalize.createFinalize();
    }
  }
  
  // This is to make sure that an idle finalizer does not cause a deadlock in single-process apps
  @Test
  public void testIdleFinalizerThread () {
    if (verifyNoPropertyViolation("+vm.process_finalizers=true")){
    }
  }
}
