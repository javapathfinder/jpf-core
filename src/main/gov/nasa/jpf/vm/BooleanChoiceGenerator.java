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

/**
 * a pretty simple ChoiceGenerator that returns a boolean
 * there is not much use in having a CG type interface (such as
 * IntChoiceGenerator) since there is hardly a need for a generic type hierarchy
 * of BooleanChoiceGenerator subtypes - what else can you do with true/false
 */
public class BooleanChoiceGenerator extends ChoiceGeneratorBase<Boolean> {

  // do we evaluate [false, true] or [true, false]
  protected boolean falseFirst = true;

  protected int count = -1;
  protected boolean next;
  
  public BooleanChoiceGenerator(Config conf, String id) {
    super(id);

    falseFirst = conf.getBoolean("cg.boolean.false_first", true);
    next = falseFirst;
  }

  public BooleanChoiceGenerator (String id) {
    super(id);
    next = falseFirst;
  }

  public BooleanChoiceGenerator( String id, boolean falseFirst ){
    super(id);
    
    this.falseFirst = falseFirst;
    next = falseFirst;
  }

  @Override
  public boolean hasMoreChoices () {
    return !isDone && (count < 1);
  }

  @Override
  public Boolean getNextChoice () {
    return next ? Boolean.TRUE : Boolean.FALSE;
  }
  
  @Override
  public Class<Boolean> getChoiceType() {
    return Boolean.class;
  }

  @Override
  public void advance () {
    if (count < 1) {
      count++;
      next = !next;
    }
  }
  
  @Override
  public Boolean getChoice (int idx){
    if (idx == 0){
      return falseFirst ? Boolean.FALSE : Boolean.TRUE;
    } else if (idx == 1){
      return falseFirst ? Boolean.TRUE : Boolean.FALSE;      
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }

  @Override
  public void reset () {
    count = -1;
    next = falseFirst;

    isDone = false;
  }
  
  @Override
  public int getTotalNumberOfChoices () {
    return 2;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return (count+1);
  }
  
  // that is pretty stupid, but for the sake of consistency we make it available
  Boolean[] getChoices(){
    Boolean[] vals = new Boolean[2];
    vals[0] = !falseFirst;
    vals[1] = falseFirst;
    
    return vals;
  }

  // not much use to support reordering, we just have two elements so reverse() will do
  
  public boolean isFalseFirst(){
    return falseFirst;
  }
  
  /**
   *  note this should only be called before the first advance since it resets
   *  the enumeration state 
   */
  public void reverse(){
    falseFirst = !falseFirst;
    reset();
  }
  
  @Override
  public String toString () {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append('[');
    sb.append("[id=\"");
    sb.append(id);
    sb.append('"');

    sb.append(",isCascaded:");
    sb.append(isCascaded);

    sb.append(",{");

    if (count < 0){
      sb.append(!next);
      sb.append(',');
      sb.append(next);
    } else if (count == 0) {
      sb.append(MARKER);
      sb.append(next);
      sb.append(',');
      sb.append(!next);
    } else {
      sb.append(!next);
      sb.append(',');
      sb.append(MARKER);
      sb.append(next);
    }
    sb.append("}]");
    return sb.toString();
  }
  
  @Override
  public BooleanChoiceGenerator randomize () {
    next = random.nextBoolean();
    return this;
  }
}
