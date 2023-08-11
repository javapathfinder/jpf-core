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

package gov.nasa.jpf.vm;

import gov.nasa.jpf.jvm.JVMStackFrame;
import gov.nasa.jpf.util.test.TestJPF;
import java.lang.reflect.Modifier;

import org.junit.Test;

/**
 * unit test for StackFrame operations
 */
public class StackFrameTest extends TestJPF {

	@Test
	public void testReplaceLocalAttr() {

		MethodInfo mInfo = new MethodInfo("[methodName]", "()V", Modifier.PUBLIC, 2, 10);
		JVMStackFrame frame = new JVMStackFrame(mInfo);
		// Initialize local values and the stack frame
		frame.push(1);

		String obj1 = "Attribute1";
		Class<?> attrType = obj1.getClass();
		frame.addLocalAttr(0, obj1);

		final Object oldAttr = frame.getLocalAttr(0, attrType);
		assertTrue(oldAttr != null && oldAttr == obj1);

		String obj2 = "Attribute2";
		frame.replaceLocalAttr(0, oldAttr, obj2);

		final Object newAttr = frame.getLocalAttr(0, attrType);
		assertTrue(newAttr != null && newAttr == obj2);
	}
	
	public static void main(String[] args) {
		runTestsOfThisClass(null);
	}
}
