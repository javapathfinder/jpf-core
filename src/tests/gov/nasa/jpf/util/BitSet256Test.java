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

import java.util.BitSet;
import java.util.Random;

import org.junit.Test;

/**
 * unit test for gov.nasa.jpf.util.BitSet
 */
public class BitSet256Test extends TestJPF {

  public static void main (String[] args){

    // our performance evals
    if (args.length == 1){
      String mthName = args[0];
      if (mthName.equals("evalBitSet")){
        evalBitSet();
        return;
      } else if (mthName.equals("evalBitSet256")){
        evalBitSet256();
        return;
      }
    }

    // the regression tests
    runTestsOfThisClass(args);
  }

  //--- regression tests

  @Test
  public void testBasic() {
    BitSet256 b = new BitSet256();

    assert b.isEmpty();
    assert !b.get(0);

    b.set(0);
    assert b.get(0);

    b.set(63);
    assert b.get(63);

    b.set(127);
    assert b.get(127);

    b.set(128);
    assert b.get(128);

    b.set(191);
    assert b.get(191);

    b.set(192);
    assert b.get(192);

    b.set(255);
    assert b.get(255);

    b.clear(255);

    assert b.cardinality() == 6;
    assert b.length() == 193;

    b = new BitSet256();
    b.set(4);
    b.set(4,false);
    assert !b.get(4);

  }

  @Test
  public void testEnumeration() {
    BitSet256 b = new BitSet256();

    assert b.nextSetBit(0) == -1;

    b.set(0);
    assert b.nextSetBit(0) == 0;

    b.clear();
    b.set(63);
    assert b.nextSetBit(0) == 63;

    b.clear();
    b.set(64);
    assert b.nextSetBit(0) == 64;

    b.clear();
    b.set(0);
    b.set(42);
    b.set(10);
    b.set(255);
    b.set(200);

    assert b.nextSetBit(0) == 0;
    assert b.nextSetBit(1) == 10;
    assert b.nextSetBit(11) == 42;
    assert b.nextSetBit(43) == 200;
    assert b.nextSetBit(201) == 255;
  }

  @Test
  public void testOutOfBounds() {
    BitSet256 b = new BitSet256();

    try {
      b.set(2264);
      throw new AssertionError("set(2264) failed to throw");
    } catch (IndexOutOfBoundsException x){
      System.out.println(x);
    }

    try {
      b.get(256);
      throw new AssertionError("get(256) failed to throw");
    } catch (IndexOutOfBoundsException x){
      System.out.println(x);
    }

    try {
      b.clear(-1);
      throw new AssertionError("clear(-1) failed to throw");
    } catch (IndexOutOfBoundsException x){
      System.out.println(x);
    }
  }


  //--- performance section

  static final int NROUNDS = 2000000;
  static final int NITER = 500000;

  public static void evalBitSet() {
    Random r = new Random(0);
    BitSet b = new BitSet(256);

    long t1 = System.currentTimeMillis();
    for (int j=0; j<NROUNDS; j++){
      int i = r.nextInt(256);
      b.set(i);
      b.get(i);
      b.clear(i);
    }
    long t2 = System.currentTimeMillis();
    System.out.println("BitSet random access: " + (t2-t1));

    b.clear();
    b.set(0);
    b.set(42);
    b.set(10);
    b.set(232);
    b.set(200);

    t1 = System.currentTimeMillis();
    for (int j=0; j<NROUNDS; j++){
      for (int k=b.nextSetBit(0); k>=0; k=b.nextSetBit(k+1));
      int n = b.cardinality();
    }
    t2 = System.currentTimeMillis();
    System.out.println("BitSet set bits iteration: " + (t2-t1));
  }

  public static void evalBitSet256() {
    Random r = new Random(0);
    BitSet256 b = new BitSet256();

    long t1 = System.currentTimeMillis();
    for (int j=0; j<NROUNDS; j++){
      int i = r.nextInt(256);
      b.set(i);
      b.get(i);
      b.clear(i);
    }
    long t2 = System.currentTimeMillis();
    System.out.println("BitSet random access: " + (t2-t1));

    b.clear();
    b.set(0);
    b.set(42);
    b.set(10);
    b.set(232);
    b.set(200);

    t1 = System.currentTimeMillis();
    for (int j=0; j<NROUNDS; j++){
      for (int k=b.nextSetBit(0); k>=0; k=b.nextSetBit(k+1));
      int n = b.cardinality();
    }
    t2 = System.currentTimeMillis();
    System.out.println("BitSet set bits iteration: " + (t2-t1));
  }
}
