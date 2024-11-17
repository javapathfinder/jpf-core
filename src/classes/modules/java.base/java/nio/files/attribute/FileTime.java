package java.nio.file.attribute;

public final class FileTime {
    private final long time;

    private FileTime(long time) {
        this.time = time;
    }

    public static FileTime fromMillis(long time) {
        return new FileTime(time);
    }

    public long toMillis() {
        return time;
    }
}