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

package gov.nasa.jpf.util;

import gov.nasa.jpf.util.test.TestJPF;

import org.junit.Test;

/**
 * unit test for MethodSpecs
 */
public class MethodSpecTest extends TestJPF {

  @Test
  public void testConstruction(){

    //-- should be all non-null
    String spec = "x.y.Foo.bar(java.lang.String,^float[])";
    MethodSpec ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms != null);

    spec = "x.y.Foo+.*";
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms != null);

    spec = "*.foo(^int, ^double)";
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms != null);

    spec = "( ^int, ^double)";
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms != null);

    spec = ".foo";
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms != null);

    spec = ".(int)";
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms != null);

    spec = "!java.*.*";  // first '*' belongs to class spec, second to method
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(ms);
    assertTrue(ms != null);

    spec = "java.*"; // not what you think - the class spec is "java" and the method is "*"
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(ms);
    

    //--- should all produce null

    spec = "*.foo(^int, ^double";  // missing ')'
    ms = MethodSpec.createMethodSpec(spec);
    System.out.println(spec + " => " + ms);
    assertTrue(ms == null);


    //System.out.println("matches (java.lang.Object,*): " +
    //                    ms.matches("java.lang.Object", "*"));

  }
}
