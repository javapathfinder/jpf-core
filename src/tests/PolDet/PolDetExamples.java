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
import org.junit.Test;
import org.junit.BeforeClass;
import org.junit.Before;
import org.junit.After;
import gov.nasa.jpf.annotation.FilterField;
import gov.nasa.jpf.vm.Verify;


/**
 *
 * Example JUnit tests for PolDet@JPF to check
 *
 * Run gradle task 'testPolDet' to execute PolDet@JPF on this class
 *
 * @author Pu Yi
 */
public class PolDetExamples {
  static int a;
  static int b;
  int c = 5;
  @BeforeClass
  public static void initialize() {
    a = 0;
    b = 1;
  }
  @Test
  public void t1() {
  }
  @Test
  public void t2() {
    a = 4;
  }
  @Test
  public void t3() {
    // change instance field, not pollution
    c = 2;
  }
  @Test
  public void t4() {
    System.out.println(ClassA.string);
  }
  @Test
  public void t5() {
    String s = "bar";
    new ClassB().setString(s);
  }
  @Test
  public void t6() {
    // false negative due to common-root isomorphism
    ClassD d = new ClassD();
  }
  @Test
  public void t7() {
    // change the object cache, should not be considered as pollution
    try {
      Class c = Class.forName("gov.nasa.jpf.BoxObjectCaches");
      java.lang.reflect.Field f = c.getDeclaredField("byteCache");
      f.setAccessible(true);
      Byte[] b = new Byte[5];
      f.set(null, b);
    } catch(Exception e) {
      System.out.println(e);
    }
  }
}

class ClassA {
  static String string = "foo";
  static void setString(String s) {
    string = s;
  }
}

class ClassB {
  void setString(String s) {
    ClassA.setString(s);
  }
}

class ClassC {
  static int instanceCount = 0;
  ClassC() {
    instanceCount++;
  }
}

class ClassD {
  ClassC c;
  ClassD() {
    c = new ClassC();
  }
}
