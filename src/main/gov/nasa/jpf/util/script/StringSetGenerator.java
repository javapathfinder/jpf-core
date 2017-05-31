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

package gov.nasa.jpf.util.script;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

class CG {}

class SingleChoice extends CG {
  Event event;

  SingleChoice (Event e) {
    event = e;
  }
  @Override
  public String toString() {
    return event.toString();
  }
}

class SetChoice extends CG {
  ArrayList<Event> choices = new ArrayList<Event>();

  public void add(Event e) {
    choices.add(e);
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder();
    sb.append('{');
    int i=0, n = choices.size();
    for (Event e : choices) {
      sb.append(e);
      if (++i < n) sb.append(',');
    }
    sb.append('}');
    return sb.toString();
  }
}

/**
 * that's mostly a test class to see what a script would be expanded to w/o
 * having any side effects in the ElementProcessor
 */
public class StringSetGenerator implements ElementProcessor {
  LinkedHashMap<String,ArrayList<CG>> sections;
  ArrayList<CG> queue;
  
  StringSetGenerator() {
    sections = new LinkedHashMap<String,ArrayList<CG>>();
    queue = new ArrayList<CG>();
    sections.put("default", queue);
  }
  
  @Override
  public void process (Section sec) {
    queue = new ArrayList<CG>();    
    sec.processChildren(this);
    
    for (String id : sec.getIds()) {
      sections.put(id,queue);      
    }
  }
  
  @Override
  public void process (Event e) {
    for (Event ee : e.expand()) {
      queue.add( new SingleChoice(ee));
    }    
  }

  @Override
  public void process (Alternative a) {
    SetChoice cg = new SetChoice();
    for (ScriptElement e = a.getFirstChild(); e != null; e = e.getNextSibling()) {
      if (e instanceof Event) {
        for (Event ee : ((Event)e).expand()) {
          cg.add(ee);
        }
      }
    }
    queue.add(cg);
  }
  

  @Override
  public void process (Repetition r) {
    int n = r.getRepeatCount();
    for (int i=0; i<n; i++) {
      r.processChildren(this);
    }
  }
  
  public LinkedHashMap<String,ArrayList<CG>> getSections () {
    return sections;
  }

  public List<CG> getCGQueue() {
    return queue;
  }
}