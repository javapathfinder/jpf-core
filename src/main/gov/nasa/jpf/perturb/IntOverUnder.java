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

package gov.nasa.jpf.perturb;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.IntChoiceGenerator;
import gov.nasa.jpf.vm.StackFrame;
import gov.nasa.jpf.vm.choice.IntChoiceFromSet;

/**
 * simple +/- delta perturbation of integer operand values
 */
public class IntOverUnder implements OperandPerturbator {

  protected int delta;
  protected int offset;

  public IntOverUnder (Config conf, String keyPrefix) {
    delta = conf.getInt(keyPrefix + ".delta", 0);
    offset = 0;
  }

  public IntOverUnder (int delta){
    this.delta = delta;
    offset = 0;
  }
  
  @Override
  public ChoiceGenerator<?> createChoiceGenerator (String id, StackFrame frame, Object refObject){
    int val = frame.peek(offset);

    int[] values = new int[3];

    values[0] = val + delta;
    values[1] = val;
    values[2] = val - delta;
    
    // set offset from refObject
    offset = (Integer)refObject;

    return new IntChoiceFromSet(id, values);
  }

  @Override
  public boolean perturb(ChoiceGenerator<?>cg, StackFrame frame) {
  	assert cg instanceof IntChoiceGenerator : "wrong choice generator type for IntOverUnder: " + cg.getClass().getName();

    int val = ((IntChoiceGenerator)cg).getNextChoice();
  	frame.setOperand(offset, val, false);
  	return cg.hasMoreChoices();
  }
  
  @Override
  public Class<? extends ChoiceGenerator<?>> getChoiceGeneratorType(){
    return IntChoiceFromSet.class;
  }
}
