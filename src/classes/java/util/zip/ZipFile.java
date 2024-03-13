package java.util.zip;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class ZipFile {
  private final class EntryIterator implements Enumeration<ZipEntry>, Iterator<ZipEntry> {
    private int counter = 0;

    @Override
    public ZipEntry nextElement() {
      ZipEntry ze = getEntry(entryNames[counter]);
      counter++;
      return ze;
    }

    @Override
    public boolean hasMoreElements() {
      return counter < entryNames.length;
    }

    @Override
    public boolean hasNext() {
      return hasMoreElements();
    }

    @Override
    public ZipEntry next() {
      return nextElement();
    }
  }

  private static final int OPENED = 1;
  private static final int CLOSED = 2;
  public static final int OPEN_READ = 0x1;
  public static final int OPEN_DELETE = 0x4;

  private int zipFileHandle;
  private String[] entryNames;
  private String name, comment;

  private int state = 0;
  private String charsetName;

  public ZipFile(String name) throws IOException {
    this(new File(name), OPEN_READ);
  }

  public ZipFile(File file, int mode) throws IOException {
    this(file, mode, StandardCharsets.UTF_8);
  }

  public ZipFile(File file) throws ZipException, IOException {
    this(file, OPEN_READ);
  }

  public ZipFile(File file, int mode, Charset charset) throws IOException {
    if((mode & OPEN_READ) == 0 || (mode & OPEN_DELETE) != 0) {
      throw new IllegalArgumentException("Illegal mode: 0x" + Integer.toHexString(mode));
    }
    String name = file.getPath();
    // This intentionally omits security checks
    if(charset == null) {
      throw new NullPointerException("charset is null");
    }
    this.name = name;
    this.charsetName = charset.name();
    this.zipFileHandle = open0();
    this.state = OPENED;
  }

  private native int open0();

  private native byte[] getEntryBytes(String entryName);

  private native byte[] getZEExtraBytes(String entryName);

  private native boolean initZEFields(String entryName, ZipEntry ze);

  public Enumeration<? extends ZipEntry> entries() {
    return new EntryIterator();
  }

  public Stream<? extends ZipEntry> stream() {
    return StreamSupport.stream(Spliterators.spliterator(new EntryIterator(), size(), Spliterator.ORDERED | Spliterator.DISTINCT | Spliterator.IMMUTABLE | Spliterator.NONNULL),
        false);
  }

  public ZipFile(String name, Charset charset) throws IOException {
    this(new File(name), OPEN_READ, charset);
  }

  public ZipFile(File file, Charset charset) throws IOException {
    this(file, OPEN_READ, charset);
  }

  public String getComment() {
    return comment;
  }

  public int size() throws IllegalStateException {
    return entryNames.length;
  }

  public ZipEntry getEntry(String entryName) {
    ZipEntry ze = new ZipEntry(entryName);
    if(!initZEFields(entryName, ze)) {
      return null;
    }
    byte[] extraBytes = getZEExtraBytes(entryName);
    ze.setExtra(extraBytes);
    return ze;
  }

  public InputStream getInputStream(ZipEntry ze) throws IOException {
    byte[] inputBytes = getEntryBytes(ze.getName());
    return new ByteArrayInputStream(inputBytes);
  }

  public String getName() {
    return name;
  }

  public void close() throws IOException {
    close0();
    state = CLOSED;
  }

  private native void close0();
}
