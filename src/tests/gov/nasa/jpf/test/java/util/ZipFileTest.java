package gov.nasa.jpf.test.java.util;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import gov.nasa.jpf.util.test.TestJPF;
import org.junit.BeforeClass;
import org.junit.Test;

public class ZipFileTest extends TestJPF {

    static String userDir = System.getProperty("user.dir");
    static String relativeZipPath = "src/tests/test.zip"; //might need to change based on project defaults
    static String fullPath = userDir + "/" + relativeZipPath;

    @Test
    public void testEntries() throws IOException {
        if (verifyNoPropertyViolation()) {
            ZipFile zf = new ZipFile(fullPath);
            assertNotNull(zf.getEntry("test.txt"));
            assertNotNull(zf.getEntry("empty.txt"));
        }
    }

    @Test
    public void testSize() throws IOException {
        if (verifyNoPropertyViolation()) {
            ZipFile zf = new ZipFile(fullPath);
            assertEquals(2, zf.size());
        }
    }

    @Test
    public void testGetInputStream() throws IOException {
        if (verifyNoPropertyViolation()) {
            ZipFile zf = new ZipFile(fullPath);
            ZipEntry ze = zf.getEntry("test.txt");
            InputStream is = zf.getInputStream(ze);

            ByteArrayOutputStream result = new ByteArrayOutputStream();
            byte[] buffer = new byte[1024];
            int length;
            while ((length = is.read(buffer)) != -1) {
                result.write(buffer, 0, length);
            }
            String content = result.toString(StandardCharsets.UTF_8);
            System.out.println(content);
            assertEquals("Hello\n", content);
        }
    }

    @Test(expected = IllegalStateException.class) // TODO: check how to test for specific exceptions in jpf
    public void testClose() throws IOException {
            ZipFile closedZip = new ZipFile(fullPath);
            closedZip.close();
            closedZip.getEntry("empty.txt");
    }

    @Test
    public void testNonExistentEntry() throws IOException {
        if (verifyNoPropertyViolation()) {
            ZipFile zf = new ZipFile(fullPath);
            assertNull(zf.getEntry("nonexistent.txt"));
        }
    }

    @Test
    public void testGetName() throws IOException {
        if (verifyNoPropertyViolation()) {
            ZipFile zf = new ZipFile(fullPath);
            System.out.println(zf.getName());
            System.out.println(fullPath);
            assertEquals(zf.getName(), fullPath);
        }
    }

    @Test
    public void testGetComment() throws IOException {
        if (verifyNoPropertyViolation()) {
            ZipFile zf = new ZipFile(fullPath);
            System.out.println(zf.getComment());
            assertEquals(zf.getComment(), "Comment");
        }
    }
}