package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;
import java.lang.annotation.*;


public class AnnotatedRecordTest extends TestJPF {

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.TYPE, ElementType.RECORD_COMPONENT, ElementType.METHOD})
    public @interface TestAnnotation {
        String value() default "";
    }

    @Retention(RetentionPolicy.RUNTIME)
    @Target({ElementType.RECORD_COMPONENT})
    public @interface Field {
        String name() default "";
        boolean required() default false;
    }

    @TestAnnotation("Record class annotation")
    record Person(
            @Field(name = "firstName", required = true)
            String firstName,

            @Field(name = "lastName", required = true)
            String lastName,

            @Field(name = "age")
            int age
    ) {
        @TestAnnotation("Custom method annotation")
        public String fullName() {
            return firstName + " " + lastName;
        }
    }

    @Test
    public void testRecordAnnotations() {
        if (verifyNoPropertyViolation()) {
            //class-level annotation
            TestAnnotation classAnnotation = Person.class.getAnnotation(TestAnnotation.class);
            assertNotNull(classAnnotation);
            assertEquals("Record class annotation", classAnnotation.value());
        }
    }

    @Test
    public void testRecordComponentAnnotations() {
        // TO DO : we can make three record components and check the annotation for each one
    }

    @Test
    public void testRecordMethodAnnotations() {
        if (verifyNoPropertyViolation()) {
            try {
                java.lang.reflect.Method fullNameMethod = Person.class.getMethod("fullName");
                TestAnnotation methodAnnotation = fullNameMethod.getAnnotation(TestAnnotation.class);
                assertNotNull(methodAnnotation);
                assertEquals("Custom method annotation", methodAnnotation.value());
            } catch (Exception e) {
                fail("Reflection on record method failed: " + e.getMessage());
            }
        }
    }

    @Test
    public void testAnnotatedRecordBehavior() {
        if (verifyNoPropertyViolation()) {
            Person person = new Person("John", "Doe", 30);

            // Basic functionality should still work
            assertEquals("John", person.firstName());
            assertEquals("Doe", person.lastName());
            assertEquals(30, person.age());
            assertEquals("John Doe", person.fullName());
        }
    }
}
