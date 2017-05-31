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
package gov.nasa.jpf.test.java.concurrent;

import gov.nasa.jpf.util.test.TestJPF;

import java.util.concurrent.atomic.AtomicReferenceFieldUpdater;

import org.junit.Test;

/**
 * raw test for java.util.concurrent.atomic.AtomicReferenceFieldUpdater
 */
public class AtomicReferenceFieldUpdaterTest extends TestJPF {

  static final String[] JPF_ARGS = {"+cg.enumreate_cas=true"};

  //--- the test methods
  String str;
  byte[] buf;

  @Test
  public void testStringField() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, String> upd =
              AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, String.class, "str");

      String s1 = "one";
      String s2 = "two";
      str = s1;

      System.out.println(str);
      assert upd.compareAndSet(this, s1, s2);
      System.out.println(str);
      assert str == s2;

      assert !upd.compareAndSet(this, s1, "nogo");
      assert str == s2;
      assert str == upd.get(this);

      assert s2 == upd.getAndSet(this, s1);
      assert str == s1;

      upd.set(this, s2);
      assert str == s2;

      upd.lazySet(this, s1);
      assert str == s1;

      assert upd.weakCompareAndSet(this, s1, s2);
      assert str == s2;

      assert !upd.weakCompareAndSet(this, s1, "nogo");
      assert str == s2;
    }
  }

  @Test
  public void testByteArrayField() {
    if (verifyNoPropertyViolation(JPF_ARGS)) {
      AtomicReferenceFieldUpdater<AtomicReferenceFieldUpdaterTest, byte[]> upd =
              AtomicReferenceFieldUpdater.newUpdater(AtomicReferenceFieldUpdaterTest.class, byte[].class, "buf");

      byte[] b1 = new byte[10];
      byte[] b2 = new byte[5];

      buf = b1;
      System.out.println(buf);
      assert upd.compareAndSet(this, b1, b2);
      System.out.println(buf);
      assert (buf == b2);

      assert !upd.compareAndSet(this, b1, new byte[3]);
      assert (buf == b2);
    }
  }
}
