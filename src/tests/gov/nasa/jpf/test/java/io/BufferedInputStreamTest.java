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
package gov.nasa.jpf.test.java.io;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * raw test for java.io.BufferedInputStream
 */
public class BufferedInputStreamTest extends TestJPF {

  @Before
  public void setUp() {
    System.out.println("setUp() creating test file");
    createTestFile();
  }

  @After
  public void tearDown() {
    System.out.println("setUp() deleting test file");
    deleteTestFile();
  }

  public static void createTestFile() {
    try {
      FileOutputStream fo = new FileOutputStream(testFile);
      fo.write(TEST_DATA);
      fo.close();
    } catch (Throwable t) {
      throw new RuntimeException("failed to create test file", t);
    }
  }

  public static void deleteTestFile() {
    if (testFile.exists()) {
      testFile.delete();
    }
  }

  //--- the tests
  static File testFile = new File("__test__");
  static final byte[] TEST_DATA = {42, 42, 42};

  @Test
  public void testSimpleRead() {
    if (verifyNoPropertyViolation()) {
      try {
        FileInputStream fis = new FileInputStream(testFile);
        BufferedInputStream bis = new BufferedInputStream(fis);
        int n = bis.available();

        assert n == TEST_DATA.length : "wrong available count: " + n;

        for (int i = 0; i < n; i++) {
          int d = bis.read();
          System.out.print(d);
          System.out.print(',');
          assert d == TEST_DATA[i] : "wrong read data";
        }
        System.out.println();

        bis.close();

      } catch (Throwable t) {
        assert false : "BufferedInputStream test failed: " + t;
      }
    }
  }
}
