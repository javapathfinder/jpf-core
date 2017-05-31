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

import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;

/**
 * an abstract choice generator that is just a list of choice generators
 */
public abstract class CompoundChoiceGenerator<T> extends ChoiceGeneratorBase<T> {

  //--- helper to implement ad hoc linked lists
  protected class Entry {
    ChoiceGenerator<T> cg;
    Entry next;
    
    Entry (ChoiceGenerator<T> cg, Entry next){
      this.cg = cg;
      this.next = next;
    }
  }
  
  protected Entry base;
  protected Entry cur;

  protected CompoundChoiceGenerator (String id){
    super(id);
  }
  
  //--- to be called from derived ctors
  
  protected void setBase (ChoiceGenerator<T> cg){
    base = cur = new Entry( cg, null);
  }
  
  protected void add (ChoiceGenerator<T> cg){
    base = cur = new Entry( cg, cur);
  }
  
  //--- the public ChoiceGenerator interface
  
  @Override
  public T getNextChoice () {
    if (cur != null){
      return cur.cg.getNextChoice();
    } else {
      return null;
    }
  }

  @Override
  public boolean hasMoreChoices () {
    if (cur != null){
      if (cur.cg.hasMoreChoices()){
        return true;
      } else {
        for (Entry e = cur.next; e != null; e = e.next){
          if (e.cg.hasMoreChoices()){
            return true;
          }
        }
        
        return false;
      }
      
    } else {
      return false;
    }
  }

  @Override
  public void advance () {
    if (cur != null){
      if (cur.cg.hasMoreChoices()){
        cur.cg.advance();
      } else {
        cur = cur.next;
        advance();
      }
    }
  }

  @Override
  public void reset () {
    cur = base;
    
    for (Entry e = base; e != null; e = e.next){
      e.cg.reset();
    }
  }

  @Override
  public int getTotalNumberOfChoices () {
    int n = 0;
    
    for (Entry e = base; e != null; e = e.next){
      n += e.cg.getTotalNumberOfChoices();
    }
    
    return n;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    int n=0;
    
    for (Entry e = base; e != null; e = e.next){
      n += e.cg.getProcessedNumberOfChoices();
      if (e == cur){
        break;
      }
    }
    
    return n;
  }

}
