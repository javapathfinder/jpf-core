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

import gov.nasa.jpf.util.Predicate;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.SystemState;


/**
 * ChoiceGenerator for Events.
 * This is basically just a pointer into the event tree
 */
public class EventChoiceGenerator extends ChoiceGeneratorBase<Event> {

  protected Event base;
  protected Event cur;
  protected int nProcessed;
  
  protected EventContext ctx; // optional, can replace/expand events during execution
  
  /**
   * convenience method to get successors from current CG chain 
   */
  public static EventChoiceGenerator getNext (SystemState ss, String id, Event base, EventContext ctx){
    EventChoiceGenerator cgPrev = ss.getLastChoiceGeneratorOfType(EventChoiceGenerator.class);
    if (cgPrev == null){
      return new EventChoiceGenerator( id, base, ctx);
    } else {
      return cgPrev.getSuccessor(id, ctx);
    }
  }
  
  public EventChoiceGenerator (String id, Event base){
    this(id, base, null);
  }
  
  public EventChoiceGenerator (String id, Event base, EventContext ctx) {
    super(id);
    this.base = base;
    this.ctx = ctx;
  }
  
  @Override
  public Event getChoice (int idx){
    if (idx >= 0){
      Event e = base;
      for (int i=0; i<idx; i++){
        e = e.alt;
        if (e == null){
          break;
        } else {
          return e;
        }
      }
      
    }
    throw new IllegalArgumentException("choice index out of range: " + idx);
  }

  
  public void setContextExpander (EventContext ctx){
    this.ctx = ctx;
  }
  
  public boolean containsMatchingChoice (Predicate<Event> predicate){
    for (Event e = base; e != null; e = e.getAlt()){
      if (predicate.isTrue(e)){
        return true;
      }
    }
    return false;
  }
  
  public void addChoice (Event newEvent){
    for (Event e = base; e != null;){
      Event eAlt = e.getAlt();
      if (eAlt == null){
        e.setAlt(newEvent);
        return;
      }
      e = eAlt;
    }
  }
  
  public EventChoiceGenerator getSuccessor (String id){
    return getSuccessor(id, null);
  }
  
  public EventChoiceGenerator getSuccessor (String id, EventContext ctx){
    if (cur == null){
      return new EventChoiceGenerator(id, base.getNext(), ctx);
      
    } else {
      Event next = cur.getNext();
      
      if (cur instanceof CheckEvent){ // CheckEvents use next for conjunction
        while (next instanceof CheckEvent){
          next = next.getNext();
        }
      }
      
      if (next != null){
        return new EventChoiceGenerator( id, next, ctx);
      } else {
        return null; // done
      }
    }
  }
  
  @Override
  public Event getNextChoice () {
    return cur;
  }


  @Override
  public boolean hasMoreChoices () {
    if (cur == null){
      return (nProcessed == 0);
    } else {
      return (cur.getAlt() != null);
    }
  }

  @Override
  public void advance () {
    if (cur == null){
      if (nProcessed == 0){
        cur = base;
        nProcessed = 1;
      }
    } else {
      cur = cur.getAlt();
      nProcessed++;
    }
    
    if (ctx != null){
      Event newCur = ctx.map(cur);
      if (newCur != cur){
        cur = newCur;
      }
    }
  }

  @Override
  public void reset () {
    isDone = false;
    cur = null;
    nProcessed = 0;
  }

  @Override
  public int getTotalNumberOfChoices () {
    return base.getNumberOfAlternatives() + 1; // include base itself
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return nProcessed;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("{id:\"");
    sb.append(id);
    sb.append('"');

    //sb.append(",isCascaded:");
    //sb.append(Boolean.toString(isCascaded));

    sb.append(",[");
    for (Event e=base; e!= null; e = e.getAlt()){
      if (e != base){
        sb.append(',');
      }
      if (e == cur){
        sb.append(MARKER);        
      }
      sb.append(e.toString());
    }
    sb.append("],cur:");
    sb.append(cur);
    sb.append("}");
    
    return sb.toString();
  }

  @Override
  public Class<Event> getChoiceType() {
    return Event.class;
  }
  
  protected Event[] getFirstNChoices(int n){
    Event[] a = new Event[n];
    
    Event e = base;
    for (int i=0; i<n; i++){
      a[i] = e;
      e = e.getAlt();
    }
    
    return a;
  }

  @Override
  public Event[] getAllChoices(){
    return getFirstNChoices( getTotalNumberOfChoices());
  }

  @Override
  public Event[] getProcessedChoices(){
    return getFirstNChoices( getProcessedNumberOfChoices());
  }
  
  @Override
  public Event[] getUnprocessedChoices(){
    int n=0;
    for (Event e=cur; e != null; e = e.getAlt()){
      n++;
    }
    
    Event[] a = new Event[n];
    
    Event e = cur;
    for (int i=0; i<n; i++){
      a[i] = e;
      e = e.getAlt();
    }
    
    return a;    
  }
}
