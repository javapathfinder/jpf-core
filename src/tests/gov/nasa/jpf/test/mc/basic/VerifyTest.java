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
package gov.nasa.jpf.test.mc.basic;


import gov.nasa.jpf.ListenerAdapter;
import gov.nasa.jpf.search.Search;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;


/**
 * test various Verify APIs
 */
public class VerifyTest extends TestJPF {

  @Test public void testBreak () {

    if (verifyNoPropertyViolation()) {
      int y = 4;
      int x = 0;

      while (x != y) { // JPF should state match on the backjump
        x = x + 1;
        if (x > 3) {
          x = 0;
        }

        Verify.breakTransition("testBreakTransition"); // this should eventually state match
      }

      assert false : "we should never get here";
    }
  }

  @Test public void testProperties () {

    if (verifyNoPropertyViolation("+hum=didum")) {
      String target = Verify.getProperty("hum");
      System.out.println("got hum=" + target);
      assert target.equals("didum");

      Verify.setProperties("foo=bar");
      String p = Verify.getProperty("foo");
      System.out.println("got foo=" + p);
      assert "bar".equals(p);
    }
  }
  
  @Test public void testChangeListener () {
    
    if (verifyNoPropertyViolation()) {
      Verify.setProperties("listener=gov.nasa.jpf.listener.StateSpaceAnalyzer");  // This used to cause a NullPointerException
    }
  }
  
  @Test public void testGetBoolean () {

    Verify.resetCounter(0);
    Verify.resetCounter(1);

    if (verifyNoPropertyViolation()) {
      Verify.incrementCounter(Verify.getBoolean() ? 1 : 0);
    } else {
      assert Verify.getCounter(0) == 1;
      assert Verify.getCounter(1) == 1;
    }
  }

  @Test public void testGetBooleanFalseFirst () {
    boolean falseFirst, value;

    Verify.resetCounter(0);

    if (verifyNoPropertyViolation()) {
    
      falseFirst = Verify.getBoolean();
    
      Verify.resetCounter(0);
    
      value = Verify.getBoolean(falseFirst);
      
      Verify.ignoreIf(Verify.getCounter(0) != 0);
      
      Verify.incrementCounter(0);
      
      assert value == !falseFirst;
    }
  }
  
  /**
   * This test ensures that stateBacktracked() is called even if the transistion 
   * is ignored.  This is important for listeners that keep a state that must 
   * match the VM's state exactly and the state is updated in the middle of 
   * transitions.  This is not possible if a backtrack happens on an ignored 
   * transition and the stateBacktracked is not called.
   */
  @Test
  public void backtrackNotificationAfterIgnore() {
    if (verifyNoPropertyViolation("+listener+=,gov.nasa.jpf.test.mc.basic.VerifyTest$CountBacktrack",
            "+vm.max_transition_length=MAX")) {
      if (Verify.getBoolean(false)) {
        Verify.ignoreIf(true);
      }
    } else {
      // 2 for the Verify.getBoolean, 1 for <root>
      assertEquals(3, CountBacktrack.getBacktrackedCount());
    }
  }
  
  public static class CountBacktrack extends ListenerAdapter {

    private static int m_backtrackedCount;

    @Override
    public void stateBacktracked(Search search) {
      m_backtrackedCount++;
    }

    public static int getBacktrackedCount() {
      return (m_backtrackedCount);
    }
  }
   
  // <2do>... and many more to come


  @Test
  public void testBitSet() {
    int id = 2;

    if (verifyNoPropertyViolation()) {      
      // JPF execution only
      Verify.setBitInBitSet(id, 3, true);
      Verify.setBitInBitSet(id, 1, true);
      
    } else {
      // host VM execution only
      assert Verify.getBitInBitSet(id, 1) == true;
      assert Verify.getBitInBitSet(id, 2) == false;
      assert Verify.getBitInBitSet(id, 3) == true;
    }
  }
}
