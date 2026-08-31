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
/**
 * This is a raw test class, which produces AssertionErrors for all
 * cases we want to catch. Make double-sure we don't refer to any
 * JPF class in here, or we start to check JPF recursively.
 * To turn this into a Junt test, you have to write a wrapper
 * TestCase, which just calls the testXX() methods.
 * The Junit test cases run JPF.main explicitly by means of specifying
 * which test case to run, but be aware of this requiring proper
 * state clean up in JPF !
 *
 * KEEP IT SIMPLE - it's already bad enough we have to mimic unit tests
 * by means of system tests (use whole JPF to check if it works), we don't
 * want to make the observer problem worse by means of enlarging the scope
 * JPF has to look at
 *
 * Note that we don't use assert expressions, because those would already
 * depend on working java.lang.Class APIs
 */
package gov.nasa.jpf.test.java.lang;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.Verify;
import gov.nasa.jpf.test.test_classes.class_test.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Method;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;

import org.junit.Test;

/**
 * test of java.lang.Class API
 */
public class ClassTest extends TestJPF implements Cloneable, Serializable {
  
  /**************************** tests **********************************/
  static String clsName = ClassTest.class.getName();

  int data = 42; // that creates a default ctor for our newInstance test


  @Test 
  public void testClassForName () throws ClassNotFoundException {
    if (verifyNoPropertyViolation()) {

      Class<?> clazz = Class.forName(clsName);
      System.out.println("loaded " + clazz.getName());

      if (clazz == null) {
        throw new RuntimeException("Class.forName() returned null object");
      }

      if (!clsName.equals(clazz.getName())) {
        throw new RuntimeException(
                "getName() wrong for Class.forName() acquired class");
      }
    }
  }
  
  private void testClassForNameArray (String className, Class<?> expectedClass) throws ClassNotFoundException {
    Class<?> clazz = Class.forName(className);
    assertNotNull(clazz);
    assertTrue(clazz.isArray());
    assertEquals(className, clazz.getName());
    assertSame(expectedClass, clazz);
  }

  @Test
  public void testClassForNamePrimitiveArrays () throws ClassNotFoundException {
    if (verifyNoPropertyViolation()) {
      testClassForNameArray("[Z", boolean[].class);
      testClassForNameArray("[I", int[].class);
      testClassForNameArray("[C", char[].class);
      testClassForNameArray("[D", double[].class);
      testClassForNameArray("[F", float[].class);
      testClassForNameArray("[I", int[].class);
      testClassForNameArray("[J", long[].class);
      testClassForNameArray("[S", short[].class);
    }
  }

  @Test
  public void testClassForNameReferenceArray () throws ClassNotFoundException {
    if (verifyNoPropertyViolation()) {
      testClassForNameArray("[L" + clsName + ";", ClassTest[].class);
    }
  }

  @Test
  public void testClassForNameException () throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {
      Class<?> clazz = Class.forName("x.y.NonExisting");
    }
  }
  
  @Test 
  public void testClassForNameExceptionForInvalidArrayType () throws ClassNotFoundException {
    if (verifyUnhandledException("java.lang.ClassNotFoundException")) {

      // Adapted from the Groovy library
      // This tests how array types are handled properly in Types.java (issue #204)
      Class<?> clazz = Class.forName("groovy.runtime.metaclass.[Ljava.lang.Object;MetaClass");
    }
  }
  
  static class X {
    static {
      System.out.println("ClassTest$X initialized");
      Verify.incrementCounter(0);
    }
  }
  
  @Test 
  public void testClassForNameInit () throws ClassNotFoundException {
    if (!isJPFRun()){
      Verify.resetCounter(0);
    }
    
    if (verifyNoPropertyViolation()) {
      Class<?> cls = Class.forName( "gov.nasa.jpf.test.java.lang.ClassTest$X", true,  this.getClass().getClassLoader());
      System.out.println("Class.forName() returned");
    }
    
    if (!isJPFRun()){
      assertTrue( Verify.getCounter(0) == 1);
    }
  }
  
  
  @Test 
  public void testGetClass () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = this.getClass();

      if (clazz == null) {
        throw new RuntimeException("Object.getClass() failed");
      }

      if (!clsName.equals(clazz.getName())) {
        throw new RuntimeException(
                "getName() wrong for getClass() acquired class");
      }
    }
  }

  @Test 
  public void testIdentity () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz1 = null;
      Class<?> clazz2 = ClassTest.class;
      Class<?> clazz3 = this.getClass();

      try {
        clazz1 = Class.forName(clsName);
      } catch (Throwable x) {
        x = null;  // Get rid of IDE warning
      }

      if (clazz1 != clazz2) {
        throw new RuntimeException(
                "Class.forName() and class field not identical");
      }

      if (clazz2 != clazz3) {
        throw new RuntimeException(
                "Object.getClass() and class field not identical");
      }
    }
  }
  
  @Test 
  public void testNewInstance () throws ReflectiveOperationException {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = ClassTest.class;
      ClassTest o = (ClassTest) clazz.getDeclaredConstructor().newInstance();
      
      System.out.println("new instance: " + o);
      
      if (o.data != 42) {
        throw new RuntimeException(
          "Class.newInstance() failed to call default ctor");        
      }
    }
  }

  static class PrivateNestMate {
    boolean assertData = true;

    private PrivateNestMate() {}
  }

  @Test 
  public void testPrivateNestMateNewInstanceAccess () throws ReflectiveOperationException {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = PrivateNestMate.class;
      PrivateNestMate clazzInstance = (PrivateNestMate) clazz.getDeclaredConstructor().newInstance();
      assertTrue(clazzInstance.assertData);
    }
  }

  static class ProtectedNestMate {
    boolean assertData = true;

    protected ProtectedNestMate() {}
  }

  @Test 
  public void testProtectedNestMateNewInstanceAccess () throws ReflectiveOperationException {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = ProtectedNestMate.class;
      ProtectedNestMate clazzInstance = (ProtectedNestMate) clazz.getDeclaredConstructor().newInstance();
      assertTrue(clazzInstance.assertData);
    }
  }

  @Test 
  public void testPrivateConstructorNewInstanceFailAccess () throws ReflectiveOperationException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      Class<?> clazz = TestNewInstance.Private.class;
      clazz.getDeclaredConstructor().newInstance();
    }
  }

  @Test 
  public void testProtectedConstructorNewInstanceAccess () throws ReflectiveOperationException {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz = TestNewInstance.Protected.class;
      TestNewInstance.Protected clazzInstance = (TestNewInstance.Protected) 
                                                  clazz.getDeclaredConstructor().newInstance();
      assertTrue(clazzInstance.assertData);

      //protected constructor of class in different package which extends ClassTest 
      Class<?> diffPkgSubClazz = DiffPkgProtectedSubClass.class;
      DiffPkgProtectedSubClass diffPkgSubClazzInst = (DiffPkgProtectedSubClass) 
                                                      diffPkgSubClazz.getDeclaredConstructor().newInstance();
      assertTrue(diffPkgSubClazzInst.assertData);
    }
  }

  @Test 
  public void testProtectedConstructorNewInstanceFailAccess () throws ReflectiveOperationException {
    if (verifyUnhandledException("java.lang.IllegalAccessException")) {
      Class<?> clazz = DiffPkgProtectedClass.class;
      clazz.getDeclaredConstructor().newInstance();
    }
  }

  static abstract class AbstractClass {
  }
    
  @Test 
  public void testNewInstanceFailAbstract () throws ReflectiveOperationException {
    if (verifyUnhandledException("java.lang.InstantiationException")){
      Class<?> clazz = AbstractClass.class;
      clazz.getDeclaredConstructor().newInstance();
    }
  }

  static class NoDefaultCtor {

    int i;

    public NoDefaultCtor(int i) {
      this.i = i;
    }
  }

  @Test
  public void testNoDefaultConstructor() throws ReflectiveOperationException {
    if (verifyUnhandledException("java.lang.InstantiationException")) {
      NoDefaultCtor.class.newInstance();
    }
  }

  @Test 
  public void testIsAssignableFrom () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz1 = Integer.class;
      Class<?> clazz2 = Object.class;
    
      assert clazz2.isAssignableFrom(clazz1);
  
      assert !clazz1.isAssignableFrom(clazz2); 
    }
  }  
  
  @Test 
  public void testInstanceOf () {
    if (verifyNoPropertyViolation()) {
      assert this instanceof Cloneable;
      assert this instanceof TestJPF;
      assert this instanceof Object;

      if (this instanceof Runnable) {
        assert false : "negative instanceof test failed";
      }
    }
  }
  
  @Test
  public void testAsSubclass () {
    if (verifyNoPropertyViolation()) {
      Class<?> clazz1 = Float.class;
    
      Class<? extends Number> clazz2 = clazz1.asSubclass(Number.class); 
      assert clazz2 != null;
      
      try {
        clazz1.asSubclass(String.class);
        assert false : "testAsSubclass() failed to throw ClassCastException";
      } catch (ClassCastException ccx) {
        ccx = null;  // Get rid of IDE warning
      } 
    }    
  }
  
  @SuppressWarnings("null")
  @Test 
  public void testClassField () {
    if (verifyNoPropertyViolation()) {

      Class<?> clazz = ClassTest.class;

      if (clazz == null) {
        throw new RuntimeException("class field not set");
      }

      if (!clsName.equals(clazz.getName())) {
        throw new RuntimeException("getName() wrong for class field");
      }
    }
  }

  @Test 
  public void testInterfaces () {
    if (verifyNoPropertyViolation()) {
      Class<?>[] ifc = ClassTest.class.getInterfaces();
      if (ifc.length != 2) {
        throw new RuntimeException("wrong number of interfaces: " + ifc.length);
      }

      int n = ifc.length;
      String[] ifcs = {Cloneable.class.getName(), Serializable.class.getName()};
      for (int i = 0; i < ifcs.length; i++) {
        for (int j = 0; j < ifc.length; j++) {
          if (ifc[j].getName().equals(ifcs[i])) {
            n--;
            break;
          }
        }
      }

      if (n != 0) {
        throw new RuntimeException("wrong interface types: " + ifc[0].getName() + ',' + ifc[1].getName());
      }
    }
  }
  
  
  static class TestClassBase {
    protected TestClassBase() {}
    public void foo () {}
  }
  
  interface TestIfc {
    void boo();                        // 4
    void foo();
  }
  
  static abstract class TestClass extends TestClassBase implements TestIfc {
    static {
      System.out.println("why is TestClass.<clinit>() executed?");
    }
    public TestClass() {}
    public TestClass (int a) {a = 0;}
    @Override
	public void foo() {}               // 1
    void bar() {}                      // 2
    public static void baz () {}       // 3
    
  }
  
  @Test
  public void testMethods() {
    if (verifyNoPropertyViolation()) {

      Class<?> cls = TestClass.class;
      Method[] methods = cls.getMethods();

      boolean fooSeen=false, bazSeen=false, booSeen=false;

      for (int i = 0; i < methods.length; i++) {
        Method m = methods[i];
        Class<?> declCls = m.getDeclaringClass();
        String mname = m.getName();

        // we don't care about the Object methods
        if (declCls == Object.class) {
          methods[i] = null;
          continue;
        }

        // non-publics, <clinit> and <init> are filtered out

        if (declCls == TestClass.class) {
          if (mname.equals("foo")) {
            methods[i] = null;
            fooSeen = true;
            continue;
          }
          if (mname.equals("baz")) {
            methods[i] = null;
            bazSeen = true;
            continue;
          }
        }

        // TestClass is abstract and doesn't implement TestIfc.boo()
        if (declCls == TestIfc.class) {
          if (mname.equals("boo")) {
            methods[i] = null;
            booSeen = true;
            continue;
          }
        }
      }

      assert fooSeen : "no TestClass.foo() seen";
      assert bazSeen : "no TestClass.baz() seen";
      assert booSeen : "no TestIfc.boo() seen";

      for (int i = 0; i < methods.length; i++) {
        assert (methods[i] == null) : ("unexpected method in getMethods(): " +
                  methods[i].getDeclaringClass().getName() + " : " + methods[i]);
      }
    }
  }

  private static class NestedClass {}

  @Test 
  public void testGetEnclosingClassExist() {
    if (verifyNoPropertyViolation()) {
      Class<?> clz = NestedClass.class;
      Class<?> enclosingClass = clz.getEnclosingClass();
      assert enclosingClass == ClassTest.class;
    }
  }

  @Test
  public void testGetEnclosingClassNotExist() {
    if (verifyNoPropertyViolation()) {
      Class<?> clz = this.getClass();
      Class<?> enclosingClass = clz.getEnclosingClass();
      assert enclosingClass == null;
    }
  }
  
  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  public @interface TestAnnotation {
  }

  @TestAnnotation()
  public static class ParentAnnotated<E> {
  }

  public static class ChildAnnotated<E> extends ParentAnnotated {
  }

  public enum TestEnum{
    item;
  }

  @TestAnnotation()
  public static class TestEnclosedClass {
    public Object foo;

    public TestEnclosedClass () {
      class LocalClass {
      }
      ;
      foo = new LocalClass();
    }

    public static class MemberClass {
    }

    public Object getLocalClassObj (){

      class LocalClass {
      }
      ;

      return new LocalClass();
    }

    public Object getAnonymousClassObj (){
      return new Object() {
      };
    }
  }

  @Test
  public void localClassEnclosingClassTest (){
    if (verifyNoPropertyViolation()){
      TestEnclosedClass testObj = new ClassTest.TestEnclosedClass();
      assertEquals(testObj.foo.getClass().getEnclosingClass(), TestEnclosedClass.class);
    }
  }

  @Test
  public void getCanonicalNameTest (){
    if (verifyNoPropertyViolation()){
      assertEquals(ArrayList.class.getCanonicalName(), "java.util.ArrayList");
      assertEquals(Class.class.getCanonicalName(), "java.lang.Class");
      assertEquals(String.class.getCanonicalName(), "java.lang.String");
      assertEquals((new Object[0]).getClass().getCanonicalName(), "java.lang.Object[]");
    }
  }

  @Test
  public void getDeclaredAnnotationsTest (){
    if (verifyNoPropertyViolation()){
      assertTrue(ClassTest.ParentAnnotated.class.getDeclaredAnnotations().length == 1);
      assertTrue(ChildAnnotated.class.getDeclaredAnnotations().length == 0);
      assertTrue(ClassTest.ParentAnnotated.class.getAnnotations().length == 1);
      assertTrue(ChildAnnotated.class.getAnnotations().length == 1);
    }
  }

  @Test
  public void getEnclosingConstructor () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      Class cls = (new ClassTest.TestEnclosedClass()).foo.getClass();
      assertTrue(cls.getEnclosingConstructor().getDeclaringClass() == ClassTest.TestEnclosedClass.class);
      assertEquals(cls.getEnclosingConstructor().getName(), "<init>");
      assertNull(cls.getEnclosingMethod());
    }
  }

  @Test
  public void getEnclosingMethod () throws SecurityException, NoSuchMethodException{
    if (verifyNoPropertyViolation()){
      Class cls = (new ClassTest.TestEnclosedClass()).getLocalClassObj().getClass();
      assertTrue(cls.getEnclosingMethod().getDeclaringClass() == ClassTest.TestEnclosedClass.class);
      assertNull(cls.getEnclosingConstructor());
      assertEquals(cls.getEnclosingMethod().getName(), "getLocalClassObj");
      Method m1 = ClassTest.TestEnclosedClass.class.getMethod("getLocalClassObj", new Class[0]);
      Method m2 = cls.getEnclosingMethod();
      assertEquals(m1, m2);
      assertTrue(cls.getEnclosingMethod().equals(ClassTest.TestEnclosedClass.class.getMethod("getLocalClassObj", new Class[0])));
    }
  }

  @Test
  public void isAnonymousClassTest (){
    if (verifyNoPropertyViolation()){
      Class cls = (new ClassTest.TestEnclosedClass()).getAnonymousClassObj().getClass();
      assertTrue(cls.isAnonymousClass());
      assertFalse(Class.class.isAnonymousClass());
    }
  }

  @Test
  public void isEnumTest (){
    if (verifyNoPropertyViolation()){
      assertTrue(TestEnum.class.isEnum());
      assertFalse(Class.class.isEnum());
    }
  }

  @Test
  public void getDeclaringClassTest (){
    if (verifyNoPropertyViolation()){
      assertTrue(TestEnclosedClass.class.getDeclaringClass() == ClassTest.class);
      assertNull(Class.class.getDeclaringClass());
      Class anonymousCls = (new ClassTest.TestEnclosedClass()).getAnonymousClassObj().getClass();
      assertNull(anonymousCls.getDeclaringClass());
      Class localCls = (new ClassTest.TestEnclosedClass()).foo.getClass();
      assertNull(localCls.getDeclaringClass());
    }
  }

  @Test
  public void isLocalClassTest (){
    if (verifyNoPropertyViolation()){
      TestEnclosedClass testObj = new ClassTest.TestEnclosedClass();
      assertTrue(testObj.foo.getClass().isLocalClass());
      assertTrue(testObj.getLocalClassObj().getClass().isLocalClass());
      assertFalse(Class.class.isLocalClass());
    }
  }

  @Test
  public void isMemberClassTest (){
    if (verifyNoPropertyViolation()){
      assertTrue(TestEnclosedClass.MemberClass.class.isMemberClass());
      assertFalse(Class.class.isMemberClass());
      assertFalse(((new TestEnclosedClass()).getLocalClassObj().getClass().isMemberClass()));
    }
  }

  @Test
  public void isSyntheticTest (){
    if (verifyNoPropertyViolation()){
      assertFalse(Class.class.isSynthetic());
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Inherited
  public @interface A9 {
  }

  @Retention(RetentionPolicy.RUNTIME)
  public @interface A10 {
  }

  @A9()
  public static class Parent {
  }

  @A10
  public static class Child1 extends Parent {
  }

  public static class Child2 extends Child1 {
  }

  @Test
  public void getAnnotationsTest (){
    if (verifyNoPropertyViolation()){
      assertTrue(Parent.class.getAnnotations().length == 1);
      assertTrue(Child1.class.getAnnotations().length == 2);
      assertTrue(Child2.class.getAnnotations().length == 1);
    }
  }
  
  @Test
  public void testIsAnnotation(){
    if (verifyNoPropertyViolation()){
      assertFalse( Child2.class.isAnnotation());
      assertTrue( A9.class.isAnnotation());
    }
  }
  
  @Test
  public void testIsAnnotationPresent(){
    if (verifyNoPropertyViolation()){
      assertFalse( Child2.class.isAnnotationPresent(SuppressWarnings.class));
      assertTrue( Child1.class.isAnnotationPresent(A10.class));
      assertTrue( Child1.class.isAnnotationPresent(A9.class));
      assertTrue( Child2.class.isAnnotationPresent(A9.class));
    }    
  }

  @Test
  public void getResourceTest() {
    if (verifyNoPropertyViolation()){
      Class c = ClassLoader.class;
      assertNotNull(c.getResource("Class.class"));
      assertNotNull(c.getResource("/java/lang/Class.class"));
      assertNull(c.getResource("java/lang/Class.class"));
      assertEquals(c.getResource("Class.class"),c.getResource("/java/lang/Class.class"));
      assertNull(c.getResource("not_existing_resources"));
    }
  }

  //--- getResourceAsStream() regression tests ---
  // resources have to be loaded from the SUT classpath, i.e. the location
  // of the class they are requested on, not from the native peer location

  static final String RESOURCE_FIXTURE_NAME = "ClassTestFixture.txt";
  static final String RESOURCE_FIXTURE_DIR = "gov/nasa/jpf/test/java/lang";
  // NOTE - deliberately pure ASCII: comparing non-ASCII content would require
  // charset decoding under JPF, which is a separate known limitation
  // (orphan java.lang.StringCoding native peers)
  static final String RESOURCE_FIXTURE_CONTENT =
          "Hello from SUT classpath!\nSecond line\n";

  /**
   * create the resource fixtures next to the compiled test class so that
   * they are on the SUT classpath when JPF verifies this test class
   */
  private static void ensureResourceFixtures () {
    try {
      File clsFile = new File(ClassTest.class.getResource("ClassTest.class").toURI());
      File dir = clsFile.getParentFile();

      File fixture = new File(dir, RESOURCE_FIXTURE_NAME);
      Files.write(fixture.toPath(),
                  RESOURCE_FIXTURE_CONTENT.getBytes(StandardCharsets.UTF_8));
      fixture.deleteOnExit();

      File subDir = new File(dir, "resource_subdir");
      Files.createDirectories(subDir.toPath());
      File subFixture = new File(subDir, RESOURCE_FIXTURE_NAME);
      Files.write(subFixture.toPath(), "nested fixture".getBytes(StandardCharsets.UTF_8));
      subFixture.deleteOnExit();
    } catch (IOException x){
      throw new RuntimeException("failed to create resource fixtures", x);
    } catch (java.net.URISyntaxException x){
      throw new RuntimeException("failed to locate compiled ClassTest.class", x);
    }
  }

  @Test
  public void getResourceAsStreamRelativeTest() {
    if (!isJPFRun()){
      ensureResourceFixtures();
    }

    if (verifyNoPropertyViolation()){
      // relative lookup resolves against the package of the requesting class
      InputStream is = ClassTest.class.getResourceAsStream(RESOURCE_FIXTURE_NAME);
      assertNotNull("relative lookup should find the fixture", is);
      assertStreamContent(is, RESOURCE_FIXTURE_CONTENT);
    }
  }

  @Test
  public void getResourceAsStreamAbsoluteTest() {
    if (!isJPFRun()){
      ensureResourceFixtures();
    }

    if (verifyNoPropertyViolation()){
      InputStream is = ClassTest.class.getResourceAsStream(
              "/" + RESOURCE_FIXTURE_DIR + "/" + RESOURCE_FIXTURE_NAME);
      assertNotNull("absolute lookup should find the fixture", is);
      assertStreamContent(is, RESOURCE_FIXTURE_CONTENT);
    }
  }

  @Test
  public void getResourceAsStreamNestedRelativeTest() {
    if (!isJPFRun()){
      ensureResourceFixtures();
    }

    if (verifyNoPropertyViolation()){
      InputStream is = ClassTest.class.getResourceAsStream(
              "resource_subdir/" + RESOURCE_FIXTURE_NAME);
      assertNotNull("nested relative lookup should find the fixture", is);
      assertStreamContent(is, "nested fixture");
    }
  }

  @Test
  public void getResourceAsStreamNotFoundTest() {
    if (!isJPFRun()){
      ensureResourceFixtures();
    }

    if (verifyNoPropertyViolation()){
      Class<?> c = ClassTest.class;

      // non-existing absolute and relative names
      assertNull(c.getResourceAsStream(
              "/" + RESOURCE_FIXTURE_DIR + "/no_such_resource.xyz"));
      assertNull(c.getResourceAsStream("no_such_resource.xyz"));

      // scoping - requesting from another class resolves against ITS package
      assertNull(System.class.getResourceAsStream(RESOURCE_FIXTURE_NAME));

      // a fully qualified path without leading '/' is still package-relative,
      // i.e. must not find the fixture in its real location
      assertNull(c.getResourceAsStream(RESOURCE_FIXTURE_DIR + "/" + RESOURCE_FIXTURE_NAME));
    }
  }

  @Test
  public void getResourceAsStreamFromJarTest() {
    if (verifyNoPropertyViolation()){
      // asm-9.5.jar is part of jpf-core.test_classpath, hence on the SUT
      // classpath; this exercises lookup inside a jar classpath entry.
      // Every Java classfile starts with the magic bytes 0xCAFEBABE.
      InputStream is = ClassTest.class.getResourceAsStream(
              "/org/objectweb/asm/AnnotationVisitor.class");
      assertNotNull("jar entry lookup should find AnnotationVisitor.class", is);

      try {
        int b0 = is.read();
        int b1 = is.read();
        int b2 = is.read();
        int b3 = is.read();
        is.close();

        assertEquals(0xCA, b0);
        assertEquals(0xFE, b1);
        assertEquals(0xBA, b2);
        assertEquals(0xBE, b3);
      } catch (IOException x){
        throw new RuntimeException(x);
      }
    }
  }

  @Test
  public void getResourceAsStreamConsistencyTest() {
    if (!isJPFRun()){
      ensureResourceFixtures();
    }

    if (verifyNoPropertyViolation()){
      Class<?> c = ClassTest.class;
      String absName = "/" + RESOURCE_FIXTURE_DIR + "/" + RESOURCE_FIXTURE_NAME;
      String missingName = "/" + RESOURCE_FIXTURE_DIR + "/no_such_resource.xyz";

      // getResource() and getResourceAsStream() have to agree on existence
      assertNotNull(c.getResource(absName));
      assertNotNull(c.getResourceAsStream(absName));

      assertNull(c.getResource(missingName));
      assertNull(c.getResourceAsStream(missingName));
    }
  }

  @Test
  public void getResourceAsStreamNullNameTest() {
    if (verifyUnhandledException("java.lang.NullPointerException")){
      // same behavior as on a standard JVM
      ClassTest.class.getResourceAsStream(null);
    }
  }

  /**
   * reads the stream content and compares it byte-wise against the expected
   * (pure ASCII) content, avoiding any charset decoding under JPF
   */
  private static void assertStreamContent (InputStream is, String expected) {
    try {
      ByteArrayOutputStream bos = new ByteArrayOutputStream();
      byte[] buf = new byte[256];
      for (int n; (n = is.read(buf)) > 0; ){
        bos.write(buf, 0, n);
      }
      is.close();

      byte[] actual = bos.toByteArray();
      assertEquals("content length mismatch", expected.length(), actual.length);
      for (int i = 0; i < actual.length; i++){
        if ((byte) expected.charAt(i) != actual[i]){
          fail("content mismatch at byte index " + i);
        }
      }
    } catch (IOException x){
      throw new RuntimeException(x);
    }
  }
}

class TestNewInstance {
  static class Protected {
    boolean assertData = true;

    protected Protected() {
    }
  }

  static class Private {
    boolean assertData = true;

    private Private() {
    }
  }
}
