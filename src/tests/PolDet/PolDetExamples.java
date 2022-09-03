/*
 * Copyright (C) 2021 Pu Yi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
 */

package PolDet; 
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
