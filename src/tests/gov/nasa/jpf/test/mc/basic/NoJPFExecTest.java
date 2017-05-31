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
package gov.nasa.jpf.test.mc.basic;

import gov.nasa.jpf.annotation.NoJPFExecution;
import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.util.TypeRef;
import org.junit.Test;

/**
 * test for NoJPFExecution annotations
 */
public class NoJPFExecTest extends TestJPF {

  @NoJPFExecution
  protected void foo(){
    System.out.println("!! foo() should not be executed under JPF");
  }

  @Test
  public void testNoJPFExec(){
    if (verifyJPFException( new TypeRef("gov.nasa.jpf.JPFException"))){
      foo();
    }
  }


  @NoJPFExecution
  protected void bar(){
    System.out.println("!! bar() bytecode should not be executed under JPF");
  }

  @Test
  public void testInterceptedNoJPFExec(){
    if (verifyNoPropertyViolation()){
      bar();
    }
  }

}
