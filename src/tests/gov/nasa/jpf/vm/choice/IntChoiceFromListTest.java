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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ChoiceGenerator;
import gov.nasa.jpf.vm.choice.IntChoiceFromList;

import org.junit.Test;

/**
 * unit test for IntChoiceFromList
 */
public class IntChoiceFromListTest extends TestJPF {

  private void testListContents(ChoiceGenerator<Integer> cg) {
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 1);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 2);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 3);
    cg.advance();
    assertEquals ((int)cg.getNextChoice(), 4);
  }

  @Test
  public void testListWithOutDuplicates() {
    IntChoiceFromList cg = new IntChoiceFromList("test", 1, 2, 3, 4);
    testListContents(cg);
    assertFalse (cg.hasMoreChoices());
  }

  @Test
  public void testListWithDuplicates() {
    IntChoiceFromList cg = new IntChoiceFromList("test1", 1, 2, 3, 4, 4);
    testListContents(cg);
    cg.advance();
    assertFalse (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 4);
  }

  @Test
  public void testListWithDuplicates2() {
    IntChoiceFromList cg = new IntChoiceFromList("test2", 1, 2, 1, 2, 1, 2);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 1);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 2);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 1);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 2);
    cg.advance();
    assertTrue (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 1);
    cg.advance();
    assertFalse (cg.hasMoreChoices());
    assertEquals ((int)cg.getNextChoice(), 2);
  }
}
