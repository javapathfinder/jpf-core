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
import gov.nasa.jpf.vm.IntChoiceGenerator;
/**
 * @author jpenix
 *
 * choose from a set of values provided in configuration as
 * xxx.class = IntChoiceFromList
 * xxx.values = {1, 2, 3, 400}
 * where "xxx" is the choice id.
 * 
 * choices can then made using: getInt("xxx");
 */
public class IntChoiceFromList extends NumberChoiceFromList<Integer> implements IntChoiceGenerator {

  
  @Override
  protected Integer[] createValueArray(int len){
    return new Integer[len];
  }
  @Override
  protected Integer getDefaultValue() {
    return 0;
  }
    
  @Override
  public Class<Integer> getChoiceType(){
    return Integer.class;
  }
  	
  @Override
  protected Integer parseLiteral (String literal, int sign){
    int val = Integer.parseInt(literal);
    return new Integer( val * sign);
  }
  
  @Override
  protected Integer newValue (Number num, int sign){
    return new Integer( num.intValue() * sign);
  }
  
  /**
   *  super constructor for subclasses that want to configure themselves
   * @param id name used in choice config
   */
  protected IntChoiceFromList(String id){
    super(id);
  }

  protected IntChoiceFromList (String id, Integer[] vals){
    super(id, vals);
  }
  
	public IntChoiceFromList(Config conf, String id) {
		super(conf, id);
	}

  public IntChoiceFromList(String id, int... val){
    super(id);

    if (val != null){
      values = new Integer[val.length];
      for (int i=0; i<val.length; i++){
        values[i] = val[i];  // enable use of cached Integer values
      }
    } else {
      throw new JPFException("empty set for IntChoiceFromList");
    }

    count = -1;
  }
}
