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
package gov.nasa.jpf.util.test;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.Error;
import gov.nasa.jpf.JPF;
import gov.nasa.jpf.JPFShell;
import gov.nasa.jpf.Property;
import gov.nasa.jpf.annotation.FilterField;
import gov.nasa.jpf.tool.RunTest;
import gov.nasa.jpf.util.DevNullPrintStream;
import gov.nasa.jpf.util.TypeRef;
import gov.nasa.jpf.util.JPFSiteUtils;
import gov.nasa.jpf.util.Reflection;
import gov.nasa.jpf.vm.ExceptionInfo;
import gov.nasa.jpf.vm.NoUncaughtExceptionsProperty;
import gov.nasa.jpf.vm.NotDeadlockedProperty;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.lang.annotation.Annotation;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

/**
 * base class for JPF unit tests. TestJPF mostly includes JPF invocations
 * that check for occurrence or absence of certain execution results
 *
 * This class can be used in two modes:
 *
 * <ol>
 * <li> wrapping a number of related tests for different SuTs into one class
 * (suite) that calls the various JPF runners with complete argument lists
 * (as in JPF.main(String[]args)) </li>
 *
 * <li> derive a class from TestJPF that uses the "..This" methods, which in
 * turn use reflection to automatically append the test class and method to the
 * JPF.main argument list (based on the calling class / method names). Note that
 * you have to obey naming conventions for this to work:
 *
 * <li> the SuT class has to be the same as the test class without "Test", e.g.
 * "CastTest" -> "Cast" </li>
 *
 * <li> the SuT method has to have the same name as the @Test method that
 * invokes JPF, e.g. "CastTest {.. @Test void testArrayCast() ..}" ->
 * "Cast {.. void testArrayCast()..} </li>
 * </ol>
 */
public abstract class TestJPF implements JPFShell  {
  static PrintStream out = System.out;

  public static final String UNNAMED_PACKAGE = "";
  public static final String SAME_PACKAGE = null;

  //--- those are only used outside of JPF execution
  @FilterField protected static boolean globalRunDirectly, globalShowConfig;

  @FilterField protected static boolean runDirectly; // don't run test methods through JPF, invoke it directly
  @FilterField protected static boolean stopOnFailure; // stop as soon as we encounter a failed test or error
  @FilterField protected static boolean showConfig; // for debugging purposes
  @FilterField protected static boolean showConfigSources; // for debugging purposes  
  @FilterField protected static boolean hideSummary;
  
  @FilterField protected static boolean quiet; // don't show test output
  
  @FilterField protected String sutClassName;

  static class GlobalArg {
    String key;
    String val;

    GlobalArg (String k, String v){
      key = k;
      val = v;
    }
  }

  // it seems wrong to pull globalArgs here instead of setting it from
  // RunTest, but RunTest has to make sure TestJPF is loaded through the
  // JPFClassLoader, i.e. cannot directly reference this class.

  @FilterField static ArrayList<GlobalArg> globalArgs;

  protected static ArrayList<GlobalArg> getGlobalArgs() {
    // NOTE - this is only set if we execute tests from build.xml
    Config globalConf = RunTest.getConfig();
    if (globalConf != null){
      ArrayList<GlobalArg> list = new ArrayList<GlobalArg>();

      //--- the "test.<key>" specs
      String[] testKeys = globalConf.getKeysStartingWith("test.");
      if (testKeys.length > 0){
        for (String key : testKeys){
          String val = globalConf.getString(key);
          // <2do> this is a hack to avoid the problem of not being able to store
          // empty/nil/null values in the global config (they are removed during global config init)
          if (val.equals("REMOVE")){
            val = null;
          }
          
          key = key.substring(5);
          
          list.add(new GlobalArg(key,val));
        }
      }

      return list;
    }

    return null;
  }

  static {
    if (!isJPFRun()){
      globalArgs = getGlobalArgs();
    }
  }

  //--- internal methods

  public static void fail (String msg, String[] args, String cause){
    StringBuilder sb = new StringBuilder();

    sb.append(msg);
    if (args != null){
      for (String s : args){
        sb.append(s);
        sb.append(' ');
      }
    }

    if (cause != null){
      sb.append(':');
      sb.append(cause);
    }

    fail(sb.toString());
  }

  public static void fail (){
    throw new AssertionError();
  }

  public static void fail (String msg){
    throw new AssertionError(msg);
  }

  public void report (String[] args) {
    if (!quiet){
      out.print("  running jpf with args:");

      for (int i = 0; i < args.length; i++) {
        out.print(' ');
        out.print(args[i]);
      }

      out.println();
    }
  }

  /**
   * compute the SuT class name for a given JUnit test class: remove
   * optionally ending "..Test", and replace package (if specified)
   * 
   * @param testClassName the JUnit test class
   * @param sutPackage optional SuT package name (without ending '.', null
   * os SAME_PACKAGE means same package, "" or UNNAMED_PACKAGE means unnamed package)
   * @return main class name of system under test
   */
  protected static String getSutClassName (String testClassName, String sutPackage){

    String sutClassName = testClassName;

    int i = sutClassName.lastIndexOf('.');
    if (i >= 0){  // testclass has a package

      if (sutPackage == null){   // use same package
        // nothing to do
      } else if (sutPackage.length() > 0) { // explicit sut package
        sutClassName = sutPackage + sutClassName.substring(i);

      } else { // unnamed sut package
        sutClassName = sutClassName.substring(i+1);
      }

    } else { // test class has no package
      if (sutPackage == null || sutPackage.length() == 0){   // use same package
        // nothing to do
      } else { // explicit sut package
        sutClassName = sutPackage + '.' + sutClassName;
      }
    }

    if (sutClassName.endsWith("JPF")) {
      sutClassName = sutClassName.substring(0, sutClassName.length() - 3);
    }

    return sutClassName;
  }

  // we can't set the sutClassName only from main() called methods (like
  // runTestsOfThisClass()) since main() doesn't get called if this is executed
  // by Ant (via <junit> task)
  // the default ctor is always executed
  public TestJPF () {
    sutClassName = getSutClassName(getClass().getName(), SAME_PACKAGE);
  }



  //------ the API to be used by subclasses

  /**
   * to be used from default ctor of derived class if the SuT is in a different
   * package
   * @param sutClassName the qualified SuT class name to be checked by JPF
   */
  protected TestJPF (String sutClassName){
    this.sutClassName = sutClassName;
  }

  public static boolean isJPFRun () {
    return false;
  }

  public static boolean isJUnitRun() {
    // intercepted by native peer if this runs under JPF
    Throwable t = new Throwable();
    t.fillInStackTrace();

    for (StackTraceElement se : t.getStackTrace()){
      if (se.getClassName().startsWith("org.junit.")){
        return true;
      }
    }

    return false;
  }

  public static boolean isRunTestRun() {
    // intercepted by native peer if this runs under JPF
    Throwable t = new Throwable();
    t.fillInStackTrace();

    for (StackTraceElement se : t.getStackTrace()){
      if (se.getClassName().equals("gov.nasa.jpf.tool.RunTest")){
        return true;
      }
    }

    return false;
  }


  protected static void getOptions (String[] args){
    runDirectly = globalRunDirectly;
    showConfig = globalShowConfig;

    // hideSummary and stopOnFailure only make sense as global options anyways

    if (args != null){   
      for (int i=0; i<args.length; i++){
        String a = args[i];
        if (a != null){
          if (a.length() > 0){
            if (a.charAt(0) == '-'){
              a = a.substring(1);
              
              if (a.equals("d")){
                runDirectly = true;
              } else if (a.equals("s") || a.equals("show")){
                showConfig = true;
              } else if (a.equals("l") || a.equals("log")){
                showConfigSources = true;
              } else if (a.equals("q") || a.equals("quiet")){
                quiet = true;                
              } else if (a.equals("x")){
                stopOnFailure = true;
              } else if (a.equals("h")){
                hideSummary = true;
              }
              args[i] = null;  // set it consumed

            } else {
              break; // done, this is a test method
            }
          }
        }
      }
    }
  }

  protected static boolean hasExplicitTestMethods(String[] args){
    for (String a : args){
      if (a != null){
        return true;
      }
    }

    return false;
  }

  protected static List<Method> getMatchingMethods(Class<? extends TestJPF> testCls,
          int setModifiers, int unsetModifiers, String[] annotationNames){
    List<Method> list = new ArrayList<Method>();
    
    for (Method m : testCls.getMethods()){
      if (isMatchingMethod(m, setModifiers, unsetModifiers, annotationNames)){
        list.add(m);
      }
    }
    
    return list;
  }

  protected static boolean isMatchingMethod(Method m, int setModifiers, int unsetModifiers, String[] annotationNames) {
    int mod = m.getModifiers();
    if (((mod & setModifiers) != 0) && ((mod & unsetModifiers) == 0)) {
      if (m.getParameterTypes().length == 0) {
        if (annotationNames != null){
          Annotation[] annotations = m.getAnnotations();
          for (int i = 0; i < annotations.length; i++) {
            String annotType = annotations[i].annotationType().getName();
            for (int j = 0; j < annotationNames.length; j++) {
              if (annotType.equals(annotationNames[j])) {
                return true;
              }
            }
          }
        } else {
          return true;
        }
      }
    }

    return false;
  }

  protected static List<Method> getContextMethods(Class<? extends TestJPF> testCls, 
                                                  int setModifiers, int unsetModifiers, String annotation){
    String[] annotations = {annotation};

    List<Method> list = new ArrayList<Method>();
    for (Method m : testCls.getMethods()){
      if (isMatchingMethod(m, setModifiers, unsetModifiers, annotations)){
        list.add(m);
      }
    }
    return list;
  }

  protected static List<Method> getBeforeMethods(Class<? extends TestJPF> testCls){
    return getContextMethods(testCls, Modifier.PUBLIC, Modifier.STATIC, "org.junit.Before");
  }

  protected static List<Method> getAfterMethods(Class<? extends TestJPF> testCls){
    return getContextMethods(testCls, Modifier.PUBLIC, Modifier.STATIC, "org.junit.After");
  }

  protected static List<Method> getBeforeClassMethods(Class<? extends TestJPF> testCls){
    return getContextMethods(testCls, Modifier.PUBLIC | Modifier.STATIC, 0, "org.junit.BeforeClass");
  }
  
  protected static List<Method> getAfterClassMethods(Class<? extends TestJPF> testCls){
    return getContextMethods(testCls, Modifier.PUBLIC | Modifier.STATIC, 0, "org.junit.AfterClass");
  }
  
  protected static boolean haveTestMethodSpecs( String[] args){
    if (args != null && args.length > 0){
      for (int i=0; i<args.length; i++){
        if (args[i] != null){
          return true;
        }
      }
    }
    
    return false;
  }
  
  protected static List<Method> getTestMethods(Class<? extends TestJPF> testCls, String[] args){
    String[] testAnnotations = {"org.junit.Test", "org.testng.annotations.Test"};

    if (haveTestMethodSpecs( args)){ // test methods specified as arguments
      List<Method> list = new ArrayList<Method>();

      for (String test : args){
        if (test != null){

          try {
            Method m = testCls.getMethod(test);

            if (isMatchingMethod(m, Modifier.PUBLIC, Modifier.STATIC, null /*testAnnotations*/ )){
              list.add(m);
            } else {
              throw new RuntimeException("test method must be @Test annotated public instance method without arguments: " + test);
            }

          } catch (NoSuchMethodException x) {
            throw new RuntimeException("method: " + test
                    + "() not in test class: " + testCls.getName(), x);
          }
        }
      }
      
      return list;

    } else { // no explicit test method specification, get all matches
      return getMatchingMethods(testCls, Modifier.PUBLIC, Modifier.STATIC, testAnnotations);
    }
  }


  protected static void reportTestStart(String mthName){
    if (!quiet){
      System.out.println();
      System.out.print("......................................... testing ");
      System.out.print(mthName);
      System.out.println("()");
    }
  }

  protected static void reportTestInitialization(String mthName){
    if (!quiet){
      System.out.print(".... running test initialization: ");
      System.out.print(mthName);
      System.out.println("()");
    }
  }

  protected static void reportTestCleanup(String mthName){
    if (!quiet){
      System.out.print(".... running test cleanup: ");
      System.out.print(mthName);
      System.out.println("()");
    }
  }

  protected static void reportTestFinished(String msg){
    if (!quiet){
      System.out.print("......................................... ");
      System.out.println(msg);
    }
  }

  protected static void reportResults(String clsName, int nTests, int nFailures, int nErrors, List<String> results){
    System.out.println();
    System.out.print("......................................... execution of testsuite: " + clsName);
    if (nFailures > 0 || nErrors > 0){
      System.out.println(" FAILED");
    } else if (nTests > 0) {
      System.out.println(" SUCCEEDED");
    } else {
      System.out.println(" OBSOLETE");
    }

    if (!quiet){
      if (results != null) {
        int i = 0;
        for (String result : results) {
          System.out.print(".... [" + ++i + "] ");
          System.out.println(result);
        }
      }
    }

    System.out.print(".........................................");
    System.out.println(" tests: " + nTests + ", failures: " + nFailures + ", errors: " + nErrors);
  }

  
  static void invoke (Method m, Object testObject) throws IllegalAccessException, InvocationTargetException  {
    PrintStream sysOut = null;
    
    try {
      if (quiet){
        sysOut = System.out;
        System.setOut( new DevNullPrintStream());
      }
      
      m.invoke( testObject);
      
    } finally {
      if (quiet){
        System.setOut( sysOut);
      }
    }
  }
  
  /**
   * this is the main test loop if this TestJPF instance is executed directly
   * or called from RunTest. It is *not* called if this is executed from JUnit
   */
  public static void runTests (Class<? extends TestJPF> testCls, String... args){
    int nTests = 0;
    int nFailures = 0;
    int nErrors = 0;
    String testMethodName = null;
    List<String> results = null;

    getOptions(args);
    globalRunDirectly = runDirectly;
    globalShowConfig = showConfig;
    boolean globalStopOnFailure = stopOnFailure;

    try {
      List<Method> testMethods = getTestMethods(testCls, args);
      results = new ArrayList<String>(testMethods.size());

      // check if we have JUnit style housekeeping methods (initialization and
      // cleanup should use the same mechanisms as JUnit)
            
      List<Method> beforeClassMethods = getBeforeClassMethods(testCls);
      List<Method> afterClassMethods = getAfterClassMethods(testCls);
            
      List<Method> beforeMethods = getBeforeMethods(testCls);
      List<Method> afterMethods = getAfterMethods(testCls);

      for (Method initMethod : beforeClassMethods) {
        reportTestInitialization(initMethod.getName());
        initMethod.invoke(null);
      }
            
      for (Method testMethod : testMethods) {
        testMethodName = testMethod.getName();
        String result = testMethodName;
        try {
          Object testObject = testCls.newInstance();

          nTests++;
          reportTestStart( testMethodName);

          // run per test initialization methods
          for (Method initMethod : beforeMethods){
            reportTestInitialization( initMethod.getName());
            invoke( initMethod, testObject);
          }

          // now run the test method itself
          invoke( testMethod, testObject);
          result += ": Ok";

          // run per test initialization methods
          for (Method cleanupMethod : afterMethods){
            reportTestCleanup( cleanupMethod.getName());
            invoke( cleanupMethod, testObject);
          }

        } catch (InvocationTargetException x) {
          Throwable cause = x.getCause();
          cause.printStackTrace();
          if (cause instanceof AssertionError) {
            nFailures++;
            reportTestFinished("test method failed with: " + cause.getMessage());
            result += ": Failed";
          } else {
            nErrors++;
            reportTestFinished("unexpected error while executing test method: " + cause.getMessage());
            result += ": Error";
          }

          if (globalStopOnFailure){
            break;
          }
        }
        
        results.add(result);
        reportTestFinished(result);
      }
      
      for (Method cleanupMethod : afterClassMethods) {
        reportTestCleanup( cleanupMethod.getName());
        cleanupMethod.invoke(null);
      }


    //--- those exceptions are unexpected and represent unrecoverable test harness errors
    } catch (InvocationTargetException x) {
      Throwable cause = x.getCause();
      cause.printStackTrace();
      nErrors++;
      reportTestFinished("TEST ERROR: @BeforeClass,@AfterClass method failed: " + x.getMessage());
      
    } catch (InstantiationException x) {
      nErrors++;
      reportTestFinished("TEST ERROR: cannot instantiate test class: " + x.getMessage());
    } catch (IllegalAccessException x) { // can't happen if getTestMethods() worked
      nErrors++;
      reportTestFinished("TEST ERROR: default constructor or test method not public: " + testMethodName);
    } catch (IllegalArgumentException x) {  // can't happen if getTestMethods() worked
      nErrors++;
      reportTestFinished("TEST ERROR: illegal argument for test method: " + testMethodName);
    } catch (RuntimeException rx) {
      nErrors++;
      reportTestFinished("TEST ERROR: " + rx.toString());
    }

    if (!hideSummary){
      reportResults(testCls.getName(), nTests, nFailures, nErrors, results);
    }

    if (nErrors > 0 || nFailures > 0){
      if (isRunTestRun()){
        // we need to reportTestFinished this test has failed
        throw new RunTest.Failed();
      }
    }
  }

  static String getProperty(String key){
    // intercepted by peer
    return null;
  }
  
  /**
   * this is the JPF entry method in case there is no main() in the test class
   * 
   * <2do> we should support test method arguments here
   */
  static void runTestMethod(String args[]) throws Throwable {
    String testClsName = getProperty("target");
    String testMthName = getProperty("target.test_method");
    
    Class<?> testCls = Class.forName(testClsName);
    Object target = testCls.newInstance();
    
    Method method = testCls.getMethod(testMthName);

    try {
      method.invoke(target);
    } catch (InvocationTargetException e) {
      throw e.getCause(); 
    }
  }

  /**
   * NOTE: this needs to be called from the concrete test class, typically from
   * its main() method, otherwise we don't know the name of the class we have
   * to pass to JPF
   */
  protected static void runTestsOfThisClass (String[] testMethods){
    // needs to be at the same stack level, so we can't delegate
    Class<? extends TestJPF> testClass = Reflection.getCallerClass(TestJPF.class);
    runTests(testClass, testMethods);
  }

  /**
   * needs to be broken up into two methods for cases that do additional
   * JPF initialization (jpf-inspector)
   *
   * this is called from the various verifyX() methods (i.e. host VM) to
   * start JPF, it is never executed under JPF
   */
  protected JPF createAndRunJPF (StackTraceElement testMethod, String[] args) {
    JPF jpf = createJPF( testMethod, args);
    if (jpf != null){
      jpf.run();
    }
    return jpf;
  }

  /**
   * this is never executed under JPF
   */
  protected JPF createJPF (StackTraceElement testMethod, String[] args) {
    JPF jpf = null;
    
    Config conf = new Config(args);

    // --- add global args (if we run under RunTest)
    if (globalArgs != null) {
      for (GlobalArg ga : globalArgs) {
        String key = ga.key;
        String val = ga.val;
        if (val != null){
          conf.put(key, val);
        } else {
          conf.remove(key);
        }
      }
    }

    setTestTargetKeys(conf, testMethod);
    
    // --- initialize the classpath from <projectId>.test_classpath
    String projectId = JPFSiteUtils.getCurrentProjectId();
    if (projectId != null) {
      String testCp = conf.getString(projectId + ".test_classpath");
      if (testCp != null) {
        conf.append("classpath", testCp, ",");
      }
    }

    // --- if we have any specific test property overrides, do so
    conf.promotePropertyCategory("test.");

    getOptions(args);

    if (showConfig || showConfigSources) {
      PrintWriter pw = new PrintWriter(System.out, true);
      if (showConfigSources) {
        conf.printSources(pw);
      }

      if (showConfig) {
        conf.print(pw);
      }
      pw.flush();
    }

    jpf = new JPF(conf);

    return jpf;
  }

  protected void setTestTargetKeys(Config conf, StackTraceElement testMethod) {
    conf.put("target.entry", "runTestMethod([Ljava/lang/String;)V");
    conf.put("target", testMethod.getClassName());
    conf.put("target.test_method", testMethod.getMethodName());
  }

  //--- the JPFShell interface
  @Override
  public void start(String[] testMethods){
    Class<? extends TestJPF> testClass = getClass(); // this is an instance method
    runTests(testClass, testMethods);
  }

  protected StackTraceElement getCaller(){
    StackTraceElement[] st = (new Throwable()).getStackTrace();
    return st[2];
  }
  
  protected StackTraceElement setTestMethod (String clsName, String mthName){
    return new StackTraceElement( clsName, mthName, null, -1);
  }
  
  protected StackTraceElement setTestMethod (String mthName){
    return new StackTraceElement( getClass().getName(), mthName, null, -1);
  }
  
  
  //--- the JPF run test methods

  /**
   * run JPF expecting a AssertionError in the SuT
   * @param args JPF main() arguments
   */
  protected JPF assertionError (StackTraceElement testMethod, String... args){
    return unhandledException( testMethod, "java.lang.AssertionError", null, args );    
  }
  protected JPF assertionError (String... args) {
    return unhandledException( getCaller(), "java.lang.AssertionError", null, args );
  }
  
  protected JPF assertionErrorDetails (StackTraceElement testMethod, String details, String... args) {
    return unhandledException( testMethod, "java.lang.AssertionError", details, args );
  }
  protected JPF assertionErrorDetails (String details, String... args) {
    return unhandledException( getCaller(), "java.lang.AssertionError", details, args );
  }
  protected boolean verifyAssertionErrorDetails (String details, String... args){
    if (runDirectly) {
      return true;
    } else {
      unhandledException( getCaller(), "java.lang.AssertionError", details, args);
      return false;
    }
  }
  protected boolean verifyAssertionError (String... args){
    if (runDirectly) {
      return true;
    } else {
      unhandledException( getCaller(), "java.lang.AssertionError", null, args);
      return false;
    }
  }

  /**
   * run JPF expecting no SuT property violations 
   */
  protected JPF noPropertyViolation (StackTraceElement testMethod, String... args) {
    JPF jpf = null;

    report(args);

    try {
      jpf = createAndRunJPF( testMethod, args);
    } catch (Throwable t) {
      // we get as much as one little hickup and we declare it failed
      t.printStackTrace();
      fail("JPF internal exception executing: ", args, t.toString());
      return jpf;
    }

    List<Error> errors = jpf.getSearchErrors();
    if ((errors != null) && (errors.size() > 0)) {
      fail("JPF found unexpected errors: " + (errors.get(0)).getDescription());
    }

    return jpf;
  }
  
  protected JPF noPropertyViolation (String... args) {
    return noPropertyViolation( getCaller(), args);    
  }
  
  protected boolean verifyNoPropertyViolation (String...args){
    if (runDirectly) {
      return true;
    } else {
      noPropertyViolation( getCaller(), args);
      return false;
    }
  }

  /**
   * NOTE: this uses the exception class name because it might be an
   * exception type that is only known to JPF (i.e. not in the native classpath)
   *
   * @param xClassName name of the exception base type that is expected
   * @param details detail message of the expected exception
   * @param args JPF arguments
   */
  protected JPF unhandledException (StackTraceElement testMethod, String xClassName, String details, String... args) {
    JPF jpf = null;

    report(args);

    try {
      jpf = createAndRunJPF(testMethod, args);
    } catch (Throwable t) {
      t.printStackTrace();
      fail("JPF internal exception executing: ", args, t.toString());
      return jpf;
    }

    Error error = jpf.getLastError();
    if (error != null){
      Property errorProperty = error.getProperty();
      if (errorProperty instanceof NoUncaughtExceptionsProperty){ 
        ExceptionInfo xi = ((NoUncaughtExceptionsProperty)errorProperty).getUncaughtExceptionInfo();
        String xn = xi.getExceptionClassname();
        if (!xn.equals(xClassName)) {
          fail("JPF caught wrong exception: " + xn + ", expected: " + xClassName);
        }

        if (details != null) {
          String gotDetails = xi.getDetails();
          if (gotDetails == null) {
            fail("JPF caught the right exception but no details, expected: " + details);
          } else {
            if (!gotDetails.endsWith(details)) {
              fail("JPF caught the right exception but the details were wrong: " + gotDetails + ", expected: " + details);
            }
          }
        }
      } else { // error not a NoUncaughtExceptionsProperty
        fail("JPF failed to catch exception executing: ", args, ("expected " + xClassName));        
      }
    } else { // no error
      fail("JPF failed to catch exception executing: ", args, ("expected " + xClassName));
    }
    
    return jpf;
  }
  
  protected JPF unhandledException (String xClassName, String details, String... args) {
    return unhandledException( getCaller(), xClassName, details, args);
  }

    
  protected boolean verifyUnhandledExceptionDetails (String xClassName, String details, String... args){
    if (runDirectly) {
      return true;
    } else {
      unhandledException( getCaller(), xClassName, details, args);
      return false;
    }
  }
  protected boolean verifyUnhandledException (String xClassName, String... args){
    if (runDirectly) {
      return true;
    } else {
      unhandledException( getCaller(), xClassName, null, args);
      return false;
    }
  }


  /**
   * run JPF expecting it to throw an exception
   * NOTE - xClassName needs to be the concrete exception, not a super class
   * @param args JPF main() arguments
   */
  protected JPF jpfException (StackTraceElement testMethod, Class<? extends Throwable> xCls, String... args) {
    JPF jpf = null;
    Throwable exception = null;

    report(args);

    try {
      jpf = createAndRunJPF( testMethod, args);
    } catch (JPF.ExitException xx) {
      exception = xx.getCause();
    } catch (Throwable x) {
      exception = x;
    }

    if (exception != null){
      if (!xCls.isAssignableFrom(exception.getClass())){
        fail("JPF produced wrong exception: " + exception + ", expected: " + xCls.getName());
      }
    } else {
      fail("JPF failed to produce exception, expected: " + xCls.getName());
    }

    return jpf;
  }
  
  protected JPF jpfException (Class<? extends Throwable> xCls, String... args) {
    return jpfException( getCaller(), xCls, args);
  }  
  
  protected boolean verifyJPFException (TypeRef xClsSpec, String... args){
    if (runDirectly) {
      return true;

    } else {
      try {
        Class<? extends Throwable> xCls = xClsSpec.asNativeSubclass(Throwable.class);

        jpfException( getCaller(), xCls, args);

      } catch (ClassCastException ccx){
        fail("not a property type: " + xClsSpec);
      } catch (ClassNotFoundException cnfx){
        fail("property class not found: " + xClsSpec);
      }
      return false;
    }
  }

  
  
  /**
   * run JPF expecting a property violation of the SuT
   * @param args JPF main() arguments
   */
  protected JPF propertyViolation (StackTraceElement testMethod, Class<? extends Property> propertyCls, String... args ){
    JPF jpf = null;

    report(args);

    try {
      jpf = createAndRunJPF( testMethod, args);
    } catch (Throwable t) {
      t.printStackTrace();
      fail("JPF internal exception executing: ", args, t.toString());
    }

    List<Error> errors = jpf.getSearchErrors();
    if (errors != null) {
      for (Error e : errors) {
        if (propertyCls == e.getProperty().getClass()) {
          return jpf; // success, we got the sucker
        }
      }
    }

    fail("JPF failed to detect error: " + propertyCls.getName());
    return jpf;
  }
  
  protected JPF propertyViolation (Class<? extends Property> propertyCls, String... args ){
    return propertyViolation( getCaller(), propertyCls, args);
  }
  
  protected boolean verifyPropertyViolation (TypeRef propertyClsSpec, String... args){
    if (runDirectly) {
      return true;

    } else {
      try {
        Class<? extends Property> propertyCls = propertyClsSpec.asNativeSubclass(Property.class);
        propertyViolation( getCaller(), propertyCls, args);

      } catch (ClassCastException ccx){
        fail("not a property type: " + propertyClsSpec);
      } catch (ClassNotFoundException cnfx){
        fail("property class not found: " + propertyClsSpec);
      }
      return false;
    }
  }


  /**
   * run JPF expecting a deadlock in the SuT
   * @param args JPF main() arguments
   */
  protected JPF deadlock (String... args) {
    return propertyViolation( getCaller(), NotDeadlockedProperty.class, args );
  }
  
  protected boolean verifyDeadlock (String... args){
    if (runDirectly) {
      return true;
    } else {
      propertyViolation( getCaller(), NotDeadlockedProperty.class, args);
      return false;
    }
  }
    
  // these are the org.junit.Assert APIs, but we don't want org.junit to be
  // required to run tests

  public static void assertEquals(String msg, Object expected, Object actual){
    if (expected == null && actual == null) { 
      return; 
    }
    
    if (expected != null && expected.equals(actual)) {
      return; 
    }
    
    fail(msg);
  }

  public static void assertEquals(Object expected, Object actual){
    assertEquals("", expected, actual); 
  }

  public static void assertEquals(String msg, int expected, int actual){
    if (expected != actual) {
      fail(msg);
    }
  }

  public static void assertEquals(int expected, int actual){    
    assertEquals("expected != actual : " + expected + " != " + actual, expected, actual);
  }  

  public static void assertEquals(String msg, long expected, long actual){
    if (expected != actual) {
      fail(msg);
    }
  }

  public static void assertEquals(long expected, long actual){    
      assertEquals("expected != actual : " + expected + " != " + actual,
                   expected, actual);
  }

  public static void assertEquals(double expected, double actual){
    if (expected != actual){
      fail("expected != actual : " + expected + " != " + actual);
    }
  }

  public static void assertEquals(String msg, double expected, double actual){
    if (expected != actual){
      fail(msg);
    }
  }

  public static void assertEquals(float expected, float actual){
    if (expected != actual){
      fail("expected != actual : " + expected + " != " + actual);
    }
  }

  public static void assertEquals(String msg, float expected, float actual){
    if (expected != actual){
      fail(msg);
    }
  }

  public static void assertEquals(String msg, double expected, double actual, double delta){
    if (Math.abs(expected - actual) > delta) {
      fail(msg);
    }
  }

  public static void assertEquals(double expected, double actual, double delta){    
    assertEquals("Math.abs(expected - actual) > delta : " + "Math.abs(" + expected + " - " + actual + ") > " + delta,
                 expected, actual, delta);
  }

  public static void assertEquals(String msg, float expected, float actual, float delta){
    if (Math.abs(expected - actual) > delta) {
      fail(msg);
    }
  }

  public static void assertEquals(float expected, float actual, float delta){    
      assertEquals("Math.abs(expected - actual) > delta : " + "Math.abs(" + expected + " - " + actual + ") > " + delta,
                   expected, actual, delta);
  }

  public static void assertArrayEquals(byte[] expected, byte[] actual){
    if (((expected == null) != (actual == null)) ||
        (expected.length != actual.length)){
      fail("array sizes different");
    }

    for (int i=0; i<expected.length; i++){
      if (expected[i] != actual[i]){
        fail("array element" + i + " different, expected " + expected[i] + ", actual " + actual[i]);
      }
    }
  }

  public static void assertNotNull(String msg, Object o) {
    if (o == null) {
      fail(msg);
    }
  }

  public static void assertNotNull(Object o){
    assertNotNull("o == null", o);
  }

  public static void assertNull(String msg, Object o){
    if (o != null) {
      fail(msg);
    }
  }

  public static void assertNull(Object o){    
    assertNull("o != null", o);
  }

  public static void assertSame(String msg, Object expected, Object actual){
    if (expected != actual) {
      fail(msg);
    }
  }

  public static void assertSame(Object expected, Object actual){
    assertSame("expected != actual : " + expected + " != " + actual, expected, actual);
  }

  public static void assertFalse (String msg, boolean cond){
    if (cond) {
      fail(msg);
    }
  }

  public static void assertFalse (boolean cond){
    assertFalse("", cond);
  }

  public static void assertTrue (String msg, boolean cond){
    if (!cond) {
      fail(msg);
    }
  }

  public static void assertTrue (boolean cond){
    assertTrue("", cond);
  }
}
