package java.nio.file.spi;

import java.io.IOException;
import java.nio.file.FileSystem;
import java.nio.file.AccessMode;
import java.nio.file.Path;
import java.net.URI;
import java.util.Map;


public abstract class FileSystemProvider {
    protected FileSystemProvider() {}

    public abstract FileSystem newFileSystem(URI uri, Map<String, ?> env);

    public abstract String getScheme();

    public abstract FileSystem getFileSystem();
}