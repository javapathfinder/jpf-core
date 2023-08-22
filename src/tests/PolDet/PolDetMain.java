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
import org.junit.internal.TextListener;
import org.junit.runner.Description;
import org.junit.runner.JUnitCore;
import org.junit.runner.notification.RunListener;
import org.junit.runner.Result;


/**
 *
 * entry class for PolDet implementation in JPF. Run gradle task 'runPolDet' to execute PolDet@JPF.
 *
 * Two parameters are required for the gradle task 'runPolDet':
 * 1. testClasspath: the classpath to the test class
 * 2. testClass: the fully qualified name of the test class
 *
 * given the fully qualified names of JUnit test classes, output the tests that pollute the shared state
 *
 * @author Pu Yi
 */
public class PolDetMain {

  public static void main (String... args) throws Exception {
    if ("".equals(args[0])) return;
    JUnitCore core = new JUnitCore();
    core.addListener(new PolDetListener());
    core.addListener(new TextListener(System.out));
    for (String testClass : args) {
      core.run(Class.forName(testClass));
    }
  }
}

class PolDetListener extends RunListener {
  public native static void capturePreState();
  public native static boolean compareStates();

  int polluterCount;

  public void testRunStarted (Description description) {
    polluterCount = 0;
  }

  public void testStarted (Description description) {
    capturePreState();
  }

  public void testFinished (Description description) {
    if (!compareStates()) {
      System.out.println(description.getClassName() + "#" + description.getMethodName() + " pollutes the state");
      polluterCount++;
    }
  }

  public void testRunFinished (Result result) {
    System.out.println("Number of tests checked: " + result.getRunCount());
    System.out.println("Number of tests that pollute the state: " + polluterCount);
  }
}
