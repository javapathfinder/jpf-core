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

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.Instruction;
import gov.nasa.jpf.vm.NativeStateHolder;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.Verify;
import org.junit.Test;

/**
 * regression test for NativeStateHolder API
 */
public class NativeStateHolderTest extends TestJPF {

  public static class NSHListener extends ListenerAdapter implements NativeStateHolder {
    
    // note that we don't restore nativeState, i.e. once it has reached the threshold
    // the search should terminate
    
    int nativeState;
    int n;
        
    @Override
    public void vmInitialized (VM vm){
      vm.getSerializer().addNativeStateHolder(this);
      System.out.println("native state holder registered");
    }
    
    @Override
    public void choiceGeneratorRegistered (VM vm, ChoiceGenerator<?> cg, ThreadInfo ti, Instruction insn){
      n++;
      System.out.print("transition: " + n);
      
      if (nativeState < 5){
        nativeState++;
        System.out.println(", nativeState modified: " + nativeState);
        
      } else {
        System.out.println(", no nativeState change");
        if (n > 10){
          fail("state leak despite constant nativesState");
        }
      }
    }
    
    @Override
    public void stateAdvanced (Search search){
      System.out.println("cg advanced, isNewState: " + search.isNewState());
    }

    @Override
    public void stateBacktracked (Search search){
      System.out.println("state backtracked");
    }

    
    @Override
    public int getHash() {
      return nativeState;
    }
  }
  

  @Test
  public void testNativeStateHolder(){    
    if (verifyNoPropertyViolation("+listener=" + getClass().getName() + "$NSHListener")){
      while (true){
        Verify.breakTransition("cycle");
      }
    }
  }
  
  
}
