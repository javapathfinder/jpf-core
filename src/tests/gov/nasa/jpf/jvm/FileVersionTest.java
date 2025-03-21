package gov.nasa.jpf.jvm;

import gov.nasa.jpf.util.test.TestJPF;
import gov.nasa.jpf.vm.ClassParseException;
import org.junit.Test;

import java.io.*;

public class FileVersionTest extends TestJPF {

    private static final String JAVA11_CLASS = "/TestClassJava11.class";
    private static final String JAVA17_CLASS = "/TestClassJava17.class";

    // loading a .class file into a byte array
    private byte[] loadClassFile(String resourceName) throws IOException {
        try (InputStream is = getClass().getResourceAsStream(resourceName)) {
            if (is == null) throw new IOException("Resource not found: " + resourceName);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            // we choose here buffer size 1024 cause its enough to read most .class files in one or two iterations
            // smaller buffer size like 256 will require more operations and larger buffers will waster memory
            // i'm not sure which is suitable for this since the test classes we complied is empty but i think both could work
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                bos.write(buffer, 0, bytesRead);
            }
            return bos.toByteArray();
        }
    }


    @Test
    public void testSupportedVersionJava11() throws IOException, ClassParseException {
        byte[] classData = loadClassFile(JAVA11_CLASS);
        ClassFile classFile = new ClassFile(classData);
        ClassFileReader reader = new ClassFileReaderAdapter();
        // this should pass with no exceptions
        classFile.parse(reader);
    }

    @Test(expected = ClassParseException.class)
    public void testUnsupportedVersionJava17() throws IOException, ClassParseException {
        byte[] classData = loadClassFile(JAVA17_CLASS);
        ClassFile classFile = new ClassFile(classData);
        ClassFileReader reader = new ClassFileReaderAdapter();
        // this should throw ClassParseException
        classFile.parse(reader);
    }
}