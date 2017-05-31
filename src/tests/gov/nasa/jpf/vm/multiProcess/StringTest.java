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

package gov.nasa.jpf.vm.multiProcess;

import java.io.IOException;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestMultiProcessJPF;

/**
 * @author Nastaran Shafiei <nastaran.shafiei@gmail.com>
 */
public class StringTest extends TestMultiProcessJPF {
  String[] args = {};
  
  @Test
  public void testInterns() throws IOException {
    
    if (mpVerifyNoPropertyViolation(2, args)) {
      String s0 = "something"; // interned string
      String s1 = new String("something"); // a new string which is not interned
      String s2 = s1.intern(); // taken from intern table
      String s3 = "something".intern(); // taken from intern table
      
      assertSame(s0.getClass(), java.lang.String.class);
      assertSame(s1.getClass(), java.lang.String.class);
      
      assertEquals(s0,s1);
      assertFalse(s0==s1);
      
      assertSame(s0,s2);
      assertSame(s0,s3);
    }
  }
}
