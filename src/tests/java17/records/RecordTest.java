package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class RecordTest extends TestJPF {
     record Person(String name, int age) {}

     @Test
    public void testRecordToString() {
        
            Person p = new Person("Alice", 20);
            assertEquals("Person[Alice, y=20]", p.toString());
        
    }

    @Test
    public void testRecordEquals() {
        Person p1 = new Person("Bob", 25);
        Person p2 = new Person("Bob", 25);
        assertEquals(p1, p2);  // Records should be equal if all fields match
    }

    @Test
    public void testRecordHashCode() {
        Person p1 = new Person("Charlie", 40);
        Person p2 = new Person("Charlie", 40);
        assertEquals(p1.hashCode(), p2.hashCode());  // Ensure consistent hashing
    }

    @Test
    public void testRecordDirectFieldAccess() {
        Person p = new Person("Dave", 50);
        assertEquals("Dave", p.name());  // Use accessor methods
        assertEquals(50, p.age());
    }
    
}
