package gov.nasa.jpf;

import gov.nasa.jpf.util.test.TestJPF;

import java.net.URL;

import org.junit.Test;

/**
 * Tests for JPFClassLoader accessor methods added for per-test
 * ClassLoader isolation (getURLs, getNativeLibs).
 */
public class JPFClassLoaderTest extends TestJPF {

  @Test
  public void testGetURLsReturnsConstructorURLs() throws Exception {
    URL[] urls = { new URL("file:///tmp/test1.jar"), new URL("file:///tmp/test2.jar") };
    JPFClassLoader cl = new JPFClassLoader(urls);

    URL[] result = cl.getURLs();
    assertNotNull(result);
    assertEquals(urls.length, result.length);
    for (int i = 0; i < urls.length; i++) {
      assertEquals(urls[i], result[i]);
    }
  }

  @Test
  public void testGetURLsEmptyByDefault() {
    JPFClassLoader cl = new JPFClassLoader(new URL[0]);
    URL[] result = cl.getURLs();
    assertNotNull(result);
    assertEquals(0, result.length);
  }

  @Test
  public void testGetNativeLibsFromConstructor() {
    String[] nativeLibs = { "/usr/lib/libfoo.so", "/usr/lib/libbar.so" };
    JPFClassLoader cl = new JPFClassLoader(new URL[0], nativeLibs, null);

    String[] result = cl.getNativeLibs();
    assertNotNull(result);
    assertEquals(nativeLibs.length, result.length);
    for (int i = 0; i < nativeLibs.length; i++) {
      assertEquals(nativeLibs[i], result[i]);
    }
  }

  @Test
  public void testGetNativeLibsNullWhenNotSet() {
    JPFClassLoader cl = new JPFClassLoader(new URL[0]);
    assertNull(cl.getNativeLibs());
  }

  @Test
  public void testSetNativeLibs() {
    JPFClassLoader cl = new JPFClassLoader(new URL[0]);
    assertNull(cl.getNativeLibs());

    String[] nativeLibs = { "/usr/lib/libtest.so" };
    cl.setNativeLibs(nativeLibs);

    String[] result = cl.getNativeLibs();
    assertNotNull(result);
    assertEquals(1, result.length);
    assertEquals("/usr/lib/libtest.so", result[0]);
  }

  @Test
  public void testChildClassLoaderDelegation() throws Exception {
    URL[] urls = { new URL("file:///tmp/test.jar") };
    String[] nativeLibs = { "/usr/lib/libfoo.so" };
    JPFClassLoader parent = new JPFClassLoader(urls, nativeLibs, null);

    JPFClassLoader child = new JPFClassLoader(parent.getURLs(), parent.getNativeLibs(), parent);

    assertNotNull(child.getParent());
    assertEquals(parent, child.getParent());

    URL[] childUrls = child.getURLs();
    assertNotNull(childUrls);
    assertEquals(1, childUrls.length);
    assertEquals(urls[0], childUrls[0]);

    String[] childLibs = child.getNativeLibs();
    assertNotNull(childLibs);
    assertEquals(1, childLibs.length);
    assertEquals(nativeLibs[0], childLibs[0]);
  }

  @Test
  public void testChildClassLoaderIndependentURLs() throws Exception {
    URL[] parentUrls = { new URL("file:///tmp/parent.jar") };
    JPFClassLoader parent = new JPFClassLoader(parentUrls, null, null);

    JPFClassLoader child = new JPFClassLoader(parent.getURLs(), null, parent);

    child.addURL(new URL("file:///tmp/child.jar"));

    assertEquals(1, parent.getURLs().length);
    assertEquals(2, child.getURLs().length);
  }
}
