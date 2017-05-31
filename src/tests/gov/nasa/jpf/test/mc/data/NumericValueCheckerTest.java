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

import gov.nasa.jpf.util.TypeRef;
import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * regression test for the NumericValueChecker listener
 */
public class NumericValueCheckerTest extends TestJPF {

  static class C1 {
    double d;
    void setValue(double v){
      d = v;
    }
  }

  @Test
  public void testField(){
    if (verifyPropertyViolation(new TypeRef("gov.nasa.jpf.listener.NumericValueChecker"),
            "+listener=.listener.NumericValueChecker",
            "+range.fields=d",
            "+range.d.field=*.NumericValueCheckerTest$C1.d",
            "+range.d.min=42")){
      C1 c1= new C1();
      c1.setValue(0);
    }
  }


  static class C2 {
    void doSomething(int d){
      int x = d;
    }
  }

  @Test
  public void testVars(){
    if (verifyPropertyViolation(new TypeRef("gov.nasa.jpf.listener.NumericValueChecker"),
            "+listener=.listener.NumericValueChecker",
            "+range.vars=x",
            "+range.x.var=*$C2.doSomething(int):x",
            "+range.x.min=42")){
      C2 c2= new C2();
      c2.doSomething(-42);
    }
  }
}
