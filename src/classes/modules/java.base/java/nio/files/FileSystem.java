package java.nio.file;

import java.nio.file.spi.FileSystemProvider;

public abstract class FileSystem {
    protected FileSystem() {}

    public abstract Path getPath(String first, String... more);

    public abstract FileSystemProvider provider();
}