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


public abstract class AbstractRestorer<Saved> implements StateRestorer<Saved>, KernelState.ChangeListener {

  protected Saved cached = null;

  VM vm;
  protected KernelState ks = null;

  
  @Override
  public void attach(VM vm) {
    this.vm = vm;
    this.ks = vm.getKernelState();
  }
  
  @Override
  public Saved getRestorableData() {
    if (cached == null) {
      cached = computeRestorableData();
      ks.pushChangeListener(this);
    }
    return cached;
  }
  
  @Override
  public void restore (Saved data) {
    doRestore(data);
    if (cached == null) {
      ks.pushChangeListener(this);
    } else {
      // invariant says we're already waiting for changes
    }
    cached = data;
  }

  @Override
  public void kernelStateChanged (KernelState same) {
    cached = null;
  }
  
  protected abstract Saved computeRestorableData();
  protected abstract void doRestore(Saved data);
}
