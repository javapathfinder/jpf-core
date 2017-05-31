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

import gov.nasa.jpf.util.FileUtils;
import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.io.IOException;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author proger
 */
public class FileTest extends TestJPF {

  public FileTest() {
  }

  @BeforeClass
  public static void setUpClass() throws Exception {
    // Creating sandbox for java.io.File testing
    File subdirs = new File("fileSandbox/parent/child");
    if (!subdirs.mkdirs())
      throw new RuntimeException("Unable to create sandbox directories");
  }

  @AfterClass
  public static void tearDownClass() throws Exception {
    if (!FileUtils.removeRecursively(new File("fileSandbox")))
      throw new RuntimeException("Unable to remove sandbox directories");
  }

  @Before
  public void setUp() {
  }

  @After
  public void tearDown() {
  }

  @Test
  public void testGetParentFile() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/parent");
      File expectedParent = new File("fileSandbox");
      File resultParent = file.getParentFile();
      
      assert expectedParent.equals(resultParent) == true;
    }
  }

  @Test
  public void testGetCanonical() throws IOException {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox/../fileSandbox/../fileSandbox");
      File root = new File(".");

      File expectedCanonical = new File(root, "fileSandbox").getCanonicalFile();
      File resultCanonical = file.getCanonicalFile();
      assert expectedCanonical.equals(resultCanonical) == true;

      String expectedCanonicalName = expectedCanonical.getCanonicalPath();
      String resultCanonicalName = resultCanonical.getCanonicalPath();
      assert expectedCanonicalName.equals(resultCanonicalName) == true;      
    }
  }

  @Test
  public void testEquals() {
    if (verifyNoPropertyViolation()) {
      File file = new File("fileSandbox");
      File sameFile = new File("fileSandbox");
      File otherFile = new File("fileSandbox/parent");

      assert file.equals(file) == true;
      assert file.equals(sameFile) == true;
      assert file.equals(otherFile) == false;
      assert file.equals(null) == false;
      assert file.equals(new Object()) == false;
    }
  }
  
}
