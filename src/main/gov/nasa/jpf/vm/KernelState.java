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

import gov.nasa.jpf.Config;
import java.util.Iterator;
import java.util.Stack;


/**
 * This class represents the SUT program state (statics, heap and threads)
 */
public class KernelState implements Restorable<KernelState> {

  /** The area containing the heap */
  public Heap heap;

  /** The list of the threads */
  public ThreadList threads;

  /** the list of the class loaders */
  public ClassLoaderList classLoaders;

  /**
   * current listeners waiting for notification of next change.
   */
  private Stack<ChangeListener> listeners = new Stack<ChangeListener>();


  static class KsMemento implements Memento<KernelState> {
    // note - order does matter: threads need to be restored before the heap
    Memento<ThreadList> threadsMemento;
    Memento<ClassLoaderList> cloadersMemento;
    Memento<Heap> heapMemento;

    KsMemento (KernelState ks){
      threadsMemento = ks.threads.getMemento();
      cloadersMemento = ks.classLoaders.getMemento();
      heapMemento = ks.heap.getMemento();
    }

    @Override
	public KernelState restore (KernelState ks) {
      // those are all in-situ objects, no need to set them in ks
      threadsMemento.restore(ks.threads);
      cloadersMemento.restore(ks.classLoaders);
      heapMemento.restore(ks.heap);

      return ks;
    }
  }

  /**
   * Creates a new kernel state object.
   */
  public KernelState (Config config) {
    Class<?>[] argTypes = { Config.class, KernelState.class };
    Object[] args = { config, this };

    classLoaders = new ClassLoaderList();  
    heap = config.getEssentialInstance("vm.heap.class", Heap.class, argTypes, args);
    threads = config.getEssentialInstance("vm.threadlist.class", ThreadList.class, argTypes, args);
  }

  @Override
  public Memento<KernelState> getMemento(MementoFactory factory) {
    return factory.getMemento(this);
  }

  public Memento<KernelState> getMemento(){
    return new KsMemento(this);
  }

  /**
   * Adds the given loader to the list of existing class loaders. 
   */
  public void addClassLoader(ClassLoaderInfo cl) {
    classLoaders.add(cl);
  }

  /**
   * Returns the ClassLoader with the given globalId
   */
  protected ClassLoaderInfo getClassLoader(int gid) {
    Iterator<ClassLoaderInfo> it = classLoaders.iterator();

    while(it.hasNext()) {
      ClassLoaderInfo cl = it.next();
      if(cl.getId() == gid) {
        return cl;
      }
    }

    return null;
  }

  public Heap getHeap() {
    return heap;
  }

  public ThreadList getThreadList() {
    return threads;
  }

  public ClassLoaderList getClassLoaderList() {
    return classLoaders;
  }
  
  /**
   * interface for getting notified of changes to KernelState and everything
   * "below" it.
   */
  public interface ChangeListener {
    void kernelStateChanged(KernelState ks);
  }

  /**
   * called by internals to indicate a change in KernelState.  list of listeners
   * is emptied.
   */
  public void changed() {
    while (!listeners.empty()) {
      listeners.pop().kernelStateChanged(this);
    }
  }

  /**
   * push a listener for notification of the next change.  further notification
   * requires re-pushing.
   */
  public void pushChangeListener(ChangeListener cl) {
    if (cl instanceof IncrementalChangeTracker && listeners.size() > 0) {
      for (ChangeListener l : listeners) {
        if (l instanceof IncrementalChangeTracker) {
          throw new IllegalStateException("Only one IncrementalChangeTracker allowed!");
        }
      }
    }
    listeners.push(cl);
  }

  public int getThreadCount () {
    return threads.length();
  }

  public void gc () {
        
    heap.gc();

    // we might have stored stale references in live objects
    // (ElementInfos on the heap have already been cleaned up in the gc)
    cleanUpDanglingStaticReferences();
  }

  
  
  private void cleanUpDanglingStaticReferences() {
    for(ClassLoaderInfo cl: classLoaders) {
      Statics sa = cl.getStatics();
      sa.cleanUpDanglingReferences(heap);
    }
  }
}
