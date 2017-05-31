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

package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassParseException;
import gov.nasa.jpf.vm.FieldInfo;
import gov.nasa.jpf.vm.MethodInfo;


import java.io.File;

import org.junit.Test;

/**
 * unit test for ClassInfo initialization
 */
public class ClassInfoTest extends TestJPF {

  @interface X {
    String value() default "nothing";
  }

  @interface Y {
    int[] value();
  }

  @X
  public static class MyClass implements Cloneable {
    public static final int D = 42;

    @X("data") String s;

    public MyClass(String s) {
      this.s = s;
      foo();
    }

    public static int whatIsIt() {
      int d = D;
      switch (d) {
        case 41:
          d = -1;
          break;
        case 42:
          d = 0;
          break;
        case 43:
          d = 1;
          break;
        default:
          d = 2;
          break;
      }
      return d;
    }

    public boolean isItTheAnswer (boolean b, @X @Y({1,2,3}) int d, String s){
      switch (d){
        case 42: return true;
        default: return false;
      }
    }

    protected void foo() throws IndexOutOfBoundsException {
      @X int d = D;

      Object[] a = new Object[2];
      String s = "blah";
      a[0] = s;

      String x = (String)a[0];
      Object o = a;
      if (o instanceof Object[]){
        o = x;
      }
      if (o instanceof String){
        o = null;
      }

      Object[][] aa = new Object[2][2];

      try {
        char c = s.charAt(d);
      } catch (IndexOutOfBoundsException ioobx) {
        System.out.println("too big");
        throw ioobx;
      }
    }

    @X
    String getString() {
      return s;
    }
  }

  @Test
  public void testClassFileInitialization() {
    File file = new File("build/tests/gov/nasa/jpf/jvm/ClassInfoTest$MyClass.class");

    try {
      ClassInfo ci = new NonResolvedClassInfo( "gov.nasa.jpf.jvm.ClassInfoTest$MyClass", file);

      assert ci.getName().equals("gov.nasa.jpf.jvm.ClassInfoTest$MyClass");

      System.out.println("-- declared instance fields");
      for (FieldInfo fi : ci.getDeclaredInstanceFields()){
        System.out.print(fi.getType());
        System.out.print(' ');
        System.out.println(fi.getName());
      }

      assert ci.getNumberOfDeclaredInstanceFields() == 1;
      assert ci.getNumberOfStaticFields() == 1;

      System.out.println();
      System.out.println("-- methods");
      for (MethodInfo mi : ci){
        System.out.println(mi.getUniqueName());
      }


    } catch (ClassParseException cfx){
      //cfx.printStackTrace();
      fail("ClassParseException: " + cfx);
    }
  }

}
