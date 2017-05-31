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

package gov.nasa.jpf.test.mc.data;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;

import org.junit.Test;


/**
 * JPF regression test for JSON test object creation
 * @author Ivan Mushketik
 */
public class JSONTest extends TestJPF {
  
  class MySup {
    int j;
  }


  @Test
  public void testFillFromJSONSingleClass() {
    if (verifyNoPropertyViolation()) {
      MySup sup = Verify.createFromJSON(MySup.class, "{'j' : 123 }");

      assert sup.j == 123;
    }
  }

  class MyClass extends MySup {
    int i;
  }

  @Test
  public void testFillFromJSONInheritance() {
    if (verifyNoPropertyViolation()) {
      MyClass sup = Verify.createFromJSON(MyClass.class, "{'j':123, 'i':321 }");

      assert sup.j == 123;
      assert sup.i == 321;
    }
  }

  class Primitives {
    boolean z;
    byte b;
    short s;
    int i;
    long l;
    float f;
    double d;
  }

  @Test
  public void testFillPrivimitivesFromJSON() {
    if (verifyNoPropertyViolation()) {
      String json = "{'z': true,'b': 10,'s': 1000,'i': 321, 'l': 123456,'f': 12.34,'d': 23.45}";
      Primitives p = Verify.createFromJSON( Primitives.class, json);

      assert p.z == true;
      assert p.b == 10;
      assert p.s == 1000;
      assert p.i == 321;
      assert p.l == 123456;
      assertEquals(12.34, p.f, 0.001);
      assertEquals(23.45, p.d, 0.001);
    }
  }

  class IntArr {
    int ints[];
  }

  @Test
  public void testFillIntArrayFromJSON() {
    if (verifyNoPropertyViolation()) {
      IntArr ia = Verify.createFromJSON( IntArr.class, "{'ints': [1, 2, 3]}");

      assert ia.ints[0] == 1;
      assert ia.ints[1] == 2;
      assert ia.ints[2] == 3;
    }
  }

  class Boxed {
    Boolean t;
    Boolean f;
    Byte b;
    Short s;
    Integer i;
    Long l;
    Float fl;
    Double d;
  }

  @Test
  public void testFillBoxedPrimitivesFromJSON() {
    if (verifyNoPropertyViolation()) {
      String json = "{'t':true, 'f':false, 'b':10, 's':1000,'i':321, 'l':123456, 'fl':12.34, 'd':23.45 }";
      Boxed b = Verify.createFromJSON( Boxed.class, json);

      assert b.t == true;
      assert b.f == false;
      assert b.b == 10;
      assert b.s == 1000;
      assert b.i == 321;
      assert b.l == 123456;
      assertEquals(12.34, b.fl, 0.001);
      assertEquals(23.45, b.d, 0.001);
    }
  }

  class PrimitiveArrays {
    boolean bools[];
    byte bytes[];
    short shorts[];
    int ints[];
    long longs[];
    float floats[];
    double doubles[];
  }

  @Test
  public void testFillPrimitiveArrays() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"bools\" : [true, false, true],"
              + "\"bytes\" : [-40, -30, -20],"
              + "\"shorts\" : [2, 3, 4],"
              + "\"ints\" : [1, 2, 3],"
              + "\"longs\" : [1000, 2000, 3000],"
              + "\"floats\" : [12.34, 23.45, 34.56],"
              + "\"doubles\" : [-12.34, -23.45, -34.56]"
              + "}";
      PrimitiveArrays pa = Verify.createFromJSON( PrimitiveArrays.class, json);

      assert pa.bools[0] == true;
      assert pa.bools[1] == false;
      assert pa.bools[2] == true;

      assert pa.bytes[0] == -40;
      assert pa.bytes[1] == -30;
      assert pa.bytes[2] == -20;

      assert pa.shorts[0] == 2;
      assert pa.shorts[1] == 3;
      assert pa.shorts[2] == 4;

      assert pa.ints[0] == 1;
      assert pa.ints[1] == 2;
      assert pa.ints[2] == 3;

      assert pa.longs[0] == 1000;
      assert pa.longs[1] == 2000;
      assert pa.longs[2] == 3000;

      assertEquals(12.34, pa.floats[0], 0.0001);
      assertEquals(23.45, pa.floats[1], 0.0001);
      assertEquals(34.56, pa.floats[2], 0.0001);

      assertEquals(-12.34, pa.doubles[0], 0.0001);
      assertEquals(-23.45, pa.doubles[1], 0.0001);
      assertEquals(-34.56, pa.doubles[2], 0.0001);
    }
  }

  class InnerClass {
    int i;
  }

  class OuterClass {
    long l;
    InnerClass ic;
  }

  @Test
  public void testInnerClassFilling() {
    if (verifyNoPropertyViolation()) {
      String json =
                "{"
              +   "'l' : 1234,"
              +   "'ic' : {"
              +      "'i' : 4321"
              +   "}"
              + "}";

      OuterClass oc = Verify.createFromJSON( OuterClass.class, json);

      assert oc.l == 1234;
      assert oc.ic.i == 4321;
    }
  }

  @Test
  public void testFillingWhenInnerClassIsNull() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"l\" : 1234,"
              + "\"ic\" : null"
              + "}";

      OuterClass oc = Verify.createFromJSON( OuterClass.class, json);

      assert oc.l == 1234;
      assert oc.ic == null;
    }
  }

  class MultiArray {
    int intsInts[][];
  }

  @Test
  public void testMultiArrayFilling() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"intsInts\" : [[1, 2, 3], [4, 5, 6]]"
              + "}";

      MultiArray ma = Verify.createFromJSON( MultiArray.class, json);

      assert ma.intsInts[0][0] == 1;
      assert ma.intsInts[0][1] == 2;
      assert ma.intsInts[0][2] == 3;

      assert ma.intsInts[1][0] == 4;
      assert ma.intsInts[1][1] == 5;
      assert ma.intsInts[1][2] == 6;
    }
  }

  class BoxIntsArr {
    Integer ints[];
  }

  @Test
  public void testBoxedTypesArrayFilling() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"ints\" : [1, 2, 3]"
              + "}";

      BoxIntsArr bia = Verify.createFromJSON( BoxIntsArr.class, json);

      assert bia.ints[0] == 1;
      assert bia.ints[1] == 2;
      assert bia.ints[2] == 3;
    }
  }

  static class IC {
    int i;
  }

  static class ArrayOfObjects {
    IC cls[];
  }

  @Test
  public void testArrayOfObjectsFilling() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"cls\" : [{\"i\" : 1}, {\"i\" : 2}, {\"i\" : 3}]"
              + "}";

      ArrayOfObjects aoo = Verify.createFromJSON( ArrayOfObjects.class, json);

      assert aoo.cls[0].i == 1;
      assert aoo.cls[1].i == 2;
      assert aoo.cls[2].i == 3;
    }
  }

  class MultObjectsArr {
    IC cls[][];
  }

  @Test
  public void testFillingMultArrayOfObjects() {
      if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"cls\" : ["
              + "[{\"i\" : 1}, {\"i\" : 2}, {\"i\" : 3}],"
              + "[{\"i\" : 4}, {\"i\" : 5}, {\"i\" : 6}],"
              + "[{\"i\" : 7}, {\"i\" : 8}, {\"i\" : 9}]"
              + "]"
              + "}";

      MultObjectsArr moa = Verify.createFromJSON( MultObjectsArr.class, json);

      assert moa.cls[0][0].i == 1;
      assert moa.cls[0][1].i == 2;
      assert moa.cls[0][2].i == 3;

      assert moa.cls[1][0].i == 4;
      assert moa.cls[1][1].i == 5;
      assert moa.cls[1][2].i == 6;

      assert moa.cls[2][0].i == 7;
      assert moa.cls[2][1].i == 8;
      assert moa.cls[2][2].i == 9;
    }
  }

  class ClassWithString {
    String s1;
    String s2;
  }

  @Test
  public void testFillStringValue() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "\"s1\" : \"val\","
              + "\"s2\" : null"
              + "}";

      ClassWithString cws = Verify.createFromJSON( ClassWithString.class, json);

      assert cws.s1.equals("val") == true;
      assert cws.s2 == null;
    }
  }

  // --- CG Tests

  class Bool {
    Bool(boolean b) {this.b = b;}
    boolean b;

    @Override
	public boolean equals(Object o) {
      Bool bool = (Bool) o;
      return this.b == bool.b;
    }
  }

  static void checkValue(Object[] expected, Object curVal) {
    for (int i = 0; i < expected.length; i++) {
      if (curVal.equals(expected[i])) {
        Verify.setBitInBitSet(0, i, true);
        break;
      }
    }

    Verify.incrementCounter(0);

    if (Verify.getCounter(0) == expected.length) {
      for (int i = 0; i < expected.length; i++) {
        assert Verify.getBitInBitSet(0, i) == true;
      }
    }
  }

  @Test
  public void testSetBoolFromCG() {
    if (verifyNoPropertyViolation()) {      
      String json = "{"
              + "'b' : TrueFalse()"
              + "}";

      Object[] expected = {
        new Bool(true),
        new Bool(false)
      };
      Bool bb = Verify.createFromJSON(Bool.class, json);      
      checkValue(expected, bb);
    }
  }

  class ByteShortIntLong {

    public ByteShortIntLong(int b, int s, int i, long l) {
      this.b = (byte) b; this.s = (short) s; this.i = i; this.l = l;
    }

    byte b; short s; int i; long l;

    @Override
	public boolean equals(Object o) {
      ByteShortIntLong bs = (ByteShortIntLong) o;

      return bs.b == b && bs.s == s && bs.i == i && bs.l == l;
    }
  }

  @Test
  public void testSetByteShortIntFromCG() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "'b' : IntSet(1, 2),"
              + "'s' : 2,"
              + "'i' : IntSet(3, 4, 5),"
              + "'l' : IntSet(8)"
              + "}";

      Object[] expected = {
        new ByteShortIntLong(1, 2, 3, 8), new ByteShortIntLong(2, 2, 3, 8),
        new ByteShortIntLong(1, 2, 4, 8), new ByteShortIntLong(2, 2, 4, 8),
        new ByteShortIntLong(1, 2, 5, 8), new ByteShortIntLong(2, 2, 5, 8),
      };
      ByteShortIntLong bsil = Verify.createFromJSON(ByteShortIntLong.class, json);
      checkValue(expected, bsil);
    }
  }

  @Test
  public void testFillWithIntevalCG() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "'b' : 1,"
              + "'s' : IntInterval(1, 3),"
              + "'i' : 1,"
              + "'l' : IntInterval(8, 10)"
              + "}";

      Object[] expected = {
        new ByteShortIntLong(1, 1, 1, 8), new ByteShortIntLong(1, 2, 1, 8), new ByteShortIntLong(1, 3, 1, 8),
        new ByteShortIntLong(1, 1, 1, 9), new ByteShortIntLong(1, 2, 1, 9), new ByteShortIntLong(1, 3, 1, 9),
        new ByteShortIntLong(1, 1, 1, 10), new ByteShortIntLong(1, 2, 1, 10), new ByteShortIntLong(1, 3, 1, 10),};
      ByteShortIntLong bsil = Verify.createFromJSON(ByteShortIntLong.class, json);
      checkValue(expected, bsil);
    }
  }

  class I {
    int i;
  }

  class O {
    I inner;
    public O(int i) {
      inner = new I();
      inner.i = i;
    }

    @Override
	public boolean equals(Object o) {
      O outer = (O) o;

      return outer.inner.i == this.inner.i;
    }
  }

  @Test
  public void testFillInnerClassCG() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "'inner' : {"
                + "'i' : IntSet(3, 4, 5)"
                + "}"
              + "}";

      Object[] expected = {
        new O(3), new O(4), new O(5),
      };
      O bsil = Verify.createFromJSON(O.class, json);
      checkValue(expected, bsil);
    }
  }

  class ArrI {
    I[] arr;

    ArrI(int... ints) {
      arr = new I[ints.length];
      for (int i = 0; i < ints.length; i++) {arr[i] = new I(); arr[i].i = ints[i];}
    }

    @Override
	public boolean equals(Object o) {
      ArrI other = (ArrI) o;

      if (other.arr.length != this.arr.length) {
        return false;
      }
      for (int i = 0; i < this.arr.length; i++) {
        if (this.arr[i].i != other.arr[i].i) {
          return false;
        }
      }
      return true;
    }
  }

  @Test
  public void testFillingObjectInArrayWithCG() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "'arr' : [ {'i' : IntSet(1, 2, 3)}, {'i' : IntSet(4, 5, 6)}]"
              + "}";

      Object[] expected = {
        new ArrI(1, 4), new ArrI(2, 4), new ArrI(3, 4),
        new ArrI(1, 5), new ArrI(2, 5), new ArrI(3, 5),
        new ArrI(1, 6), new ArrI(2, 6), new ArrI(3, 6),
      };
      ArrI arri = Verify.createFromJSON(ArrI.class, json);
      checkValue(expected, arri);
    }
  }

  class BoxedInteger {
    Integer bi;

    BoxedInteger(Integer newI) {
      bi = newI;
    }

    @Override
    public boolean equals(Object obj) {
      BoxedInteger bic = (BoxedInteger) obj;
      return this.bi.equals(bic.bi);
    }
  }

  @Test
  public void testObjectFromCG() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "'bi' : IntSet(1, 2, 3)"
              + "}";

      Object[] expected = {
        new BoxedInteger(1), new BoxedInteger(2), new BoxedInteger(3),
      };
      BoxedInteger bi = Verify.createFromJSON(BoxedInteger.class, json);
      checkValue(expected, bi);
    }
  }

  class BoxedDouble {
    Double d;

    public BoxedDouble(Double d) {
      this.d = d;
    }

    @Override
	public boolean equals(Object o) {
      BoxedDouble bd = (BoxedDouble) o;

      return doublesEqual(bd.d, this.d);
    }

    boolean doublesEqual(double d1, double d2) {
      double diff = 0.001;

      return Math.abs(d1 - d2) <= diff;
    }
  }

  @Test
  public void testBoxedDoubleFromCG() {
    if (verifyNoPropertyViolation()) {
      String json = "{"
              + "'d' : DoubleSet(1.1, 2.2, 3.3)"
              + "}";

      Object[] expected = {
        new BoxedDouble(1.1), new BoxedDouble(2.2), new BoxedDouble(3.3),
      };
      BoxedDouble bd = Verify.createFromJSON(BoxedDouble.class, json);
      checkValue(expected, bd);
    }
  }

  
}


