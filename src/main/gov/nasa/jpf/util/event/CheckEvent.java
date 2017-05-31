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

import gov.nasa.jpf.vm.MJIEnv;

/**
 * a pseudo event that encapsulates a (possibly composed) check
 * 
 * This event type uses 'alt' for disjunction and 'next' for conjunction if
 * they point to CheckEvents
 */
public abstract class CheckEvent extends SystemEvent {
  
  protected CheckEvent (String name, Object... arguments){
    super(name, arguments);
  }

  /**
   * this is the single check condition for this event
   */
  public abstract boolean evaluate(MJIEnv env, int objRef);

  /**
   * conjunctions and disjunctions of this check event
   */
  public boolean check (MJIEnv env, int objRef){
    if (!evaluate(env, objRef)){
      if (alt != null && alt instanceof CheckEvent){
        return ((CheckEvent)alt).check(env, objRef);
      } else {
        return false;
      }
      
    } else {
      if (next != null && next instanceof CheckEvent){
        return ((CheckEvent)next).check(env, objRef);
      } else {
        return true;
      }
    }
  }

  /**
   * generic check evaluation that throws assertions if failed
   */
  @Override
  public void process (MJIEnv env, int objRef){
    if (!check(env, objRef)){
      env.throwAssertion("check event failed: " + this);
    }
  }

  public CheckEvent or (CheckEvent orCheck){
    addAlternative(orCheck);
    
    return this;
  }
  
  public CheckEvent and (CheckEvent andCheck){
    addNext( andCheck);
    
    return this;
  }
  
  public String getExpression(){
    if (alt == null && !(next instanceof CheckEvent)){
      return toString();
      
    } else {
      StringBuilder sb = new StringBuilder();
      
      sb.append('(');
      sb.append(name);
      
      for (Event e = alt; e != null; e = e.alt){
        sb.append( " || ");
        sb.append(e.name);
      }
      
      for (Event e = next; e instanceof CheckEvent; e = e.next){
        sb.append( " && ");
        sb.append(e.name);        
      }
      
      sb.append(')');
      
      return sb.toString();
    }
  }
}
