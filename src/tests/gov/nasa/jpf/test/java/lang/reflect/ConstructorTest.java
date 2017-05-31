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

package gov.nasa.jpf.test.java.lang.reflect;

import gov.nasa.jpf.util.test.TestJPF;
import java.lang.reflect.Constructor;
import org.junit.Test;

public class ConstructorTest extends TestJPF {

  public static void main (String[] args){
    runTestsOfThisClass(args);
  }

  public static class Test1 {
    public Test1 (Class<?>... argTypes) {
    }

    public Test1 (Object[] argTypes) {
    }
  }

  @Test
  public void equalsTest () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      Constructor ctor1 = String.class.getConstructor();
      Constructor ctor2 = String.class.getConstructor();
      assertTrue(ctor1.equals(ctor2));
      assertFalse(ctor1 == ctor2);
    }
  }

  @Test
  public void isVarArgsTest () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      assertTrue(Test1.class.getConstructors()[0].isVarArgs());
      assertFalse(Test1.class.getConstructors()[1].isVarArgs());
    }
  }

  @Test
  public void hashCodeTest (){
    if (verifyNoPropertyViolation()){
      Constructor ctor1 = Test1.class.getConstructors()[0];
      Constructor ctor2 = Test1.class.getConstructors()[1];
      assertTrue(ctor1.hashCode() == ctor2.hashCode());
    }
  }
}
