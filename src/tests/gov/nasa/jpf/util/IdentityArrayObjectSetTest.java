
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
 * regression test for IdentityArrayObjectSet
 */
public class IdentityArrayObjectSetTest extends TestJPF {

  @Test
  public void testBasic(){
    String a = "a";
    String b = "b";

    IdentityArrayObjectSet<String> s = new IdentityArrayObjectSet<String>();

    assertTrue(  s.isEmpty());
    System.out.println(s);

    assertTrue(  s.add(a));
    assertTrue(  s.size() == 1);
    assertTrue(  s.contains(a));
    System.out.println(s);

    assertFalse( s.add(a)); // already in
    assertTrue(  s.size() == 1);
    System.out.println(s);

    assertTrue(  s.add(b));
    assertTrue(  s.size() == 2);
    assertTrue(  s.contains(b));
    System.out.println(s);

    assertTrue(  s.remove(a));
    assertTrue(  s.size() == 1);
    assertFalse( s.contains(a));
    assertTrue(  s.contains(b));
    System.out.println(s);

    assertFalse( s.remove(a)); // can't remove it a second time
    System.out.println(s);

    assertTrue(  s.remove(b));
    assertTrue(  s.isEmpty());
    System.out.println(s);
  }

  @Test
  public void testIteratorRemoveFromFullArray() {
    IdentityArrayObjectSet<String> set = new IdentityArrayObjectSet<String>();

    String a = new String("a");
    String b = new String("b");
    String c = new String("c");
    String d = new String("d");

    set.add(a);
    set.add(b);
    set.add(c);
    set.add(d);

    java.util.Iterator<String> it = set.iterator();

    assertTrue(it.next() == a);
    it.remove();

    assertTrue(set.size() == 3);
    assertFalse(set.contains(a));
    assertTrue(set.contains(b));
    assertTrue(set.contains(c));
    assertTrue(set.contains(d));

    assertTrue(it.next() == b);
    assertTrue(it.next() == c);
    assertTrue(it.next() == d);
    assertFalse(it.hasNext());
  }

  @Test(expected = IllegalStateException.class)
  public void testIteratorRemoveBeforeNext() {
    IdentityArrayObjectSet<String> set = new IdentityArrayObjectSet<String>();
    set.add(new String("a"));

    java.util.Iterator<String> it = set.iterator();
    it.remove();
  }

  @Test(expected = IllegalStateException.class)
  public void testIteratorRemoveTwice() {
    IdentityArrayObjectSet<String> set = new IdentityArrayObjectSet<String>();
    set.add(new String("a"));

    java.util.Iterator<String> it = set.iterator();
    it.next();
    it.remove();
    it.remove();
  }

  @Test
  public void testIteratorRemoveClearsVacatedSlot() {
    IdentityArrayObjectSet<String> set = new IdentityArrayObjectSet<String>();

    set.add(new String("a"));
    set.add(new String("b"));
    set.add(new String("c"));
    set.add(new String("d"));

    java.util.Iterator<String> it = set.iterator();
    it.next();
    it.remove();

    assertTrue(set.elements[3] == null);
  }
}
