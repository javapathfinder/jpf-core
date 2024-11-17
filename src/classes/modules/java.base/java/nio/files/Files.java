package java.nio.file;

import java.nio.file.attribute.*;
import java.nio.file.spi.FileSystemProvider;
import java.util.Set;
import java.util.stream.Stream;
import java.io.IOException;

public class Files {
    private Files() {}

    public static FileSystem getFileSystem(Path path) {
        return path.getFileSystem();
    }

    public static FileSystemProvider provider(Path path) {
        return path.getFileSystem().provider();
    }

    public static <A extends BasicFileAttributes> A readAttributes(
            Path path, Class<A> type, LinkOption... options) throws IOException {
        return (A) new BasicFileAttributes() {
            public FileTime lastModifiedTime() { return FileTime.fromMillis(System.currentTimeMillis()); }
            public FileTime lastAccessTime() { return FileTime.fromMillis(System.currentTimeMillis()); }
            public FileTime creationTime() { return FileTime.fromMillis(System.currentTimeMillis()); }
            public boolean isRegularFile() { return true; }
            public boolean isDirectory() { return false; }
            public boolean isSymbolicLink() { return false; }
            public boolean isOther() { return false; }
            public long size() { return 0L; }
            public Object fileKey() { return null; }
        };
    }

    public static Stream<Path> walk(Path start, FileVisitOption... options) throws IOException {
        return Stream.of(start);
    }

    public static Stream<Path> walk(Path start, int maxDepth, FileVisitOption... options) throws IOException {
        return Stream.of(start);
    }

    public static boolean exists(Path path) throws IOException {
        try {
             return Files.exists(path);
        } catch (IOException e) {
             return false;
        }
    }
}