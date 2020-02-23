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

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;

import org.junit.Test;

/**
 * regression test for object streams
 */
public class PrintStreamTest extends TestJPF {

  @Test 
  public void testPrintCharFormat () {
    if (verifyNoPropertyViolation()){
	ByteArrayOutputStream baos = new ByteArrayOutputStream(1);
	PrintStream baps = new PrintStream(baos, true);
	baps.printf("%c", 'a'); 
	assert (baos.toByteArray()[0] == 97);
    }
  }
}