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

/**
 * state storer/restorer that works solely on a snapshot basis
 */
public abstract class MementoRestorer extends AbstractRestorer<Memento<KernelState>> implements MementoFactory {


  @Override
  protected Memento<KernelState> computeRestorableData() {
    return ks.getMemento(this);
  }

  @Override
  protected void doRestore(Memento<KernelState> data) {

    // it's identity preserving, so we don't have to worry about updating external fields
    ks = data.restore(ks);
  }

}
