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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.junit.Test;

public class FileUtilsTest extends TestJPF {

  @Test
  public void testGetContentsAsString() throws IOException {
    File file = File.createTempFile("jpf-fileutils", ".txt");
    try {
      FileUtils.setContents(file, "hello");
      assertEquals("hello", FileUtils.getContentsAsString(file));
    } finally {
      file.delete();
    }
  }

  @Test(expected = IOException.class)
  public void testGetContentsAsStringOfDirectory() throws IOException {
    File dir = Files.createTempDirectory("jpf-fileutils").toFile();
    try {
      FileUtils.getContentsAsString(dir);
    } finally {
      dir.delete();
    }
  }
}
