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
package gov.nasa.jpf.vm;

import gov.nasa.jpf.util.ImmutableList;

public class DefaultBacktracker<KState> implements Backtracker {
  /** where we keep the saved KernelState head */ 
  protected ImmutableList<KState> kstack;
   
  /** and that adds the SystemState specifics */
  protected ImmutableList<Object> sstack;
  
  protected SystemState ss;
  protected StateRestorer<KState> restorer;
  
  @Override
  public void attach(VM vm) {
    ss = vm.getSystemState();
    restorer = vm.getRestorer();
  }

  //--- the backtrack support (depth first only)
  
  protected void backtrackKernelState() {
    KState data = kstack.head;
    kstack = kstack.tail;
    
    restorer.restore(data);
  }

  protected void backtrackSystemState() {
    Object o = sstack.head;
    sstack = sstack.tail;
    ss.backtrackTo(o);
  }

  
  /**
   * Moves one step backward. This method and forward() are the main methods
   * used by the search object.
   * Note this is called with the state that caused the backtrack still being on
   * the stack, so we have to remove that one first (i.e. popping two states
   * and restoring the second one)
   */
  @Override
  public boolean backtrack () {
    if (sstack != null) {
  
      backtrackKernelState();
      backtrackSystemState();

      return true;
    } else {
      // we are back to the top of where we can backtrack to
      return false;
    }
  }
  
  /**
   * Saves the state of the system.
   */
  @Override
  public void pushKernelState () {
    kstack = new ImmutableList<KState>(restorer.getRestorableData(),kstack);
  }
  
  /**
   * Saves the backtracking information.
   */
  @Override
  public void pushSystemState () {
    sstack = new ImmutableList<Object>(ss.getBacktrackData(),sstack);
  }

  
  //--- the restore support
  
  // <2do> this saves both the backtrack and the restore data - too redundant
  class RestorableStateImpl implements RestorableState {
    final ImmutableList<KState> savedKstack;
    final ImmutableList<Object> savedSstack;
    
    final KState kcur;
    final Object scur;
    
    RestorableStateImpl() {
      savedKstack = kstack;
      savedSstack = sstack;
      kcur = restorer.getRestorableData();
      scur = ss.getRestoreData();
    }
    
    void restore() {
      kstack = savedKstack;
      sstack = savedSstack;
      restorer.restore(kcur);
      ss.restoreTo(scur);
    }
  }
  
  @Override
  public void restoreState (RestorableState state) {
    ((RestorableStateImpl) state).restore();
  }
  
  @Override
  public RestorableState getRestorableState() {
    return new RestorableStateImpl();
  }
}
