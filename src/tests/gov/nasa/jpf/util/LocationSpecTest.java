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
 * unit test for gov.nasa.jpf.util.LocationSpec
 */
public class LocationSpecTest extends TestJPF {

  @Test
  public void testSingleLocation() {
    LocationSpec ls = LocationSpec.createLocationSpec("Foobar.java:42");
    System.out.println("# testing: " + ls);

    assertTrue(ls.matchesFile("Foobar.java"));
    assertTrue(!ls.isLineInterval());
    assertTrue(ls.getLine() == 42);

    assertFalse(ls.matchesFile("Bull"));

    assertTrue(ls.matchesFile("/x/y/Foobar.java"));
  }

  @Test
  public void testAbsoluteLocation() {
    LocationSpec ls = LocationSpec.createLocationSpec("/x/y/z/Foobar.java:42");
    System.out.println("# testing: " + ls);

    assertTrue(ls.matchesFile("/x/y/z/Foobar.java"));
  }

  @Test
  public void testPlatformLocation() {
    LocationSpec ls = LocationSpec.createLocationSpec("C:\\x\\y\\z\\Foobar.java:42");
    System.out.println("# testing: " + ls);

    assertTrue(ls.matchesFile("C:\\x\\y\\z\\Foobar.java"));
    assertTrue(ls.getLine() == 42);
  }


  @Test
  public void testRelativeLocation() {
    LocationSpec ls = LocationSpec.createLocationSpec("x/y/z/Foobar.java:42");
    System.out.println("# testing: " + ls);

    assertTrue(ls.matchesFile("x/y/z/Foobar.java"));
  }

  @Test
  public void testWildcards() {
    LocationSpec ls = LocationSpec.createLocationSpec("x/*/Foo*.java:42");
    System.out.println("# testing: " + ls);

    assertTrue(ls.matchesFile("x/y/z/Foobar.java"));
    assertTrue(ls.matchesFile("Fooboo.java"));
  }

  @Test
  public void testAbsoluteRange(){
    LocationSpec ls = LocationSpec.createLocationSpec("Foobar.java:42-48");
    System.out.println("# testing: " + ls);

    assertTrue(ls.isLineInterval());
    assertTrue(ls.getFromLine() == 42);
    assertTrue(ls.getToLine() == 48);
  }

  @Test
  public void testRelativeRange(){
    LocationSpec ls = LocationSpec.createLocationSpec("Foobar.java:42+6");
    System.out.println("# testing: " + ls);

    assertTrue(ls.isLineInterval());
    assertTrue(ls.getFromLine() == 42);
    assertTrue(ls.getToLine() == 48);
  }

  @Test
  public void testOpenRange(){
    LocationSpec ls = LocationSpec.createLocationSpec("Foobar.java:42+");
    System.out.println("# testing: " + ls);

    assertTrue(ls.isLineInterval());
    assertTrue(ls.getFromLine() == 42);
    assertTrue(ls.getToLine() == Integer.MAX_VALUE);
  }

}
