package java8;
import gov.nasa.jpf.util.test.TestJPF;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipFile;

public class ZipUtilsTest {

@Test
 public void zipFilesTest() throws IOException {
  FileOutputStream fos = new FileOutputStream("jpf-core/src/tests/java8/ZipFileTest/src_sample1.zip");
  File file = new File("jpf-core\\src\\tests\\java8\\ZipFileTest\\sample.txt");
  File file1 = new File("jpf-core\\src\\tests\\java8\\ZipFileTest\\sample1.txt");
  File file2 = new File("jpf-core\\src\\tests\\java8\\ZipFileTest\\sample.txt");

  List<File> files = new ArrayList<>();
  files.add(file);
  files.add(file1);
  files.add(file2);
  ZipFile.zipFile(files, fos);
 }
 
}