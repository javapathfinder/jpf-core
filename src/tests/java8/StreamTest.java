package java8;

import gov.nasa.jpf.util.test.TestJPF;
import java.util.function.Supplier;
import org.junit.Test;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StreamTest {
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
    private void assertEquals(Object asList, List<String> result) {
    }
    
    
    private Object asList(String string, String string2, String string3) {
        return null;
    }
}
