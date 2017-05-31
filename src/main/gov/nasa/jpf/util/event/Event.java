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

package gov.nasa.jpf.util.event;

import gov.nasa.jpf.util.Misc;
import gov.nasa.jpf.util.OATHash;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

/**
 * class that represents an external stimulus to the SUT, which is produced by EventTree instances
 * (our environment models)
 * 
 * Note that albeit concrete EventTree can provide their own, specialized Event types, this class
 * is generic enough that we don't declare it as abstract
 */
public class Event implements Cloneable {

  static final Object[] NO_ARGUMENTS = new Object[0];
  
  //--- linkage
  protected Event next;
  protected Event prev;
  protected Event alt;

  //--- payload
  protected String name;
  protected Object[] arguments;
  
  protected Object source;  // optional, set on demand to keep track of where an event came from

  public Event (String name){
    this( name, NO_ARGUMENTS, null);
  }

  public Event (String name, Object source){
    this( name, NO_ARGUMENTS, source);
  }  
  
  public Event(String name, Object[] arguments) {
    this(name, arguments, null);
  }
  
  public Event(String name, Object[] arguments, Object source) {
    this.name = name;
    this.arguments = arguments != null ? arguments : NO_ARGUMENTS;
    this.source = source;
  }

  
  
  @Override
  public boolean equals (Object o){
    if (o instanceof Event){
      Event other = (Event)o;
      
      if (name.equals(other.name)){
        return Misc.equals(arguments, other.arguments);
      }
    }
    
    return false;
  }
  
  @Override
  public int hashCode(){
    int h = name.hashCode();
    
    for (int i=0; i<arguments.length; i++){
      h = OATHash.hashMixin(h, arguments[i].hashCode());
    }
    
    return OATHash.hashFinalize(h);
  }
  
  protected void setNext (Event e){
    next = e;
    e.prev = this;
  }

  protected void setPrev (Event e){
    prev = e;

    if (alt != null){
      alt.setPrev(e);
    }
  }

  protected void setAlt (Event e){
    alt = e;

    if (prev != null) {
      e.setPrev(prev);
    }
  }
  
  public void setLinksFrom (Event other){
    prev = other.prev;
    next = other.next;
    alt = other.alt;
  }

  public Event replaceWithSequenceFrom (List<Event> list){
    Event eLast = null;
    
    for (Event e: list){
      if (eLast == null){
        e.prev = prev;
        e.alt = alt;
      } else {
        e.prev = eLast;
        eLast.next = e;
      }
      
      eLast = e;
    }
    
    if (eLast != null){
      eLast.next = next;
      return list.get(0);
    } else {
      return null;
    }
  }
  
  public Event replaceWithAlternativesFrom (List<Event> list){
    Event eLast = null;
    for (Event e: list){
      e.prev = prev;
      e.next = next;
      
      if (eLast != null){
        eLast.alt = e;
      }
      
      eLast = e;
    }
    
    if (eLast != null){
      eLast.alt = alt;
      return list.get(0);
    } else {
      return null;
    }
  }

  public Event replaceWith (Event e){
    e.prev = prev;
    e.next = next;
    e.alt = alt;
    
    return e;
  }
  
  protected void setSource (Object source){
    this.source = source;
  }
  
  public int getNumberOfAlternatives(){
    int n = 0;
    for (Event e = alt; e != null; e = e.alt) {
      n++;
    }

    return n;
  }

  public boolean hasAlternatives(){
    return (alt != null);
  }
  
  public List<Event> getAlternatives(){
    List<Event> list = new ArrayList<Event>();
    list.add(this);
    for (Event e = alt; e != null; e = e.alt) {
      list.add(e);
    }
    return list;
  }
  
  
  public Event unlinkedClone(){
    try {
      Event e = (Event)super.clone();
      e.next = e.prev = e.alt = null;
      return e;

    } catch (CloneNotSupportedException x) {
      throw new RuntimeException("event clone failed", x);
    }
    
  }

  public Event clone(){
    try {
      return (Event) super.clone();
    } catch (CloneNotSupportedException cnsx){
      throw new RuntimeException("Event clone failed");
    }
  }

  public Event deepClone(){
    try {
      Event e = (Event)super.clone();

      if (next != null) {
        e.next = next.deepClone();
        e.next.prev = e;

        if (next.alt != null){
          e.next.alt.prev = e;
        }
      }

      if (alt != null) {
        e.alt = alt.deepClone();
      }

      return e;

    } catch (CloneNotSupportedException x) {
      throw new RuntimeException("event clone failed", x);
    }
  }

  public String getName(){
    return name;
  }

  public Object[] getArguments(){
    return arguments;
  }

  public Object getArgument(int idx){
    if (idx < arguments.length){
      return arguments[idx];
    }
    return null;
  }
  
  public Event getNext(){
    return next;
  }
  
  public Event getAlt(){
    return alt;
  }
  
  public Event getPrev(){
    return prev;
  }
  
  public Object getSource(){
    return source;
  }
  
  public Event addNext (Event e){
    boolean first = true;
    for (Event ee : endEvents()){  // this includes alternatives
      if (!first){
        e = e.deepClone();
      } else {
        first = false;      // first one doesn't need a clone
      }
      ee.setNext(e);
      e.setPrev(ee);
    }

    return this;
  }

  public Event addAlternative (Event e){
    Event ea ;
    for (ea = this; ea.alt != null; ea = ea.alt);
    ea.setAlt(e);

    if (next != null){
      e.setNext( next.deepClone());
    }

    return this;
  }
  
  protected static Event createClonedSequence (int firstIdx, int len, Event[] events){
    Event base = events[firstIdx].unlinkedClone();
    Event e = base;

    for (int i = firstIdx+1; i < len; i++) {
      Event ne = events[i].unlinkedClone();
      e.setNext( ne);
      e = ne;
    }
    
    return base;
  }
  
  /**
   * extend this tree with a new path 
   */
  public void addPath (int pathLength, Event... path){
    Event t = this;
    Event pe;
    
    outer:
    for (int i=0; i<pathLength; i++){
      pe = path[i];
      for (Event te = t; te != null; te = te.alt){
        if (pe.equals(te)){      // prefix is in tree
          
          if (te.next == null){  // reached tree leaf
            if (++i < pathLength){ // but the path still has events
              Event tail = createClonedSequence( i, pathLength, path);
              te.setNext(tail);
              tail.setAlt( new NoEvent()); // preserve the tree path
            }
            return;
            
          } else { // there is a next in the tree
            t = te.next;
            
            if (i == pathLength-1){ // but the path is done, add a NoEvent as a next alternative to mark the end
              Event e = t.getLastAlt();
              e.setAlt(new NoEvent());
              return;
              
            } else {
              continue outer;
            }
          }
        }
      }
      
      //--- path prefix was not in tree, append as (last) alternative
      Event tail = createClonedSequence( i, pathLength, path);
      Event e = t.getLastAlt();
      e.setAlt( tail);
      
      return;
    }
  }

  public Event getLastAlt (){
    Event e;
    for (e=this; e.alt != null; e = e.alt);
    return e;
  }
  
  protected void collectEndEvents (List<Event> list, boolean includeNoEvents) {
    if (next != null) {
      next.collectEndEvents(list, includeNoEvents);
      
    } else { // base case: no next
      // strip trailing NoEvents 
      if (prev == null){
        list.add(this); // root NoEvents have to stay
        
      } else { // else we skip trailing NoEvents up to root
        Event ee = this;
        if (!includeNoEvents){
          for (Event e=this; e.prev != null && (e instanceof NoEvent); e = e.prev){
            ee = e.prev;
          }
        }
        list.add(ee);
      }
    }

    if (alt != null) {
      alt.collectEndEvents(list, includeNoEvents);
    }
  }

  public Event endEvent() {
    if (next != null) {
      return next.endEvent();
    } else {
      return this;
    }
  }

  public List<Event> visibleEndEvents(){
    List<Event> list = new ArrayList<Event>();
    collectEndEvents(list, false);
    return list;
  }
 
  
  public List<Event> endEvents(){
    List<Event> list = new ArrayList<Event>();
    collectEndEvents(list, true);
    return list;
  }
  
 
  private void interleave (Event a, Event b, Event[] path, int pathLength, int i, Event result){
    if (a == null && b == null){ // base case
      result.addPath(pathLength, path);
      
    } else {
      if (a != null) {
        path[i] = a;
        interleave(a.prev, b, path, pathLength, i - 1, result);
      }

      if (b != null) {
        path[i] = b;
        interleave(a, b.prev, path, pathLength, i - 1, result);
      }
    }
  }
  
  /**
   * this creates a new tree that contains all paths resulting from
   * all interleavings of all paths of this tree with the specified other events
   * 
   * BEWARE: this is a combinatorial bomb that should only be used if we know all
   * paths are short
   */
  public Event interleave (Event... otherEvents){
    Event t = new NoEvent(); // we need a root for the new tree
    
    Event[] pathBuffer = new Event[32];
    int mergedTrees = 0;
    
    for (Event o : otherEvents){
      List<Event> endEvents = (mergedTrees++ > 0) ? t.visibleEndEvents() : visibleEndEvents();

      for (Event ee1 : endEvents) {
        for (Event ee2 : o.visibleEndEvents()) {
          int n = ee1.getPathLength() + ee2.getPathLength();
          if (n > pathBuffer.length){
            pathBuffer = new Event[n];
          }

          interleave(ee1, ee2, pathBuffer, n, n - 1, t);
        }
      }
    }
        
    return t.alt;
  }
  
  
  
  private void removeSource (Object src, Event[] path, int i, Event result){
    
    if (alt != null){
      alt.removeSource(src, path, i, result);
    }
    
    if (source != src){
      path[i++] = this;
    }
    
    if (next != null){
      next.removeSource(src, path, i, result);
      
    } else { // done, add path to result
      result.addPath( i, path);
    }
  }
  
  /**
   * remove all events from this tree that are from the specified source 
   */
  public Event removeSource (Object src){
    Event base = new NoEvent(); // we need a root to add to
    int maxDepth = getMaxDepth();
    Event[] pathBuffer = new Event[maxDepth];
    
    removeSource( src, pathBuffer, 0, base);
    
    return base.alt;
  }
  
  private void printPath (PrintStream ps, boolean isLast){
    if (prev != null){
      prev.printPath(ps, false);
    }
    
    if (!isNoEvent()){
      ps.print(name);
      if (!isLast){
        ps.print(',');
      }
    }
  }
  
  public void printPath (PrintStream ps){
    printPath(ps, true);
  }

  @Override
  public String toString(){
    StringBuilder sb = new StringBuilder();
    sb.append(name);
    if (arguments != NO_ARGUMENTS) {
      sb.append('(');
      boolean first = true;
      for (Object a : arguments) {
        if (first){
          first = false;
        } else {
          sb.append(',');
        }
        sb.append(a.toString());
      }
      sb.append(')');
    }
    return sb.toString();
  }

  
  /**
   * upwards path length 
   */
  public int getPathLength(){
    int n=0;
    
    for (Event e=this; e != null; e = e.prev){
      n++;
    }
    
    return n;
  }
  
  
  private int getMaxDepth (int depth){
    int maxAlt = depth;
    int maxNext = depth;
    
    if (alt != null){
      maxAlt = alt.getMaxDepth(depth);
    }
    
    if (next != null){
      maxNext = next.getMaxDepth(depth + 1);
    }
    
    if (maxAlt > maxNext){
      return maxAlt;
    } else {
      return maxNext;
    }
  }
  
  /**
   * maximum downwards tree depth 
   */
  public int getMaxDepth(){
    return getMaxDepth(1);
  }
  
  public Event[] getPath(){
    int n = getPathLength();
    Event[] trace = new Event[n];
    
    for (Event e=this; e != null; e = e.prev){
      trace[--n] = e;
    }
    
    return trace;
  }
  
  public void printTree (PrintStream ps, int level) {
    for (int i = 0; i < level; i++) {
      ps.print(". ");
    }
    
    ps.print(this);
    //ps.print(" [" + prev + ']');
    ps.println();

    if (next != null) {
      next.printTree(ps, level + 1);
    }

    if (alt != null) {
      alt.printTree(ps, level);
    }
  }
  
  public boolean isEndOfTrace (String[] eventNames){
    int n = eventNames.length-1;
    
    for (Event e=this; e!= null; e = e.prev){
      if (e.getName().equals(eventNames[n])){
        n--;
      } else {
        return false;
      }
    }
    
    return (n == 0);
  }
  
  protected void collectTrace (StringBuilder sb, String separator, boolean isLast){
    if (prev != null){
      prev.collectTrace(sb, separator, false);    
    }

    if (!isNoEvent()){
      sb.append(toString());
      
      if (!isLast && separator != null){
        sb.append(separator);        
      }
    }
  }
  
  public String getPathString (String separator){
    StringBuilder sb = new StringBuilder();
    collectTrace( sb, separator, true);
    return sb.toString();
  }
  
  public boolean isNoEvent(){
    return false;
  }

  public boolean isSystemEvent(){
    return false;
  }

  //--- generic processing interface
  
  public void process(Object source){
    // can be overridden by subclass if instance has sufficient context info
  }
  
  public void setProcessed(){
    // can be overridden by subclass, e.g. to maintain event caches
  }
}
