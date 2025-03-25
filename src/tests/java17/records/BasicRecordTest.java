package java17.records;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class BasicRecordTest extends TestJPF {

    record Point(int x, int y) {}

    record Temperature(double celsius) {
        public Temperature {
            if (celsius < -273.15) {
                throw new IllegalArgumentException("Temperature below absolute zero");
            }
        }
    }

    record Rectangle(double width, double height) {
        public double area() {
            return width * height;
        }

        public double perimeter() {
            return 2 * (width + height);
        }
    }

    @Test
    public void testRecordCreation() {
       
            Point p = new Point(10, 20);
            assertNotNull(p);
        
    }

    @Test
    public void testRecordAccessors() {
        
            Point p = new Point(10, 20);
            // Test accessor methods
            assertEquals(10, p.x());
            assertEquals(20, p.y());

            try {
                Temperature t2=new Temperature(-300.0);
                System.out.println("Unexpected creation: "+t2);
                fail("Should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                System.out.println("Expected exception caught: "+e.getMessage());
            }
        
    }
    /*
    @Test
    public void testRecordDirectFieldAccess() {
        
            // This should NOT work if JPF properly enforces record encapsulation
            // Field access should fail at compile time, but currently doesn't in JPF
            Point p = new Point(10, 20);
            try {
                // using reflection to test access, as direct access would fail compilation in correct Java
                java.lang.reflect.Field field = p.getClass().getDeclaredField("x");
                field.setAccessible(true);
                int value = (int) field.get(p);
                assertEquals(10, value);

                try {
                    field.set(p, 30);
                    fail("Should not be able to modify final field");
                } catch (IllegalAccessException e) {
                    // expected
                }
            } catch (Exception e) {
                // expected if JPF correctly implements record field access -> will do later
                assertTrue(e instanceof IllegalAccessException ||
                        e instanceof NoSuchFieldException);
            }
        
    }
*/
    @Test
    public void testRecordConstructorValidation() {
        
            Temperature t1 = new Temperature(25.0);
            assertEquals(25.0, t1.celsius(), 0.001);

            // Invalid temperature (should throw exception)
            try {
                Temperature t2 = new Temperature(-300.0);
                fail("Should have thrown IllegalArgumentException");
            } catch (IllegalArgumentException e) {
                // expected
            }
        
    }

    @Test
    public void testRecordCustomMethods() {
       
            Rectangle r = new Rectangle(5.0, 10.0);
            assertEquals(50.0, r.area(), 0.001);
            assertEquals(30.0, r.perimeter(), 0.001);
        
    }

    @Test
    public void testRecordToString() {
        
            Point p = new Point(10, 20);
            assertEquals("Point[x=10, y=20]", p.toString());
        
    }
}

