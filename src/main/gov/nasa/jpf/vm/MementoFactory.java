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
 * interface to create Memento objects for state storage/restore
 *
 * this follows some sort of a visitor pattern
 */
public interface MementoFactory {

  Memento<KernelState> getMemento(KernelState ks);


  Memento<ThreadList> getMemento(ThreadList tlist);

  Memento<ThreadInfo> getMemento(ThreadInfo ti);

  Memento<ClassLoaderList> getMemento(ClassLoaderList cllist);

  Memento<ClassLoaderInfo> getMemento(ClassLoaderInfo cl);

  Memento<ClassPath> getMemento (ClassPath cp);

  Memento<Heap> getMemento(Heap heap);

  Memento<Statics> getMemento(Statics sa);

}
