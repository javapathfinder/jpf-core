package gov.nasa.jpf.test;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;



public class RecordTest {

    record Person(String name, int age) {}

@Test
public void testRecordAccessor() {
    Person p = new Person("John", 30);
    //testing accessor method
    assertEquals("John", p.name());
    assertEquals(30, p.age());
}
}