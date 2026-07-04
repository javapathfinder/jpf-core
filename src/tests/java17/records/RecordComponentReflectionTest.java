package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.lang.reflect.RecordComponent;
import java.lang.annotation.*;

public class RecordComponentReflectionTest extends TestJPF {

  record Point(int x, int y) {}
  record Person(String name, int age) {}

  @Test
  public void testGetRecordComponents_returnsCorrectCount() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = Point.class.getRecordComponents();
      assertNotNull(components);
      assertEquals(2, components.length);
    }
  }

  @Test
  public void testGetRecordComponents_returnsCorrectNames() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = Point.class.getRecordComponents();
      assertNotNull(components);
      assertEquals("x", components[0].getName());
      assertEquals("y", components[1].getName());
    }
  }

  @Test
  public void testGetRecordComponents_nonRecordReturnsNull() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = String.class.getRecordComponents();
      assertNull(components);
    }
  }

  @Test
  public void testGetRecordComponents_multipleComponents() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = Person.class.getRecordComponents();
      assertNotNull(components);
      assertEquals(2, components.length);
      assertEquals("name", components[0].getName());
      assertEquals("age", components[1].getName());
    }
  }

  // This will expose missing getType() peer if not implemented
  @Test
  public void testGetRecordComponents_correctTypes() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = Point.class.getRecordComponents();
      assertNotNull(components);
      assertEquals(int.class, components[0].getType());
      assertEquals(int.class, components[1].getType());
    }
  }

  @Test
  public void testGetRecordComponents_getAccessor() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = Point.class.getRecordComponents();
      assertNotNull(components);
      java.lang.reflect.Method accessor = components[0].getAccessor();
      assertNotNull(accessor);
      assertEquals("x", accessor.getName());
    }
  }

  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.RECORD_COMPONENT)
  @interface ComponentLabel {
    String value();
  }

  record AnnotatedPoint(@ComponentLabel("x-coord") int x, @ComponentLabel("y-coord") int y) {}

  @Test
  public void testGetRecordComponents_annotations() {
    if (verifyNoPropertyViolation()) {
      RecordComponent[] components = AnnotatedPoint.class.getRecordComponents();
      assertNotNull(components);
      assertEquals(2, components.length);
      java.lang.annotation.Annotation[] annotations = components[0].getAnnotations();
      assertNotNull(annotations);
      assertTrue(annotations.length > 0);
    }
  }

}