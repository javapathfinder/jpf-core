package gov.nasa.jpf.test.java.util;

import java.util.Arrays;

import org.junit.Test;

import gov.nasa.jpf.util.test.TestJPF;

/**
 * Test for host JVM Array class operations.
 */
public class ArraysTest extends TestJPF {
    @Test
    public void testLongArrayEquals() {
        if (verifyNoPropertyViolation()) {
            long[] arr1 = {1, 2};
            long[] arr2 = {1, 2};
        
            assertTrue(Arrays.equals(arr1, arr2));
        }
    }

    @Test
    public void testIntArrayEquals() {
      if (verifyNoPropertyViolation()) {
        int[] arr1 = {1, 2};
        int[] arr2 = {1, 2};

        assertTrue(Arrays.equals(arr1, arr2));
      }
    } 

    @Test
    public void testFloatArrayEquals() {
      if (verifyNoPropertyViolation()) {
        float[] arr1 = {1, 2};
        float[] arr2 = {1, 2};

        assertTrue(Arrays.equals(arr1, arr2));
      }
    }

    @Test
    public void testDoubleArrayEquals() {
      if (verifyNoPropertyViolation()) {
        double[] arr1 = {1.0, 2.0};
        double[] arr2 = {1.0, 2.0};

        assertTrue(Arrays.equals(arr1, arr2));
      }
    }

    @Test
    public void testCharArrayEquals() {
      if (verifyNoPropertyViolation()) {
        char[] arr1 = {'a', 'b'};
        char[] arr2 = {'a', 'b'};

        assertTrue(Arrays.equals(arr1, arr2));
      }
    }
}