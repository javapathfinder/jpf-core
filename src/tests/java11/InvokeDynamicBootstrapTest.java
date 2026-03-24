package java11;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.Handle;
import org.objectweb.asm.MethodVisitor;
import org.objectweb.asm.Opcodes;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.lang.reflect.Method;
import java.nio.file.Paths;

public class InvokeDynamicBootstrapTest extends TestJPF {
  private static final String GENERATED_CLASS_NAME = "java11.gen.DynamicBootstrapSubject";
  private static final String GENERATED_INTERNAL_NAME = "java11/gen/DynamicBootstrapSubject";
  private static final String GENERATED_OUTPUT_DIR = "build/tests-generated";

  @Test
  public void testDynamicBootstrapWithoutArguments() throws Exception {
    ensureGeneratedClass();

    if (verifyNoPropertyViolation("+classpath=" + GENERATED_OUTPUT_DIR + ",build/tests")) {
      Class<?> cls = Class.forName(GENERATED_CLASS_NAME);
      Method method = cls.getMethod("callInt");
      Object result = method.invoke(null);

      assertEquals(42, ((Integer) result).intValue());
    }
  }

  @Test
  public void testDynamicBootstrapWithStringArgument() throws Exception {
    ensureGeneratedClass();

    if (verifyNoPropertyViolation("+classpath=" + GENERATED_OUTPUT_DIR + ",build/tests")) {
      Class<?> cls = Class.forName(GENERATED_CLASS_NAME);
      Method method = cls.getMethod("callString", String.class);
      Object result = method.invoke(null, "hello");

      assertEquals("prefix:hello", result);
    }
  }

  @Test
  public void testDynamicBootstrapWithStaticBootstrapArgument() throws Exception {
    ensureGeneratedClass();

    if (verifyNoPropertyViolation("+classpath=" + GENERATED_OUTPUT_DIR + ",build/tests")) {
      Class<?> cls = Class.forName(GENERATED_CLASS_NAME);
      Method method = cls.getMethod("callStringWithConstant", String.class);
      Object result = method.invoke(null, "hello");

      assertEquals("constant:hello", result);
    }
  }

  @Test
  public void testDynamicBootstrapWithDoubleArgument() throws Exception {
    ensureGeneratedClass();

    if (verifyNoPropertyViolation("+classpath=" + GENERATED_OUTPUT_DIR + ",build/tests")) {
      Class<?> cls = Class.forName(GENERATED_CLASS_NAME);
      Method method = cls.getMethod("callDouble", double.class);
      Object result = method.invoke(null, 3.5d);

      assertEquals("double:3.5", result);
    }
  }

  private static void ensureGeneratedClass() {
    if (isJPFRun()) {
      return;
    }

    try {
      java.nio.file.Path dir = Paths.get(GENERATED_OUTPUT_DIR);
      java.nio.file.Path classFile = dir.resolve(GENERATED_INTERNAL_NAME + ".class");
      java.nio.file.Files.createDirectories(classFile.getParent());
      java.nio.file.Files.write(classFile, createDynamicBootstrapClass());
    } catch (IOException e) {
      throw new UncheckedIOException(e);
    }
  }

  private static byte[] createDynamicBootstrapClass() {
    ClassWriter cw = new ClassWriter(ClassWriter.COMPUTE_FRAMES | ClassWriter.COMPUTE_MAXS);
    cw.visit(Opcodes.V11, Opcodes.ACC_PUBLIC | Opcodes.ACC_SUPER, GENERATED_INTERNAL_NAME, null,
        "java/lang/Object", null);

    MethodVisitor mv = cw.visitMethod(Opcodes.ACC_PUBLIC, "<init>", "()V", null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitMethodInsn(Opcodes.INVOKESPECIAL, "java/lang/Object", "<init>", "()V", false);
    mv.visitInsn(Opcodes.RETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    Handle intBootstrap = new Handle(
        Opcodes.H_INVOKESTATIC,
        "java11/DynamicBootstrapSupport",
        "bootstrapInt",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
        false);

    mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "callInt", "()I", null, null);
    mv.visitCode();
    mv.visitInvokeDynamicInsn("run", "()I", intBootstrap);
    mv.visitInsn(Opcodes.IRETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    Handle stringBootstrap = new Handle(
        Opcodes.H_INVOKESTATIC,
        "java11/DynamicBootstrapSupport",
        "bootstrapString",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
        false);

    mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "callString",
        "(Ljava/lang/String;)Ljava/lang/String;", null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitInvokeDynamicInsn("format", "(Ljava/lang/String;)Ljava/lang/String;", stringBootstrap);
    mv.visitInsn(Opcodes.ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    Handle stringConstantBootstrap = new Handle(
        Opcodes.H_INVOKESTATIC,
        "java11/DynamicBootstrapSupport",
        "bootstrapStringWithConstant",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;Ljava/lang/String;)Ljava/lang/invoke/CallSite;",
        false);

    mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "callStringWithConstant",
        "(Ljava/lang/String;)Ljava/lang/String;", null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.ALOAD, 0);
    mv.visitInvokeDynamicInsn("formatConstant", "(Ljava/lang/String;)Ljava/lang/String;",
        stringConstantBootstrap, "constant:");
    mv.visitInsn(Opcodes.ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    Handle doubleBootstrap = new Handle(
        Opcodes.H_INVOKESTATIC,
        "java11/DynamicBootstrapSupport",
        "bootstrapDouble",
        "(Ljava/lang/invoke/MethodHandles$Lookup;Ljava/lang/String;Ljava/lang/invoke/MethodType;)Ljava/lang/invoke/CallSite;",
        false);

    mv = cw.visitMethod(Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC, "callDouble", "(D)Ljava/lang/String;",
        null, null);
    mv.visitCode();
    mv.visitVarInsn(Opcodes.DLOAD, 0);
    mv.visitInvokeDynamicInsn("describe", "(D)Ljava/lang/String;", doubleBootstrap);
    mv.visitInsn(Opcodes.ARETURN);
    mv.visitMaxs(0, 0);
    mv.visitEnd();

    cw.visitEnd();
    return cw.toByteArray();
  }
}
