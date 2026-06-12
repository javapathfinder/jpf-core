package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassInfo;
import gov.nasa.jpf.vm.ClassParseException;

import java.io.File;

import org.junit.Test;

public class SealedClassParsingTest extends TestJPF {

  static final String PREFIX = "gov.nasa.jpf.jvm.SealedClassParsingTest$";

  public sealed interface Shape permits Circle, Square {}
  public static final class Circle implements Shape {}
  public static final class Square implements Shape {}

  public abstract static sealed class Vehicle permits Car {}
  public static final class Car extends Vehicle {}

  public static class PlainClass {}

  private static ClassInfo parse(String simpleName) throws ClassParseException {
    File file = new File("build/tests/gov/nasa/jpf/jvm/SealedClassParsingTest$" + simpleName + ".class");
    return new NonResolvedClassInfo(PREFIX + simpleName, file);
  }

  @Test
  public void testSealedInterfaceIsDetected() throws ClassParseException {
    ClassInfo ci = parse("Shape");
    assertTrue("sealed interface not detected as sealed", ci.isSealed());
  }

  @Test
  public void testSealedClassIsDetected() throws ClassParseException {
    ClassInfo ci = parse("Vehicle");
    assertTrue("sealed class not detected as sealed", ci.isSealed());
  }

  @Test
  public void testPermittedSubclassesContainExpectedNames() throws ClassParseException {
    ClassInfo ci = parse("Shape");
    assertTrue("Circle should be a permitted subclass",
               ci.isPermittedSubclass(PREFIX + "Circle"));
    assertTrue("Square should be a permitted subclass",
               ci.isPermittedSubclass(PREFIX + "Square"));
  }

  @Test
  public void testNonPermittedSubclassIsRejected() throws ClassParseException {
    ClassInfo ci = parse("Shape");
    assertFalse("PlainClass must not be a permitted subclass",
                ci.isPermittedSubclass(PREFIX + "PlainClass"));
  }

  @Test
  public void testNonSealedClassIsNotSealed() throws ClassParseException {
    ClassInfo ci = parse("PlainClass");
    assertFalse("plain class must not be detected as sealed", ci.isSealed());
  }
}
