package java11;

import org.junit.Test;
import gov.nasa.jpf.util.test.TestJPF;

/**
 * Regression tests for issue #619: invokedynamic string concat bugs.
 *
 * Bug 1: JPFStringConcatHelper.concatenate() misread the char after \u0002 as a
 *        1-based constant index, dropping the constant and the following recipe char.
 * Bug 2: JVMClassInfo.stringConcatenation() never called setResolvedArgs(), so the
 *        constants array was always empty.
 */
public class StringConcatConstantTest extends TestJPF {

  @Test
  public void testConstantWithTagCharIsPreserved() {
    if (verifyNoPropertyViolation()) {
      // "\u0001hi" contains a TAG_ARG char so javac cannot inline it into the
      // recipe -- it emits it as a bootstrap constant (\u0002 TAG_CONST marker).
      // Before the fix: constant was dropped, "world" was also dropped.
      String name = "world";
      String s = "\u0001hi" + name;
      assertEquals("\u0001hiworld", s);
    }
  }

  @Test
  public void testConstantPrecedesArgument() {
    if (verifyNoPropertyViolation()) {
      // Constant \u0002x forces TAG_CONST; the variable follows as TAG_ARG.
      // Verifies that both the constant AND the argument appear in the result.
      String arg = "end";
      String s = "\u0002x" + arg;
      assertEquals("\u0002xend", s);
    }
  }

  @Test
  public void testMultipleConstantsAndArgs() {
    if (verifyNoPropertyViolation()) {
      // Two variables interleaved with two constants that contain tag chars.
      String a = "foo";
      String b = "bar";
      String s = "\u0001prefix" + a + "\u0002mid" + b;
      assertEquals("\u0001prefixfoo\u0002midbar", s);
    }
  }

  @Test
  public void testPlainConcatStillWorks() {
    if (verifyNoPropertyViolation()) {
      // Ordinary concat (no TAG_CONST) must continue to work after the fix.
      String name = "world";
      String s = "Hello " + name + "!";
      assertEquals("Hello world!", s);
    }
  }
}
