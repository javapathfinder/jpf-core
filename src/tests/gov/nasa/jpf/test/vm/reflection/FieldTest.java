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
package gov.nasa.jpf.test.vm.reflection;

import gov.nasa.jpf.util.test.TestJPF;

import java.lang.reflect.Field;

import org.junit.Test;

public class FieldTest extends TestJPF {

  int instInt = 42;
  double instDouble = 42.0;
  double primField = 42.0;
  Object refField = new Integer(42);
  int[] arrayField = new int[]{42};

  static int statInt = 43;

  @Test
  public void testInstanceInt() {
    if (verifyNoPropertyViolation()) {
      try {
        Class<?> cls = FieldTest.class;
        Field f = cls.getField("instInt");

        int i = f.getInt(this);
        assert i == 42;

        f.setInt(this, 43);
        assert instInt == 43;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }

  @Test
  public void testStaticInt() {
    if (verifyNoPropertyViolation()) {
      try {
        Class<?> cls = FieldTest.class;
        Field f = cls.getField("statInt");

        int i = f.getInt(this);
        assert i == 43;
        System.out.println("statInt = " + i);

        f.setInt(null, 44);
        assert statInt == 44;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }


  @Test
  public void testInstanceDouble() {
    if (verifyNoPropertyViolation()) {
      try {
        Class<?> cls = FieldTest.class;
        Field f = cls.getField("instDouble");

        double d = f.getDouble(this);
        assert d == 42.0;

        f.setDouble(this, 43.0);
        assert instDouble == 43.0;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }

  @Test
  public void testSetPrimitive() {
    if (verifyNoPropertyViolation()) {
      try {
        Class<?> cls = FieldTest.class;
        Field f = cls.getField("primField");

        double d = ((Double) f.get(this)).doubleValue();
        assert d == 42.0;

        f.set(this, new Double(43.0));
        assert primField == 43.0;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }

  @Test
  public void testSetReference() {
    if (verifyNoPropertyViolation()) {

      try {
        Class<?> cls = FieldTest.class;
        Field f = cls.getField("refField");

        Object o = f.get(this);
        assert o == refField;

        Object ob = new Double(43.0);
        f.set(this, ob);
        assert ob == refField;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }

  @Test
  public void testSetArray() {
    if (verifyNoPropertyViolation()) {

      try {
        Class<?> cls = FieldTest.class;
        Field f = cls.getField("arrayField");

        int[] a = (int[]) f.get(this);
        assert a == arrayField;

        int[] ar = new int[]{43};
        f.set(this, ar);
        assert ar == arrayField;

      } catch (Throwable t) {
        assert false : "unexpected exception: " + t;
      }
    }
  }
  
  //--- field enumeration
  interface I {
    static int I_S = 42;
  }
  
  static class X implements I {
    public int pub_x_i;  // 1
    protected int prot_x_i;
    public static int pub_x_s;  // 2
    static int prot_x_s;
  }

  static class Y extends X {
    public static int pub_y_s; // 3
    static int prot_y_s;
    public int pub_y_i;  // 4
    protected int prot_y_i;
  }

  @Test
  public void testGetFields(){
    if (verifyNoPropertyViolation()){
      Field[] publicFields = Y.class.getFields();
      String[] fnames = {"pub_x_i", "pub_x_s", "pub_y_s", "pub_y_i", "I_S"};
      
      assertTrue("wrong number of public fields", publicFields.length == fnames.length);
      
      for (Field f : publicFields){
        String fname = f.getName();
        System.out.println(fname);
        
        for (int i=0; i<fnames.length; i++){  
          if (fname.equals(fnames[i])){
            fnames[i] = null;
          }
        }
      }
      
      for (int i=0; i<fnames.length; i++){
        if (fnames[i] != null){
          fail("unseen field: " + fnames[i]);
        }
      }
    }
  }
}
