/*
 * Copyright (C) 2015, United States Government, as represented by the
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

import gov.nasa.jpf.util.PermutationGenerator;
import gov.nasa.jpf.vm.ChoiceGeneratorBase;

/**
 * a CG that creates permutation choices
 * 
 * since PermutationCGs have a potentially huge number of choices, we don't
 * compute and store them upfront, but rather keep the enumeration state in a
 * low level pull-based generator
 */
public class PermutationCG extends ChoiceGeneratorBase<int[]>{
  
  protected PermutationGenerator pg;
  protected int[] permutation;
  
  public PermutationCG (PermutationGenerator pg){
    this.pg = pg;
  }

  public PermutationCG (String id, PermutationGenerator pg){
    super(id);
    this.pg = pg;
  }
  
  @Override
  public int[] getNextChoice() {
    return permutation;
  }

  @Override
  public Class<int[]> getChoiceType() {
    return int[].class;
  }

  @Override
  public boolean hasMoreChoices() {
    return pg.hasNext();
  }

  @Override
  public void advance() {
    permutation = pg.next();
  }

  @Override
  public void reset() {
    pg.reset();
  }

  @Override
  public int getTotalNumberOfChoices() {
    return (int) pg.getNumberOfPermutations();
  }

  @Override
  public int getProcessedNumberOfChoices() {
    return (int) pg.getNumberOfGeneratedPermutations();
  }
}
