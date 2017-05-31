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

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Types;

import org.junit.Test;

/**
 * unit tests for gov.nasa.jpf.vm.Types
 */
public class TypesTest extends TestJPF {

  @Test public void testGetSignatureName () {
    
    String in  = "int foo(int,java.lang.String)";
    String out = "foo(ILjava/lang/String;)I";    
    String s = Types.getSignatureName(in);
    System.out.println( in + " => " + s);
    assert out.equals(s);

    in  = "double[] what_ever (char[], X )";
    out = "what_ever([CLX;)[D";
    s = Types.getSignatureName(in);
    System.out.println( in + " => " + s);
    assert out.equals(s);

    in  = "bar()";
    out = "bar()";
    s = Types.getSignatureName(in);
    System.out.println( in + " => " + s);
    assert out.equals(s);

  }

  //... and many more to come
}
