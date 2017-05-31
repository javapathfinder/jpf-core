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
package gov.nasa.jpf.util;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * regression test for .util.StringSetMatcher
 */
public class StringSetMatcherTest extends TestJPF {
 
  @Test
  public void testInversion (){
    StringSetMatcher ssm = new StringSetMatcher("!failure-*", "failure-10");
    
    assertTrue( ssm.matchesAny("blah"));
    assertFalse( ssm.matchesAny("failure-0"));
    
    assertTrue( ssm.matchesAny("failure-10"));
    assertFalse( ssm.matchesAll("failure-10"));
  }
  
  @Test
  public void testMatchesAll (){
    StringSetMatcher ssm = new StringSetMatcher("a*", "*blah");
    
    assertTrue( ssm.matchesAll("aXXblah"));
    assertFalse( ssm.matchesAll("xblah"));
  }
  
  @Test
  public void testMatchesAny (){
    StringSetMatcher ssm = new StringSetMatcher("blah", "gna");
    
    assertTrue( ssm.matchesAny("blah"));
    assertFalse( ssm.matchesAny("xblah"));
  }

  @Test
  public void testHasAnyPattern(){
    StringSetMatcher ssm = new StringSetMatcher("*", "gna");
    assertTrue( ssm.matchesAny("blubb"));
    assertTrue( ssm.matchesAll("gna"));
    
    ssm = new StringSetMatcher("*");  // single pattern optimization
    assertTrue(ssm.matchesAll("gna"));
    assertTrue(ssm.matchesAny("gulp"));
  }
  
}
