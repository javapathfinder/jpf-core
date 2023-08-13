/*
 * Copyright (C) 2021 Pu Yi
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You can find a copy of the GNU General Public License at
 * <http://www.gnu.org/licenses/>.
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
