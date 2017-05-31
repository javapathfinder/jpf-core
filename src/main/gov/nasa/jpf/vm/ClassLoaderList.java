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

import java.util.Iterator;
import java.util.NoSuchElementException;

/**
 * container for all ClassLoaderInfos that are in the current state.
 * It is important to keep this as canonically (search globally) sorted list so that
 * we can use the iterator for state matching
 */
public class ClassLoaderList implements Cloneable, Iterable<ClassLoaderInfo>, Restorable<ClassLoaderList> {

  /** the ordered list of the class loaders */
  ClassLoaderInfo[] classLoaders;

  static class CllMemento implements Memento<ClassLoaderList> {
    Memento<ClassLoaderInfo>[] clMementos;

    CllMemento (ClassLoaderList cll) {
      ClassLoaderInfo[] classLoaders = cll.classLoaders;
      
      int len = classLoaders.length;
      clMementos =  new Memento[len];
    
      for (int i=0; i<len; i++){
        ClassLoaderInfo cl = classLoaders[i];
        Memento<ClassLoaderInfo> m = cl.getMemento();
        clMementos[i] = m;
      }
    }

    @Override
	public ClassLoaderList restore (ClassLoaderList cll){
      int len = clMementos.length;
      ClassLoaderInfo[] classLoaders = new ClassLoaderInfo[len];
      for (int i=0; i<len; i++){
        Memento<ClassLoaderInfo> m = clMementos[i];
        ClassLoaderInfo cl = m.restore(null);
        classLoaders[i] = cl;
      }
      cll.classLoaders = classLoaders;

      return cll;
    }
  }
  
  class CllIterator implements Iterator<ClassLoaderInfo>{
    int next = 0;
    
    @Override
    public boolean hasNext() {
      if (classLoaders != null) {
        return next < classLoaders.length;
      } else {
        return false;
      }
    }

    @Override
    public ClassLoaderInfo next() {
      if (classLoaders != null) {
        if (next < classLoaders.length) {
          return classLoaders[next++];
        }
      }
      
      throw new NoSuchElementException();
    }

    @Override
    public void remove() {
      throw new UnsupportedOperationException();
    }
  }

  public ClassLoaderList() {
  }

  @Override
  public Memento<ClassLoaderList> getMemento (MementoFactory factory) {
    return factory.getMemento(this);
  }

  public Memento<ClassLoaderList> getMemento(){
    return new CllMemento(this);
  }

  @Override
  public Iterator<ClassLoaderInfo> iterator () {
    return new CllIterator();
  }

  public void add (ClassLoaderInfo cli) {
    int id = cli.getId();
    
    if (classLoaders == null) {
      classLoaders = new ClassLoaderInfo[1];
      classLoaders[0] = cli;
      
    } else { // sort it in
      int len = classLoaders.length;
      ClassLoaderInfo[] a = new ClassLoaderInfo[len+1];
      
      for (int i=0; i<len; i++) {
        ClassLoaderInfo c = classLoaders[i];
        if (c.getId() > id) {
          System.arraycopy(classLoaders, i, a, i+1, (len-i));
          a[i] = cli;
          classLoaders = a;
          return;
        } else {
          a[i] = c;
        }
      }
      
      a[len] = cli;
      classLoaders = a;
    }
  }

  public ClassLoaderInfo get(int i) {
    return classLoaders[i];
  }
  
  public ClassLoaderInfo getClassLoaderInfoWithId (int id) {
    int len = classLoaders.length;
    for (int i=0; i<len; i++) {
      ClassLoaderInfo c = classLoaders[i];
      if (c.getId() == id) {
        return c;
      }
    }
    
    return null;
  }

  public int size() {
    return classLoaders.length;
  }
  
  public void markRoots (Heap heap) {
    int len = classLoaders.length;
    for (int i=0; i<len; i++) {
      ClassLoaderInfo cli = classLoaders[i];
      cli.getStatics().markRoots(heap);
    }
  }
}
