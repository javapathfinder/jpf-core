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

package gov.nasa.jpf.test.basic;

import gov.nasa.jpf.util.TypeRef;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

/**
 * model class for MJI test
 */
public class MJITest extends TestJPF {

  static int sdata;

  static {
    // only here to be intercepted
    sdata = 0; // dummy insn required for the Eclipse compiler (skips empty methods)
  }

  int idata = 0;

  public MJITest () {
    // not intercepted
  }

  MJITest (int data) {
    // only here to be intercepted
  }

  @Test
  public void testNativeClInit () {
    if (verifyNoPropertyViolation()){
      assert (sdata == 42) : "native '<clinit>' failed";
    }
  }

  @Test
  public void testNativeInit () {
    if (verifyNoPropertyViolation()){
      MJITest t = new MJITest(42);
      assert (t.idata == 42)  : "native '<init>' failed";
    }
  }

  @Test
  public void testNativeCreate2DimIntArray () {
    if (verifyNoPropertyViolation()){
      int[][] a = nativeCreate2DimIntArray(2, 3);

      assert (a != null) : "native int[][]  creation failed: null";

      assert (a.getClass().isArray()) : "native int[][] creation failed: not an array";

      assert (a.getClass().getComponentType().getName().equals("[I")) :
              "native int[][] creation failed: wrong component type";

      assert ((a[1][1] == 42)) : "native int[][] element init failed";
    }
  }

  @Test
  public void testNativeCreateIntArray () {
    if (verifyNoPropertyViolation()){
      int[] a = nativeCreateIntArray(3);

      assert (a != null) : "native int array creation failed: null";

      assert (a.getClass().isArray()) : "native int array creation failed: not an array";

      assert (a.getClass().getComponentType() == int.class) :
              "native int array creation failed: wrong component type";

      assert ((a[1] == 1)) : "native int array element init failed";
    }
  }

  @Test
  public void testNativeCreateStringArray () {
    if (verifyNoPropertyViolation()){
      String[] a = nativeCreateStringArray(3);

      assert (a != null) : "native String array creation failed: null";

      assert (a.getClass().isArray()) :
              "native String array creation failed: not an array";

      assert (a.getClass().getComponentType() == String.class) :
              "native String array creation failed: wrong component type";

      assert ("one".equals(a[1])) : "native String array element init failed";
    }
  }

  @Test
  public void testNativeException () {
    if (verifyNoPropertyViolation()){
      try {
        nativeException();
      } catch (UnsupportedOperationException ux) {
        String details = ux.getMessage();

        if ("caught me".equals(details)) {
          ux.printStackTrace();
          return;
        } else {
          assert false : "wrong native exception details: " + details;
        }
      } catch (Throwable t) {
        assert false : "wrong native exception type: " + t.getClass();
      }

      assert false : "no native exception thrown";
    }
  }

  @Test
  public void testNativeCrash () {
    if (verifyJPFException(new TypeRef("gov.nasa.jpf.JPFNativePeerException"))){
      nativeCrash();
    }
  }

  @Test
  public void testNativeInstanceMethod () {
    if (verifyNoPropertyViolation()){
      int res = nativeInstanceMethod(2.0, '?', true, 40);

      assert (res == 42) : "native instance method failed: " + res;
    }
  }

  @Test
  public void testNativeStaticMethod () {
    if (verifyNoPropertyViolation()){
      long res = nativeStaticMethod(40, "Blah");

      assert (res == 42L) : "native static method failed";
    }
  }


  int roundtrip (int a){ // that's called from the native testRoundtripLoop0
    System.out.println("### roundtrip " + a);
    return nativeInnerRoundtrip(a);
  }

  @Test
  public void testRoundtripLoop () {
    if (verifyNoPropertyViolation()){
      int res = nativeRoundtripLoop(42);

      assert (res == 54) : ("roundtrip loop failed (expected 54) : " + res);
    }
  }

  @Test
  public void testHiddenRoundtrip () {
    if (verifyNoPropertyViolation()){
      System.out.println("## entering testHiddenroundtrip()");
      int res = echo(20) + nativeHiddenRoundtrip(21); // get something on the operand stack
      assert (res == 42) : ("atomic roundtrip failed (expected 42): " + res);

      System.out.println("## exiting testHiddenroundtrip()");
    }
  }

  @Test
  public void testHiddenRoundtripException () {
    if (verifyNoPropertyViolation()){
      System.out.println("## entering testHiddenroundtripException()");
      int res = echo(20) + nativeHiddenRoundtrip(-1); // get something on the operand stack
      assert (res == 19) : "atomic roundtrip exception not caught";

      System.out.println("## exiting testHiddenroundtripException()");
    }
  }

  int atomicStuff (int a) {  // this is called from nativeAtomicRoundtrip()
    System.out.print("## in atomicStuff : ");
    System.out.println(a);

    if (a < 0) {
      System.out.println("## atomicStuff throwing IllegalArgumentException");
      throw new IllegalArgumentException("negative atomicStuff argument");
    }

    int res = echo(a + 1);
    return res;
  }
  int echo (int a) {
    System.out.print("## in echo : ");
    System.out.println(a);
    return a;
  }

  native int nativeHiddenRoundtrip (int a);

  native int nativeInnerRoundtrip (int a);

  native int nativeRoundtripLoop (int a);

  native int[][] nativeCreate2DimIntArray (int s1, int s2);

  native int[] nativeCreateIntArray (int size);

  native String[] nativeCreateStringArray (int size);

  native void nativeException ();

  native int nativeCrash ();

  native int nativeInstanceMethod (double d, char c, boolean b, int i);

  native long nativeStaticMethod (long l, String s);

}
