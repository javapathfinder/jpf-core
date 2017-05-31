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
package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;

public class DataChoiceTest extends TestJPF {

  class MyType {

    String id;

    MyType(String id) {
      this.id = id;
    }

    @Override
	public String toString() {
      return ("MyType " + id);
    }
  }


  int intField = 42;
  double doubleField = -42.2;

  @Test
  public void testIntFromSet() {

    if (!isJPFRun()) {
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+my_int_from_set.class=gov.nasa.jpf.vm.choice.IntChoiceFromSet",
            "+my_int_from_set.values=1,2,3,intField,localVar")) {
      int localVar = 43;  // read by choice generator

      int i = Verify.getInt("my_int_from_set");
      Verify.incrementCounter(0);
      System.out.println(i);

    }

    if (!isJPFRun()) { // this is only executed outside JPF
      if (Verify.getCounter(0) != 5) {
        fail("wrong number of backtracks");
      }
    }
  }

  @Test
  public void testIntFromArray () {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation()){
      int i = Verify.getIntFromList(1,2,3,4,5); // ..and change the combination on my luggage
      System.out.println(i);
      if (i>0 && i < 6){
        Verify.incrementCounter(0);
      } else {
        assert false : "wrong int choice value: " + i;
      }
    }

    if (!isJPFRun()) { // this is only executed outside JPF
      if (Verify.getCounter(0) != 5) {
        fail("wrong number of backtracks");
      }
    }
  }


  @Test
  public void testDoubleFromSet() {

    if (!isJPFRun()) {
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation("+my_double_from_set.class=gov.nasa.jpf.vm.choice.DoubleChoiceFromSet",
            "+my_double_from_set.values=42.0,43.5,doubleField,localVar")) {

      double localVar = 4200.0; // read by choice generator

      double d = Verify.getDouble("my_double_from_set");
      Verify.incrementCounter(0);
      System.out.println(d);
    }

    if (!isJPFRun()) { // this is only executed outside JPF
      if (Verify.getCounter(0) != 4) {
        fail("wrong number of backtracks");
      }
    }
  }

  @Test
  public void testDoubleFromArray () {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }

    if (verifyNoPropertyViolation()){
      double d = Verify.getDoubleFromList(-42.0,0,42.0);
      System.out.println(d);

      if (d == -42.0 || d == 0.0 || d == 42.0){
        Verify.incrementCounter(0);
      } else {
        assert false : "wrong double choice value: " + d;
      }
    }

    if (!isJPFRun()) { // this is only executed outside JPF
      if (Verify.getCounter(0) != 3) {
        fail("wrong number of backtracks");
      }
    }
  }
}
