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

package gov.nasa.jpf.util;

import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

/**
 * a generic, listener- created trace over property specific operations
 * 
 * we could register this as a listener itself, but since it usually is used from
 * a listener, we might as well just delegate from there
 */
public class Trace<T> extends ListenerAdapter implements Iterable<T> {

  TraceElement<T> lastElement;
  TraceElement<T> lastTransition;

  // for HeuristicSearches. Ok, that's braindead but at least no need for cloning
  HashMap<Integer,TraceElement<T>> storedTransition;


  // iterator that traverses the trace LIFO, i.e. starting from the last T
  class TraceIterator implements Iterator<T> {

    TraceElement<T> cur;
    
    TraceIterator () {
      cur = lastElement;
    }
    
    @Override
	public boolean hasNext () {
      return (cur != null);
    }

    @Override
	public T next () {
      if (cur != null){
        T op = cur.op;
        cur = cur.prevElement;
        return op;
      } else {
        return null;
      }
    }

    @Override
	public void remove () {
      throw new UnsupportedOperationException("TraceElement removal not supported");
    } 
  }
  
  @Override
  public Iterator<T> iterator() {
    return new TraceIterator();
  }
  
  public void addOp (T o){
    TraceElement<T> op = new TraceElement<T>(o);
    
    if (lastElement == null){
      lastElement = op;
    } else {
      assert lastElement.stateId == 0;
      
      op.prevElement = lastElement;
      lastElement = op;
    }
  }

  public void removeLastOp() {
    if (lastElement != null){
      lastElement = lastElement.prevElement;
    }
  }

  public T getLastOp() {
    if (lastElement != null) {
      return lastElement.getOp();
    }
    
    return null;
  }

  public int size() {
    int n=0;
    for (TraceElement<T> te = lastElement; te != null; te = te.prevElement) {
      n++;
    }
    
    return n;
  }
  
  public List<T> getOps () {
    // this is a rather braindead way around the limitation that we can't explicitly
    // create an T[] array object
    
    ArrayList<T> list = new ArrayList<T>();
    
    for (TraceElement<T> te = lastElement; te != null; te = te.prevElement) {
      list.add(te.getOp());
    }
    
    // reverse
    for (int i=0, j=list.size()-1; i<j; i++, j--) {
      T tmp = list.get(j);
      list.set(j, list.get(i));
      list.set(i, tmp);
    }
    
    return list;
  }

  @Override
  public void stateAdvanced (Search search) {
    if (search.isNewState() && (lastElement != null)) {
      int stateId = search.getStateId();
      
      for (TraceElement<T> op=lastElement; op != null; op=op.prevElement) {
        assert op.stateId == 0;
        
        op.stateId = stateId;
      }
      
      lastElement.prevTransition = lastTransition;
      lastTransition = lastElement;
    }
    
    lastElement = null;
  }

  @Override
  public void stateBacktracked (Search search){
    int stateId = search.getStateId();
    while ((lastTransition != null) && (lastTransition.stateId > stateId)){
      lastTransition = lastTransition.prevTransition;
    }
    lastElement = null;
  }

  @Override
  public void stateStored (Search search) {
    if (storedTransition == null){
      storedTransition = new HashMap<Integer,TraceElement<T>>();
    }
    
    // always called after stateAdvanced
    storedTransition.put(search.getStateId(), lastTransition);
  }

  @Override
  public void stateRestored (Search search) {
    int stateId = search.getStateId();
    TraceElement<T> op = storedTransition.get(stateId);
    if (op != null) {
      lastTransition = op;
      storedTransition.remove(stateId);  // not strictly required, but we don't come back
    }
  }

  @Override
  public Trace clone() {
    TraceElement<T> e0 = null, eLast = null;
    
    for (TraceElement<T> e = lastElement; e != null; e = e.prevElement){
      TraceElement<T> ec = e.clone();

      if (eLast != null){
        eLast.prevElement = ec;
        eLast = ec;
      } else {
        e0 = eLast = ec;
      }
    }
    
    Trace<T> t = new Trace<T>();
    t.lastElement = e0;
    
    return t;
  }
}
