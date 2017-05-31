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

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.ThreadInfo;

import java.util.Arrays;
import java.util.Comparator;

/** 
 * common root for list based number choice generators
 */
public abstract class NumberChoiceFromList<T extends Number> extends ChoiceGeneratorBase<T> {

  // int values to choose from stored as Strings or Integers
  protected T[] values;
  protected int count = -1;
  
  /**
   *  super constructor for subclasses that want to configure themselves
   * @param id name used in choice config
   */
  protected NumberChoiceFromList(String id){
    super(id);
  }

  protected NumberChoiceFromList (String id, T[] vals){
    super(id);
    values = vals;
    count = -1;
  }
  
  protected abstract T[] createValueArray(int len);
  protected abstract T getDefaultValue();
  protected abstract T parseLiteral (String literal, int sign);
  protected abstract T newValue (Number num, int sign);
  
  /**
   * @param conf JPF configuration object
   * @param id name used in choice config
   */
  public NumberChoiceFromList(Config conf, String id) {
    super(id);

    String[] vals = conf.getCompactStringArray(id + ".values");
    if (vals == null || vals.length == 0) {
      throw new JPFException("no value specs for IntChoiceFromList " + id);
    }

    // get the choice values here because otherwise successive getNextChoice()
    // calls within the same transition could see different values when looking
    // up fields and locals
    values = createValueArray(vals.length);
    StackFrame resolveFrame = ThreadInfo.getCurrentThread().getLastNonSyntheticStackFrame();
    for (int i=0; i<vals.length; i++){
      values[i] = parse(vals[i], resolveFrame);
    }
  }
  
  @Override
  public T getChoice (int idx){
    if (idx >=0 && idx < values.length){
      return values[idx];
    } else {
      throw new IllegalArgumentException("choice index out of range: " + idx);
    }
  }

  @Override
  public void reset () {
    count = -1;

    isDone = false;
  }
      
  /** 
   * @see gov.nasa.jpf.vm.IntChoiceGenerator#getNextChoice()
   **/
  @Override
  public T getNextChoice() {

    if ((count >= 0) && (count < values.length)) {
      return values[count];
    }

    return getDefaultValue();
  }

  /**
   * @see gov.nasa.jpf.vm.ChoiceGenerator#hasMoreChoices()
   **/
  @Override
  public boolean hasMoreChoices() {
    if (!isDone && (count < values.length-1))  
      return true;
    else
      return false;
  }

  /**
   * @see gov.nasa.jpf.vm.ChoiceGenerator#advance()
   **/
  @Override
  public void advance() {
    if (count < values.length-1) count++;
  }

  /**
   * get String label of current value, as specified in config file
   **/
  public String getValueLabel(){
    return values[count].toString();
  }

  @Override
  public int getTotalNumberOfChoices () {
    return values.length;
  }

  @Override
  public int getProcessedNumberOfChoices () {
    return count+1;
  }
  
  @Override
  public boolean supportsReordering(){
    return true;
  }
  
  
  protected T parse (String varSpec, StackFrame resolveFrame){
    int sign = 1;

    char c = varSpec.charAt(0);
    if (c == '+'){
      varSpec = varSpec.substring(1);
    } else if (c == '-'){
      sign = -1;
      varSpec = varSpec.substring(1);
    }

    if (varSpec.isEmpty()){
      throw new JPFException("illegal value spec for IntChoiceFromList " + id);
    }

    c = varSpec.charAt(0);
    if (Character.isDigit(c)){ // its an integer literal
      return parseLiteral(varSpec, sign);

    } else { // a variable or field name
      Object o = resolveFrame.getLocalOrFieldValue(varSpec);
      if (o == null){
        throw new JPFException("no local or field '" + varSpec + "' value found for NumberChoiceFromList " + id);
      }
      if (o instanceof Number){
        return newValue( (Number)o, sign);
      } else {
        throw new JPFException("non-numeric local or field '" + varSpec + "' value for NumberChoiceFromList " + id);
      }
    }
  }

  
  @Override
  public NumberChoiceFromList<T> reorder (Comparator<T> comparator){
    
    T[] newValues = values.clone();
    Arrays.sort( newValues, comparator);
    
    // we can't instantiate directly
    try {
    NumberChoiceFromList<T> clone = (NumberChoiceFromList<T>)clone();
    clone.values = newValues;
    clone.count = -1;
    return clone;
    
    } catch (CloneNotSupportedException cnsx){
      // can't happen
      throw new JPFException("can't clone NumberChoiceFromList " + this);
    }
  }
  
  @Override
  public String toString() {
    StringBuilder sb = new StringBuilder(getClass().getName());
    sb.append("[id=\"");
    sb.append(id);
    sb.append('"');

    sb.append(",isCascaded:");
    sb.append(isCascaded);

    sb.append(",");
    for (int i=0; i<values.length; i++) {
      if (i > 0) {
        sb.append(',');
      }
      if (i == count) {
        sb.append(MARKER);
      }
      sb.append(values[i]);
    }
    sb.append(']');
    return sb.toString();
  }
  
  
  @Override
  public NumberChoiceFromList<T> randomize () {
    for (int i = values.length - 1; i > 0; i--) {
      int j = random.nextInt(i + 1);
      T tmp = values[i];
      values[i] = values[j];
      values[j] = tmp;
    }
    return this;
  }

}
