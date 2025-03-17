package gov.nasa.jpf.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

class RecordTest {

    record Person(String name, int age) {}

    @Test
    void testRecordAccessor() {
        Person p = new Person("John", 30);
        assertEquals("John", p.name());
        assertEquals(30, p.age());
    }
}
