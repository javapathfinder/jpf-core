package java.nio.file;
import java.net.URI;

public interface Path extends Comparable<Path> {
    FileSystem getFileSystem();
    boolean isAbsolute();
    Path getRoot();
    Path getFileName();
    Path getParent();
    int getNameCount();
    Path getName(int index);
    Path subpath(int beginIndex, int endIndex);
    boolean startsWith(Path other);
    boolean endsWith(Path other);
    Path normalize();
    Path resolve(Path other);
    Path relativize(Path other);
    URI toUri();
    Path toAbsolutePath();
    Path toRealPath(LinkOption... options);

    static Path of(String first, String... more) {
        return FileSystems.getDefault().getPath(first, more);
    }
}