package java17;

import gov.nasa.jpf.util.test.TestJPF;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.nio.file.Files;

import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;

/**
 * regression tests for java.lang.runtime.SwitchBootstraps support, which backs
 * Java 17+ pattern matching switches (JEP 441).
 *
 * Pattern matching switch is a preview feature in Java 17, so we cannot write it
 * in the test sources directly. Instead we generate the required invokedynamic
 * call sites with ASM and verify the semantics of the resulting index:
 *
 *   null receiver                                   -> -1
 *   first matching label at or after restart index   -> its index
 *   no match                                         -> number of labels
 */
public class PatternSwitchTest extends TestJPF {

  static final String TYPE_SWITCH_CLS = "java17.GeneratedTypeSwitch";
  static final String ENUM_SWITCH_CLS = "java17.GeneratedEnumSwitch";

  //--- fixtures the generated switches dispatch on

  public static class Alpha {}
  public static class Beta {}

  public enum Hue { RED, GREEN, BLUE }

  //--- bytecode generation (host side only)

  /**
   * generates a class with
   *   public static int test (Object receiver, int restartIndex)
   * whose body is a single invokedynamic against SwitchBootstraps, returning the
   * computed switch index
   */
  private static byte[] generateSwitchClass (String internalName, String bsmName,
                                             Object[] labels) {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_MAXS | ClassWriter.COMPUTE_FRAMES);
    cw.visit(Opcodes.V17, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER,
             internalName, null, "java/lang/Object", null);

    MethodVisitor ctor = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    ctor.visitCode();
    ctor.visitVarInsn(Opcodes.ALOAD, 0);
    ctor.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    ctor.visitInsn(Opcodes.RETURN);
    ctor.visitMaxs(0, 0);
    ctor.visitEnd();

    Handle bsm = new Handle(Opcodes.H_INVOKESTATIC,
            "java/lang/runtime/SwitchBootstraps", bsmName,
            "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;"
            + "Ljava/lang/invoke/MethodType;[Ljava/lang/Object;)"
            + "Ljava/lang/invoke/CallSite;",
            false);

    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC,
            "test", "(Ljava/lang/Object;I)I", null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.ALOAD, 0);              // receiver
    mv.visitVarInsn(Opcodes.ILOAD, 1);              // restart index
    mv.visitInvokeDynamicInsn(bsmName, "(Ljava/lang/Object;I)I", bsm, labels);
    mv.visitInsn(Opcodes.IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cw.visitEnd();
    return cw.toByteArray();
  }

  /**
   * writes the generated classes next to the compiled test classes so that they
   * are on the classpath JPF uses for the system under test
   */
  private static void ensureGeneratedClasses () {
    try {
      File clsFile = new File(PatternSwitchTest.class.getResource("PatternSwitchTest.class").toURI());
      File dir = clsFile.getParentFile();

      byte[] typeSwitch = generateSwitchClass("java17/GeneratedTypeSwitch", "typeSwitch",
              new Object[] {
                Type.getObjectType("java17/PatternSwitchTest$Alpha"),
                Type.getObjectType("java17/PatternSwitchTest$Beta")
              });
      File typeSwitchFile = new File(dir, "GeneratedTypeSwitch.class");
      Files.write(typeSwitchFile.toPath(), typeSwitch);
      typeSwitchFile.deleteOnExit();

      byte[] enumSwitch = generateSwitchClass("java17/GeneratedEnumSwitch", "enumSwitch",
              new Object[] { "RED", "BLUE" });
      File enumSwitchFile = new File(dir, "GeneratedEnumSwitch.class");
      Files.write(enumSwitchFile.toPath(), enumSwitch);
      enumSwitchFile.deleteOnExit();

    } catch (IOException x) {
      throw new RuntimeException("failed to write generated switch classes", x);
    } catch (java.net.URISyntaxException x) {
      throw new RuntimeException("failed to locate compiled test class", x);
    }
  }

  //--- helper executed under JPF

  private static int invokeSwitch (String clsName, Object receiver, int restartIndex) {
    try {
      Class<?> cls = Class.forName(clsName);
      Method m = cls.getMethod("test", Object.class, int.class);
      Object result = m.invoke(null, receiver, restartIndex);
      return ((Integer) result).intValue();
    } catch (Exception x) {
      throw new RuntimeException("failed to invoke generated switch", x);
    }
  }

  //--- tests

  @Test
  public void testTypeSwitchMatchesFirstLabel () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      assertEquals(0, invokeSwitch(TYPE_SWITCH_CLS, new Alpha(), 0));
    }
  }

  @Test
  public void testTypeSwitchMatchesSecondLabel () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      assertEquals(1, invokeSwitch(TYPE_SWITCH_CLS, new Beta(), 0));
    }
  }

  @Test
  public void testTypeSwitchNoMatchReturnsLabelCount () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      // no label matches - the index has to be the number of labels so that the
      // default branch of the generated switch is taken
      assertEquals(2, invokeSwitch(TYPE_SWITCH_CLS, "not a fixture", 0));
    }
  }

  @Test
  public void testTypeSwitchNullReceiver () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      // null never matches a type label
      assertEquals(-1, invokeSwitch(TYPE_SWITCH_CLS, null, 0));
    }
  }

  @Test
  public void testTypeSwitchRestartIndexSkipsEarlierLabels () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      // starting the search behind the matching label must not match it again
      assertEquals(2, invokeSwitch(TYPE_SWITCH_CLS, new Alpha(), 1));
      // ... while a later label is still found
      assertEquals(1, invokeSwitch(TYPE_SWITCH_CLS, new Beta(), 1));
    }
  }

  @Test
  public void testTypeSwitchSubclassMatchesLabel () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      // labels match by instanceof, not by exact class
      assertEquals(0, invokeSwitch(TYPE_SWITCH_CLS, new Alpha(){}, 0));
    }
  }

  @Test
  public void testEnumSwitch () {
    if (!isJPFRun()){
      ensureGeneratedClasses();
    }

    if (verifyNoPropertyViolation()){
      assertEquals(0, invokeSwitch(ENUM_SWITCH_CLS, Hue.RED, 0));
      assertEquals(1, invokeSwitch(ENUM_SWITCH_CLS, Hue.BLUE, 0));
      // GREEN is not a label of this switch
      assertEquals(2, invokeSwitch(ENUM_SWITCH_CLS, Hue.GREEN, 0));
      assertEquals(-1, invokeSwitch(ENUM_SWITCH_CLS, null, 0));
    }
  }
}
