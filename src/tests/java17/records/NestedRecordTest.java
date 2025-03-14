package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class NestedRecordTest extends TestJPF {

    // Basic records for nesting
    record Point(int x, int y) {}
    record Size(int width, int height) {}

    // Record containing another record
    record Rectangle(Point topLeft, Size size) {
        public Point bottomRight() {
            return new Point(topLeft.x() + size.width(), topLeft.y() + size.height());
        }
    }

    // Complex nested record structure
    record Address(String street, String city, String zipCode) {}
    record Person(String name, int age, Address address) {}
    record Company(String name, Address headquarters, Person[] employees) {}

    @Test
    public void testNestedRecordCreation() {
        if (verifyNoPropertyViolation()) {
            Point p = new Point(10, 20);
            Size s = new Size(30, 40);
            Rectangle r = new Rectangle(p, s);

            assertNotNull(r);
            assertEquals(p, r.topLeft());
            assertEquals(s, r.size());
        }
    }

    @Test
    public void testNestedRecordMethods() {
        if (verifyNoPropertyViolation()) {
            Rectangle r = new Rectangle(new Point(10, 20), new Size(30, 40));
            Point bottomRight = r.bottomRight();

            assertEquals(40, bottomRight.x());
            assertEquals(60, bottomRight.y());
        }
    }

    @Test
    public void testDeepNestedRecords() {
        if (verifyNoPropertyViolation()) {
            Address addr1 = new Address("123 Main St", "Springfield", "12345");
            Person p1 = new Person("John Doe", 30, addr1);
            Person p2 = new Person("Jane Smith", 28, new Address("456 Elm St", "Springfield", "12345"));

            Company company = new Company("Acme Inc",
                    new Address("789 Corporate Ave", "Business City", "54321"),
                    new Person[]{p1, p2});

            // Test nested access
            assertEquals("Acme Inc", company.name());
            assertEquals("Business City", company.headquarters().city());
            assertEquals(2, company.employees().length);
            assertEquals("John Doe", company.employees()[0].name());
            assertEquals("123 Main St", company.employees()[0].address().street());
        }
    }

    @Test
    public void testNestedRecordEquality() {
        if (verifyNoPropertyViolation()) {
            // Two identical nested structures should be equal
            Rectangle r1 = new Rectangle(new Point(10, 20), new Size(30, 40));
            Rectangle r2 = new Rectangle(new Point(10, 20), new Size(30, 40));

            assertEquals(r1, r2);
            assertEquals(r1.hashCode(), r2.hashCode());

            // Different nested structures should not be equal
            Rectangle r3 = new Rectangle(new Point(15, 25), new Size(30, 40));
            assertNotEquals(r1, r3);
        }
    }

    @Test
    public void testNestedRecordToString() {
        if (verifyNoPropertyViolation()) {
            Rectangle r = new Rectangle(new Point(10, 20), new Size(30, 40));

            // Expected format for nested toString()
            assertEquals("Rectangle[topLeft=Point[x=10, y=20], size=Size[width=30, height=40]]", r.toString());
        }
    }
}
