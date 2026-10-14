
import gov.nasa.jpf.util.FileUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class FileUtilsTest {

  @Test(expected = IOException.class)
  public void getContentsAsStringDirectoryThrowsIOException() throws IOException {
    File directory = Files.createTempDirectory("jpf-fileutils-test").toFile();
    try {
      FileUtils.getContentsAsString(directory);
    } finally {
      Files.deleteIfExists(directory.toPath());
    }
  }

  @Test
  public void getContentsAsStringReadsValidFile() throws IOException {
    File file = File.createTempFile("jpf-fileutils-test", ".txt");
    try {
      Files.write(file.toPath(), "hello JPF".getBytes(java.nio.charset.StandardCharsets.UTF_8));
      assertEquals("hello JPF", FileUtils.getContentsAsString(file));
    } finally {
      Files.deleteIfExists(file.toPath());
    }
  }
}
