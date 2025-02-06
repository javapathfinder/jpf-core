package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CollectionTest extends TestJPF {
    @Test
    public void testCollectionsMethods() {
        if (verifyNoPropertyViolation()) {
            List<String> list = List.copyOf(Arrays.asList("A", "B", "C"));
            assertEquals(3, list.size());

            // `Collectors.toUnmodifiableList()`
            List<String> immutableList = List.of("X", "Y", "Z")
                    .stream().collect(Collectors.toUnmodifiableList());
            assertEquals(3, immutableList.size());
        }
    }
}
