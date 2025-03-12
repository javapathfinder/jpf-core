package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.util.List;
import java.util.ArrayList;
import java.util.Arrays;

public class GenericRecordTest extends TestJPF {

    record Pair<T, U>(T first, U second) {}

    record Box<T extends Comparable<T>>(T value) {
        public boolean isGreaterThan(Box<T> other) {
            return this.value.compareTo(other.value) > 0;
        }
    }

    // record with collection type
    record Collection<T>(List<T> items) {
        public Collection<T> add(T item) {
            List<T> newList = new ArrayList<>(items);
            newList.add(item);
            return new Collection<>(newList);
        }

        public int size() {
            return items.size();
        }
    }

    @Test
    public void testGenericRecordCreation() {
        if (verifyNoPropertyViolation()) {
            Pair<String, Integer> p1 = new Pair<>("Hello", 42);
            Pair<Double, Boolean> p2 = new Pair<>(3.14, true);

            assertEquals("Hello", p1.first());
            assertEquals(Integer.valueOf(42), p1.second());
            assertEquals(Double.valueOf(3.14), p2.first());
            assertEquals(Boolean.TRUE, p2.second());
        }
    }

    @Test
    public void testGenericRecordWithCollections() {
        if (verifyNoPropertyViolation()) {
            Collection<String> c1 = new Collection<>(new ArrayList<>());
            Collection<String> c2 = c1.add("one").add("two").add("three");

            assertEquals(0, c1.size());
            assertEquals(3, c2.size());
            assertEquals("one", c2.items().get(0));
            assertEquals("two", c2.items().get(1));
            assertEquals("three", c2.items().get(2));
        }
    }

    @Test
    public void testGenericRecordEquality() {
        if (verifyNoPropertyViolation()) {
            Pair<String, Integer> p1 = new Pair<>("Hello", 42);
            Pair<String, Integer> p2 = new Pair<>("Hello", 42);
            Pair<String, Integer> p3 = new Pair<>("Hello", 43);

            assertEquals(p1, p2);
            assertNotEquals(p1, p3);
            assertEquals(p1.hashCode(), p2.hashCode());
        }
    }

    @Test
    public void testGenericRecordWithArrays() {
        if (verifyNoPropertyViolation()) {
            Pair<int[], String[]> arrayPair = new Pair<>(
                    new int[]{1, 2, 3},
                    new String[]{"a", "b", "c"}
            );

            assertTrue(Arrays.equals(new int[]{1, 2, 3}, arrayPair.first()));
            assertTrue(Arrays.equals(new String[]{"a", "b", "c"}, arrayPair.second()));
        }
    }
}
