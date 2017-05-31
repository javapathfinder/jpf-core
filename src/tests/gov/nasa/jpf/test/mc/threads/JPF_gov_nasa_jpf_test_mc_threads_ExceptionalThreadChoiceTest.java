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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.annotation.MJI;
import gov.nasa.jpf.vm.MJIEnv;
import gov.nasa.jpf.vm.NativePeer;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.choice.ExceptionThreadChoiceFromSet;

/**
 *
 */
public class JPF_gov_nasa_jpf_test_mc_threads_ExceptionalThreadChoiceTest extends NativePeer {

  @MJI
  public void foo____V (MJIEnv env, int objRef){
    
    ThreadInfo ti = env.getThreadInfo();
    if (!ti.isFirstStepInsn()){
      String[] exceptions = { "java.net.SocketTimeoutException", "java.io.IOException" };
      
      System.out.println("    in top half of native foo()");
      VM vm = ti.getVM();
      ThreadInfo[] runnables = vm.getThreadList().getAllMatching(vm.getTimedoutRunnablePredicate());
      ExceptionThreadChoiceFromSet cg = new ExceptionThreadChoiceFromSet("FOO_CG", runnables, ti, exceptions);
      
      ti.getVM().getSystemState().setNextChoiceGenerator(cg);
      env.repeatInvocation();
      
    } else {
      System.out.println("    in bottom half of native foo()");
      
      ExceptionThreadChoiceFromSet cg = ti.getVM().getCurrentChoiceGenerator("FOO_CG", ExceptionThreadChoiceFromSet.class);
      if (cg == null){
        throw new JPFException("wrong CG: " + cg);
      }
        
      String exceptionName = cg.getExceptionForCurrentChoice();
      if (exceptionName != null){
        env.throwException(exceptionName);
      }
    }
  }
}
