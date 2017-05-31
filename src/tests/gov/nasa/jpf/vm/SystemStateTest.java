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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.BooleanChoiceGenerator;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.SystemState;
import gov.nasa.jpf.vm.choice.DoubleChoiceFromList;
import gov.nasa.jpf.vm.choice.IntChoiceFromSet;

import org.junit.Test;


/**
 * unit test driver for SystemState functions
 */
public class SystemStateTest extends TestJPF {

  static class MyJVM extends SingleProcessVM {

    @Override
	protected void notifyChoiceGeneratorSet (ChoiceGenerator<?>cg) {
      System.out.println("notifyChoiceGeneratorSet: " + cg);
    }
    @Override
	protected void notifyChoiceGeneratorAdvanced (ChoiceGenerator<?>cg) {
      System.out.println("notifyChoiceGeneratorAdvanced: " + cg);
    }
    @Override
	protected void notifyChoiceGeneratorProcessed (ChoiceGenerator<?>cg) {
      System.out.println("notifyChoiceGeneratorProcessed: " + cg);
    }
  }

  static class MySystemState extends SystemState {
  }


  @Test
  public void testCascadedCGops() {

    MyJVM vm = new MyJVM();
    MySystemState ss = new MySystemState();

    IntChoiceFromSet       cg0 = new IntChoiceFromSet( "cg0", -100, -200); // not cascaded
    BooleanChoiceGenerator cg1 = new BooleanChoiceGenerator("cg1"); // false,true
    IntChoiceFromSet       cg2 = new IntChoiceFromSet( "cg2", 1, 2);
    DoubleChoiceFromList    cg3 = new DoubleChoiceFromList( "cg3", 42.1, 42.2);

    cg2.isCascaded = true;
    cg1.isCascaded = true;

    cg3.prev = cg2;
    cg2.prev = cg1;
    cg1.prev = cg0;
    ss.curCg = cg3;

    cg0.advance();

    //--- test initial advance
    System.out.println("--- testing advanceCurCg()");
    ss.advanceCurCg(vm);

    assert cg0.getNextChoice() == -100;
    assert cg1.getNextChoice() == false;
    assert cg2.getNextChoice() == 1;
    assert cg3.getNextChoice() == 42.1;


    //--- test advanceCascadedParent
    System.out.println("--- testing advanceCascadedParent()");
    cg2.advance(2);
    cg3.advance(2);

    assert !cg2.hasMoreChoices();
    assert !cg3.hasMoreChoices();

    System.out.println(cg1);
    System.out.println(cg2);
    System.out.println(cg3);
        
    ss.advanceCascadedParent(vm,cg3);

    assert cg0.getNextChoice() == -100;
    assert cg1.getNextChoice() == true;
    assert cg2.getNextChoice() == 1;
    assert cg3.getNextChoice() == 42.1;
  }

  @Test
  public void testCascadedCGadvance() {

    MyJVM vm = new MyJVM();
    MySystemState ss = new MySystemState();

    BooleanChoiceGenerator cg1 = new BooleanChoiceGenerator("cg1"); // false,true
    IntChoiceFromSet       cg2 = new IntChoiceFromSet( "cg2", 1, 2);
    DoubleChoiceFromList    cg3 = new DoubleChoiceFromList( "cg3", 42.1, 42.2);

    cg2.isCascaded = true;
    cg1.isCascaded = true;

    cg3.prev = cg2;
    cg2.prev = cg1;
    ss.curCg = cg3;

    int n = 0;
    while (ss.advanceCurCg(vm)){
      System.out.println("--");
      n++;
    }

    assert n == 8;
  }
}
