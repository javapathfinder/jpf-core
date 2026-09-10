package gov.nasa.jpf.test.java.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.jar.Manifest;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class JarFileTest extends TestJPF {

    static String userDir = System.getProperty("user.dir");
    static String relativeJarPath = "src/tests/test.jar";
    static String fullPath = userDir + "/" + relativeJarPath;

    @Test
    public void testEntries() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            assertNotNull(jf.getEntry("test.txt"));
            // entries should contain test.txt
            Enumeration<JarEntry> e = jf.entries();
            boolean found = false;
            while (e.hasMoreElements()) {
                JarEntry je = e.nextElement();
                if ("test.txt".equals(je.getName())) {
                    found = true;
                    break;
                }
            }
            assertTrue(found);
            jf.close();
        }
    }

    @Test
    public void testSize() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            // test.jar should have at least test.txt, maybe manifest
            assertTrue(jf.size() >= 1);
            jf.close();
        }
    }

    @Test
    public void testGetInputStream() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            JarEntry je = jf.getJarEntry("test.txt");
            assertNotNull(je);
            InputStream is = jf.getInputStream(je);
            assertNotNull(is);
            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String content = result.toString(StandardCharsets.UTF_8.name());
            System.out.println(content);
            assertEquals("Hello\n", content);
            jf.close();
        }
    }

    @Test
    public void testGetManifest() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            Manifest man = jf.getManifest();
            // test.jar may or may not have manifest; if present, check Manifest-Version
            if (man != null) {
                String version = man.getMainAttributes().getValue("Manifest-Version");
                assertNotNull(version);
                assertEquals("1.0", version);
            } else {
                // If no manifest, entries still work
                assertNotNull(jf.getEntry("test.txt"));
            }
            jf.close();
        }
    }

    @Test
    public void testGetJarEntry() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            JarEntry je = jf.getJarEntry("test.txt");
            assertNotNull(je);
            assertEquals("test.txt", je.getName());
            JarEntry nonExistent = jf.getJarEntry("nonexistent.txt");
            assertNull(nonExistent);
            jf.close();
        }
    }

    @Test(expected = IllegalStateException.class)
    public void testClose() throws IOException {
        JarFile closedJar = new JarFile(fullPath);
        closedJar.close();
        closedJar.getEntry("test.txt");
    }

    @Test
    public void testNonExistentEntry() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            assertNull(jf.getEntry("nonexistent.txt"));
            jf.close();
        }
    }

    @Test
    public void testGetName() throws IOException {
        if (verifyNoPropertyViolation()) {
            JarFile jf = new JarFile(fullPath);
            assertEquals(jf.getName(), fullPath);
            jf.close();
        }
    }
}
