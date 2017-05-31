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
import gov.nasa.jpf.vm.FloatChoiceGenerator;

public class FloatChoiceFromList extends NumberChoiceFromList<Float> implements FloatChoiceGenerator {

  
  @Override
  protected Float[] createValueArray(int len){
    return new Float[len];
  }
  @Override
  protected Float getDefaultValue() {
    return 0.0f;
  }
    
  @Override
  public Class<Float> getChoiceType(){
    return Float.class;
  }
    
  @Override
  protected Float parseLiteral (String literal, int sign){
    Float val = Float.parseFloat(literal);
    return new Float( val * sign);
  }
  
  @Override
  protected Float newValue (Number num, int sign){
    return new Float( num.intValue() * sign);
  }
  
  /**
   *  super constructor for subclasses that want to configure themselves
   * @param id name used in choice config
   */
  protected FloatChoiceFromList(String id){
    super(id);
  }

  protected FloatChoiceFromList (String id, Float[] vals){
    super(id, vals);
  }
  
  public FloatChoiceFromList(Config conf, String id) {
    super(conf, id);
  }

  public FloatChoiceFromList(String id, float... val){
    super(id);

    if (val != null){
      values = new Float[val.length];
      for (int i=0; i<val.length; i++){
        values[i] = val[i];  // enable use of cached Float values
      }
    } else {
      throw new JPFException("empty set for FloatChoiceFromList");
    }

    count = -1;
  }

}
