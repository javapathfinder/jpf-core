package gov.nasa.jpf.jvm;

import gov.nasa.jpf.vm.ClassFileMatch;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

/**
 * ClassFileContainer to hold classes from the run-time image
 * Uses the new URL scheme, jrt, to references the classes stored in the run-time image
 * jrt:/ refers to the root path of the container where all class and resource files are stored
 */
public class JRTClassFileContainer extends JVMClassFileContainer {

    public JRTClassFileContainer() {
        super("jrt", "jrt:/");
    }

    @Override
    public ClassFileMatch getMatch(String clsName) {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        byte[] data;
        try {
            data = Files.readAllBytes(fs.getPath("modules", getClassEntryURL(clsName)));
            return new JVMClassFileMatch(clsName, getClassURL(clsName), data);
        } catch (IOException e) {
            return null;
        }
    }
}
