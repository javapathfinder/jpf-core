package gov.nasa.jpf.jvm;

import gov.nasa.jpf.JPFException;
import gov.nasa.jpf.vm.ClassFileMatch;

import java.io.IOException;
import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.FileSystems;
import java.nio.file.Files;

public class JModClassFileContainer extends JVMClassFileContainer {

    public JModClassFileContainer(String name) {
        super(name, getContainerURL(name));
    }

    static String getContainerURL(String name) {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        return fs.getPath("modules", getClassEntryURL(name)).toString();
    }

    @Override
    public ClassFileMatch getMatch(String clsName) {
        FileSystem fs = FileSystems.getFileSystem(URI.create("jrt:/"));
        byte[] data;
        try {
            // combine with above if possible
            data = Files.readAllBytes(fs.getPath("modules", getClassEntryURL(clsName)));
        } catch (IOException e) {
            throw new JPFException("Match not found in jrt file system for " + clsName, e);
        }
        return new JVMClassFileMatch(clsName, getClassURL(clsName), data);
    }
}
