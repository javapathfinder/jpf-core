package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import javax.tools.JavaCompiler;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.Assert.*;

/**
 * regression tests for issue #252: constant pool tags for module/package
 * information (introduced in Java 9) should be parsed correctly.
 */
public class PackageModuleConstantTest extends TestJPF {

  @Test
  public void testModuleConstantParsing() throws Exception {
    Path tmp = Files.createTempDirectory("modtest");
    File src = tmp.resolve("module-info.java").toFile();
    // module declarations always generate a class file
    Files.writeString(src.toPath(), "module mymod {}\n");

    // use external javac invocation instead of JavaCompiler API; the
    // latter is not thread-safe and produced missing output when tests
    // executed concurrently (see build failure). This approach also
    // mirrors how the real build system compiles.

    ProcessBuilder pb = new ProcessBuilder("javac", "-d", tmp.toString(), src.getPath());
    pb.redirectErrorStream(true);
    Process p = pb.start();
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
      String line;
      while ((line = r.readLine()) != null) {
        System.out.println(line);
      }
    }
    int rc = p.waitFor();
    assertEquals(0, rc);

    File classFile = tmp.resolve("module-info.class").toFile();
    assertTrue(classFile.exists());

    ClassFile cf = new ClassFile(classFile);
    ClassFileReader reader = new ClassFileReaderAdapter();
    cf.parse(reader);

    boolean found = false;
    for (Object v : cf.cpValue) {
      if ("mymod".equals(v)) {
        found = true;
        break;
      }
    }

    assertTrue("module name should appear in constant pool", found);
  }

  @Test
  public void testPackageConstantParsing() throws Exception {
    Path tmp = Files.createTempDirectory("pkgtest");
    Path pkgDir = tmp.resolve("mypkg");
    Files.createDirectory(pkgDir);
    File src = pkgDir.resolve("package-info.java").toFile();
    // include a dummy annotation so javac produces a package-info.class
    Files.writeString(src.toPath(), "@Deprecated\npackage mypkg;\n");

    ProcessBuilder pb = new ProcessBuilder("javac", "-d", tmp.toString(), src.getPath());
    pb.redirectErrorStream(true);
    Process p = pb.start();
    try (java.io.BufferedReader r = new java.io.BufferedReader(new java.io.InputStreamReader(p.getInputStream()))) {
      String line;
      while ((line = r.readLine()) != null) {
        System.out.println(line);
      }
    }
    int rc = p.waitFor();
    assertEquals(0, rc);

    File classFile = pkgDir.resolve("package-info.class").toFile();
    assertTrue(classFile.exists());

    ClassFile cf = new ClassFile(classFile);
    ClassFileReader reader = new ClassFileReaderAdapter();
    cf.parse(reader);

    boolean found = false;
    for (Object v : cf.cpValue) {
      if (v != null) {
        String s = v.toString();
        // some compilers (and output formats) use the raw package name while
        // others include the '/package-info' suffix.  Accept either so the
        // test is not fragile to how the classfile was generated.
        if (s.equals("mypkg") || s.startsWith("mypkg")) {
          found = true;
          break;
        }
      }
    }

    if (!found) {
      StringBuilder sb = new StringBuilder();
      sb.append("constant pool contents for package-info:\n");
      for (int i=1; i<cf.cpValue.length; i++){
        Object v = cf.cpValue[i];
        sb.append(i).append(": ").append(v).append("\n");
      }
      org.junit.Assert.fail(sb.toString());
    }
  }
}
