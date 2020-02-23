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

package gov.nasa.jpf.test.vm.basic;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.vm.VM;
import gov.nasa.jpf.vm.ThreadInfo;
import gov.nasa.jpf.vm.ThreadList;

/**
 * listener that checks SUT and JPF consistency in program end states
 */
public class EndStateListener extends ListenerAdapter {

  @Override
  public void stateAdvanced (Search search){
    if (search.isEndState()){

      VM vm = search.getVM();
      ThreadList tl = vm.getThreadList();

      for (ThreadInfo ti : tl){
        System.out.println("EndStateListener checking thread: " + ti.getStateDescription());

        // check if there are no alive threads anymore
        assert ti.isTerminated();

        // check if none of the threads still holds a lock
        assert !ti.hasLockedObjects();
      }
    }
  }
}
