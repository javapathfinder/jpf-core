package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.util.Objects;

public class RecordEqualsTest extends TestJPF {

    // Basic record
    record Point(int x, int y) {}

    // Record with reference fields
    record Person(String name, int age) {}

    // Record with arrays
    record ArrayHolder(int[] numbers, String[] names) {
        // equals for arrays
        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            ArrayHolder that = (ArrayHolder) obj;
            return java.util.Arrays.equals(numbers, that.numbers) &&
                    java.util.Arrays.equals(names, that.names);
        }

        @Override
        public int hashCode() {
            return 31 * java.util.Arrays.hashCode(numbers) + java.util.Arrays.hashCode(names);
        }
    }

    // Record with custom equals and hashCode
    record CustomEqualsRecord(int id, String data) {
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CustomEqualsRecord that = (CustomEqualsRecord) o;
            return id == that.id;
        }

        @Override
        public int hashCode() {
            return Objects.hash(id);
        }
    }

    @Test
    public void testBasicEquals() {
        if (verifyNoPropertyViolation()) {
            Point p1 = new Point(10, 20);
            Point p2 = new Point(10, 20);
            Point p3 = new Point(30, 40);

            assertEquals(p1, p2);

            assertNotEquals(p1, p3);

            // Symmetry property
            assertTrue(p1.equals(p2));
            assertTrue(p2.equals(p1));

            assertTrue(p1.equals(p1));

            assertFalse(p1.equals(null));

            assertFalse(p1.equals("Not a Point"));
        }
    }

    @Test
    public void testHashCode() {
        if (verifyNoPropertyViolation()) {
            Point p1 = new Point(10, 20);
            Point p2 = new Point(10, 20);
            Point p3 = new Point(30, 40);

            // Debug output
            System.out.println("p1 hashCode: " + p1.hashCode());
            System.out.println("p2 hashCode: " + p2.hashCode());
            System.out.println("p3 hashCode: " + p3.hashCode());

            // Equal objects should have equal hash codes
            assertEquals(p1.hashCode(), p2.hashCode());

            // Different objects likely have different hash codes
            // (not guaranteed but likely)
            assertNotEquals(p1.hashCode(), p3.hashCode());
        }
    }

    @Test
    public void testReferenceFieldEquals() {
        if (verifyNoPropertyViolation()) {
            Person p1 = new Person("John", 30);
            Person p2 = new Person("John", 30);
            Person p3 = new Person("John", 31);
            Person p4 = new Person(new String("John"), 30); // Different String instance

            assertEquals(p1, p2);
            assertNotEquals(p1, p3);

            // Record equals should use equals() for reference fields, not ==
            assertEquals(p1, p4);
        }
    }

    @Test
    public void testArrayFieldEquals() {
        if (verifyNoPropertyViolation()) {
            ArrayHolder a1 = new ArrayHolder(new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
            ArrayHolder a2 = new ArrayHolder(new int[]{1, 2, 3}, new String[]{"a", "b", "c"});
            ArrayHolder a3 = new ArrayHolder(new int[]{1, 2, 4}, new String[]{"a", "b", "c"});

            assertEquals(a1, a2);
            assertNotEquals(a1, a3);

            assertEquals(a1.hashCode(), a2.hashCode());
        }
    }

    @Test
    public void testCustomEquals() {
        if (verifyNoPropertyViolation()) {
            CustomEqualsRecord r1 = new CustomEqualsRecord(1, "data1");
            CustomEqualsRecord r2 = new CustomEqualsRecord(1, "data2");
            CustomEqualsRecord r3 = new CustomEqualsRecord(2, "data1");

            assertEquals(r1, r2);
            assertNotEquals(r1, r3);

            assertEquals(r1.hashCode(), r2.hashCode());
            assertNotEquals(r1.hashCode(), r3.hashCode());
        }
    }

    @Test
    public void testTransitiveEquals() {
        if (verifyNoPropertyViolation()) {
            Point p1 = new Point(10, 20);
            Point p2 = new Point(10, 20);
            Point p3 = new Point(10, 20);

            assertEquals(p1, p2);
            assertEquals(p2, p3);
            assertEquals(p1, p3);
        }
    }
}