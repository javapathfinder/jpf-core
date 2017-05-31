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
 * a MementoRestorer that uses the default mementos
 */
public class DefaultMementoRestorer extends MementoRestorer {

  @Override
  public Memento<KernelState> getMemento(KernelState ks) {
    return ks.getMemento();
  }

  @Override
  public Memento<ThreadList> getMemento(ThreadList tlist) {
    return tlist.getMemento();
  }

  @Override
  public Memento<ThreadInfo> getMemento(ThreadInfo ti) {
    return ti.getMemento();
  }
  
  @Override
  public Memento<Heap> getMemento(Heap heap){
    return heap.getMemento();
  }

  @Override
  public Memento<Statics> getMemento(Statics statics){
    return statics.getMemento();
  }
  
  @Override
  public Memento<ClassLoaderList> getMemento (ClassLoaderList cllist) {
    return cllist.getMemento();
  }

  @Override
  public Memento<ClassLoaderInfo> getMemento (ClassLoaderInfo cl) {
    return cl.getMemento();
  }

  @Override
  public Memento<ClassPath> getMemento (ClassPath cp) {
    return cp.getMemento();
  }
}
