package java8;

import gov.nasa.jpf.util.test.TestJPF;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.Test;
import static java.util.Arrays.asList;

public class StreamTest extends TestJPF {
    public static List<String> allToUpperCase(List<String> words) {
        return words.stream()
                    .map(string -> string.toUpperCase())
                    .collect(Collectors.toList());
    }

    @Test
public void multipleWordsToUppercase() {
    List<String> input = Arrays.asList("a", "b", "hello");
    List<String> result = allToUpperCase(input);
    assertEquals(asList("A", "B", "HELLO"), result);
    }
}
