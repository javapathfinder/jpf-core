package java.nio.file;

import java.nio.file.spi.FileSystemProvider;
import java.net.URI;
import java.util.Map;
import java.util.HashMap;

public class FileSystems {
    private static volatile FileSystem defaultFileSystem;

    static {
        defaultFileSystem = new FileSystem() {
            private final FileSystemProvider provider = new FileSystemProvider() {
                @Override
                public FileSystem newFileSystem(URI uri, Map<String, ?> env) {
                    return this.getFileSystem();
                }

                @Override
                public String getScheme() {
                    return "file";
                }

                @Override
                public FileSystem getFileSystem() {
                    return defaultFileSystem;
                }
            };

            @Override
            public Path getPath(String first, String... more) {
                return new Path() {
                    private final String path;
                    private final String[] segments;

                    {
                        StringBuilder sb = new StringBuilder(first);
                        if (more != null) {
                            for (String s : more) {
                                if (s != null && !s.isEmpty()) {
                                    if (sb.length() > 0 && sb.charAt(sb.length() - 1) != '/') {
                                        sb.append('/');
                                    }
                                    sb.append(s);
                                }
                            }
                        }
                        path = sb.toString();
                        segments = path.split("/");
                    }

                    @Override
                    public FileSystem getFileSystem() {
                        return defaultFileSystem;
                    }

                    @Override
                    public boolean isAbsolute() {
                        return path.startsWith("/");
                    }

                    @Override
                    public Path getRoot() {
                        return isAbsolute() ? getFileSystem().getPath("/") : null;
                    }

                    @Override
                    public Path getFileName() {
                        if (path.isEmpty()) return null;
                        return getFileSystem().getPath(segments[segments.length - 1]);
                    }

                    @Override
                    public Path getParent() {
                        int lastSep = path.lastIndexOf('/');
                        return lastSep > 0 ? getFileSystem().getPath(path.substring(0, lastSep)) : null;
                    }

                    @Override
                    public int getNameCount() {
                        if (path.isEmpty()) return 0;
                        if (path.equals("/")) return 0;
                        int count = segments.length;
                        if (path.startsWith("/")) count--;
                        if (path.endsWith("/")) count--;
                        return count;
                    }

                    @Override
                    public Path getName(int index) {
                        if (index < 0 || index >= getNameCount()) {
                            throw new IllegalArgumentException();
                        }
                        return getFileSystem().getPath(segments[path.startsWith("/") ? index + 1 : index]);
                    }

                    @Override
                    public Path subpath(int beginIndex, int endIndex) {
                        if (beginIndex < 0 || beginIndex >= getNameCount() ||
                            endIndex > getNameCount() || beginIndex >= endIndex) {
                            throw new IllegalArgumentException();
                        }

                        StringBuilder result = new StringBuilder();
                        int start = path.startsWith("/") ? beginIndex + 1 : beginIndex;
                        for (int i = start; i < start + (endIndex - beginIndex); i++) {
                            if (result.length() > 0) result.append('/');
                            result.append(segments[i]);
                        }
                        return getFileSystem().getPath(result.toString());
                    }

                    @Override
                    public boolean startsWith(Path other) {
                        return path.startsWith(other.toString());
                    }

                    @Override
                    public boolean endsWith(Path other) {
                        return path.endsWith(other.toString());
                    }

                    @Override
                    public Path normalize() {
                        return this; // Simplified implementation
                    }

                    @Override
                    public Path resolve(Path other) {
                        return getFileSystem().getPath(path + "/" + other.toString());
                    }

                    @Override
                    public Path relativize(Path other) {
                        return other; // Simplified implementation
                    }

                    @Override
                    public URI toUri() {
                        try {
                            return new URI("file", null, path, null);
                        } catch (Exception e) {
                            return null;
                        }
                    }

                    @Override
                    public Path toAbsolutePath() {
                        if (isAbsolute()) return this;
                        return getFileSystem().getPath("/" + path);
                    }

                    @Override
                    public Path toRealPath(LinkOption... options) {
                        return toAbsolutePath();
                    }

                    @Override
                    public String toString() {
                        return path;
                    }

                    @Override
                    public int compareTo(Path other) {
                        return path.compareTo(other.toString());
                    }
                };
            }

            @Override
            public FileSystemProvider provider() {
                return provider;
            }
        };
    }

    public static FileSystem getDefault() {
        return defaultFileSystem;
    }

    public static FileSystem newFileSystem(URI uri, Map<String, ?> env, ClassLoader loader) {
        if (uri == null)
            throw new NullPointerException();
        return defaultFileSystem;
    }
}