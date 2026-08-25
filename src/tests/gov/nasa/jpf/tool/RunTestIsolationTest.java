package gov.nasa.jpf.tool;

import gov.nasa.jpf.Config;
import gov.nasa.jpf.JPFClassLoader;
import gov.nasa.jpf.util.test.TestJPF;

import java.net.URL;

import org.junit.Test;

/**
 * Tests for RunTest's per-test ClassLoader isolation feature.
 * These tests run on the host JVM (not inside JPF) since they test
 * the host-side test runner infrastructure.
 */
public class RunTestIsolationTest extends TestJPF {

  @Test
  public void testCreateIsolatedClassLoaderCreatesChild() throws Exception {
    URL[] urls = { new URL("file:///tmp/test.jar") };
    String[] nativeLibs = { "/usr/lib/libfoo.so" };
    JPFClassLoader parent = new JPFClassLoader(urls, nativeLibs, null);

    String[] testPathElements = { "src/tests" };
    JPFClassLoader isolated = RunTest.createIsolatedTestClassLoader(parent, testPathElements);

    assertNotNull(isolated);
    assertTrue("Isolated CL should be different from parent", parent != isolated);
    assertEquals(parent, isolated.getParent());
  }

  @Test
  public void testIsolatedClassLoaderInheritsParentURLs() throws Exception {
    URL[] parentUrls = { new URL("file:///tmp/parent.jar") };
    JPFClassLoader parent = new JPFClassLoader(parentUrls, null, null);

    String[] testPathElements = {};
    JPFClassLoader isolated = RunTest.createIsolatedTestClassLoader(parent, testPathElements);

    URL[] isolatedUrls = isolated.getURLs();
    assertNotNull(isolatedUrls);
    assertTrue(isolatedUrls.length >= parentUrls.length);
    for (URL url : parentUrls) {
      boolean found = false;
      for (URL iUrl : isolatedUrls) {
        if (url.equals(iUrl)) {
          found = true;
          break;
        }
      }
      assertTrue("Parent URL should be inherited: " + url, found);
    }
  }

  @Test
  public void testIsolatedClassLoaderHasSeparateNamespace() throws Exception {
    URL[] urls = { new URL("file:///tmp/test.jar") };
    JPFClassLoader parent = new JPFClassLoader(urls, null, null);

    String[] testPathElements = {};
    JPFClassLoader isolated1 = RunTest.createIsolatedTestClassLoader(parent, testPathElements);
    JPFClassLoader isolated2 = RunTest.createIsolatedTestClassLoader(parent, testPathElements);

    assertTrue("Two isolated CLs should be different instances", isolated1 != isolated2);
    assertEquals(parent, isolated1.getParent());
    assertEquals(parent, isolated2.getParent());
  }

  @Test
  public void testConfigIsolatedClassloaderDefault() {
    Config config = new Config(new String[0]);
    boolean isolated = config.getBoolean("jpf.test.isolated_classloader", false);
    assertFalse("Isolation should be disabled by default", isolated);
  }

  @Test
  public void testConfigIsolatedClassloaderEnabled() {
    String[] args = { "+jpf.test.isolated_classloader=true" };
    Config config = new Config(args);
    boolean isolated = config.getBoolean("jpf.test.isolated_classloader", false);
    assertTrue("Isolation should be enabled", isolated);
  }

  @Test
  public void testIsolatedClassLoaderSharesNativeLibs() throws Exception {
    URL[] urls = { new URL("file:///tmp/test.jar") };
    String[] nativeLibs = { "/usr/lib/libfoo.so", "/usr/lib/libbar.so" };
    JPFClassLoader parent = new JPFClassLoader(urls, nativeLibs, null);

    String[] testPathElements = {};
    JPFClassLoader isolated = RunTest.createIsolatedTestClassLoader(parent, testPathElements);

    String[] isolatedLibs = isolated.getNativeLibs();
    assertNotNull(isolatedLibs);
    assertEquals(nativeLibs.length, isolatedLibs.length);
    for (int i = 0; i < nativeLibs.length; i++) {
      assertEquals(nativeLibs[i], isolatedLibs[i]);
    }
  }
}
