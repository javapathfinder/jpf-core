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

package gov.nasa.jpf.vm.choice;

import gov.nasa.jpf.util.Invocation;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;

import java.io.PrintWriter;
import java.util.List;
import java.util.ListIterator;

/**
 * ChoiceGenerator that represents method calls
 */
public class InvocationCG extends ChoiceGeneratorBase<Invocation> {

  protected List<Invocation> invokes;
  protected Invocation cur;
  protected ListIterator<Invocation> it;
  
  public InvocationCG (String id, List<Invocation> invokes){
    super(id);
    
    this.invokes = invokes;
    
    it = invokes.listIterator();
  }
  
  @Override
  public Invocation getChoice (int idx){
    if (idx >=0 && idx < invokes.size()){
      return invokes.get(idx);
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }
  
  @Override
  public void advance () {
    cur = it.next();
  }

  @Override
  public Class<Invocation> getChoiceType () {
    return Invocation.class;
  }

  @Override
  public Invocation getNextChoice () {
    return cur;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return it.nextIndex();
  }

  @Override
  public int getTotalNumberOfChoices () {
    return invokes.size();
  }

  @Override
  public boolean hasMoreChoices () {
    return it.hasNext();
  }

  @Override
  public ChoiceGenerator<Invocation> randomize () {
    // <2do>
    return this;
  }

  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append(" [");
    int n = invokes.size();
    for (int i=0; i<n; i++) {
      if (i > 0) sb.append(',');
      Invocation inv = invokes.get(i);
      if (inv == cur) {
        sb.append(MARKER);
      }
      sb.append(inv);
    }
    sb.append(']');
    return sb.toString();
  }
  
  public void printOn (PrintWriter pw) {
    pw.print(toString());
  }
  
  @Override
  public void reset () {
    cur = null;
    it = invokes.listIterator();

    isDone = false;
  }

}
