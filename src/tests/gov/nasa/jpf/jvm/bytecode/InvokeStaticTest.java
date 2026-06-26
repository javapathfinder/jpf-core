package gov.nasa.jpf.jvm.bytecode;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.io.File;
import java.nio.file.Files;
import java.util.Arrays;
import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;

public class InvokeStaticTest extends TestJPF {

  @Test
  public void testIncompatibleClassChangeError() throws Exception {
    File tempDir = Files.createTempDirectory("jpf_invoke_static_test").toFile();
    tempDir.deleteOnExit();
    
    File cJava = new File(tempDir, "C.java");
    File dJava = new File(tempDir, "D.java");
    
    cJava.deleteOnExit();
    dJava.deleteOnExit();

    Files.write(cJava.toPath(), Arrays.asList(
      "public class C {",
      "  public static void main(String[] args) {",
      "    D.m();",
      "  }",
      "}"
    ));

    Files.write(dJava.toPath(), Arrays.asList(
      "public class D {",
      "  public static void m() {}",
      "}"
    ));

    JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
    if (compiler == null) {
      System.out.println("No system java compiler found, skipping test");
      return;
    }
    
    int res = compiler.run(null, null, null, cJava.getPath(), dJava.getPath());
    assertTrue("Initial compilation failed", res == 0);

    // Recompile D.java without static
    Files.write(dJava.toPath(), Arrays.asList(
      "public class D {",
      "  public void m() {}",
      "}"
    ));
    res = compiler.run(null, null, null, dJava.getPath());
    assertTrue("Second compilation failed", res == 0);

    // Schedule C class on temp classpath
    if (verifyUnhandledException("java.lang.IncompatibleClassChangeError", "+classpath=" + tempDir.getAbsolutePath(), "C")) {
       // This block runs in JPF. 
       // However, since we pass "C" as application class in the args to verifyUnhandledException, 
       // JPF will execute C.main(). We don't need to put any code here.
    }
  }
}
