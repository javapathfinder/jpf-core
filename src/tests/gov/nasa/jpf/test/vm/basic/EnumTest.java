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

package gov.nasa.jpf.test.vm.basic;

import java.util.EnumSet;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

public class EnumTest extends TestJPF {

  //--- helper type
  enum A {
    ONE,
    TWO;
  }

  
  @Test
  public void testValueOf () {
    if (verifyNoPropertyViolation()) {
      assert A.valueOf("ONE") == A.ONE;
    }
  }

  @Test
  public void testEnumerate () {
    if (verifyNoPropertyViolation()){
      boolean[] seen = new boolean[2];

      for (A a : A.values()) {
        switch (a) {
          case ONE:
            System.out.println("this is ONE");
            break;
          case TWO:
            System.out.println("this is TWO");
            break;
          default:
            throw new RuntimeException("unknown enumeration constant");
        }
        seen[a.ordinal()] = true;
      }

      for (boolean b : seen){
        assert b : "unseen enum constant";
      }
    }
  }

  enum Option {
    A
  }

  @Test
  public void testEnumSet() {
    
    if (verifyNoPropertyViolation()){
      //Option o = Option.A; // <2do> init missing
      
      EnumSet<Option> options = EnumSet.allOf(Option.class);

      for (Option option : options) {
        System.out.println(option);
        assert option != null;
      }
    }
  }
  
}

