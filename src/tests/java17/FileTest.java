package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.nio.file.Files;
import java.nio.file.Path;

public class FileTest extends TestJPF{
    @Test
    public void testFileMethods() throws Exception {
        if (verifyNoPropertyViolation()) {
            Path file1 = Files.createTempFile("testFile1", ".txt");
            Path file2 = Files.createTempFile("testFile2", ".txt");

            Files.writeString(file1, "Hello, JPF!");
            Files.writeString(file2, "Hello, JPF!");

            // 读取文件
            String content = Files.readString(file1);
            assertEquals("Hello, JPF!", content);

            // `mismatch()` 计算文件不同位置
            long mismatchIndex = Files.mismatch(file1, file2);
            assertEquals(-1, mismatchIndex); // -1 表示两个文件完全相同
        }
    }
}
