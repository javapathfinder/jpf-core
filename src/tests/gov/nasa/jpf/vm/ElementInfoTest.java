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

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.DynamicElementInfo;
import gov.nasa.jpf.vm.ElementInfo;

import org.junit.Test;

/**
 * unit test for ElementInfos
 */
public class ElementInfoTest extends TestJPF {

  @Test
  public void testPinDownCounter() {
    DynamicElementInfo ei = new DynamicElementInfo();

    assert !ei.isPinnedDown();

    assert ei.incPinDown(); // should return true because this is the first inc

    // count up to the max number
    for (int i=2; i<= ElementInfo.ATTR_PINDOWN_MASK; i++){
      assert !ei.incPinDown();
      assert ei.getPinDownCount() == i;
    }

    // count exceeded, now it should throw a JPFException
    try {
      ei.incPinDown();
      assert false : "incPinDown did not throw";
    } catch (JPFException x){
      System.out.println("caught " + x + ", getPinDownCount() = " + ei.getPinDownCount());
    }

    // count down to the first one
    for (int i=ElementInfo.ATTR_PINDOWN_MASK-1; i>0; i--){
      assert !ei.decPinDown() : "decPinDown() from " + ei.getPinDownCount() + " returned true";
      assert ei.getPinDownCount() == i : "getPinDownCount() = " + ei.getPinDownCount() +
              " != " + i;
    }

    assert ei.decPinDown(); // should return true now
    assert ei.getPinDownCount() == 0 : "getPinDownCount() != 0";
  }
  
  
  
  @Test
  public void testALiveFlag() {
    DynamicElementInfo ei = new DynamicElementInfo();

    assert !ei.isMarked();
    
    ei.setAlive(true);
    
    assert  ei.isAlive(true);
    assert !ei.isAlive(false);
    assert  ei.isMarkedOrAlive(true);
    assert !ei.isMarkedOrAlive(false);
    
    
    ei.setAlive(false);
    
    assert !ei.isAlive(true);
    assert  ei.isAlive(false);
    assert !ei.isMarkedOrAlive(true);
    assert  ei.isMarkedOrAlive(false);
    
  }

  @Test
  public void testMarkedFlag() {
    DynamicElementInfo ei = new DynamicElementInfo();

    assert !ei.isMarked();
    
    ei.setMarked();
    assert ei.isMarked();

    ei.setUnmarked();
    assert !ei.isMarked();
    
  }

  @Test
  public void testMarkedOrAlive() {
    DynamicElementInfo ei = new DynamicElementInfo();
    boolean[] boolValues = { true, false};
    
    assert !ei.isMarked();
    
    ei.setMarked();
    assert ei.isMarked();
    
    for(boolean b : boolValues) {
       ei.setAlive(b);
       
      assert ei.isMarkedOrAlive(true);
      assert ei.isMarkedOrAlive(false);
    }
    

    ei.setUnmarked();
    assert !ei.isMarked();
    
    for(boolean b : boolValues) {
      ei.setAlive(b);
      
      assert ei.isMarkedOrAlive(true) == ei.isAlive(true);
      assert ei.isMarkedOrAlive(false) == ei.isAlive(false);
    }
  }

}
