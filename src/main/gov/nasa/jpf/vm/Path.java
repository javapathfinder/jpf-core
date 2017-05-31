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

import gov.nasa.jpf.util.Printable;

import java.io.PrintWriter;
import java.util.Iterator;
import java.util.LinkedList;


/**
 * Path represents the data structure in which a execution trace is recorded.
 */
public class Path implements Printable, Iterable<Transition>, Cloneable {
  String             application;  
  private LinkedList<Transition> stack;
  
  private Path() {} // for cloning
  
  public Path (String app) {
    application = app;
    stack = new LinkedList<Transition>();
  }
  
  @Override
  public Path clone() {
    Path clone = new Path();
    clone.application = application;
    
    // we need to deep copy the stack to preserve CG and ThreadInfo state
    LinkedList<Transition> clonedStack = new LinkedList<Transition>();
    for (Transition t : stack){
      clonedStack.add( (Transition)t.clone());
    }
    clone.stack = clonedStack;
    
    return clone;
  }
  
  public String getApplication () {
    return application;
  }

  public Transition getLast () {
    if (stack.isEmpty()) {
      return null;
    } else {
      return stack.getLast();
    }
  }

  public void add (Transition t) {
    stack.add(t);
  }

  public Transition get (int pos) {
    return stack.get(pos);
  }

  public boolean isEmpty() {
    return (stack.size() == 0);
  }
  
  public int size () {
    return stack.size();
  }

  public boolean hasOutput () {
    for (Transition t : stack) {
      if (t.getOutput() != null) {
        return true;
      }
    }
    
    return false;
  }
  
  public void printOutputOn (PrintWriter pw) {
    for (Transition t : stack) {
      String output = t.getOutput();
      if (t != null) {
        pw.print(output);
      }
    }
  }
  
  @Override
  public void printOn (PrintWriter pw) {
/**** <2do> this is going away
    int    length = size;
    Transition entry;

    for (int index = 0; index < length; index++) {
      pw.print("Transition #");
      pw.print(index);
      
      if ((entry = get(index)) != null) {
        pw.print(' ');

        entry.printOn(pw);
      }
    }
***/
  }

  public void removeLast () {
    stack.removeLast();
  }
  
  @Override
  public Iterator<Transition> iterator () {
    return stack.iterator();
  }
  
  public Iterator<Transition> descendingIterator() {
    return stack.descendingIterator();
  }
}
