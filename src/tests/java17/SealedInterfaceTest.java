package java17;

import gov.nasa.jpf.util.test.TestJPF;
import org.junit.Test;

public class SealedInterfaceTest extends TestJPF {

    // "Shape" is a "sealed" interface, and it allows only "Circle", "Square" and "Rectangle" to implement.
    sealed interface Shape permits Square, Rectangle {
        double area();
    }


    // Here "Square" class is allowed to implement "Shape". Sealed interface "Shape" allows "Square" to implement in "permits" clause.
    // But "Square" is "final class" so it cannot be extended by anyone more.
    final class Square implements Shape {
        private double length;

        public Square(double length){
            this.length = length;
        }

        @Override
        public double area() {
            return (length * length);
        }
    }


    // Here "Rectangle" class is allowed to implement "Shape". Sealed interface "Shape" allows "Rectangle" to implement in "permits" clause.
    // But "Rectangle" is "non-sealed class" so it can be extended by anyone more.
    public non-sealed class Rectangle implements Shape {
        private double length;
        private double width;

        public Rectangle(double length, double width) {
            this.length = length;
            this.width = width;
        }

        @Override
        public double area() {
            return (length * width);
        }
    }


    // Here "CustomRectangle" class is allowed to extend "non-sealed class Rectangle".
    public class CustomRectangle extends Rectangle {
        public CustomRectangle(double length, double width) {
            super(length, width);
        }
    }

    @Test
    public void testSquare() {
        if (verifyNoPropertyViolation()){
            Shape shape = new Square(4);

            // Test Square Instance
            assertTrue("",shape instanceof Square);

            // Test Square "area"
            assertEquals(16, shape.area());
        }
    }

    @Test
    public void testRectangle() {
        if (verifyNoPropertyViolation()) {
            Shape shape = new Rectangle(3, 7);

            // Test Rectangle Instance
            assertTrue("", shape instanceof Rectangle);

            // Test Rectangle "area"
            assertEquals(21, shape.area());
        }
    }

    @Test
    public void testCustomRectangle() {
        if (verifyNoPropertyViolation()){
            Shape shape = new CustomRectangle(4, 5);
            Rectangle rectangle = new CustomRectangle(3,4);

            // Test CustomRectangle Instance
            assertTrue("",shape instanceof CustomRectangle);
            assertTrue("",rectangle instanceof CustomRectangle);

            // Test CustomRectangle "area"
            assertEquals(20, shape.area());
        }
    }
}
