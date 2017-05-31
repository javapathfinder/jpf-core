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

import gov.nasa.jpf.util.Misc;
import java.util.List;



public abstract class AbstractSerializer
                 implements StateSerializer, KernelState.ChangeListener {
  // INVARIANT: non-null iff registered for changes to KernelState
  protected int[] cached = null;

  protected VM vm;
  protected KernelState ks = null;

  // optional list of native state holders that contribute to storingData computation
  protected NativeStateHolder[] nativeStateHolders = new NativeStateHolder[0];

  @Override
  public void attach(VM vm) {
    this.vm = vm;
    this.ks = vm.getKernelState();
  }

  public int getCurrentStateVectorLength() {
    return cached.length;
  }

  @Override
  public int[] getStoringData() {
    if (cached == null) {
      cached = computeStoringData();
      ks.pushChangeListener(this);
    }
    return cached;
  }

  @Override
  public void kernelStateChanged (KernelState same) {
    cached = null;
  }

  
  @Override
  public void addNativeStateHolder (NativeStateHolder nsh){
    nativeStateHolders = Misc.appendUniqueElement(nativeStateHolders, nsh);
  }
  
  protected abstract int[] computeStoringData();
}
