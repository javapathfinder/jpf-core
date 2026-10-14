package gov.nasa.jpf.util;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;

import org.junit.Test;

public class FileUtilsRemoveRecursivelyTest {

  @Test
  public void removeRecursivelyDeletesRegularFile() throws IOException {
    File file = Files.createTempFile("jpf-fileutils-", ".tmp").toFile();

    try {
      assertTrue("Test file should exist", file.isFile());

      assertTrue("Regular file should be deleted",
          FileUtils.removeRecursively(file));

      assertFalse("File should no longer exist", file.exists());
    } finally {
      Files.deleteIfExists(file.toPath());
    }
  }
}
